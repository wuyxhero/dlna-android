/******************************************************************
 *
 *	MediaServer for CyberLink
 *
 *	Copyright (C) Satoshi Konno 2003
 *
 *	File : AVTransport.java
 *
 *	Revision:
 *
 *	02/22/08
 *		- first revision.
 *
 ******************************************************************/

package com.iqiyi.android.dlna.sdk.mediarenderer.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cybergarage.util.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.control.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.iqiyi.android.dlna.sdk.mediarenderer.AVTransportListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.DlnaMediaModel;
import com.iqiyi.android.dlna.sdk.mediarenderer.MediaRenderer;
import com.iqiyi.android.dlna.sdk.mediarenderer.StandardDLNAListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.AVTransportConstStr;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ActionDispatcher.Dispatcher;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ActionDispatcher.Dispatcher_MGTV;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ActionDispatcher.Dispatcher_QPLAY;

public class AVTransport extends Service implements ActionListener, QueryListener, ServiceInterface {

    public enum LastChange {PLAYING, STOPPED, PAUSED, NOMEDIA, OTHER, TRANSITIONING};

    private MediaRenderer mMediaRenderer = null;
    private StandardDLNAListener mStandardDLNAListener = null;

    public AVTransport(MediaRenderer render) {
        mMediaRenderer = render;
        mStandardDLNAListener = render.getStandardDLNAListener();
        initService();
        setActionListener(this);
    }

