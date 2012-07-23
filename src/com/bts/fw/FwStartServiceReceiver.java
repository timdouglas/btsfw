package com.bts.fw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast Receiver to start service on request
 * 
 * @author tim
 *
 */
public class FwStartServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, FwService.class);
		context.startService(service);
	}

}
