/**
 * 
 */
package com.spatialind.imoboard;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * @author Bill Femmer
 *
 */
public class MoboUtilities {
	public static double calculateContactSpeed(double _drm, double _srm, double _Co, double _So) {
		double angle;
		double temp, contactSpeed;
		
		// Calculate angle between the DRM vector and 
		// own ship's vector
		if (_Co > 180.0) angle = _Co + 180 - _drm;
		else angle = _Co - _drm;
		
		// Check angle is positive
		if (angle < 0) angle += 180.0;
		
		// Law of cosines to calculate contact's speed
		temp = _So * _So + 
			_srm * _srm -
			2.0 * _So * _srm * Math.cos(Math.toRadians(angle));
		contactSpeed = Math.sqrt(temp);
		
		return contactSpeed;
	}
	
	public static double calculateTrueWindSpeed(double _drw, double _srw, double _Co, double _So) {
		double temp, angle, trueSpeed;
		
		if (_drw > 180.0) angle = 360.0 - _drw;
		else angle = _drw;
		
		// Law of cosines to calculate contact's speed
		temp = _So * _So + 
			_srw * _srw -
			2.0 * _So * _srw * Math.cos(Math.toRadians(angle));
		trueSpeed = Math.sqrt(temp);
		
		return trueSpeed;
	}
	
	/**
	 * @brief Calculate course of contact
	 * 
	 * Law of cosines
	 * side a = So (own ship's speed)
	 * side b = SRM (speed of relative motion)
	 * side c = S (contact speed)
	 * 
	 * @param _drm Direction of Relative Motion
	 * @param _srm Speed of Relative Motion
	 * @param _contactSpeed Speed of contact in knots
	 * @param _Co Own ship's course
	 * @param _So Own ship's speed
	 * @return
	 */
	public static double calculateContactCourse(double _drm, 
			double _srm, double _contactSpeed, double _Co, double _So) {
		double temp, contactCourse, Cr, sideAngleB;
		//anga = Math.acos((-sidea*sidea+sideb*sideb+sidec*sidec)/(2*sideb*sidec))
		//angb = Math.acos((-sideb*sideb+sidea*sidea+sidec*sidec)/(2*sidea*sidec))
		//angc = Math.acos((-sidec*sidec+sidea*sidea+sideb*sideb)/(2*sidea*sideb))

		temp = (-_srm * _srm +
				_So * _So +
				_contactSpeed * _contactSpeed) /
				(2 * _So * _contactSpeed);
		
		// Calculate course in radians
		sideAngleB = Math.acos(temp);
		
		// Convert to degrees
		sideAngleB = Math.toDegrees(sideAngleB);
		
		// Calculate reciprocal ship course
		Cr = _Co + 180.0;
		if (Cr > 360.0) Cr -= 360.0;
		
		// Determine if DRM is left or right of reciprocal course
		// and calculate course of contact
		if (Cr > _drm) {
			contactCourse = _Co + sideAngleB;
			if (contactCourse >= 360.0) contactCourse -= 360.0;
		} else {
			contactCourse = _Co - sideAngleB;
			if (contactCourse < 0) contactCourse += 360.0;
		}
		
		// Return
		return contactCourse;
	}
	
	public static double calculateTrueWindDirection(double _drw, 
			double _srw, double _stw, double _Co, double _So) {
		double drm, dtw;
		
		// Calculate reciprocal vector of relative wind as DRM
		drm = _Co + _drw + 180.0;
		if (drm > 360.0) drm -= 360.0;
		
		dtw = calculateTrueVector(drm, _srw, _stw, _Co, _So);
		return dtw;
	}
	
	private static double calculateTrueVector(double _drm, 
			double _srm, double _contactSpeed, double _Co, double _So) {
		double temp, trueVector, Cr, sideAngleB;
		//anga = Math.acos((-sidea*sidea+sideb*sideb+sidec*sidec)/(2*sideb*sidec))
		//angb = Math.acos((-sideb*sideb+sidea*sidea+sidec*sidec)/(2*sidea*sidec))
		//angc = Math.acos((-sidec*sidec+sidea*sidea+sideb*sideb)/(2*sidea*sideb))

		temp = (-_srm * _srm +
				_So * _So +
				_contactSpeed * _contactSpeed) /
				(2 * _So * _contactSpeed);
		
		// Calculate course in radians
		sideAngleB = Math.acos(temp);
		
		// Convert to degrees
		sideAngleB = Math.toDegrees(sideAngleB);
		
		// Calculate reciprocal ship course
		Cr = _Co + 180.0;
		if (Cr > 360.0) Cr -= 360.0;
		
		// Determine if DRM is left or right of reciprocal course
		// and calculate vector (contact course or true wind direction)
		if (Cr > _drm) {
			trueVector = _Co + sideAngleB;
			if (trueVector >= 360.0) trueVector -= 360.0;
		} else {
			trueVector = _Co - sideAngleB;
			if (trueVector < 0) trueVector += 360.0;
		}
		
		// Return
		return trueVector;
	}
	
