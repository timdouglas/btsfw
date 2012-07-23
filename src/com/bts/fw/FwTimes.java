package com.bts.fw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to hold the running order sent from webservice
 * 
 * Expected JSON:
 * {
 *   "acts": [ { "act": "act_name", "on": <timestamp>, "off": <timestamp> }, { ... }, ... ],
 *   "notes": "random string of stuff"
 * }
 * 
 * @author tim
 *
 */
public class FwTimes {
	private ArrayList<FwAct> acts;
	private String notes;
		
	public FwTimes(JSONObject http_response) {
		try {
			
			JSONArray acts_arr = http_response.getJSONArray("acts");
			this.acts = new ArrayList<FwAct>();
			
			for(int i = 0; i < acts_arr.length(); i++) {
				JSONObject act_json = acts_arr.getJSONObject(i);
				FwAct act = new FwAct(act_json);
				this.acts.add(act);
			}
			
			//sort acts
			Collections.sort(this.acts, new FwActsSort());
			
			this.notes = http_response.getString("notes");
		}
		catch(JSONException je) {
			this.acts = new ArrayList<FwAct>();
			this.notes = "INVALID JSON";
		}
	}
	
	public ArrayList<FwAct> getActs() {
		return this.acts;
	}
	
	public String getNotes() {
		return this.notes;
	}
	
	public FwAct getNowOnStage(Date time) {
		FwAct now = null;
		
		for(FwAct act : this.acts) {
			if(time.after(act.getOnStage()) && time.before(act.getOffStage())) {
				now = act;
			}
		}
		
		return now;
	}
	
	public FwAct getNextOnStage(Date time) {
		FwAct on = this.getNowOnStage(time);
		FwAct next = null;
		
		//nothing on stage at the moment, so logically the first in the list is next...
		if(on == null) {
			return this.acts.get(0);
		}
		
		for(FwAct act : this.acts) {
			if(act.getOnStage().after(on.getOffStage()) || act.getOnStage().equals(on.getOffStage())) {
				next = act;
				break;
			}
		}
		
		return next;
	}
}