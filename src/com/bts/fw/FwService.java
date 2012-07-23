package com.bts.fw;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

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
		//check file
		/*
		try {
			FileInputStream fis = openFileInput("fw_times");
		}
		catch(Exception e)
		{
			Log.e(TAG, e.getMessage());
		}
		*/
		
		return Service.START_NOT_STICKY;
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

	

}
