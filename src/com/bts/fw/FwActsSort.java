package com.bts.fw;

import java.util.Comparator;
/**
 * Helper class to sort the acts in an array list by time on stage
 * @author tim
 *
 */
public class FwActsSort implements Comparator<FwAct> {
	@Override
	public int compare(FwAct a, FwAct b) {
		return a.getOnStage().compareTo(b.getOnStage());
	}
}