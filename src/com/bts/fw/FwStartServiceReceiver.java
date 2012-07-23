package com.bts.fw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Broadcast Receiver to start service on request
 * 
 * @author tim
 *
 */
public class FwStartServiceReceiver extends BroadcastReceiver {
	private static String TAG = "btsfw";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(FwActivity.canShowNotifications(context)) {
			Intent service = new Intent(context, FwService.class);
			Log.d(TAG, "starting service in start receiver");
			context.startService(service);
		}
	}
}
