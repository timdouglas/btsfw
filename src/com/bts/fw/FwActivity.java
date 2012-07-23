package com.bts.fw;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
        
        //start service
        sendBroadcast(new Intent(this, FwScheduleReceiver.class));
        
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
    	
    	//TODO remove this:
    	stopService(new Intent(this, FwService.class));
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
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