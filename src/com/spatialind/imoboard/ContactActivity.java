package com.spatialind.imoboard;

import java.util.Calendar;

import com.spatialind.imoboard.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class ContactActivity extends Activity {
	private Cursor cursor;
	private String contactName;
	private float bearing;
	private float range;
	private float course;
	private float speed;
	private float ownCourse;
	private float ownSpeed;
	private int obs_hour;
	private int obs_minute;
	private ContactDatabaseAdapter db;
	private TextView targetTextView;
	private TextView targetRangeTextView;
	private TextView targetBearingTextView;
	private TextView ownShipCourseTextView;
	private TextView targetCourseTextView;
	private TextView ownShipSpeedTextView;
	private TextView targetSpeedTextView;
	private ShipCompassView targetView;
	private ShipCompassView ownShipView;
	
	public final int ADD_OBS_DIALOG_ID = 0;
	public final int UPDATE_OWNSHIP_DIALOG_ID = 1;
	private final int TGT_SHIP_COMPASS = 2;
	private final int OWN_SHIP_COMPASS = 3;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup progress bar before calling setContentView
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        
        setContentView(R.layout.contact);
        
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
        	contactName = extras.getString("TARGET");
        } else {
        	contactName = "yyy";
        }
        
        // Open database
	    db = new ContactDatabaseAdapter(this);
	    db.open();
	    
        // Get objects from UI
        targetView = (ShipCompassView)this.findViewById(R.id.targetView);
        ownShipView = (ShipCompassView)this.findViewById(R.id.ownShipView);
        targetTextView = (TextView)this.findViewById(R.id.targetTextView);
        ownShipCourseTextView = (TextView)this.findViewById(R.id.losOwnCourse);
        targetCourseTextView = (TextView)this.findViewById(R.id.targetCourse);
        targetRangeTextView = (TextView)this.findViewById(R.id.targetRange);
        targetBearingTextView = (TextView)this.findViewById(R.id.targetBearing);
        ownShipSpeedTextView = (TextView)this.findViewById(R.id.losOwnSpeed);
        targetSpeedTextView = (TextView)this.findViewById(R.id.targetSpeed);
        
        targetTextView.setText(this.getResources().getString(R.string.contact_label) + 
        		": " + contactName);
        
        // Multiple compass views can fire the course change event, 
        // so each view must be tagged with an identifier to
        // differentiate them.
        targetView.setIdentifier(TGT_SHIP_COMPASS);
        ownShipView.setIdentifier(OWN_SHIP_COMPASS);
        
        // Set bearing mode for own ship
        ownShipView.setBearingMode(true);
        
        // Setup listening for course changes. 
        targetView.setOnShipCompassListener(new OnShipCompassListener() {
        	public void onCourseChanged(int identifier, float course) {
        		if (identifier == TGT_SHIP_COMPASS) {
        			targetCourseTextView.setText(String.format("%03.2f", course));
        		}
        	}
        	
        	public void onBearingChanged(int identifier, float bearing) {
        		// Do nothing
        	}
        });
        
        ownShipView.setOnShipCompassListener(new OnShipCompassListener() {
        	public void onCourseChanged(int identifier, float course) {
        		if (identifier == OWN_SHIP_COMPASS) {
        			ownShipCourseTextView.setText(String.format("%03.2f", course));
        		}
        	}
        	
        	public void onBearingChanged(int identifier, float bearing) {
        		if (identifier == OWN_SHIP_COMPASS) {
        			targetBearingTextView.setText(String.format("%03.2f", bearing));
        			targetView.setBearing(bearing);
        		}
        	}
        });
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
    		LayoutInflater inflater = 
    			(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		final View layout = inflater.inflate(R.layout.observationdialog,
    				(ViewGroup)findViewById(R.id.observationRoot));
    		final EditText bearingEditText = 
    			(EditText)layout.findViewById(R.id.bearingText);
    		final EditText rangeEditText = 
    			(EditText)layout.findViewById(R.id.rangeText);
    		final TimePicker timePicker = 
    			(TimePicker)layout.findViewById(R.id.observationTimePicker);

    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    				bearing = Float.valueOf(bearingEditText.getText().toString());
    				range = Float.valueOf(rangeEditText.getText().toString());
    				
    				addObservation();
    				ContactActivity.this.removeDialog(ADD_OBS_DIALOG_ID);
    			}
			});
    		
    		// Setup for Cancel button
    		builder.setNegativeButton(android.R.string.cancel, 
    				new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					bearing = 0;
					ContactActivity.this.removeDialog(ADD_OBS_DIALOG_ID);
				}
			});
    		
    		AlertDialog dialog = builder.create();
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
    				
    				if (courseEditText.getText().toString().length() < 1) return;
    				if (speedEditText.getText().toString().length() < 1) return;
    				
    				ownCourse = Float.valueOf(courseEditText.getText().toString());
    				ownSpeed = Float.valueOf(speedEditText.getText().toString());
    				
    				// Update database
    				updateOwnShipCourseAndSpeed();
    				
    				// Update text views
    				ownShipCourseTextView.setText(String.valueOf(ownCourse));
    				ownShipSpeedTextView.setText(String.valueOf(ownSpeed));
    	    		
    				// Update ship view
    				ownShipView.setCourse(Float.valueOf(ownCourse));
    				
    				ContactActivity.this.removeDialog(UPDATE_OWNSHIP_DIALOG_ID);
    			}
			});
    		
    		// Setup for Cancel button
    		builder.setNegativeButton(android.R.string.cancel, 
    				new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					bearing = 0;
					ContactActivity.this.removeDialog(UPDATE_OWNSHIP_DIALOG_ID);
				}
			});
    		
    		dialog = builder.create();
    		return dialog;
    	}
    	return null;
    }
    
    /**
	 * Called when the options menu is created. Inflates the menu
	 * from the targetmenu XML resource.
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
					  			// Do nothing here ... just close
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
	
	@Override
    public void onDestroy() {
    	super.onDestroy();
    	// Close database. Note that it's commented out to
    	// prevent force close when the Refresh menu was clicked.
    	//dbAdapter.close();
    }
	
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
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	try {
    		// Reopen database.
        	db.open();
        	fillData();
    	}
    	catch (SQLiteException sqlEx) {

		}
		catch (Exception ex) {

		}
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
	
	/**
	 * \brief Retrieves the last record from a cursor of observation data
	 * for a contact and sets the activity's UI elements with that data.
	 * 
	 * Note that the method <c>startManagingCursor</c> is not used and that the
	 * cursor is closed after it is no longer needed.
	 */
	private void fillData() {
		try {
			cursor = db.getCursorOfObservations(contactName);
			
			if (cursor.moveToLast()) {
				bearing = Float.valueOf(cursor.getString(cursor.getColumnIndex(ContactDatabaseAdapter.KEY_BEARING)));
				range = Float.valueOf(cursor.getString(cursor.getColumnIndex(ContactDatabaseAdapter.KEY_RANGE)));
				
		        targetView.setBearing(bearing);
		        ownShipView.setBearing(bearing);
		        targetBearingTextView.setText(String.format("%1$.1f", bearing));
		        targetRangeTextView.setText(String.format("%1$.1f", range)); //"%05d"
			} else {
				targetTextView.setText(this.getResources().getString(R.string.contact_label) + 
		        		": " + contactName);
		        targetView.setBearing(0);
		        ownShipView.setBearing(0);
		        targetView.setCourse(0);
		        ownShipView.setCourse(0);
		        targetBearingTextView.setText("000.0");
		        targetRangeTextView.setText("00000.0");
		        ownShipSpeedTextView.setText("00");
		        targetSpeedTextView.setText("00");
			}
			
			// Cleanup (close cursor)
			cursor.close();
			
			// Get contact and own ship course/speed from database
			ownCourse = Float.valueOf(db.getOwnShipCourse(contactName));
			ownSpeed = Float.valueOf(db.getOwnShipSpeed(contactName));
			course = Float.valueOf(db.getContactCourse(contactName));
			speed = Float.valueOf(db.getContactSpeed(contactName));
			
			ownShipView.setCourse(Float.valueOf(ownCourse));
			targetView.setCourse(Float.valueOf(course));
			
			ownShipCourseTextView.setText(String.format("%1$.2f", ownCourse));
			ownShipSpeedTextView.setText(String.format("%1$.2f", ownSpeed));
			targetCourseTextView.setText(String.format("%1$.2f", course));
			targetSpeedTextView.setText(String.format("%1$.2f", speed));
		}
		catch (SQLiteException sqlEx) {
			
		}
		catch (Exception ex) {
			
		}
	}
}
