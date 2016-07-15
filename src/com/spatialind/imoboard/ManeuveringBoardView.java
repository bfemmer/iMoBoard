package com.spatialind.imoboard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.spatialind.imoboard.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

public class ManeuveringBoardView extends View{
	private int textHeight;
	private int scale;
	private int colorMode;
	private boolean showObservationTime;
	private boolean showDegrees;
	private boolean showCPA;
	private boolean useRecentObservations;
	private boolean roundValues;
	private boolean showBacklight;
	private Paint markerPaint;
	private Paint degreesTextPaint;
	private Paint cpaTextPaint;
	private Paint circlePaint;
	private Paint testPaint;
	private Paint outerRangePaint;
	private Paint rangePaint;
	private Paint relativeTrackPaint;
	private Paint cpaPaint;
	private Paint scalePaint;
	private ArrayList<Contact> contactList;
	private Contact contact;
	private OnZoomListener onZoomListener = null;
	private ZoomButtonsController zoomButtonController;
	private float ownShipCourse;
	private float ownShipSpeed;
	private static final int rangeAtLowestScale = 5000;
	private final int COLOR_MODE_DARK = 0;
	private final int COLOR_MODE_LIGHT = 1;
	
	public void setTargetList(ArrayList<Contact> targetList) {
		this.contactList = targetList;
	}

	public ArrayList<Contact> getTargetList() {
		return contactList;
	}
	
	/**
	 * @param showCPA the showCPA to set
	 */
	public void setShowCPA(boolean showCPA) {
		this.showCPA = showCPA;
	}

	/**
	 * @return the showCPA
	 */
	public boolean isShowCPA() {
		return showCPA;
	}

	/**
	 * @param showDegrees the showDegrees to set
	 */
	public void setShowDegrees(boolean showDegrees) {
		this.showDegrees = showDegrees;
	}

	/**
	 * @return the showDegrees
	 */
	public boolean isShowDegrees() {
		return showDegrees;
	}

	/**
	 * @param showObservationTime the showObservationTime to set
	 */
	public void setShowObservationTime(boolean showObservationTime) {
		this.showObservationTime = showObservationTime;
	}

	/**
	 * @return the showObservationTime
	 */
	public boolean isShowObservationTime() {
		return showObservationTime;
	}

	/**
	 * @param useFirstAndLastObservations the useFirstAndLastObservations to set
	 */
	public void setUseFirstAndLastObservations(boolean useFirstAndLastObservations) {
		this.useRecentObservations = useFirstAndLastObservations;
	}

	/**
	 * @return the useFirstAndLastObservations
	 */
	public boolean isUseFirstAndLastObservations() {
		return useRecentObservations;
	}

	/**
	 * @param roundValues the roundValues to set
	 */
	public void setRoundValues(boolean roundValues) {
		this.roundValues = roundValues;
	}

	/**
	 * @return the roundValues
	 */
	public boolean isRoundValues() {
		return roundValues;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getScale() {
		return scale;
	}
	
	public void setContact(Contact target) {
		this.contact = target;
	}

	public Contact getContact() {
		return contact;
	}
	
	public void setOwnShipCourse(float ownShipCourse) {
		this.ownShipCourse = ownShipCourse;
	}

	public float getOwnShipCourse() {
		return ownShipCourse;
	}

	public void setOwnShipSpeed(float ownShipSpeed) {
		this.ownShipSpeed = ownShipSpeed;
	}

	public float getOwnShipSpeed() {
		return ownShipSpeed;
	}

	public void setColorMode(int colorMode) {
		this.colorMode = colorMode;
	}

	public int getColorMode() {
		return colorMode;
	}

	public void setShowBacklight(boolean showBacklight) {
		this.showBacklight = showBacklight;
	}

	public boolean isShowBacklight() {
		return showBacklight;
	}
	
	public void  setOnZoomListener(
			OnZoomListener listener) {
		onZoomListener = listener;
		zoomButtonController.setOnZoomListener(onZoomListener);
	}
	
	public void onVisibilityChanged(boolean visible) {
		// Check if the Listener was set, 
		// otherwise an Exception occurs when we try to call it
		if(onZoomListener!=null) {
			onZoomListener.onVisibilityChanged(visible);
		}
	}

	public void onZoom(boolean zoomIn) {
		// Check if the Listener was set, 
		// otherwise an Exception occurs when we try to call it
		if(onZoomListener!=null) {
			onZoomListener.onZoom(zoomIn);
		}
	}
	
	public void setZoomInEnabled(boolean zoomIn) {
		zoomButtonController.setZoomInEnabled(zoomIn);
	}
	
public void setZoomOutEnabled(boolean zoomOut) {
		zoomButtonController.setZoomOutEnabled(zoomOut);
	}

	public ManeuveringBoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		scale = 3;
		showBacklight = false;
		colorMode = COLOR_MODE_DARK;
		initMoBoView();
	}

