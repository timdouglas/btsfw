package com.bts.fw;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Class to hold an individual act
 * @author tim
 *
 */
public class FwAct {
	private static String TAG = "btsfw"; 
	private Date on_stage;
	private Date off_stage;
	private String name;
	
	public FwAct(String name, Date on, Date off) {
		this.name = name;
		this.on_stage = on;
		this.off_stage = off;
	}
	
	public FwAct(JSONObject json) {
		try {
			this.name = json.getString("act");
			this.on_stage = new Date(json.getLong("on")*1000);
			this.off_stage = new Date(json.getLong("off")*1000);
		}
		catch(JSONException je) {
			Log.e(TAG, je.getMessage());
			this.name = "INVALID JSON";
			this.on_stage = new Date();
			this.off_stage = new Date();
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public Date getOnStage() {
		return this.on_stage;
	}
	
	public Date getOffStage() {
		//get off the stage!
		return this.off_stage;
	}
}
