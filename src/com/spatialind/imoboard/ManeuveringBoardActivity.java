package com.spatialind.imoboard;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.spatialind.imoboard.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ZoomButtonsController.OnZoomListener;

public class ManeuveringBoardActivity extends Activity implements OnSharedPreferenceChangeListener {
	private float range;
	private float bearing;
	private float ownCourse;
	private float ownSpeed;
	private Contact contact;
	private int obs_hour;
	private int obs_minute;
	private int scale;
	private boolean useRecentData;
	private Cursor cursor;
	private String contactName;
	private ContactDatabaseAdapter db;
	private SharedPreferences preferences;
	private ManeuveringBoardView moboardView;
	private LinearLayout moboLinearLayout;
	private TextView moboTextView;
	private LayoutInflater inflater;
	private AlertDialog.Builder builder;
	private AlertDialog dialog;
	private TextView contactDrmLabelTextView;
	private TextView contactDrmTextView;
	private TextView contactSrmLabelTextView;
	private TextView contactSrmTextView;
	private TextView contactCourseLabelTextView;
	private TextView contactCourseTextView;
	private TextView contactSpeedLabelTextView;
	private TextView contactSpeedTextView;
	private TextView cpaBearingLabelTextView;
	private TextView cpaBearingTextView;
	private TextView cpaRangeLabelTextView;
	private TextView cpaRangeTextView;
	private TextView cpaTimeAtLabelTextView;
	private TextView cpaTimeAtTextView;
	private TextView cpaTimeToLabelTextView;
	private TextView cpaTimeToTextView;
	private TextView contactHeaderTextView;
	private TextView cpaHeaderTextView;
	private static final int rangeAtLowestScale = 5000;
	public static final String PREF_SHOW_OBS_TIME = "PREF_SHOW_OBS_TIME";
	public static final String PREF_ROUND_VALUES = "PREF_ROUND_VALUES";
	public static final String PREF_SHOW_DEGREES = "PREF_SHOW_DEGREES";
	public static final String PREF_SHOW_CPA = "PREF_SHOW_CPA";
	public static final String PREF_SCALE = "PREF_SCALE";
	public static final String PREF_USE_RECENT_DATA = "PREF_USE_RECENT_DATA";
	public final int ADD_OBS_DIALOG_ID = 0;
	public final int UPDATE_OWNSHIP_DIALOG_ID = 1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup progress bar before calling setContentView
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        
        setContentView(R.layout.moboard);
        
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
        	contactName = extras.getString("TARGET");
        } else {
        	contactName = "yyy";
        }
        
        // Get preferences and update maneuvering board
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(this);
        
        // Get moboard objects
        moboardView = (ManeuveringBoardView)this.findViewById(R.id.moboView);
        moboLinearLayout = (LinearLayout)this.findViewById(R.id.moboLinearLayout);
        moboTextView = (TextView)this.findViewById(R.id.moboTextView);
        
        // Get table textview objects
        contactDrmLabelTextView = (TextView)this.findViewById(R.id.contactDrmLabel);
        contactDrmTextView = (TextView)this.findViewById(R.id.contactDrm);
        contactSrmLabelTextView = (TextView)this.findViewById(R.id.contactSrmLabel);
        contactSrmTextView = (TextView)this.findViewById(R.id.contactSrm);
        contactCourseLabelTextView = (TextView)this.findViewById(R.id.contactCourseLabel);
        contactCourseTextView = (TextView)this.findViewById(R.id.contactCourse);
        contactSpeedLabelTextView = (TextView)this.findViewById(R.id.contactSpeedLabel);
        contactSpeedTextView = (TextView)this.findViewById(R.id.contactSpeed);
        cpaBearingLabelTextView = (TextView)this.findViewById(R.id.cpaBearingLabel);
        cpaBearingTextView = (TextView)this.findViewById(R.id.cpaBearing);
        cpaRangeLabelTextView = (TextView)this.findViewById(R.id.cpaRangeLabel);
        cpaRangeTextView = (TextView)this.findViewById(R.id.cpaRange);
        cpaTimeAtLabelTextView = (TextView)this.findViewById(R.id.cpaTimeAtLabel);
        cpaTimeAtTextView = (TextView)this.findViewById(R.id.cpaTimeAt);
        cpaTimeToLabelTextView = (TextView)this.findViewById(R.id.cpaTimeToLabel);
        cpaTimeToTextView = (TextView)this.findViewById(R.id.cpaTimeTo);
        
        // Get table header textview objects
        contactHeaderTextView = (TextView)this.findViewById(R.id.contactHeader1TextView);
        cpaHeaderTextView = (TextView)this.findViewById(R.id.contactHeader2TextView);
        
        // Set initial text at top of activity
        moboTextView.setText(this.getResources().getString(R.string.contact_label) + 
        		": " + contactName);

        // Setup zoom listener for moboard view
        moboardView.setOnZoomListener(new OnZoomListener() {

			public void onVisibilityChanged(boolean visible) {
				// Nothing special to do
			}

			public void onZoom(boolean zoomIn) {
				if (zoomIn) {
					if (scale == 1)return;
					else {
						// Decrease scale
						scale--;
						
						// Disable zoom-in button if we're maxed out
						if (scale == 1) moboardView.setZoomInEnabled(false);
						moboardView.setZoomOutEnabled(true);
					}
				}
				else {
					if (scale == 10) return;
					else {
						// Increase scale
						scale++;
						
						// Disable zoom-out button if we're maxed out
						if (scale == 1) moboardView.setZoomOutEnabled(false);
						moboardView.setZoomInEnabled(true);
					}
				}
				
				// Update moboard with new scale
			    moboardView.setScale(scale);
				moboardView.invalidate();
				
				// Edit preferences
				SharedPreferences.Editor editor = preferences.edit();
			    editor.putString(PREF_SCALE, String.valueOf(scale));
			    
			    // Commit the edit
			    editor.commit();
			}
        });
        
        // Configure moboard view from preferences
        updateMoboardFromPreferences();
        
        // Open database
	    db = new ContactDatabaseAdapter(this);
	    db.open();
        
	    contact = new Contact();
        fillData();
    }
    
    /** Called when the activity (and, because this is the main activity, the program)
     * is destroyed (no longer running).
     */
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    /** Called when the activity is paused.
     */
    @Override
    public void onPause() {
    	super.onPause();
    	
    	try {
    		// Close database.
        	db.close();
    	}
    	catch (SQLiteException sqlEx) {

		}
		catch (Exception ex) {

		}
    }
    
    /** Called when the activity is resumed.
     */
    @Override
    public void onResume() {
    	super.onResume();
    	
    	try {
    		// Configure moboard view from preferences
            updateMoboardFromPreferences();
            
    		// Reopen database.
        	db.open();
        	fillData();
    	}
    	catch (SQLiteException sqlEx) {

		}
		catch (Exception ex) {

		}
    }
    
    /**
     * Method called to set up a dialog based on the parameter. This method isn't
     * called directly, but is executed when the showDialog method is called. Depending
     * on the parameter, the method inflates a layout, sets up an AlertBuilder object
     * with an OK and Cancel button, adds an onClick handler for the buttons, and then
     * calls the object's create method.
     * 
     * @param id The ID of the dialog (either ADDRESS_DIALOG_ID or SEARCH_DIALOG_ID)
     */
    @Override
    public Dialog onCreateDialog(int id) {
    	switch(id) {
    	case ADD_OBS_DIALOG_ID:
    		inflater = 
    			(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		final View layout = inflater.inflate(R.layout.observationdialog,
    				(ViewGroup)findViewById(R.id.observationRoot));
    		final EditText bearingEditText = 
    			(EditText)layout.findViewById(R.id.bearingText);
    		final EditText rangeEditText = 
    			(EditText)layout.findViewById(R.id.rangeText);
    		final TimePicker timePicker = 
    			(TimePicker)layout.findViewById(R.id.observationTimePicker);

    		builder = new AlertDialog.Builder(this);
    		builder.setView(layout);
    		builder.setTitle(this.getResources().getString(R.string.add_obs_caption));

    		timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                	obs_hour = hourOfDay;
                	obs_minute = minute;
                	Log.d("setOnTimeChangedListener", String.valueOf(obs_hour));
                }
            });

    		// Setup for OK button
    		builder.setPositiveButton(android.R.string.ok, 
    				new DialogInterface.OnClickListener() {

    			public void onClick(DialogInterface dialog, int which) {
    				if (bearingEditText.getText().toString().length() < 1)
    				{
    					return;
    				}
    				
    				if (rangeEditText.getText().toString().length() < 1)
    				{
    					return;
    				}
    				
    				// Retrieve values from edit boxes
    				bearing = Float.valueOf(bearingEditText.getText().toString());
    				range = Float.valueOf(rangeEditText.getText().toString());
    				
    				// Validate values
    				if (bearing >= 360.0) bearing -= 360.0;
    				if (range >= 50000.0) return;
    				
    				addObservation();
    				ManeuveringBoardActivity.this.removeDialog(ADD_OBS_DIALOG_ID);
    			}
			});
    		
    		// Setup for Cancel button
    		builder.setNegativeButton(android.R.string.cancel, 
    				new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					bearing = 0;
					ManeuveringBoardActivity.this.removeDialog(ADD_OBS_DIALOG_ID);
				}
			});
    		
    		dialog = builder.create();
    		return dialog;
    	case UPDATE_OWNSHIP_DIALOG_ID:
    		inflater = 
    			(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		final View ownShipLayout = inflater.inflate(R.layout.ownshipdialog,
    				(ViewGroup)findViewById(R.id.ownShipRoot));
    		final EditText courseEditText = 
    			(EditText)ownShipLayout.findViewById(R.id.ownShipCourseText);
    		final EditText speedEditText = 
    			(EditText)ownShipLayout.findViewById(R.id.ownShipSpeedText);
    		
    		builder = new AlertDialog.Builder(this);
    		builder.setView(ownShipLayout);
    		builder.setTitle(this.getResources().getString(R.string.update_own_ship_caption));
    		courseEditText.setText(String.valueOf(ownCourse));
    		speedEditText.setText(String.valueOf(ownSpeed));

    		// Setup for OK button
    		builder.setPositiveButton(android.R.string.ok, 
    				new DialogInterface.OnClickListener() {

    			public void onClick(DialogInterface dialog, int which) {
    				if (courseEditText.getText().toString().length() < 1)
    				{
    					return;
    				}
    				
    				if (speedEditText.getText().toString().length() < 1)
    				{
    					return;
    				}
    				
    				// Retrieve values from edit boxes
    				ownCourse = Float.valueOf(courseEditText.getText().toString());
    				ownSpeed = Float.valueOf(speedEditText.getText().toString());
    				
    				// Validate values
    				if (ownCourse >= 360.0) ownCourse -= 360.0;
    				if (ownSpeed >= 50.0) return;
    				
    				// Update database
    				updateOwnShipCourseAndSpeed();
    				
    				// Update CPA calculations
    				updateCalculations();
    				
    				ManeuveringBoardActivity.this.removeDialog(UPDATE_OWNSHIP_DIALOG_ID);
    			}
			});
    		
    		// Setup for Cancel button
    		builder.setNegativeButton(android.R.string.cancel, 
    				new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					bearing = 0;
					ManeuveringBoardActivity.this.removeDialog(UPDATE_OWNSHIP_DIALOG_ID);
				}
			});
    		
    		dialog = builder.create();
    		return dialog;
    	}
    	return null;
    }
    
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, 
			String key) {
		updateMoboardFromPreferences();
	}
	
	/**
	 * Reads the maneuvering board preferences for satellite and street views
	 * and then updates the view.
	 */
	private void updateMoboardFromPreferences() {
    	String moboardScale = 
    		preferences.getString(PREF_SCALE, "3");
    	boolean showObservationTime = 
    		preferences.getBoolean(PREF_SHOW_OBS_TIME, true);
    	boolean showCompassValues = 
    		preferences.getBoolean(PREF_SHOW_DEGREES, true);
    	boolean showCPA = 
    		preferences.getBoolean(PREF_SHOW_CPA, true);
    	
    	String temp = 
    		preferences.getString(PREF_USE_RECENT_DATA, "1");
    	if (temp.equals("1")) {
    		useRecentData = false;
    	}
    	else {
    		useRecentData = true;
    	}
    	boolean roundValues = 
    		preferences.getBoolean(PREF_ROUND_VALUES, false);
    	
    	Resources r = this.getResources();
    	
		moboLinearLayout.setBackgroundColor(Color.BLACK);
		moboardView.setBackgroundColor(Color.BLACK);
		moboardView.setColorMode(0);
		
		moboTextView.setTextColor(r.getColor(R.color.lime));
		
		contactHeaderTextView.setTextColor(r.getColor(R.color.lime));
		cpaHeaderTextView.setTextColor(r.getColor(R.color.lime));
		
		contactDrmLabelTextView.setTextColor(r.getColor(R.color.lime));
		contactSrmLabelTextView.setTextColor(r.getColor(R.color.lime));
		contactCourseLabelTextView.setTextColor(r.getColor(R.color.lime));
		contactSpeedLabelTextView.setTextColor(r.getColor(R.color.lime));
		
		contactDrmTextView.setTextColor(r.getColor(R.color.lime));
		contactSrmTextView.setTextColor(r.getColor(R.color.lime));
		contactCourseTextView.setTextColor(r.getColor(R.color.lime));
		contactSpeedTextView.setTextColor(r.getColor(R.color.lime));
		
		cpaBearingLabelTextView.setTextColor(r.getColor(R.color.lime));
		cpaRangeLabelTextView.setTextColor(r.getColor(R.color.lime));
		cpaTimeAtLabelTextView.setTextColor(r.getColor(R.color.lime));
		cpaTimeToLabelTextView.setTextColor(r.getColor(R.color.lime));
		
		cpaBearingTextView.setTextColor(r.getColor(R.color.lime));
		cpaRangeTextView.setTextColor(r.getColor(R.color.lime));
		cpaTimeAtTextView.setTextColor(r.getColor(R.color.lime));
		cpaTimeToTextView.setTextColor(r.getColor(R.color.lime));
    	
    	// Added length check below because on first attempt at
    	// reading scale, the value "h:mm:ss" was being returned.
    	// No clue yet as to how it got set as such.
    	if (moboardScale.length() > 2)
    	{
    		scale = 3;
    		moboardView.setScale(3);
    	}
    	else
    	{
    		scale = Integer.valueOf(moboardScale);
    		moboardView.setScale(Integer.valueOf(moboardScale));
    	}
    	
    	// Enable zoom buttons based on current scale
    	if (scale == 1) {
    		moboardView.setZoomInEnabled(false);
    		moboardView.setZoomOutEnabled(true);
    	}
    	else if (scale == 10) {
    		moboardView.setZoomInEnabled(true);
    		moboardView.setZoomOutEnabled(false);
    	}
    	
    	// Configure moboard
    	moboardView.setShowObservationTime(showObservationTime);
    	moboardView.setShowDegrees(showCompassValues);
    	moboardView.setShowCPA(showCPA);
    	moboardView.setUseFirstAndLastObservations(useRecentData);
    	moboardView.setRoundValues(roundValues);
    	
    	// Update moboard
    	moboLinearLayout.invalidate();
		moboardView.invalidate();
    }
	
	/**
	 * Called when the options menu is created. Inflates the menu
	 * from the mainmenu XML resource.
	 */
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.moboardmenu, menu);
    	
    	return true;
    }
	
	/**
	 * Called when a menu item is selected.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
		switch (item.getItemId()) {
		case (R.id.addobservation):
			// Update values with current time (needed in the
			// event that the user doesn't edit the time picker)
			final Calendar c = Calendar.getInstance();
	        obs_hour = c.get(Calendar.HOUR_OF_DAY);
	        obs_minute = c.get(Calendar.MINUTE);

			showDialog(ADD_OBS_DIALOG_ID);
			return true;
		case (R.id.clearobservations):
			// Confirm clearing of all observations
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(this.getResources().getString(R.string.confirm_data_clear_caption));
			dialog.setMessage(this.getResources().getString(R.string.confirm_clear_obs_message));
			dialog.setPositiveButton(android.R.string.yes,
					  new OnClickListener() {
				  		public void onClick(DialogInterface dialog, int arg1) {
				  			clearObservations();
				  		}
				  	  }
			);
			dialog.setNegativeButton(android.R.string.no,
					  new OnClickListener() {
				  		public void onClick(DialogInterface dialog, int arg1) {
				  			// Do nothing here ... just close
				  		}
				  	  }
			);
			dialog.show();
			return true;
		case (R.id.updateownship):
			showDialog(UPDATE_OWNSHIP_DIALOG_ID);
			return true;
		case (R.id.preferences):
			intent = new Intent(this, UserPreferences.class);
			startActivityForResult(intent, 0);
			return true;
		case (R.id.info2):
			intent = new Intent(this, InfoActivity.class);
			startActivityForResult(intent, 0);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void addObservation() {
		String timeOfObservation = String.format("%02d", obs_hour);
		timeOfObservation += ":" + String.format("%02d", obs_minute);
		
		db.addObservation(contactName, bearing, range, timeOfObservation);
		fillData();
	}
	
	private void updateOwnShipCourseAndSpeed() {
		db.updateOwnShipCourseAndSpeed(contactName, ownCourse, ownSpeed);
	}
	
	private void clearObservations() {
		db.clearObservations(contactName);
		fillData();
	}
	
	/**
	 * \brief Iterates through a cursor of observation data for a contact and adds
	 * it to the contact's observation list property.
	 * 
	 * Note that the method <c>startManagingCursor</c> is not used and that the
	 * cursor is closed after it is no longer needed.
	 */
	private void fillData() {
		String hour, minute;
		
		try {
			cursor = db.getCursorOfObservations(contactName);
			//startManagingCursor(cursor);

			// Initialize contact object
			contact.setId(contactName);
			contact.getObservationList().clear();
			
			if (cursor.moveToFirst()) {
				do {
					// Get observation data from database
					bearing = Float.valueOf(
							cursor.getString(cursor.getColumnIndex(ContactDatabaseAdapter.KEY_BEARING)));
					range = Float.valueOf(
							cursor.getString(cursor.getColumnIndex(ContactDatabaseAdapter.KEY_RANGE)));
					String timeValue = cursor.getString(
							cursor.getColumnIndex(ContactDatabaseAdapter.KEY_TIME));
			        
					final Calendar c = Calendar.getInstance();
				    
				    // Initialize hour/minute
					hour = String.valueOf(c.get(Calendar.HOUR));
					minute = String.valueOf(c.get(Calendar.MINUTE));
					
					// Parse time string
					Scanner scanner = new Scanner(timeValue);
				    scanner.useDelimiter(":");
				    if ( scanner.hasNext() ){
				      hour = scanner.next();
				      minute = scanner.next();
				    }
				    
			        Date obs_date = new Date();
			        obs_date.setYear(c.get(Calendar.YEAR));
			        obs_date.setMonth(c.get(Calendar.MONTH));
			        obs_date.setDate(c.get(Calendar.DAY_OF_MONTH));
			        obs_date.setHours(Integer.valueOf(hour));
			        obs_date.setMinutes(Integer.valueOf(minute));
			        
			        // Create observation object
					Observation obs = new Observation();
					obs.setBearing(bearing);
					obs.setRange(range);
					obs.setTime(obs_date);
					obs.setObservationType(ObservationType.BearingAndDistance);
					
					// Add observation object to target
					contact.getObservationList().add(obs);
					
				} while (cursor.moveToNext());
			}
			
			// Cleanup (close cursor)
			cursor.close();
			
			// Get own ship course and speed from database
			ownCourse = db.getOwnShipCourse(contactName);
			ownSpeed = db.getOwnShipSpeed(contactName);
			
			// Update CPA calculations
			updateCalculations();
			
			// Give target and own ship course/speed data to moboard view
			moboardView.setContact(contact);
			moboardView.setOwnShipCourse(Float.valueOf(ownCourse));
			moboardView.setOwnShipSpeed(Float.valueOf(ownSpeed));
			
			moboardView.invalidate();
		}
		catch (SQLiteException sqlEx) {
			
		}
		catch (Exception ex) {
			
		}
	}
	
	private void updateCalculations() {
		float range, bearing;
		boolean onCollisionCourse = false;
		double drm = 777;   // Direction of relative movement of contact
		double contactSpeed = 0;
		double contactCourse = 0;
		
		// Speed of relative movement (in knots) of the target.
		// SRM is an abbreviation for speed of relative movement, and is
		// calculated the same way as the other speed variable in this
		// method (previousToCurrentTrackSpeed).
		double srm = 0;
		double slope = 1;
		PointF originPoint = new PointF(0, 0);
		PointF firstPoint = new PointF(0, 0);
		PointF currentPoint = new PointF(0, 0);
		PointF previousPoint = new PointF(0, 0);
		PointF extendedPoint = new PointF(0, 0);
		Date currentTime = Calendar.getInstance().getTime();
		Date previousTime = currentTime;
		Date firstTime = currentTime;
		long previousToCurrentTrackSpan;
		double previousToCurrentTrackDistance;
		float previousBearing = 0;
		
		
		// Speed of relative movement (in yds/msec) of the target.
		// Calculated as the distance between the previous and current 
		// positions of the target divided by the time span between the
		// observations of those two points.
		double previousToCurrentTrackSpeed;
		boolean firstObservation = true;
		
		// Plot all observation points and connecting lines on maneuvering board
		Iterator<Observation> observations = contact.getObservationList().iterator();
		while (observations.hasNext()) {
			Observation observation = observations.next();
			
			if (observation.getObservationType() == ObservationType.BearingAndDistance) {
				// Get bearing and range
				range = observation.getRange();
				bearing = observation.getBearing();
				
				// Determine if we're on a collision course
				if (firstObservation == false){
					if (bearing == previousBearing) {
						onCollisionCourse = true;
					}
					else {
						onCollisionCourse = false;
					}
				}
					
				previousBearing = bearing;
				
				// Store previous point and time
				previousPoint = currentPoint;
				previousTime = currentTime;
				
				// Calculate point based on range and bearing
				currentPoint = MoboUtilities.getPointFromRangeAndBearing(range, bearing);
				currentTime = observation.getTime();
				
				// If this is the first observation point, store it for later use.
				// Otherwise, draw a line from the previous point to the current point.
				if (firstObservation == true) {
					firstPoint = currentPoint;
					firstTime = currentTime;
					firstObservation = false;
				} 
			}
		}
		
		// CPA calculations are more accurate when the distance
		// between points is greater. The most accurate calculation
		// will be achieved by using the first and last observation
		// (assuming target has not changed course or speed).
		if (!useRecentData) {
			previousPoint = firstPoint;
			previousTime = firstTime;
		}
		
		// If fewer than 2 observation points exist, reset
		// the calculated fields and return.
		if (contact.getObservationList().size() < 2) {
			contactDrmTextView.setText(" ");
			contactSrmTextView.setText(" ");
			contactCourseTextView.setText(" ");
			contactSpeedTextView.setText(" ");
			
			cpaBearingTextView.setText(" ");
			cpaRangeTextView.setText(" ");
			cpaTimeAtTextView.setText(" ");
			cpaTimeToTextView.setText(" ");
			
			return;
		}
		
		drm = MoboUtilities.getAngleBetweenPoints(previousPoint, currentPoint);
		slope = MoboUtilities.getSlopeBetweenPoints(previousPoint, currentPoint);		
		
		// Calculated extended track and draw
		if (drm != 777) {
			extendedPoint = MoboUtilities.getPointFromRangeAndBearing(currentPoint, 
					(float)(scale * rangeAtLowestScale), (float)drm);
		}		
		
		if (contact.isClosing()) {			
			previousToCurrentTrackDistance = MoboUtilities.getDistanceBetweenPoints(previousPoint, currentPoint);
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
			
			// If bearings between observations are the same, this
			// we're on a collision course ... use that bearing (sometimes
			// odd numbers will be calculated for CPA bearing when range is 0).
			if (onCollisionCourse) {
				bearingToCPA = previousBearing;
			}
			
			// Calculate time span between last two observations
			previousToCurrentTrackSpan = TimeUnit.MILLISECONDS.toMillis(
					currentTime.getTime() - previousTime.getTime());
			
			//Calculate target speed (s=d/t) in yds/msec
			previousToCurrentTrackSpeed = 
				previousToCurrentTrackDistance / previousToCurrentTrackSpan;
			
			// Calculate target time (in msec) to CPA 
			long timeToCPA = (long)(targetRangeToCPA / previousToCurrentTrackSpeed);
			
			// Convert milliseconds to minutes and seconds. Assume we haven't passed
			// an hour since observations (although this will still work if it did).
			int minutes = (int)(timeToCPA / 60000); // Minutes
			int seconds = (int)(timeToCPA % 60000) / 1000; // Seconds
			double minutesToCPA = (double)timeToCPA / 60000.0;
			
			// Add target time of travel to observation time to get an HH:MM:SS ETA of CPA
			Calendar cpaCalendar = Calendar.getInstance();
			cpaCalendar.setTime(currentTime);
			cpaCalendar.add(Calendar.MINUTE, minutes);
			cpaCalendar.add(Calendar.SECOND, seconds);
			String cpaTimeString = String.valueOf(cpaCalendar.getTime().getHours());
			cpaTimeString += ":";
			cpaTimeString += String.format("%02d", cpaCalendar.getTime().getMinutes());
			cpaTimeString += ":";
			cpaTimeString += String.format("%02d", cpaCalendar.getTime().getSeconds());
			
			// Calculate speed of relative movement in knots (nautical miles / hour)
			srm = (previousToCurrentTrackDistance / 2025.3718285214) / 
				((double)previousToCurrentTrackSpan / 3600000.0);
			
			// Calculate true contact speed and course
			contactSpeed = MoboUtilities.calculateContactSpeed(drm, srm, Double.valueOf(ownCourse), Double.valueOf(ownSpeed));
			contactCourse = MoboUtilities.calculateContactCourse(drm, srm, contactSpeed, Double.valueOf(ownCourse), Double.valueOf(ownSpeed));
			
			db.updateContactCourseAndSpeed(contactName, (float)contactCourse, (float)contactSpeed);
			
			// Only update values when two or more observations have been made.
			// Necessary because calculations will not be correct with one.
			if (!firstObservation) {
				if (preferences.getBoolean(PREF_ROUND_VALUES, false)) {
					contactDrmTextView.setText(String.valueOf(Math.round(drm)));
					contactSrmTextView.setText(String.valueOf(Math.round(srm)));
					contactCourseTextView.setText(String.valueOf(Math.round(contactCourse)));
					contactSpeedTextView.setText(String.valueOf(Math.round(contactSpeed)));
					
					cpaBearingTextView.setText(String.valueOf(Math.round(bearingToCPA)));
					cpaRangeTextView.setText(String.valueOf(Math.round(rangeToCPA)));
					cpaTimeAtTextView.setText(cpaTimeString);
					cpaTimeToTextView.setText(String.valueOf(Math.round(minutesToCPA)));
				}
				else {
					contactDrmTextView.setText(String.format("%1$.2f", drm));
					contactSrmTextView.setText(String.format("%1$.2f", srm));
					contactCourseTextView.setText(String.format("%1$.2f", contactCourse));
					contactSpeedTextView.setText(String.format("%1$.2f", contactSpeed));
					
					cpaBearingTextView.setText(String.format("%1$.2f", bearingToCPA));
					cpaRangeTextView.setText(String.format("%1$.2f", rangeToCPA));
					cpaTimeAtTextView.setText(cpaTimeString);
					cpaTimeToTextView.setText(String.valueOf(Math.round(minutesToCPA)));
				}
			}
		}
	}
	
	
}
