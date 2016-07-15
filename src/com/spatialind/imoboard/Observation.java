package com.spatialind.imoboard;

import java.util.Calendar;
import java.util.Date;

public class Observation {
	private Date time;
	private ObservationType observationType;
	private float bearing;
	private float range;
	private int angleOnTheBow;
	
	public void setTime(Date _time) {
		this.time = _time;
	}
	public Date getTime() {
		return this.time;
	}
	public void setObservationType(ObservationType observationType) {
		this.observationType = observationType;
	}
	public ObservationType getObservationType() {
		return observationType;
	}
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	public float getBearing() {
		return bearing;
	}
	public void setRange(float distance) {
		this.range = distance;
	}
	public float getRange() {
		return range;
	}
	public void setAngleOnTheBow(int angleOnTheBow) {
		this.angleOnTheBow = angleOnTheBow;
	}
	public int getAngleOnTheBow() {
		return angleOnTheBow;
	}
	
	public Observation() {
		this.time = Calendar.getInstance().getTime();
	}

	public Observation(ObservationType _observationType, 
			int _bearing) {
		this.time = Calendar.getInstance().getTime();
		observationType = _observationType;
		bearing = _bearing;
	}
	
	public Observation(ObservationType _observationType, 
			int _bearing, int _angleOnTheBow) {
		this.time = Calendar.getInstance().getTime();
		observationType = _observationType;
		bearing = _bearing;
		angleOnTheBow = _angleOnTheBow;
	}
	
	public Observation(ObservationType _observationType, 
		int _bearing, int _range, int _angleOnTheBow) {
		this.time = Calendar.getInstance().getTime();
		observationType = _observationType;
		bearing = _bearing;
		range = _range;
		angleOnTheBow = _angleOnTheBow;
	}
	
	public Observation(Date _time, ObservationType _observationType, 
			int _bearing, int _range, int _angleOnTheBow) {
		this.time = _time;
		observationType = _observationType;
		bearing = _bearing;
		range = _range;
		angleOnTheBow = _angleOnTheBow;
	}
}