    @Override
    public void initService() {
        // 设置服务的描述地址，控制地址，订阅地址
        setServiceType(AVTransportConstStr.SERVICE_TYPE);
        setServiceID(AVTransportConstStr.SERVICE_ID);
        setControlURL(AVTransportConstStr.CONTROL_URL);
        setSCPDURL(AVTransportConstStr.SCPDURL);
        setEventSubURL(AVTransportConstStr.EVENTSUB_URL);

        try {
            loadSCPD(AVTransportConstStr.SCPD);
            // 初始化lastchange内容
            sendLastChangeEvent(AVTransportConstStr.DEFAULT_LASTCHANGE);
            // 初始化TransportState
            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.NO_MEDIA_PRESENT, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AVTransportListener mAVTransportListener = null;

    public void setAVTransportListener(AVTransportListener listener) {
        mAVTransportListener = listener;
    }

    public AVTransportListener getAVTransportListener() {
        return mAVTransportListener;
    }

    // 用于第三方APP定制响应内容
    private Dispatcher mDispatcher = null;

    public boolean actionControlReceived(Action action) {
        boolean isActionSuccess = true;

        String actionName = action.getName();
        Debug.message("Process AVTransport action: " + actionName);
        if (actionName == null) {
            Debug.warning("[Error] AVTransport action: actionName == null");
            action.setStatus(UPnPStatus.INVALID_ACTION);
            return false;
        }

        ActionListener listener = mMediaRenderer.getActionListener();
        if (listener != null) {
            Debug.message("Deliver AVTransport action: " + actionName);
            listener.actionControlReceived(action);
        }

        if (mDispatcher != null && !actionName.equals(AVTransportConstStr.SETAVTRANSPORTURI)) {
            if (mDispatcher.actionControlReceived(action))
                return true;
        }

        if (actionName.equals(AVTransportConstStr.SETAVTRANSPORTURI)) {
            // 设置播放的URI地址
            int instanceID = action.getArgument(AVTransportConstStr.INSTANCEID).getIntegerValue();
            String uri = action.getArgument(AVTransportConstStr.CURRENTURI).getValue();
            String metaData = action.getArgument(AVTransportConstStr.CURRENTURIMETADATA).getValue();

            getStateVariable(AVTransportConstStr.AVTRANSPORTURI).setValue(uri, false);
            getStateVariable(AVTransportConstStr.AVTRANSPORTURIMETADATA).setValue(metaData, false);
            getStateVariable(AVTransportConstStr.CURRENTTRACKURI).setValue(uri, false);
            getStateVariable(AVTransportConstStr.CURRENTTRACKMETADATA).setValue(metaData, false);
            getStateVariable(AVTransportConstStr.CURRENTTRACK).setValue(1);
            getStateVariable(AVTransportConstStr.CURRENTTRACKDURATION).setValue("00:00:00", false);
            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.TRANSPORTING, false);

            mDispatcher = setupActionDispatcher(uri);
            if (mDispatcher != null && mDispatcher.actionControlReceived(action)) {
                return true;
            }

            if(uri.contains("http://127.0.0.1")) {
                Debug.message("Parse loopback url [Before]: " + uri);
                String mobileIp = ((InetSocketAddress)action.getCurActionReq().getSocket().getSocket().getRemoteSocketAddress()).getHostName();
                uri = uri.replace("http://127.0.0.1", "http://" + mobileIp);
                Debug.message("Parse loopback url [After]: " + uri);
            }

            if (mStandardDLNAListener != null) {
                DlnaMediaModel model = createFromMetaData(metaData, uri);
                mStandardDLNAListener.SetAVTransportURI(instanceID, uri, model);
            }
        } else if (actionName.equals(AVTransportConstStr.GETTRANSPORTINFO)) {
            action.setArgumentValue(AVTransportConstStr.CURRENTTRANSPORTSTATE, getStateVariable(AVTransportConstStr.TRANSPORTSTATE).getValue_dlna());
            action.setArgumentValue(AVTransportConstStr.CURRENTTRANSPORTSTATUS, "OK");
            action.setArgumentValue(AVTransportConstStr.CURRENTSPEED, "1");
        } else if (actionName.equals(AVTransportConstStr.GETPOSITIONINFO)) {
            String trackDuration = "00:00:00";
            String absTime = "00:00:00";
            String relTime = "00:00:00";
            if (mAVTransportListener != null) {
                trackDuration = mAVTransportListener.getTrackDuration();
                absTime = mAVTransportListener.getPosition();
                relTime = absTime;
            }

            String transportState = getStateVariable(AVTransportConstStr.TRANSPORTSTATE).getValue_dlna();
            if (AVTransportConstStr.PLAYING.equals(transportState) || AVTransportConstStr.PAUSED_PLAYBACK.equals(transportState))
                action.setArgumentValue(AVTransportConstStr.TRACK, "1");
            else
                action.setArgumentValue(AVTransportConstStr.TRACK, "0");

            action.setArgumentValue(AVTransportConstStr.TRACKDURATION, trackDuration);
            action.setArgumentValue(AVTransportConstStr.TRACKMETADATA, getStateVariable(AVTransportConstStr.CURRENTTRACKMETADATA).getValue_dlna());
            action.setArgumentValue(AVTransportConstStr.TRACKURI, getStateVariable(AVTransportConstStr.CURRENTTRACKURI).getValue_dlna());
            action.setArgumentValue(AVTransportConstStr.RELTIME, absTime);
            action.setArgumentValue(AVTransportConstStr.ABSTIME, relTime);
            action.setArgumentValue(AVTransportConstStr.RELCOUNT, "2147483647");
            action.setArgumentValue(AVTransportConstStr.ABSCOUNT, "2147483647");
        } else if (actionName.equals(AVTransportConstStr.SETNEXTAVTRANSPORTURI)) {
            // 设置下一个要播放的URI地址
            int instanceID = action.getArgument(AVTransportConstStr.INSTANCEID).getIntegerValue();
            String nextUri = action.getArgument(AVTransportConstStr.NEXTURI).getValue();
            String nextUriMetaData = action.getArgument(AVTransportConstStr.NEXTURIMETADATA).getValue();

            getStateVariable(AVTransportConstStr.NEXTAVTRANSPORTURI).setValue(nextUri, false);
            getStateVariable(AVTransportConstStr.NEXTAVTRANSPORTURIMETADATA).setValue(nextUriMetaData, false);

            if (mStandardDLNAListener != null) {
                DlnaMediaModel model = createFromMetaData(nextUriMetaData, nextUri);
                mStandardDLNAListener.SetNextAVTransportURI(instanceID, nextUri, model);
            }
        } else if (actionName.equals(AVTransportConstStr.SETPLAYMODE)) {
            // 设置当前的播放模式
            int instanceID = action.getArgument(AVTransportConstStr.INSTANCEID).getIntegerValue();
            String newPlayMode = action.getArgument(AVTransportConstStr.NEWPLAYMODE).getValue();

            getStateVariable(AVTransportConstStr.CURRENTPLAYMODE).setValue(newPlayMode, false);

            if (mStandardDLNAListener != null) {
                mStandardDLNAListener.SetPlayMode(instanceID, newPlayMode);
            }
        } else if (actionName.equals(AVTransportConstStr.GETMEDIAINFO)) {
            // 获取当前播放的媒体信息
            String trackDuration = "00:00:00";
            if (mAVTransportListener != null) {
                trackDuration = mAVTransportListener.getTrackDuration();
            }

            String transportState = getStateVariable(AVTransportConstStr.TRANSPORTSTATE).getValue_dlna();
            if (AVTransportConstStr.PLAYING.equals(transportState) || AVTransportConstStr.PAUSED_PLAYBACK.equals(transportState))
                action.setArgumentValue(AVTransportConstStr.NRTRACKS, "1");
            else
                action.setArgumentValue(AVTransportConstStr.NRTRACKS, "0");

            action.getArgument(AVTransportConstStr.MEDIADURATION).setValue(trackDuration);
            action.getArgument(AVTransportConstStr.CURRENTURI).setValue(getStateVariable(AVTransportConstStr.CURRENTTRACKURI).getValue_dlna());
            action.getArgument(AVTransportConstStr.CURRENTURIMETADATA).setValue(getStateVariable(AVTransportConstStr.CURRENTTRACKMETADATA).getValue_dlna());
            action.getArgument(AVTransportConstStr.PLAYMEDIUM).setValue("NONE");
            action.getArgument(AVTransportConstStr.RECORDMEDIUM).setValue(AVTransportConstStr.NOT_IMPLEMENTED);
            action.getArgument(AVTransportConstStr.WRITESTATUS).setValue(AVTransportConstStr.NOT_IMPLEMENTED);
        } else if (actionName.equals(AVTransportConstStr.PLAY)) {
            // 播放视频
            int instanceID = action.getArgument(AVTransportConstStr.INSTANCEID).getIntegerValue();
            String speed = action.getArgument(AVTransportConstStr.SPEED).getValue();

            getStateVariable(AVTransportConstStr.TRANSPORTPLAYSPEED).setValue(speed, false);
            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.PLAYING, false);

            if (mStandardDLNAListener != null) {
                mStandardDLNAListener.Play(instanceID, speed);
            }
        } else if (actionName.equals(AVTransportConstStr.NEXT)) {
            // 下一个
            int instanceID = action.getArgument(AVTransportConstStr.INSTANCEID).getIntegerValue();

            if (mStandardDLNAListener != null) {
                mStandardDLNAListener.Next(instanceID);
            }
        } else if (actionName.equals(AVTransportConstStr.PREVIOUS)) {
            // 上一个
            int instanceID = action.getArgument(AVTransportConstStr.INSTANCEID).getIntegerValue();

            if (mStandardDLNAListener != null) {
                mStandardDLNAListener.Previous(instanceID);
            }
        } else if (actionName.equals(AVTransportConstStr.STOP)) {
            int instanceID = action.getArgument(AVTransportConstStr.INSTANCEID).getIntegerValue();

            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.STOPPED, false);

            if (mStandardDLNAListener != null) {
                mStandardDLNAListener.Stop(instanceID);
            }
        } else if (actionName.equals(AVTransportConstStr.PAUSE)) {
            int instanceID = action.getArgument(AVTransportConstStr.INSTANCEID).getIntegerValue();

            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.PAUSED_PLAYBACK, false);

