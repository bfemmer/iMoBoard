package com.spatialind.imoboard;

import java.util.Calendar;

import com.spatialind.imoboard.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

public class ObservationListActivity extends ListActivity {
	private String bearingText;
	private String rangeText;
	private float bearing;
	private float range;
	private int obs_hour;
	private int obs_minute;
	private long selectedIndex;
	private Cursor cursor;
	private String targetName;
	private ContactDatabaseAdapter db;
	private TextView targetTextView;
	
	private LayoutInflater inflater;
	private View layout; // was final
	private EditText bearingEditText; // was final
	private EditText rangeEditText; // was final
	private TimePicker timePicker; // was final
	private AlertDialog.Builder builder;
	private AlertDialog dialog;
	
	public final int ADD_OBS_DIALOG_ID = 0;
	public final int EDIT_OBS_DIALOG_ID = 2;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.observationlist);

	    Bundle extras = getIntent().getExtras();
        if(extras !=null) {
        	targetName = extras.getString("TARGET");
        } else {
        	targetName = "yyy";
        }
        
	    // Open database and set list adapter to cursor
	    db = new ContactDatabaseAdapter(this);
	    db.open();
	    fillData();
	    
	    // Initialize caption at top of activity with contact name
	    targetTextView = (TextView)this.findViewById(R.id.targetTextView03);
	    targetTextView.setText(this.getResources().getString(R.string.contact_label) + 
        		": " + targetName);
	    
	    // Register all listview items for context menu
	    registerForContextMenu(getListView());
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
    		layout = inflater.inflate(R.layout.observationdialog,
    				(ViewGroup)findViewById(R.id.observationRoot));
    		bearingEditText = 
    			(EditText)layout.findViewById(R.id.bearingText);
    		rangeEditText = 
    			(EditText)layout.findViewById(R.id.rangeText);
    		timePicker = 
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
    				bearing = Float.valueOf(bearingEditText.getText().toString());
    				range = Float.valueOf(rangeEditText.getText().toString());
    				
    				addObservation();
    				ObservationListActivity.this.removeDialog(ADD_OBS_DIALOG_ID);
    			}
			});
    		
    		// Setup for Cancel button
    		builder.setNegativeButton(android.R.string.cancel, 
    				new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					bearing = 0;
					ObservationListActivity.this.removeDialog(ADD_OBS_DIALOG_ID);
				}
			});
    		
    		dialog = builder.create();
    		return dialog;
    	case EDIT_OBS_DIALOG_ID:
    		inflater = 
    			(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		layout = inflater.inflate(R.layout.observationdialog,
    				(ViewGroup)findViewById(R.id.observationRoot));
    		bearingEditText = 
    			(EditText)layout.findViewById(R.id.bearingText);
    		rangeEditText = 
    			(EditText)layout.findViewById(R.id.rangeText);
    		timePicker = 
    			(TimePicker)layout.findViewById(R.id.observationTimePicker);

    		builder = new AlertDialog.Builder(this);
    		builder.setView(layout);
    		builder.setTitle(this.getResources().getString(R.string.edit_obs_caption));

    		// Set bearing/range
    		bearingEditText.setText(bearingText);
    		rangeEditText.setText(rangeText);
    		
    		// Set observation time
    		timePicker.setCurrentHour(obs_hour);
    		timePicker.setCurrentMinute(obs_minute);
  
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
    				
    				editObservation();
    				ObservationListActivity.this.removeDialog(EDIT_OBS_DIALOG_ID);
    			}
			});
    		
    		// Setup for Cancel button
    		builder.setNegativeButton(android.R.string.cancel, 
    				new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					bearing = 0;
					ObservationListActivity.this.removeDialog(EDIT_OBS_DIALOG_ID);
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
    	inflater.inflate(R.menu.observationmenu, menu);
    	
    	return true;
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);
	  MenuInflater inflater = getMenuInflater();
	  inflater.inflate(R.menu.obscontextmenu, menu);
	}
    
	/**
	 * Called when a menu item is selected.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
		switch (item.getItemId()) {
			case (R.id.addobservation):
				showAddObservationDialog();
				return true;
			case (R.id.clearobservations):
				showClearObservationsDialog();
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
	
	public boolean onContextItemSelected(MenuItem item) {
		String temp;
		Object o;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  
		switch (item.getItemId()) {
		case R.id.obscontextedit:
			cursor.moveToPosition(info.position);
			o = cursor.getString(cursor.getColumnIndex(ContactDatabaseAdapter.KEY_BEARING));
			bearingText = o.toString();
			o = cursor.getString(cursor.getColumnIndex(ContactDatabaseAdapter.KEY_RANGE));
			rangeText = o.toString();
			o = cursor.getString(cursor.getColumnIndex(ContactDatabaseAdapter.KEY_TIME));
			temp = o.toString();
			obs_hour = Integer.valueOf(temp.substring(0, 2));
			obs_minute = Integer.valueOf(temp.substring(3));
			selectedIndex = info.id;
			
			showDialog(EDIT_OBS_DIALOG_ID);
			return true;
		case R.id.obscontextdelete:
			showDeleteObservationDialog(info.id);
			return true;
		case R.id.obscontextclear:
			showClearObservationsDialog();
			return true;
		default:
	    return super.onContextItemSelected(item);
	  }
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
	}
	
	private boolean showClearObservationsDialog() {
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
	}
	
	@Override
    public void onDestroy() {
    	super.onDestroy();
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
    
    private void showDeleteObservationDialog(long _rowId) {
    	final long id = _rowId;
    	
		// Confirm clearing of contact and it's observation data
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(this.getResources().getString(R.string.confirm_obs_delete_caption));
		dialog.setMessage(this.getResources().getString(R.string.confirm_obs_delete_message));
		dialog.setPositiveButton(android.R.string.yes,
				  new OnClickListener() {
			  		public void onClick(DialogInterface dialog, int arg1) {
			  			deleteObservation(id);
			  			fillData();
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
	}
	
    private void showAddObservationDialog() {
    	// Update values with current time (needed in the
		// event that the user doesn't edit the time picker)
		final Calendar c = Calendar.getInstance();
        obs_hour = c.get(Calendar.HOUR_OF_DAY);
        obs_minute = c.get(Calendar.MINUTE);

		showDialog(ADD_OBS_DIALOG_ID);
    }
    
	private void addObservation() {
		String timeOfObservation = String.format("%02d", obs_hour);
		timeOfObservation += ":" + String.format("%02d", obs_minute);
		
		db.addObservation(targetName, bearing, range, timeOfObservation);
		fillData();
	}
	
	private void editObservation() {
		String timeOfObservation = String.format("%02d", obs_hour);
		timeOfObservation += ":" + String.format("%02d", obs_minute);
		
		db.updateObservation(selectedIndex, bearing, range, timeOfObservation);
		fillData();
	}
	
	private void deleteObservation(long _id) {
		db.deleteObservation(_id);
		fillData();
	}
	
	private void clearObservations() {
		db.clearObservations(targetName);
		fillData();
	}
	
	/**
	 * \brief Creates a cursor adapter of observation data and attaches
	 * it to the list adapter.
	 * 
	 * Note that the method <c>startManagingCursor</c> is used and that the
	 * cursor is not closed.
	 */
	private void fillData() {
		try {
			cursor = db.getCursorOfObservations(targetName);
			
			String[] from = new String[] { ContactDatabaseAdapter.KEY_TIME,
					ContactDatabaseAdapter.KEY_BEARING,
					ContactDatabaseAdapter.KEY_RANGE};
			int[] to = new int[] { R.id.obsTimeTextView, 
					R.id.obsBearingTextView, 
					R.id.obsRangeTextView };

			// Now create an array adapter and set it to display using our row
			SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
					R.layout.observationrow, cursor, from, to);
			setListAdapter(cursorAdapter);
		}
		catch (SQLiteException sqlEx) {
			
		}
		catch (Exception ex) {
			
		}
	}
}
