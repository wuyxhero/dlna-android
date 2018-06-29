package com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ActionDispatcher;

import com.iqiyi.android.dlna.sdk.mediarenderer.service.AVTransport;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.AVTransportConstStr;

import org.cybergarage.upnp.Action;
import org.cybergarage.util.Debug;
import org.cybergarage.xml.XML;

/**
 * Created by wangzhen on 12/11/17.
 */

public class Dispatcher_QPLAY implements Dispatcher {
    public static final String TAG = "Dispatcher_QPLAY";

    private AVTransport mAVTransport;

    public Dispatcher_QPLAY(AVTransport avTransport) {
        mAVTransport = avTransport;
    }

    @Override
    public boolean actionControlReceived(Action action) {

        String actionName = action.getName();

        if (actionName.equals(AVTransportConstStr.GETMEDIAINFO)) {
            //获取当前播放的媒体信息
            String trackDuration = "00:00:00";
            if (mAVTransport.getAVTransportListener() != null) {
                trackDuration = mAVTransport.getAVTransportListener().getTrackDuration();
            }

            action.getArgument(AVTransportConstStr.NRTRACKS).setValue("1");
            action.getArgument(AVTransportConstStr.MEDIADURATION).setValue(trackDuration);
            // 这里给QPLAY Source的需要是qplay://xxxxxxxx这种
            action.getArgument(AVTransportConstStr.CURRENTURI).setValue(
                    mAVTransport.getStateVariable(AVTransportConstStr.AVTRANSPORTURI).getValue_dlna());
            action.getArgument(AVTransportConstStr.CURRENTURIMETADATA).setValue(
                    mAVTransport.getStateVariable(AVTransportConstStr.AVTRANSPORTURIMETADATA).getValue_dlna());
            action.getArgument(AVTransportConstStr.PLAYMEDIUM).setValue("NONE");
            action.getArgument(AVTransportConstStr.RECORDMEDIUM).setValue(AVTransportConstStr.NOT_IMPLEMENTED);
            action.getArgument(AVTransportConstStr.WRITESTATUS).setValue(AVTransportConstStr.NOT_IMPLEMENTED);

            return true;
        }

        return false;
    }

    @Override
    public boolean sendLastChangeEvent(AVTransport.LastChange action, String uri, int num, String metaData) {
        String lastChangeExpected;

        // QPlay发送SetAVTransportURI没有设置下面两个参数，需要上层设置
        if (uri != null)
            mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKURI).setValue(uri, false);

        if (metaData != null) {
            mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKMETADATA).setValue(metaData, false);
        }

        // 需要转换为escapeXMLChars格式QPLAY Source才能识别
        // 实际上这是QPLAY Source端的一个BUG
        // 这里转换成escapeXMLChars格式后发送前还会再进行一次转义
        // 所以形如&quot;就会变成&amp;quot;
        // 但是QPLAY Source只能识别这种错误的格式
        metaData = XML.escapeXMLChars(mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKMETADATA).getValue_dlna());

        if (action == AVTransport.LastChange.PLAYING) {
            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
                    "<InstanceID val=\"0\">" +
                    "<TransportState val=\"PLAYING\"/>" +
                    "<AVTransportURI val=\"" + mAVTransport.getStateVariable(AVTransportConstStr.AVTRANSPORTURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackURI val=\"" + mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackMetaData val=\"" + metaData + "\"/>" +
                    "</InstanceID></Event>";
        } else if (action == AVTransport.LastChange.STOPPED) {
            // 修复电视果播放手机A发送来的QPlay音乐时 手机B上电视果APP点击退出播放 手机A又重新推送QPlay过来的问题
            mAVTransport.getStateVariable(AVTransportConstStr.AVTRANSPORTURI).setValue("qplay://xxxxxx", false);

            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
                    "<InstanceID val=\"0\">" +
                    "<TransportState val=\"STOPPED\"/>" +
                    "<AVTransportURI val=\"" + mAVTransport.getStateVariable(AVTransportConstStr.AVTRANSPORTURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackURI val=\"" + mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackMetaData val=\"" + metaData + "\"/>" +
                    "</InstanceID>" + "</Event>";
        } else if (action == AVTransport.LastChange.PAUSED) {
            lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
                    "<InstanceID val=\"0\">" +
                    "<TransportState val=\"PAUSED_PLAYBACK\"/>" +
                    "<AVTransportURI val=\"" + mAVTransport.getStateVariable(AVTransportConstStr.AVTRANSPORTURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackURI val=\"" + mAVTransport.getStateVariable(AVTransportConstStr.CURRENTTRACKURI).getValue_dlna() + "\"/>" +
                    "<CurrentTrackMetaData val=\"" + metaData + "\"/>" +
                    "</InstanceID></Event>";
        } else {
            Debug.message(TAG, "SendLastChangeEvent: " + action.name() + " not handled");
            return false;
        }

        mAVTransport.sendLastChangeEvent(lastChangeExpected);
        return true;
    }
}
