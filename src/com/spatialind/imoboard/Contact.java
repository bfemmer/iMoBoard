package com.spatialind.imoboard;

import java.util.ArrayList;
import java.util.Iterator;

public class Contact {
	private String id;
	private float course;
	private float speed;
	private ArrayList<Observation> observationList;
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setCourse(float course) {
		this.course = course;
	}
	public float getCourse() {
		return course;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	public float getSpeed() {
		return speed;
	}
	public void setObservationList(ArrayList<Observation> observationList) {
		this.observationList = observationList;
	}
	public ArrayList<Observation> getObservationList() {
		return observationList;
	}
	
	public Contact() {
		observationList = new ArrayList<Observation>();
	}
	
	public Contact(String _id) {
		id = _id;
		observationList = new ArrayList<Observation>();
	}
	
	public Contact(String _id, float _course, float _speed) {
		id = _id;
		course = _course;
		speed = _speed;
		observationList = new ArrayList<Observation>();
	}
	
	public int getLastBearingRate() {
		return 1;
	}
	
	/**
	 * Adds an observation to the array list
	 * 
	 * @param observation Object to add to array list
	 */
	public void addObservation(Observation observation) {
		observationList.add(observation);
	}
	
	/**
	 * Clears all observations objects in the array list
	 */
	public void clearObservations() {
		observationList.clear();
	}
	
	/**
	 * Returns whether target is closing in distance (range)
	 * 
	 * @return true if target is closing in range, false if opening in range
	 */
	public boolean isClosing() {
		boolean closing = false;
		float currentRange, previousRange;
		int observationCount = observationList.size();
		
		currentRange = previousRange = 0;
		
		// Look for more than one observation in order to determine
		// a range rate
		if (observationCount > 1) {
			Iterator<Observation> observationIterator = 
				observationList.iterator();
        	while(observationIterator.hasNext())
        	{
        		currentRange = observationIterator.next().getRange();
        		
        		if (currentRange >= previousRange) closing = false;
        		else closing = true;
        		
        		previousRange = currentRange;
        	}
			
			return closing;
		}
		else return false;
	}
	
	public float getLastBearing() {
		float bearing = 361;
		int observationCount = observationList.size();

		if (observationCount > 0) {
			bearing = observationList.get(observationCount - 1).getBearing();
		}
		
		return bearing;
	}
	
	@Override public String toString() {
		float lastBearing = getLastBearing();
		String value = this.id + " ";
		
		if (lastBearing != 361)
			value += "bearing " + String.valueOf(lastBearing) + " ";
		value += (isClosing() ? "(closing)" : "(opening)");
		return value ;  
	}
}
