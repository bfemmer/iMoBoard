/**
 * 
 */
package com.spatialind.imoboard;

/**
 * @author Bill Femmer
 *
 */
public interface OnShipCompassListener {
	public abstract void  onCourseChanged(int identifier, float course);
	public abstract void  onBearingChanged(int identifier, float bearing);
}
