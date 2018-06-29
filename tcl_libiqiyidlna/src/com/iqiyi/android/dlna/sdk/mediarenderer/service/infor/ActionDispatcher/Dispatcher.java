package com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ActionDispatcher;

import com.iqiyi.android.dlna.sdk.mediarenderer.service.AVTransport;

import org.cybergarage.upnp.Action;

/**
 * Created by wangzhen on 12/11/17.
 */

public interface Dispatcher {

    boolean actionControlReceived(Action action);

    boolean sendLastChangeEvent(AVTransport.LastChange action, String uri, int num, String metadata);
}