	public ManeuveringBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scale = 3;
		showBacklight = false;
		colorMode = COLOR_MODE_DARK;
		initMoBoView();
	}

	public ManeuveringBoardView(Context context) {
		super(context);
		scale = 3;
		showBacklight = false;
		colorMode = COLOR_MODE_DARK;
		initMoBoView();
	}
	
	protected void onDetachedFromWindow() {
		zoomButtonController.setVisible(false);
	}
	
	@Override  
	public boolean onTouchEvent(MotionEvent event) {  
		zoomButtonController.setVisible(true);
	    return true;
	}
	
	@Override
	protected void onMeasure(int wMeasureSpec, int hMeasureSpec) {
		int measuredHeight = measureHeight(hMeasureSpec);
		int measuredWidth = measureWidth(wMeasureSpec);
		int d = Math.min(measuredWidth, measuredHeight);
		
		setMeasuredDimension(d, d);
	}
	
	private int measureHeight(int measureSpec) {
		int specSize = MeasureSpec.getSize(measureSpec);
		return specSize;
	}
	
	private int measureWidth(int measureSpec) {
		int specSize = MeasureSpec.getSize(measureSpec);
		return specSize;
	}
	
	protected void initMoBoView() {
		zoomButtonController = new ZoomButtonsController(this);
        zoomButtonController.setAutoDismissed(true);
        
		setFocusable(true);
		contact = new Contact();
		initMoBoColors();
	}
	
	private void initMoBoColors() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		float dp;
		int pixels;
		
		// Converting DP to pixels example
		//dp = 14f;
		//fpixels = metrics.density * dp;
		//pixels = (int) (metrics.density * dp + 0.5f);
		
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setStrokeWidth(1);
		circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		// Outer degrees (text)
		dp = 14f;
		pixels = (int) (metrics.density * dp + 0.5f);
		degreesTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		degreesTextPaint.setTextSize(pixels);
		textHeight = (int)degreesTextPaint.measureText("yY");
		
		dp = 12f;
		pixels = (int) (metrics.density * dp + 0.5f);
		cpaTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		cpaTextPaint.setTextSize(pixels);
		
		// Solid CPA line (connects the dots)
		testPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dp = 4f;
		pixels = (int) (metrics.density * dp + 0.5f);
		testPaint.setStrokeWidth(pixels);
		
		// Ring scale text
		dp = 10f;
		pixels = (int) (metrics.density * dp + 0.5f);
		scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		scalePaint.setTextSize(pixels);
		
		// Dashed inner lines of moboard
		markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		DashPathEffect dashPath = new DashPathEffect(new float[]{1,1}, 1);
		markerPaint.setPathEffect(dashPath);
		dp = 2f;
		pixels = (int) (metrics.density * dp + 0.5f);
		markerPaint.setStrokeWidth(pixels);
		
		// Solid outer circle
		outerRangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		outerRangePaint.setStyle(Paint.Style.STROKE);
		dp = 2f;
		pixels = (int) (metrics.density * dp + 0.5f);
		outerRangePaint.setStrokeWidth(pixels);
		
		// Dashed inner circles of moboard
		rangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rangePaint.setPathEffect(dashPath);
		rangePaint.setStyle(Paint.Style.STROKE);
		dp = 2f;
		pixels = (int) (metrics.density * dp + 0.5f);
		rangePaint.setStrokeWidth(pixels);
		
		// Relative track line of contact (extension of observed track)
		relativeTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		relativeTrackPaint.setPathEffect(dashPath);
		dp = 4f;
		pixels = (int) (metrics.density * dp + 0.5f);
		relativeTrackPaint.setStrokeWidth(pixels);
		
		// CPA line (line from center of moboard to CPA)
		cpaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dp = 4f;
		pixels = (int) (metrics.density * dp + 0.5f);
		cpaPaint.setStrokeWidth(pixels);  // was 5
	
		updateColors();
	}
	
	private void updateColors() {
		Resources r = this.getResources();
		
		if (colorMode == COLOR_MODE_DARK) {
			if (showBacklight) {
				circlePaint.setColor(r.getColor(R.color.lime));
			}
			else {
				circlePaint.setColor(r.getColor(R.color.black));
			}
				
			degreesTextPaint.setColor(r.getColor(R.color.lime));
			scalePaint.setColor(r.getColor(R.color.lime));
			cpaTextPaint.setColor(r.getColor(R.color.opaque_red));
			testPaint.setColor(r.getColor(R.color.opaque_red));
			markerPaint.setColor(r.getColor(R.color.grid_translucent_color));
			outerRangePaint.setColor(r.getColor(R.color.grid_translucent_color));
			rangePaint.setColor(r.getColor(R.color.grid_translucent_color));
			relativeTrackPaint.setColor(r.getColor(R.color.opaque_red));
			cpaPaint.setColor(r.getColor(R.color.testblue));
		}
		else {
			if (showBacklight) {
				circlePaint.setColor(r.getColor(R.color.lime));
			}
			else {
				circlePaint.setColor(r.getColor(R.color.white));
			}
			
			degreesTextPaint.setColor(r.getColor(R.color.green));
			scalePaint.setColor(r.getColor(R.color.green));
			cpaTextPaint.setColor(r.getColor(R.color.opaque_red));
			testPaint.setColor(r.getColor(R.color.opaque_red));
			markerPaint.setColor(r.getColor(R.color.green));
			outerRangePaint.setColor(r.getColor(R.color.green));
			rangePaint.setColor(r.getColor(R.color.green));
			relativeTrackPaint.setColor(r.getColor(R.color.opaque_red));
			cpaPaint.setColor(r.getColor(R.color.testblue));
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int px = getMeasuredWidth() / 2;
		int py = getMeasuredHeight() / 2;
		int radius = Math.min(px, py) - textHeight;
		
		updateColors();
		drawBoard(px, py, radius, canvas);
		drawContact(px, py, radius, canvas);
	}
	
	/**
	 * Draws the maneuvering board on the view
	 * 
	 * This method is responsible for drawing the rings, degrees lines within
	 * the circle, and the degree values (in increments of 10) around the
	 * circle. Is also responsible for drawing the scale text in the lower
	 * right corner of the view.
	 * 
	 * @param px The center of the view in the X axis
	 * @param py The center of the view in the Y axis
	 * @param radius The calculated radius of the circle
	 * @param canvas The Canvas object to paint to
	 */
	private void drawBoard(int px, int py, int radius, Canvas canvas) {
		// Clear previous objects
		canvas.drawCircle(px, py, radius, circlePaint); 
		
		// Paint circle features
		canvas.drawCircle(px, py, radius, outerRangePaint);
		canvas.drawCircle(px, py, (int)(radius * 0.2), rangePaint);
		canvas.drawCircle(px, py, (int)(radius * 0.4), rangePaint);
		canvas.drawCircle(px, py, (int)(radius * 0.6), rangePaint);
		canvas.drawCircle(px, py, (int)(radius * 0.8), rangePaint);
		
		// Determine scale values and ring dimensions to display to user
		String scaleValue = this.getResources().getString(R.string.one_ring_string) + 
			" = " + 
			String.valueOf(scale) 
			+ "kyds";
		
		// Calculate text positions to bottom right corner
		float scaleTextWidth = scalePaint.measureText(scaleValue);
		int scaleTextX = (int)((px * 2) - scaleTextWidth - 3);
		int scaleTextY = (int)((py * 2) - 3);
		
		// Paint scale
		canvas.drawText(scaleValue, scaleTextX, scaleTextY - (textHeight * 0), scalePaint);
		
		canvas.save();
		
		for (int i = 0; i < 36; i++) {
			// Draw a marker
			if (i % 2 == 0) {
				canvas.drawLine(px, py-radius, px, py - 10, markerPaint);
			} else {
				canvas.drawLine(px, py-radius, px, py - radius + 10, markerPaint);
			}
			
			canvas.save();
			canvas.translate(0, textHeight);
			
			// Draw numbers in increments of 10 degrees
			if (showDegrees) {
				if (i % 2 == 0) {
					String angle = String.valueOf(i * 10);
					float angleTextWidth = degreesTextPaint.measureText(angle);
					
					int angleTextX = (int)(px - angleTextWidth / 2);
					int angleTextY = py - radius - textHeight - 2;
					canvas.drawText(angle, angleTextX, angleTextY, degreesTextPaint);
				}
			}
			canvas.restore();
			canvas.rotate(10, px, py);
		}
		canvas.restore();	
	}
	
	private void drawContact(int px, int py, int radius, Canvas canvas) {
		float range = 0, bearing = 0;
		double drm = 777;   // Direction of relative movement of contact
		
		// Speed of relative movement (in knots) of the target.
		// SRM is an abbreviation for speed of relative movement, and is
		// calculated the same way as the other speed variable in this
		// method (previousToCurrentTrackSpeed).
		double slope = 1;
		PointF originPoint = new PointF(0, 0);
		PointF firstPoint = new PointF(px, py);
		PointF currentPoint = new PointF(px, py);
		PointF previousPoint = new PointF(px, py);
		PointF extendedPoint = new PointF(px, py);
		Date currentTime = Calendar.getInstance().getTime();
		float previousBearing = 0;
		float previousRange = 0;
		
		// Speed of relative movement (in yds/msec) of the target.
		// Calculated as the distance between the previous and current 
		// positions of the target divided by the time span between the
		// observations of those two points.
		boolean firstObservation = true;
		
		// Plot all observation points and connecting lines on maneuvering board
		Iterator<Observation> observations = contact.getObservationList().iterator();
		while (observations.hasNext()) {
			Observation observation = observations.next();
			
			if (observation.getObservationType() == ObservationType.BearingAndDistance) {
				// Get bearing and range
				range = observation.getRange();
				bearing = observation.getBearing();
				
				// Store previous data
				previousPoint = currentPoint;
				previousBearing = bearing;
				previousRange = range;
				
				// Calculate point based on range and bearing
				currentPoint = MoboUtilities.getPointFromRangeAndBearing(range, bearing);
				currentTime = observation.getTime();
				
				// Paint point
				drawPoint(currentPoint, px, py, radius, canvas, testPaint);
				
				// Paint time (per preferences)
				if (showObservationTime) {
					String observationTime = String.format("%02d", currentTime.getHours());
					observationTime += String.format("%02d", currentTime.getMinutes());
					drawText(currentPoint, observationTime, px, py, radius, canvas, cpaTextPaint);
				}
				
				// If this is the first observation point, store it for later user.
				// Otherwise, draw a line from the previous point to the current point.
				if (firstObservation == true) {
					firstPoint = currentPoint;
					firstObservation = false;
				} else {
					if (useRecentObservations) {
						drawLine(previousPoint, currentPoint, px, py, radius, canvas, testPaint);
					}
				}
			}
		}
		
		// CPA calculations are more accurate when the distance
		// between points is greater. The most accurate calculation
		// will be achieved by using the first and last observation
		// (assuming target has not changed course or speed).
		if (!useRecentObservations) {
			previousPoint = firstPoint;
			drawLine(previousPoint, currentPoint, px, py, radius, canvas, testPaint);
		}
		
		// We need at least 2 observation points to calculate and draw CPA
		if (contact.getObservationList().size() < 2) return;
		
		drm = MoboUtilities.getAngleBetweenPoints(previousPoint, currentPoint);
		slope = MoboUtilities.getSlopeBetweenPoints(previousPoint, currentPoint);		
		
		// Calculated extended track and draw
		if (drm != 777) {
			extendedPoint = MoboUtilities.getPointFromRangeAndBearing(currentPoint, 
					(float)(scale * rangeAtLowestScale), (float)drm);
			drawLine(currentPoint, extendedPoint, px, py, radius, canvas, relativeTrackPaint);
		}
		
		if (showCPA) {
			if (contact.isClosing()) {
				double rangeToCPA = MoboUtilities.calculateShortestDistanceToLine(originPoint, currentPoint, extendedPoint);
				double rangeToCurrentTargetPoint = MoboUtilities.getDistanceBetweenPoints(originPoint, currentPoint);
				double targetRangeToCPA = 
					Math.sqrt((rangeToCurrentTargetPoint * rangeToCurrentTargetPoint) - (rangeToCPA * rangeToCPA)); 
				
				// cpa slope is the target track slope, inverted (1/x) and multiplied by -1
				// This is how we find a line perpendicular to the target track
				double cpaSlope = 1 / slope;
				cpaSlope *= (-1.0);
				
				// Once the slope is obtained, calculate the angle (bearing) by using the
				// atan function (resulting in a value in radians) and converting to degrees
				double radians = Math.atan(cpaSlope);
				double bearingToCPA = Math.toDegrees(radians); // radians * 180.0 / Math.PI;
				
				// Correct cpa bearing by adjusting for 90 degree compass shift (0 = north)
				PointF pointOfCPA = MoboUtilities.getPointFromRangeAndBearing(
						currentPoint, (float)targetRangeToCPA, (float)drm);
				bearingToCPA += (pointOfCPA.x <= 0) ? 90 : 270;
				bearingToCPA *= -1.0;
				bearingToCPA += 360.0;
				
				pointOfCPA = MoboUtilities.getPointFromRangeAndBearing(
						originPoint, (float)rangeToCPA, (float)bearingToCPA);
				
				// Paint CPA point and draw a line to it
				drawPoint(pointOfCPA, px, py, radius, canvas, cpaPaint);
				drawLine(originPoint, pointOfCPA, px, py, radius, canvas, cpaPaint);
			}
		}
	}
	
	private void drawText(PointF pointToPlot, String textToPlot, int px, int py, int radius, 
			Canvas canvas, Paint paint) {
		Point plotPoint = new Point();
		
		// Convert point into display coordinates
		plotPoint.x = (int)(radius * pointToPlot.x) / (rangeAtLowestScale * scale);
		plotPoint.y = (int)(radius * pointToPlot.y) / (rangeAtLowestScale * scale);
		
		// Draw text
		canvas.drawText(textToPlot, px + plotPoint.x + 9, py - plotPoint.y - 3, paint);
	}
	
	private void drawPoint(PointF pointToPlot, int px, int py, int radius, 
			Canvas canvas, Paint paint) {
		Point plotPoint = new Point();
		
		// Convert point into display coordinates
		plotPoint.x = (int)(radius * pointToPlot.x) / (rangeAtLowestScale * scale);
		plotPoint.y = (int)(radius * pointToPlot.y) / (rangeAtLowestScale * scale);
		
		// Draw point as a circle
		canvas.drawCircle(px + plotPoint.x, py - plotPoint.y, 6, paint);
	}
	
	private void drawLine(PointF fromPoint, PointF toPoint, int px, int py, 
			int radius, Canvas canvas, Paint paint) {
		Point endPoint1 = new Point();
		Point endPoint2 = new Point();
		
		// Convert points into local coordinates
		endPoint1.x = (int)(radius * fromPoint.x) / (rangeAtLowestScale * scale);
		endPoint1.y = (int)(radius * fromPoint.y) / (rangeAtLowestScale * scale);
		endPoint2.x = (int)(radius * toPoint.x) / (rangeAtLowestScale * scale);
		endPoint2.y = (int)(radius * toPoint.y) / (rangeAtLowestScale * scale);
		
		// Draw line
		canvas.drawLine(px + endPoint1.x, 
				py - endPoint1.y, 
				px + endPoint2.x, 
				py - endPoint2.y, 
				paint);
	}
}