            if (mStandardDLNAListener != null) {
                mStandardDLNAListener.Pause(instanceID);
            }
        } else if (actionName.equals(AVTransportConstStr.SEEK)) {
            int instanceID = action.getArgument(AVTransportConstStr.INSTANCEID).getIntegerValue();
            String unit = action.getArgument(AVTransportConstStr.UNIT).getValue();
            String target = action.getArgument(AVTransportConstStr.TARGET).getValue();

            if (mStandardDLNAListener != null) {
                mStandardDLNAListener.Seek(instanceID, 0, unit + "=" + target);
            }
        } else if (actionName.equals(AVTransportConstStr.GETDEVICECAPABILITIES)) {
            String playbackStorageMedia = getStateVariable(AVTransportConstStr.POSSIBLEPLAYBACKSTORAGEMEDIA).getValue_dlna();

            action.setArgumentValue(AVTransportConstStr.PLAYMEDIA, playbackStorageMedia);
            action.setArgumentValue(AVTransportConstStr.RECMEDIA, AVTransportConstStr.NOT_IMPLEMENTED);
            action.setArgumentValue(AVTransportConstStr.RECQUALITYMODES, AVTransportConstStr.NOT_IMPLEMENTED);
        } else if (actionName.equals(AVTransportConstStr.RECORD)) {
            // Nothing
        } else if (actionName.equals(AVTransportConstStr.GETTRANSPORTSETTINGS)) {
            action.setArgumentValue(AVTransportConstStr.PLAYMODE, AVTransportConstStr.NORMAL);
            action.setArgumentValue(AVTransportConstStr.RECQUALITYMODE, AVTransportConstStr.NOT_IMPLEMENTED);
        } else if (actionName.equals(AVTransportConstStr.SETRECORDQUALITYMODE)) {
            String mode = action.getArgument(AVTransportConstStr.NEWRECORDQUALITYMODE).getValue();
        } else if (actionName.equals(AVTransportConstStr.GETCURRENTTRANSPORTACTIONS)) {
            action.setArgumentValue(AVTransportConstStr.ACTIONS, AVTransportConstStr.CurrentTransportActions);
        } else {
            Debug.message("Unknown Avt actionControlReceived() action: " + actionName);
            action.setStatus(UPnPStatus.INVALID_ACTION);
            isActionSuccess = false;
        }

        return isActionSuccess;
    }

    // 根据URI来选择Dispatcher
    private Dispatcher setupActionDispatcher(String uri) {
        Debug.message("Choose Dispatcher: " + uri);
        if (uri == null) {
            return null;
        }

        if (uri.contains("mgtv.com") || uri.contains("hifuntv.com")) {
            Debug.message("Use Dispatcher: " + Dispatcher_MGTV.class.getName());
            return new Dispatcher_MGTV(this);
        } else if (uri.contains("qplay://")) {
            Debug.message("Use Dispatcher: " + Dispatcher_QPLAY.class.getName());
            return new Dispatcher_QPLAY(this);
        }

        Debug.message("Use Default Dispatcher");
        return null;
    }

    private DlnaMediaModel createFromMetaData(String uriMetaData, String url) {
        DlnaMediaModel mediainfo = new DlnaMediaModel();
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;

        if (uriMetaData.contains("&") && !uriMetaData.contains("&amp;")) {
            uriMetaData = uriMetaData.replace("&", "&amp;");
        }

        try {
            mediainfo.setUrl(url);
            if (uriMetaData != null && !uriMetaData.equals("")) {
                documentBuilder = dfactory.newDocumentBuilder();
                InputStream is = new ByteArrayInputStream(uriMetaData.getBytes("UTF-8"));
                Document doc = documentBuilder.parse(is);
                mediainfo.setTitle(getElementValue(doc, "dc:title"));
                mediainfo.setArtist(getElementValue(doc, "upnp:artist"));
                mediainfo.setAlbum(getElementValue(doc, "upnp:album"));
                mediainfo.setAlbumUri(getElementValue(doc, "upnp:albumArtURI"));
                mediainfo.setObjectClass(getElementValue(doc, "upnp:class"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediainfo;
    }

    private static String getElementValue(Document doc, String element) {
        NodeList containers = doc.getElementsByTagName(element);
        for (int j = 0; j < containers.getLength(); ++j) {
            Node container = containers.item(j);
            NodeList childNodes = container.getChildNodes();
            if (childNodes.getLength() != 0) {
                Node childNode = childNodes.item(0);
                return childNode.getNodeValue();
            }
        }

        return "";
    }

    @Override
    public boolean queryControlReceived(StateVariable stateVar) {
        return false;
    }

    // 上层APP调用
    public void setPlayingState(String state, String url, int trackNum, String metadata, String escapeMetaData) {
        Debug.message("setPlayingState: state=" + state + " url=" + url + " trackNum=" + trackNum
                + " metadata=" + metadata + " escapeMetaData=" + escapeMetaData);

        if (state.equals(AVTransportConstStr.PLAYING)) {
            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.PLAYING, false);
            sendLastChangeEvent(LastChange.PLAYING, url, trackNum, metadata);
        } else if (state.equals(AVTransportConstStr.STOPPED)) {
            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.STOPPED, false);
            sendLastChangeEvent(LastChange.STOPPED, url, trackNum, metadata);
        } else if (state.equals(AVTransportConstStr.PAUSED_PLAYBACK)) {
            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.PAUSED_PLAYBACK, false);
            sendLastChangeEvent(LastChange.PAUSED, url, trackNum, metadata);
        } else if (state.equals(AVTransportConstStr.NO_MEDIA_PRESENT)) {
            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.NO_MEDIA_PRESENT, false);
            sendLastChangeEvent(LastChange.NOMEDIA, url, trackNum, metadata);
        } else if (state.equals(AVTransportConstStr.TRANSPORTING)) {
            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.TRANSPORTING, false);
            sendLastChangeEvent(LastChange.TRANSITIONING, url, trackNum, metadata);
        } else {
            getStateVariable(AVTransportConstStr.TRANSPORTSTATE).setValue(AVTransportConstStr.NO_MEDIA_PRESENT, false);
            sendLastChangeEvent(LastChange.OTHER, url, trackNum, metadata);
        }
    }

    private void sendLastChangeEvent(LastChange action, String uri, int trackNum, String metadata) {
        Debug.message("sendLastChangeEvent[AVT]: action=" + action.name());
        Debug.message("sendLastChangeEvent[ATV]: uri=" + uri);
        Debug.message("sendLastChangeEvent[ATV]: trackNum=" + trackNum);
        Debug.message("sendLastChangeEvent[ATV]: metadata=" + metadata);

        if (mDispatcher != null && mDispatcher.sendLastChangeEvent(action, uri, trackNum, metadata)) {
            return;
        }

        String lastChangeExpected = null;

        if (action == LastChange.PLAYING) {
            String trackDuration = "00:00:00";
            String absTime = "00:00:00";
            String relTime = "00:00:00";
            if (mAVTransportListener != null) {
                trackDuration = mAVTransportListener.getTrackDuration();
                absTime = mAVTransportListener.getPosition();
                relTime = absTime;
            }

            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
                    "<InstanceID val=\"0\">" +
                    "<AVTransportURI val=\"" + getStateVariable(AVTransportConstStr.AVTRANSPORTURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackURI val=\"" + uri + "\"/>" +
                    "<TransportState val=\"PLAYING\"/>" +
                    "<CurrentTransportActions val=\"Play,Pause,Stop\"/>" +
                    "<CurrentTrackDuration val=\"" + trackDuration + "\"/>" +
                    "<CurrentMediaDuration val=\"" + trackDuration + "\"/>" +
                    "<AbsoluteTimePosition val=\"" + absTime + "\"/>" +
                    "<RelativeTimePosition val=\"" + relTime + "\"/>" +
                    "<NumberOfTracks val=\"1\"/>"  +
                    "<CurrentTrack val=\"1\"/>"  +
                    "<TransportStatus val=\"OK\"/>" +
                    "</InstanceID>" + "</Event>";
        } else if (action == LastChange.STOPPED) {
            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
                    "<InstanceID val=\"0\">" +
                    "<AVTransportURI val=\"" + getStateVariable(AVTransportConstStr.AVTRANSPORTURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackURI val=\"" + uri + "\"/>" +
                    "<TransportState val=\"STOPPED\"/>" +
                    "<CurrentTransportActions val=\"Play\"/>" +
                    "<NumberOfTracks val=\"" + String.valueOf(trackNum) + "\"/>" +
                    "<TransportStatus val=\"OK\"/>" +
                    "</InstanceID>" + "</Event>";
        } else if (action == LastChange.PAUSED) {
            String trackDuration = "00:00:00";
            String absTime = "00:00:00";
            String relTime = "00:00:00";
            if (mAVTransportListener != null) {
                trackDuration = mAVTransportListener.getTrackDuration();
                absTime = mAVTransportListener.getPosition();
                relTime = absTime;
            }

            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
                    "<InstanceID val=\"0\">" +
                    "<AVTransportURI val=\"" + getStateVariable(AVTransportConstStr.AVTRANSPORTURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackURI val=\"" + getStateVariable(AVTransportConstStr.CURRENTTRACKURI).getValue_dlna() + "\"/>" +
                    "<TransportState val=\"PAUSED_PLAYBACK\"/>" +
                    "<CurrentTransportActions val=\"Play,Stop\"/>" +
                    "<CurrentTrackDuration val=\"" + trackDuration + "\"/>" +
                    "<CurrentMediaDuration val=\"" + trackDuration + "\"/>" +
                    "<AbsoluteTimePosition val=\"" + absTime + "\"/>" +
                    "<RelativeTimePosition val=\"" + relTime + "\"/>" +
                    "<NumberOfTracks val=\"" + String.valueOf(trackNum) + "\"/>" +
                    "<TransportStatus val=\"OK\"/>" +
                    "</InstanceID>" + "</Event>";
        } else if (action == LastChange.NOMEDIA) {
            lastChangeExpected = AVTransportConstStr.DEFAULT_LASTCHANGE;
        } else if (action == LastChange.OTHER) {
            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
                    "<InstanceID val=\"0\">" +
                    "<TransportState val=\"" + getStateVariable(AVTransportConstStr.TRANSPORTSTATE).getValue_dlna() + "\"/>" +
                    "</InstanceID></Event>";
        } else if (action == LastChange.TRANSITIONING) {
            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
                    "<InstanceID val=\"0\">" +
                    "<TransportState val=\"TRANSITIONING\"/>" +
                    "<AVTransportURI val=\"" + getStateVariable(AVTransportConstStr.AVTRANSPORTURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackURI val=\"" + getStateVariable(AVTransportConstStr.CURRENTTRACKURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackMetaData val=\"" + getStateVariable(AVTransportConstStr.CURRENTTRACKMETADATA).getValue_dlna() + "\"/>" +
                    "</InstanceID>" + "</Event>";
        } else {
            Debug.warning("[Error] sendLastChangeEvent: Unknown Action Name: " + action.name());
            return;
        }

        sendLastChangeEvent(lastChangeExpected);
    }

    public void sendLastChangeEvent(String content) {
        if (content == null) {
            Debug.warning("[Error] sendLastChangeEvent: content == null");
            return;
        }

        Debug.message("SendLastChangeEvent [NEW]: " + content);
        String previous = getStateVariable(AVTransportConstStr.LASTCHANGE).getValue_dlna();
        Debug.message("SendLastChangeEvent [OLD]: " + previous);

        if (content.equals(previous)) {
            Debug.message("SendLastChangeEvent: [Skip For No Change]");
        } else {
            getStateVariable(AVTransportConstStr.LASTCHANGE).setValue(content, false);
        }
    }
}
