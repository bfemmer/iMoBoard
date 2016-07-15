package com.spatialind.imoboard;

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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ContactListActivity extends ListActivity{
	private Cursor cursor;
	private String contactID;
	private ContactDatabaseAdapter db;
	public final int ADD_CONTACT_DIALOG_ID = 0;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.contactlist);
	    
	    // Open database
	    db = new ContactDatabaseAdapter(this);
	    db.open();
	    
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
    	case ADD_CONTACT_DIALOG_ID:
    		LayoutInflater inflater = 
    			(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		final View layout = inflater.inflate(R.layout.addcontactdialog,
    				(ViewGroup)findViewById(R.id.addTargetRoot));
    		final EditText targetEditText = 
    			(EditText)layout.findViewById(R.id.nameText);

    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setView(layout);
    		builder.setTitle("Add Contact");

    		// Setup for OK button
    		builder.setPositiveButton(android.R.string.ok, 
    				new DialogInterface.OnClickListener() {

    			public void onClick(DialogInterface dialog, int which) {
    				contactID = targetEditText.getText().toString();
    				addContactToDatabase();
    				ContactListActivity.this.removeDialog(ADD_CONTACT_DIALOG_ID);
    			}
			});
    		
    		// Setup for Cancel button
    		builder.setNegativeButton(android.R.string.cancel, 
    				new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					//bearing = 361;
					ContactListActivity.this.removeDialog(ADD_CONTACT_DIALOG_ID);
				}
			});
    		
    		AlertDialog dialog = builder.create();
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
    	inflater.inflate(R.menu.contactmenu, menu);
    	
    	return true;
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);
	  MenuInflater inflater = getMenuInflater();
	  inflater.inflate(R.menu.contactcontextmenu, menu);
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
//			Toast toast = Toast.makeText(this, "CON SQL Pause: " + sqlEx.getMessage(), Toast.LENGTH_LONG);
//			toast.show();
		}
		catch (Exception ex) {
//			Toast toast = Toast.makeText(this, "CON Pause: " + ex.getMessage(), Toast.LENGTH_LONG);
//			toast.show();
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
//			Toast toast = Toast.makeText(this, "CON SQL Resume: " + sqlEx.getMessage(), Toast.LENGTH_LONG);
//			toast.show();
		}
		catch (Exception ex) {
//			Toast toast = Toast.makeText(this, "CON Resume: " + ex.getMessage(), Toast.LENGTH_LONG);
//			toast.show();
		}
    }
    
	/**
	 * Called when a menu item is selected.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
		switch (item.getItemId()) {
			case (R.id.addcontact):
				showDialog(ADD_CONTACT_DIALOG_ID);
				return true;
			case (R.id.clearcontacts):
				showClearContactsDialog();
				return true;
			case (R.id.preferences):
				intent = new Intent(this, UserPreferences.class);
				startActivityForResult(intent, 0);
				return true;
			case (R.id.info1):
				intent = new Intent(this, InfoActivity.class);
				startActivityForResult(intent, 0);
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		String temp;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.concontextdelete:
			cursor.moveToPosition(info.position);
			Object o = cursor.getString(cursor.getColumnIndex(ContactDatabaseAdapter.KEY_NAME));
			temp = o.toString();
			showDeleteContactDialog(temp);
			return true;
		case R.id.concontextclear:
			showClearContactsDialog();
			return true;
		  default:
		    return super.onContextItemSelected(item);
		  }
		}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		try {
			// Get the item that was clicked
			cursor.moveToPosition(position);
			Object o = cursor.getString(cursor.getColumnIndex(ContactDatabaseAdapter.KEY_NAME));
			String targetName = o.toString();
			
			// Create intent to launch details activity and add rock name
			Intent intent = new Intent(getApplicationContext(), 
					ContactTabActivity.class);
			intent.putExtra("TARGET", targetName);
			startActivityForResult(intent, 0);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private void addContactToDatabase() {
		db.addContact(contactID, 0, 10);
		db.addOwnShipCourseAndSpeed(contactID, 0, 10);
		fillData();
	}
	
	private void showDeleteContactDialog(String _contactName) {
		// Confirm clearing of contact and it's observation data
		String confirmMessage = "The contact " + _contactName + ", ";
		confirmMessage += "along with observation data, ";
		confirmMessage += "will be deleted. Continue?";
		final String contact = _contactName;
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		  dialog.setTitle("Confirm Contact Delete");
		  dialog.setMessage(confirmMessage);
		  dialog.setPositiveButton("Yes",
				  new OnClickListener() {
			  		public void onClick(DialogInterface dialog, int arg1) {
			  			deleteContact(contact);
			  			fillData();
			  		}
			  	  }
		  );
		  dialog.setNegativeButton("No",
				  new OnClickListener() {
			  		public void onClick(DialogInterface dialog, int arg1) {
			  			// Do nothing here ... just close
			  		}
			  	  }
		  );
		  dialog.show();
	}
	
	private void showClearContactsDialog() {
		// Confirm clearing of all observations
		String confirmMessage = "All contact information, ";
		confirmMessage += "including observation data, ";
		confirmMessage += "will be deleted. Continue?";
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		  dialog.setTitle("Confirm Data Clear");
		  dialog.setMessage(confirmMessage);
		  dialog.setPositiveButton("Yes",
				  new OnClickListener() {
			  		public void onClick(DialogInterface dialog, int arg1) {
			  			clearContacts();
			  		}
			  	  }
		  );
		  dialog.setNegativeButton("No",
				  new OnClickListener() {
			  		public void onClick(DialogInterface dialog, int arg1) {
			  			// Do nothing here ... just close
			  		}
			  	  }
		  );
		  dialog.show();
	}
	
	private void deleteContact(String contact) {
		db.deleteContact(contact);
		fillData();
	}
	
	private void clearContacts() {
		db.clearContacts();
		fillData();
	}
	
	/**
	 * \brief Creates a cursor adapter of contact data and attaches
	 * it to the list adapter.
	 * 
	 * Note that the method <c>startManagingCursor</c> is used and that the
	 * cursor is not closed.
	 */
	private void fillData() {
		try {
			//stopManagingCursor(cursor);
			cursor = db.getCursorOfContacts();
			//startManagingCursor(cursor);

			String[] from = new String[] { ContactDatabaseAdapter.KEY_NAME };
			int[] to = new int[] { R.id.label };

			// Now create an array adapter and set it to display using our row
			SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
					R.layout.contactrow, cursor, from, to);
			setListAdapter(cursorAdapter);
		}
		catch (SQLiteException sqlEx) {
			Toast toast = Toast.makeText(this, "CON SQL: " + sqlEx.getMessage(), Toast.LENGTH_LONG);
			toast.show();
		}
		catch (Exception ex) {
			Toast toast = Toast.makeText(this, "CON: " + ex.getMessage(), Toast.LENGTH_LONG);
			toast.show();
		}
	}
}
