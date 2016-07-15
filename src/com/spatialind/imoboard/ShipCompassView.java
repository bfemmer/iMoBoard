package com.spatialind.imoboard;

import com.spatialind.imoboard.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class ShipCompassView extends View {
	private boolean bearingMode;
	private float bearing;
	private float course;
	private int textHeight;
	private int identifier;
	private Paint markerPaint;
	private Paint textPaint;
	private Paint circlePaint;
	private Paint testPaint;
	private String northString;
	private String eastString;
	private String southString;
	private String westString;
	private int screenCenterPixelX;
	private GestureDetector gestures;
	OnShipCompassListener onShipCompassListener = null;
	
	public void setBearingMode(boolean bearingMode) {
		this.bearingMode = bearingMode;
	}

	public boolean isBearingMode() {
		return bearingMode;
	}

	public void setBearing(float _bearing) {
		bearing = _bearing;
		invalidate();
	}
	
	public float getBearing() {
		return bearing;
	}
	
	public void setCourse(float course) {
		this.course = course;
		invalidate();
	}

	public float getCourse() {
		return course;
	}
	
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the identifier
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	public ShipCompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		gestures = new GestureDetector(context, new GestureListener());
		initMoBoView();
	}

	public ShipCompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestures = new GestureDetector(context, new GestureListener());
		initMoBoView();
	}

	public ShipCompassView(Context context) {
		super(context);
		bearingMode = false;
		gestures = new GestureDetector(context, new GestureListener());
		initMoBoView();
	}

	@Override  
	public boolean onTouchEvent(MotionEvent event) {  
	    gestures.onTouchEvent(event);
	    
	    return true;
	}
	
	@Override
	protected void onMeasure(int wMeasureSpec, int hMeasureSpec) {
		//int measuredHeight = measureHeight(hMeasureSpec);
		int measuredWidth = measureWidth(wMeasureSpec);
//		int d = Math.min(measuredWidth, measuredHeight);
//		d /= 2;
		
		int d = measuredWidth / 2;
		
		// This is necessary for the scroll event
		screenCenterPixelX = d;
		setMeasuredDimension(d, d);
	}
	
	private int measure(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 200
			result = 200;
		} else {
			result = specSize;
		}
		return result;
	}
	
	private int measureHeight(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		return specSize;
	}
	
	private int measureWidth(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		return specSize;
	}
	
	protected void initMoBoView() {
		setFocusable(true);
		Resources r = this.getResources();
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		float dp, fpixels;
		int pixels;
		
		identifier = 0;
		
		northString = r.getString(R.string.cardinal_north);
		eastString = r.getString(R.string.cardinal_east);
		southString = r.getString(R.string.cardinal_south);
		westString = r.getString(R.string.cardinal_west);
		
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//circlePaint.setColor(r.getColor(R.color.background_color));
		circlePaint.setStrokeWidth(1);
		circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(r.getColor(R.color.text_color));
		dp = 14f;
		fpixels = metrics.density * dp;
		pixels = (int) (metrics.density * dp + 0.5f);
		textPaint.setTextSize(pixels);
		
		testPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		testPaint.setColor(r.getColor(R.color.translucent_red));
		
		textHeight = (int)textPaint.measureText("yY");
		
		markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		markerPaint.setColor(r.getColor(R.color.lime));
		
		screenCenterPixelX = 320;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int tempX = getMeasuredWidth();
		int tempY = getMeasuredHeight();
		int minDimension = Math.min(tempX, tempY);
		int px = minDimension / 2;
		int py = minDimension / 2;
		int radius = Math.min(px, py);
		
		DrawShip(px, py, radius, canvas);
	}
	
	private void DrawShip(int px, int py, int radius, Canvas canvas) {
		// Draw filled circle
		//canvas.drawCircle(px, py, radius, circlePaint);
		canvas.save();
		canvas.rotate(-bearing, px, py);
		
		int textWidth = (int)textPaint.measureText("W");
		int cardinalX = px - textWidth / 2;
		int cardinalY = py - radius + textHeight;
		
		for (int i = 0; i < 24; i++) {
			// Draw a marker
			canvas.drawLine(px, py-radius, px, py - radius + 5, markerPaint);
			
			canvas.save();
			canvas.translate(0, textHeight);
			
			// Draw cardinal points
			if (i % 6 == 0) {
				String dirString = "";
				switch (i) {
				case (0): dirString = northString; break;
				case (6): dirString = eastString; break;
				case (12): dirString = southString; break;
				case (18): dirString = westString; break;
				}
				
				canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
			}
			else if (i % 3 == 0) {
				// Draw text every 45 degrees
				String angle = String.valueOf(i * 15);
				float angleTextWidth = textPaint.measureText(angle);
				
				int angleTextX = (int)(px - angleTextWidth / 2);
				int angleTextY = py - radius + textHeight;
				canvas.drawText(angle, angleTextX, angleTextY, textPaint);
			}
			
			canvas.restore();
			canvas.rotate(15, px, py);
		}
		
		// Draw ship
		canvas.rotate(course, px, py);
		int arrowY = py - radius + (2 * textHeight) + 10;
		int bowLeft = px - (int)(radius / 4.5);
		int bowRight = px + (int)(radius / 4.5);
		int shipLength = (radius * 2) - (2 * textHeight + 20);
		int bowLength = arrowY + (shipLength / 4);
		
		
		// Draw bow
		canvas.drawLine(px, arrowY, bowLeft, bowLength, markerPaint);
		canvas.drawLine(px, arrowY, bowRight, bowLength, markerPaint);
		
		// Draw sides
		canvas.drawLine(bowLeft, bowLength, bowLeft, shipLength, markerPaint);
		canvas.drawLine(bowRight, bowLength, bowRight, shipLength, markerPaint);
		
		// Draw stern
		canvas.drawLine(bowLeft, shipLength, bowRight, shipLength, markerPaint);
		
		canvas.restore();	
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
	    @Override
	    public boolean onDown(MotionEvent ev) {
	        //Log.d("GestureListener", "onDown(): Triggered.");
	        return true;
	    }
	    
	    @Override
	    public void onLongPress(MotionEvent e)
	    {
	    	//Log.d("GestureListener", "onLongPress(): Triggered.");
	    }
	    
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	    {
	    	//Log.d("GestureListener", "onFling(): Triggered.");
	    	//bearing = 180;
	    	//invalidate();
	        return true;
	    }
	    
	    @Override
	    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
	      float distanceY) {
		     //Log.d("---onScroll---",e1.toString()+e2.toString());
	    	
	    	if (bearingMode) {
	    		if (e1.getRawX() <= screenCenterPixelX) bearing -= distanceY;
		    	else bearing += distanceY;
		    	
		    	if (bearing >= 360) bearing -= 360;
		    	if (bearing < 0) bearing += 360;
		    	
		    	invalidate();
		    	OnBearingChanged((int) bearing);
			     return false;
	    	}
	    	else {
	    		if (e1.getRawX() <= screenCenterPixelX) course += distanceY;
		    	else course -= distanceY;
		    	
		    	if (course >= 360) course -= 360;
		    	if (course < 0) course += 360;
		    	
		    	invalidate();
		    	OnCourseChanged((int) course);
			     return false;
	    	}
	    }
	}

	/**
	 * Allows other objects to listen for these events
	 * 
	 * @param listener
	 */
	public void  setOnShipCompassListener(
			OnShipCompassListener listener) {
		onShipCompassListener = listener;
	}
	
	private void  OnCourseChanged(int course) {
		// Check if the Listener was set, 
		// otherwise an Exception occurs when we try to call it
		if(onShipCompassListener!=null) {
			onShipCompassListener.onCourseChanged(identifier, course);
		}
	}
	
	private void  OnBearingChanged(int bearing) {
		// Check if the Listener was set, 
		// otherwise an Exception occurs when we try to call it
		if(onShipCompassListener!=null) {
			onShipCompassListener.onBearingChanged(identifier, bearing);
		}
	}
}
