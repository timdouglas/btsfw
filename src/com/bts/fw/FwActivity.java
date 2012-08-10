package com.bts.fw;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Fetches JSON from server and parses it before
 * displaying who will be on stage now and next
 *
 * Also starts a service to make notifications when somebody is about to
 * go onto stage - TODO finish that...
 *
 * @author tim
 *
 */
public class FwActivity extends Activity {
	private static String TAG = "btsfw";
	protected static final String NOTIFICATIONS_FILE = "fw_notify";
	private static String data_url ="http://stuff.timdouglas.co.uk/bts/fw.php";
	private FwTimes times;
	private TextView on_now_text, on_next_text, notes_text;
	private Timer timer = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        on_now_text = (TextView)findViewById(R.id.onnow_text);
        on_next_text = (TextView)findViewById(R.id.onnext_text);
        notes_text = (TextView)findViewById(R.id.notes_text);

        DataTask data = new DataTask();
        data.execute(data_url);

        //set up timer
        timer = new Timer("fw_timer");
        timer.scheduleAtFixedRate(new TimerTask() {
        	@Override
        	public void run() {
        		DataTask data = new DataTask();
        		data.execute(FwActivity.data_url);
        	}
        }, 300000, 300000);
    }

    @Override
    protected void onStop() {
    	super.onStop();
    	if(timer != null) {
    		timer.cancel();
    	}
    }

    @Override
    protected void onResume() {
    	super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.options, menu);
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    		case R.id.toggle_notifications:
    			toggleNotifications((String)item.getTitle());
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }

    /**
     * If notification file exists, change menu item
     * text to the correct toggle name
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	File file = getFileStreamPath(NOTIFICATIONS_FILE);
    	MenuItem item = menu.findItem(R.id.toggle_notifications);

    	if(file.exists()) {
    		item.setTitle(R.string.disable_notifications);
    	}
    	else {
    		item.setTitle(R.string.enable_notifications);
    	}

    	return true;
    }

    /**
     * static so can be called by services
     * @param context - application base context
     * @return true if can, false if not
     */
    public static boolean canShowNotifications(Context context) {
    	File file = context.getFileStreamPath(NOTIFICATIONS_FILE);

    	if(file.exists()) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    /**
     * write or remove a file to indicate
     * whether the user wants to receive
     * notifications or not
     */
    protected void toggleNotifications(String title) {
    	File file = getFileStreamPath(NOTIFICATIONS_FILE);

    	//probably shouldn't check the title, but meh...
    	if(title.equals("Enable Notifications")) {
    		if(!file.exists()) {
    			try {
    				FileOutputStream fos = openFileOutput(NOTIFICATIONS_FILE, Context.MODE_PRIVATE);
	    			fos.write("show notifications".getBytes()); //doesn't really matter this...
	    			fos.close();

	    			//start service
	    			sendBroadcast(new Intent(this, FwStartServiceReceiver.class));
    			}
    			catch(IOException io) {
    				Log.e(TAG, io.getMessage());
    			}
    		}
    	}
    	else {
    		if(file.exists()) {
    			file.delete();
    			stopService(new Intent(this, FwService.class));
    		}
    	}
    }

    protected void scheduleNotification(FwAct act) {
    	Date on = act.getOnStage();
    	long notification_time = on.getTime() - (60 * 5 * 1000); //five minutes before act goes on stage

    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(System.currentTimeMillis());
    	calendar.add(Calendar.DATE, (int) notification_time);

    	AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
    	int id = (int) System.currentTimeMillis();

    	Intent intent = new Intent(this, FwStartServiceReceiver.class);
    	intent.putExtra("ACT", true);
    	intent.putExtra("ACTName", act.getName());
    	intent.putExtra("ACTOn", act.getOnStage().getTime());
    	intent.putExtra("ACTOff", act.getOffStage().getTime());

    	PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    	alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);

      //can i just set some PendingIntents here with the time for the notification set?
      
      //NotificationManager notManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      //        
      //int icon = R.drawable.notification_icon; //http://developer.android.com/guide/practices/ui_guidelines/icon_design_status_bar.html (icon sizes)
      //CharSequence not_text = "FW 2012 - act due on stage in 5 minutes";
      //long when = System.currentTimeMillis(); //long when = notification_time;
      //Notification not = new Notification(icon, not_text, when);
      //not.flags |= Notification.FLAG_AUTO_CANCEL;
      //not.defaults |= Notification.DEFAULT_SOUND;
      //not.defaults |= Notification.DEFAULT_VIBRATE; //requires vibrate permission
      //                                          
      //CharSequence not_title = "FW 2012 - Act due on stage";
      //CharSequence not_body = act.getName()+" is due on stage in at "+act.getOnStage().toString()+" until...";
      // PendingIntent not_intent = PendingIntent.getActivity(context, 0, done, PendingIntent.FLAG_CANCEL_CURRENT);
      //                                                                              
      //int ID = 1; //should be static private etc - reference for updating notification
      //                                                                                         
      //not.setLatestEventInfo(context, not_title, not_body, not_intent);
      //notManager.notify(ID, not);
    }

    /**
     * AsyncTask to fetch and parse and draw JSON
     * @author tim
     *
     */
    private class DataTask extends AsyncTask<String, Void, String> {
    	@Override
    	protected String doInBackground(String... urls) {
    		String response = "";

    		for(String url : urls) {
    			HttpURLConnection url_connection = null;

    			try {
    				URL data_url = new URL(url);
    				Log.d(TAG, "connecting to "+url);
    				url_connection = (HttpURLConnection) data_url.openConnection();
    				InputStream in = new BufferedInputStream(url_connection.getInputStream());
    				BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
    				StringBuffer sb = new StringBuffer("");

    				String s = "";

    				while((s = buffer.readLine()) != null) {
    					sb.append(s+"\n");
    				}

    				response += sb.toString();

	    		}
    			catch (MalformedURLException e) {
	    			Log.e(TAG, e.getMessage());
	    		}
	            catch (IOException io) {
	            	Log.e(TAG, io.getMessage());
	            }
	    		finally {
	    			if(url_connection != null) {
	    				url_connection.disconnect();
	    			}
	            }
    		}

    		return response;
    	}

    	@Override
    	protected void onPostExecute(String response) {
    		//parse response
    		try {
    			JSONObject json = (JSONObject) new JSONTokener(response).nextValue();

    			//TODO compare existing times with received json to see if we need an update
    			//otherwise, don't change this:
    			times = new FwTimes(json);

    			FwAct on_now = times.getNowOnStage(new Date());

    			if(on_now != null) {
    				on_now_text.setText(on_now.getName()+"\nUntil: "+formatTime(on_now.getOffStage().getHours(), on_now.getOffStage().getMinutes()));
    			}
    			else {
    				on_now_text.setText("No acts on stage yet");
    			}

    			FwAct on_next = times.getNextOnStage(new Date());

    			if(on_next != null) {
    				on_next_text.setText(on_next.getName()+"\nAt: "+formatTime(on_next.getOnStage().getHours(), on_next.getOnStage().getMinutes()));
    			}
    			else {
    				on_next_text.setText("No more acts to come");
    			}

    			notes_text.setText(times.getNotes());

    			//write to storage
    			String FILENAME = "fw_times";
    			try {
	    			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
	    			fos.write(response.getBytes());
	    			fos.close();
    			}
    			catch(IOException io) {
    				Log.e(TAG, io.getMessage());
    			}

    			if(FwActivity.canShowNotifications(getApplicationContext())) {
    				for(FwAct act : times.getActs()) {
    					scheduleNotification(act);
    				}
    			}
    		}
    		catch(JSONException je) {
    			Log.e(TAG, je.getMessage());
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
}