	public static int calculateShortestDistanceToLine(PointF pointC, PointF lineEndPointA, PointF lineEndPointB) {
		// Calculate vector of line points (AB)
		PointF VectorAB = new PointF(lineEndPointB.x - lineEndPointA.x, 
				lineEndPointB.y - lineEndPointA.y);
		
		// Calculate vector of one line point and the single point (AC)
		PointF VectorAC = new PointF(pointC.x - lineEndPointA.x, 
				pointC.y - lineEndPointA.y);

		// Cross multiply vectors AB and AC (cross product)
		double crossProductVector = (VectorAB.x * VectorAC.y) - (VectorAB.y * VectorAC.x);
		
		// Divide cross product value by the length of vector AB (which is sqrt of x^2 + y^2)
		int dist = (int)(crossProductVector / 
				Math.sqrt(VectorAB.x * VectorAB.x + VectorAB.y * VectorAB.y));
		
		// Return absolute value
		return Math.abs(dist);
	}
	
	public static PointF getPointFromRangeAndBearing(float _range, float _bearing)
    {
        PointF point = new PointF();

        // Convert angle to radians
        double angleInRadians = _bearing * Math.PI / 180.0;

        // Calculate point
        point.y = (float)(_range * Math.cos(angleInRadians));
        point.x = (float)(_range * Math.sin(angleInRadians));

        return point;
    }
	
	public static PointF getPointFromRangeAndBearing(PointF center, float _range, float _bearing)
    {
        PointF point = new PointF();

        // Convert angle to radians
        double angleInRadians = _bearing * Math.PI / 180.0;

        // Calculate point
        point.y = (float)(center.y + _range * Math.cos(angleInRadians));
        point.x = (float)(center.x + _range * Math.sin(angleInRadians));

        return point;
    }
	
	public static double getAngleBetweenPoints(PointF point1, PointF point2)
    {
        double angleInRadians;
        double angleInDegrees;
        float slope;
        
        // Get slope of line
        slope = getSlopeBetweenPoints(point1, point2);
        
        // Calculate angle in radians
        angleInRadians = Math.atan(slope);

        // Convert to degrees
        angleInDegrees = angleInRadians * 180.0 / Math.PI;

        // Adjust angle for quadrant
        angleInDegrees += (point2.x < point1.x) ? 90 : 270;

        // Invert angle (-1)
        angleInDegrees *= (-1.0);
        
        // Add 360 to keep the angle positive
        angleInDegrees += 360.0;

        return angleInDegrees;
    }
	
	public static float getSlopeBetweenPoints(PointF point1, PointF point2)
    {
        float deltaX = point2.x - point1.x;
        float deltaY = point2.y - point1.y;

        // Prevent divide-by-zero
        if (deltaX == 0) deltaX = 0.0000000000001F;

        return deltaY / deltaX;
    }
	
	public static double getDistanceBetweenPoints(PointF originPoint, PointF intersectPoint)
    {
        double deltaX = (double)(intersectPoint.x - originPoint.x);
        double deltaY = (double)(intersectPoint.y - originPoint.y);

        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
	
	public static PointF getIntersection(PointF previousPoint, PointF pointToPlot, 
            PointF originPoint, Point point4)
    {
        float s1_x, s1_y, s2_x, s2_y, s, t;
        double intersectX, intersectY;
        
        intersectX = intersectY = 0;
        s1_x = pointToPlot.x - previousPoint.x;
        s1_y = pointToPlot.y - previousPoint.y;
        s2_x = point4.x - originPoint.x;
        s2_y = point4.y - originPoint.y;

        s = (-s1_y * (previousPoint.x - originPoint.x) + s1_x * (previousPoint.y - originPoint.y)) / 
            (-s2_x * s1_y + s1_x * s2_y);
        t = (s2_x * (previousPoint.y - originPoint.y) - s2_y * (previousPoint.x - originPoint.x)) / 
            (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
        {
            // Collision detected
            intersectX = previousPoint.x + (t * s1_x);
            intersectY = previousPoint.y + (t * s1_y);
            PointF intersect = new PointF((float)intersectX, (float)intersectY);
            return intersect;
        }
        else
        {
        	return new PointF(777, 777);
        }
    }
}
