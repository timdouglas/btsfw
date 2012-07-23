package com.bts.fw;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Service to check stored JSON & display a notification
 * when an act is about to appear on stage (ie in time for 
 * a change over)
 * 
 * TODO finish this!
 * 
 * @author tim
 *
 */
public class FwService extends Service {
	private static String TAG = "btsfw";
	private final IBinder mBinder = new MyBinder();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(FwActivity.canShowNotifications(getApplicationContext())) {
			//have we got the act in the intent?
			if(intent.getBooleanExtra("ACT", false) == true) {
				
				//construct act
				Date on = new Date(intent.getLongExtra("ACTOn", 0));
				Date off = new Date(intent.getLongExtra("ACTOff", 0));
				String name = intent.getStringExtra("ACTName");
				
				FwAct act = new FwAct(name, on, off);
				
				//double check times
				Date now = new Date(System.currentTimeMillis());
				Date notify_time = new Date(on.getTime() - (5 * 60 * 1000));
				
				//TODO notification isn't firing - fix this!
				if(now.after(notify_time) || now.equals(notify_time)) {

					//show notification
					NotificationManager notifications = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
					
					Intent app = new Intent(getApplicationContext(), FwActivity.class);
					PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, app, PendingIntent.FLAG_UPDATE_CURRENT);
					
					String body = act.getName()+" will be on stage at "+formatTime(act.getOnStage().getHours(), act.getOnStage().getMinutes());
					
					Notification notification = new Notification(R.drawable.notification_icon, "FW: "+act.getName()+" about to go on stage", System.currentTimeMillis());
					notification.setLatestEventInfo(getApplicationContext(), "Freshers' Week Times", body, pending);
					notifications.notify(1, notification);
				}
			}
		}
		
		Toast.makeText(getApplicationContext(), "started service", Toast.LENGTH_LONG).show();
		
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "service destroyed");
		Toast.makeText(getApplicationContext(), "stopped service", Toast.LENGTH_LONG).show();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		FwService getService() {
			return FwService.this;
		}
	}
	
	/**
	 * This should probably be a prototype on Date
	 * @param hours
	 * @param minutes
	 * @return formatted string hh:mm
	 */
	private String formatTime(int hours, int minutes) {
		String h = ""+hours;
		String m = ""+minutes;
		
		if(h.length() == 1) {
			h = "0"+h;
		}
		
		if(m.length() == 1) {
			m = "0"+m;
		}
		
		return h+":"+m;
	}

	

}
