package com.bts.fw;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast Receiver used to restart the service when
 * it's completed notification checks & at defined intervals
 * 
 * TODO finish & test this...
 * 
 * @author tim
 *
 
public class FwScheduleReceiver extends BroadcastReceiver {
	
	//Restart service every 5 minutes
	private static final long REPEAT_TIME = 1000*60*5;

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, FwStartServiceReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		
		// Fetch every 30 seconds
		// InexactRepeating allows Android to optimise the energy consumption
		service.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, REPEAT_TIME, pending);

		// service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);

	}

}
*/