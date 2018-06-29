package com.iqiyi.android.sdk.dlan.mediacontroller.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ScreenActionReceiver extends BroadcastReceiver {

	private boolean isRegisterReceiver = false;

	public void registerScreenActionReceiver(Context mContext) {
		if (!isRegisterReceiver) {
			isRegisterReceiver = true;

			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_SCREEN_ON);
			mContext.registerReceiver(ScreenActionReceiver.this, filter);
		}
	}

	public void unRegisterScreenActionReceiver(Context mContext) {
		if (isRegisterReceiver) {
			isRegisterReceiver = false;
			mContext.unregisterReceiver(ScreenActionReceiver.this);
		}
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		
	}

}