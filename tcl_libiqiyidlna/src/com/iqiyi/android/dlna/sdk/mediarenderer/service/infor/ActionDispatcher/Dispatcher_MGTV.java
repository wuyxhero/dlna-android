package com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ActionDispatcher;

import com.iqiyi.android.dlna.sdk.mediarenderer.service.AVTransport;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.AVTransportConstStr;

import org.cybergarage.upnp.Action;
import org.cybergarage.util.Debug;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by wangzhen on 12/11/17.
 */

public class Dispatcher_MGTV implements Dispatcher {
    public static final String TAG = "Dispatcher_MGTV";

    private AVTransport mAVTransport;
    private String mTrackDuration = "00:00:00";

    public Dispatcher_MGTV(AVTransport avTransport) {
        mAVTransport = avTransport;
    }

    @Override
    public boolean actionControlReceived(Action action) {
        String actionName = action.getName();

        if (actionName.equals(AVTransportConstStr.SETAVTRANSPORTURI)) {
            sendLastChangeEvent(AVTransport.LastChange.TRANSITIONING, null, 0, null);
        } else if (actionName.equals(AVTransportConstStr.GETPOSITIONINFO)) {
            String trackDuration = "00:00:00";
            String absTime = "00:00:00";
            String relTime = "00:00:00";
            if (mAVTransport.getAVTransportListener() != null) {
                trackDuration = mAVTransport.getAVTransportListener().getTrackDuration();
                absTime = mAVTransport.getAVTransportListener().getPosition();
                relTime = absTime;
            }
            String transportState = mAVTransport.getStateVariable(AVTransportConstStr.TRANSPORTSTATE).getValue_dlna();
            if (AVTransportConstStr.PLAYING.equals(transportState) || AVTransportConstStr.PAUSED_PLAYBACK.equals(transportState))
                action.setArgumentValue(AVTransportConstStr.TRACK, "1");
            else
                action.setArgumentValue(AVTransportConstStr.TRACK, "0");

            if (AVTransportConstStr.PLAYING.equals(transportState)) {
                mTrackDuration = trackDuration;
            }
            if (AVTransportConstStr.STOPPED.equals(transportState)) {
                trackDuration = mTrackDuration;
            }
            action.setArgumentValue(AVTransportConstStr.TRACKDURATION, trackDuration);
            action.setArgumentValue(AVTransportConstStr.TRACKMETADATA, mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKMETADATA).getValue_dlna());
            action.setArgumentValue(AVTransportConstStr.TRACKURI, mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKURI).getValue_dlna());
            action.setArgumentValue(AVTransportConstStr.RELTIME, absTime);
            action.setArgumentValue(AVTransportConstStr.ABSTIME, relTime);
            action.setArgumentValue(AVTransportConstStr.RELCOUNT, "2147483647");
            action.setArgumentValue(AVTransportConstStr.ABSCOUNT, "2147483647");
            return true;
        }

        return false;
    }

    @Override
    public boolean sendLastChangeEvent(AVTransport.LastChange action, String uri, int num, String metadata) {
        String lastChangeExpected;

        if (action == AVTransport.LastChange.PLAYING) {
            SimpleDateFormat.getDateInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String date = df.format(new Date());
            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\"><InstanceID val=\"0\"><TransportState val=\"PLAYING\"/><AVTransportURI val=\"" + uri +
                    "\"/><CurrentTrackURI val=\"" + uri +
                    "\"/><CurrentTrackMetaData val=\"<?xml version=\"1.0\"?><DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"><item id=\"id\" parentID=\"0\" restricted=\"0\"><dc:title>name</dc:title><upnp:artist>unknow</upnp:artist><upnp:class>object.item.videoItem</upnp:class><dc:date>" + date +
                    "</dc:date><res protocolInfo=\"http-get:*:*/*:*\"  >" + uri +
                    "</res></item></DIDL-Lite>\"/></InstanceID></Event>";
        } else if (action == AVTransport.LastChange.STOPPED) {
            lastChangeExpected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<e:propertyset xmlns:e=\"urn:schemas-upnp-org:event-1-0\"><e:property><LastChange><Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\"><InstanceID val=\"0\"><TransportState val=\"STOPPED\"/></InstanceID></Event></LastChange></e:property></e:propertyset>";
        } else if (action == AVTransport.LastChange.PAUSED) {
            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\"><InstanceID val=\"0\"><TransportState val=\"PAUSED_PLAYBACK\"/></InstanceID></Event>";
        } else if (action == AVTransport.LastChange.TRANSITIONING) {
            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
                    "<InstanceID val=\"0\">" +
                    "<TransportState val=\"TRANSITIONING\"/>" +
                    "<AVTransportURI val=\"" + mAVTransport.getStateVariable(AVTransportConstStr.AVTRANSPORTURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackURI val=\"" + mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackMetaData val=\"" + mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKMETADATA).getValue_dlna() + "\"/>" +
                    "</InstanceID></Event>";
        } else {
            Debug.message(TAG, "SendLastChangeEvent: " + action.name() + " not handled");
            return false;
        }

        mAVTransport.sendLastChangeEvent(lastChangeExpected);
        return true;
    }
}
