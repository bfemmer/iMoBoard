package com.spatialind.imoboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class ContactDatabaseAdapter {
	private static final String DATABASE_NAME = "contacts.db";
	private static final String TABLE_CONTACTS = "contacts";
	private static final String TABLE_OBSERVATIONS = "observations";
	private static final String TABLE_OWNSHIP = "ownship";
	private static final int DATABASE_VERSION = 1;
	
	// The index (key) column name for use in where clauses
	public static final String KEY_ID = "_id"; // Index column
	public static final String KEY_NAME = "contact_name"; // ID of contact
	public static final String KEY_TIME = "obs_time"; //YYYY-MM-DD HH:MM:SS.SSS
	public static final String KEY_BEARING = "bearing";
	public static final String KEY_RANGE = "range";
	public static final String KEY_COURSE = "course"; // Own ship course
	public static final String KEY_SPEED = "speed"; // Own ship speed
	
	private static final String TABLE_CONTACTS_CREATE = "create table " +
		TABLE_CONTACTS + "("+ KEY_ID +
		" integer primary key autoincrement, "+
		KEY_NAME + " text not null, "+
		KEY_COURSE + " text not null, "+
		KEY_SPEED + " text not null);";
	
	private static final String TABLE_OBSERVATIONS_CREATE = "create table " +
	TABLE_OBSERVATIONS + " (" + KEY_ID +
	" integer primary key autoincrement, "+
	KEY_NAME + " text not null, "+
	KEY_TIME + " text not null, "+
	KEY_BEARING + " text not null, "+
	KEY_RANGE + " text not null);";
	
	private static final String TABLE_OWNSHIP_CREATE = "create table " +
	TABLE_OWNSHIP + " ("+ KEY_ID +
	" integer primary key autoincrement, "+
	KEY_NAME + " text not null, "+
	KEY_COURSE + " text not null, "+
	KEY_SPEED + " text not null);";
	
	private static SQLiteDatabase db;
	private final Context context;
	private ContactDBOpenHelper dbHelper;
	
	public ContactDatabaseAdapter(Context _context) {
	    this.context = _context;
	    dbHelper = new ContactDBOpenHelper(context, DATABASE_NAME, 
	    		null, DATABASE_VERSION);
	}
	  
	public void close() {
		db.close();
	}
	  
	public void open() throws SQLiteException { 
		try {
	      db = dbHelper.getWritableDatabase();
	    } catch (SQLiteException ex) {
	      db = dbHelper.getReadableDatabase();
	    }
	}
	
	public float getContactCourse(String _contactID) {
		String selection, course;
		Cursor cursor;
		
		// Setup and execute query
		selection = KEY_NAME + " = '" + _contactID + "'";
		cursor = db.query(TABLE_CONTACTS, 
	    		null, selection, null, null, null, null);
		
		// Evaluate record count and throw exception if no records found
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
	      throw new SQLException("No record found with contact number: " + _contactID);
	    }
	    
	    // Extract value and return
	    course = cursor.getString(cursor.getColumnIndex(KEY_COURSE));
	    return Float.valueOf(course);
	}
	
	public float getOwnShipCourse(String _contactID) {
		String selection, course;
		Cursor cursor;
		
		// Setup and execute query
		selection = KEY_NAME + " = '" + _contactID + "'";
		cursor = db.query(TABLE_OWNSHIP, 
	    		null, selection, null, null, null, null);
		
		// Evaluate record count and throw exception if no records found
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
	      throw new SQLException("No record found with contact number: " + _contactID);
	    }
	    
	    // Extract value and return
	    course = cursor.getString(cursor.getColumnIndex(KEY_COURSE));
	    return Float.valueOf(course);
	}
	
	public float getContactSpeed(String _contactID) {
		String selection, speed;
		Cursor cursor;
		
		// Setup and execute query
		selection = KEY_NAME + " = '" + _contactID + "'";
		cursor = db.query(TABLE_CONTACTS, 
	    		null, selection, null, null, null, null);
		
		// Evaluate record count and throw exception if no records found
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
	      throw new SQLException("No record found with contact number: " + _contactID);
	    }
	    
	    // Extract value and return
	    speed = cursor.getString(cursor.getColumnIndex(KEY_SPEED));
	    return Float.valueOf(speed);
	}
	
	public float getOwnShipSpeed(String _contactID) {
		String selection, speed;
		Cursor cursor;
		
		// Setup and execute query
		selection = KEY_NAME + " = '" + _contactID + "'";
		cursor = db.query(TABLE_OWNSHIP, 
	    		null, selection, null, null, null, null);
		
		// Evaluate record count and throw exception if no records found
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
	      throw new SQLException("No record found with contact number: " + _contactID);
	    }
	    
	    // Extract value and return
	    speed = cursor.getString(cursor.getColumnIndex(KEY_SPEED));
	    return Float.valueOf(speed);
	}
	
	public Cursor getCursorOfContacts() {
		// Return cursor
	    return db.query(TABLE_CONTACTS, null, null, null, null, null, null);
	}
	
	public Cursor getCursorOfObservations(String _contactID) {
		String selection;
		selection = KEY_NAME + " = '" + _contactID + "'";
		
		// Return cursor
		try {
			return db.query(TABLE_OBSERVATIONS, 
	    		null, selection, null, null, null, null);
		} catch (SQLiteException ex) {
			Log.d("ContactDatabaseAdapter", ex.getMessage());
			return null;
		}
	}
	
	public Cursor getCursorOfOwnShipCourse(String _contactID) {
		String selection;
		selection = KEY_NAME + " = '" + _contactID + "'";
		
		// Return cursor
		try {
			return db.query(TABLE_OWNSHIP, 
	    		null, selection, null, null, null, null);
		} catch (SQLiteException ex) {
			Log.d("ContactDatabaseAdapter", ex.getMessage());
			return null;
		}
	}
	
	public long addContact(String _contactID,
			float _course, float _speed) {
		ContentValues values = 
			createContentValues(_contactID, String.valueOf(_course), String.valueOf(_speed));

		return db.insert(TABLE_CONTACTS, null, values);
	}
	
	public long addOwnShipCourseAndSpeed(String _contactID, 
			float _course, float _speed) {
		ContentValues values = 
			createContentValues(_contactID, String.valueOf(_course), String.valueOf(_speed));

		return db.insert(TABLE_OWNSHIP, null, values);
	}
	
	public long updateOwnShipCourse(String _contactID, String _course) {
		// Create content values
		ContentValues values = new ContentValues();
		values.put(KEY_COURSE, _course);
		
		// Create where clause for contact ID
		String whereClause = KEY_NAME + "='"+ _contactID + "'";
		
		// Update table
		return db.update(TABLE_OWNSHIP, values, whereClause, null);
	}
	
	public long updateOwnShipSpeed(String _contactID, String _speed) {
		// Create content values
		ContentValues values = new ContentValues();
		values.put(KEY_SPEED, _speed);
		
		// Create where clause for contact ID
		String whereClause = KEY_NAME + "='"+ _contactID + "'";
		
		// Update table
		return db.update(TABLE_OWNSHIP, values, whereClause, null);
	}
	
	public long updateContactCourseAndSpeed(String _contactID, float _course, float _speed) {
		// Create content values
		ContentValues values = new ContentValues();
		values.put(KEY_COURSE, String.valueOf(_course));
		values.put(KEY_SPEED, String.valueOf(_speed));
		
		// Create where clause for contact ID
		String whereClause = KEY_NAME + "='"+ _contactID + "'";
		
		// Update table
		return db.update(TABLE_CONTACTS, values, whereClause, null);
	}
	
	public long updateOwnShipCourseAndSpeed(String _contactID, float _course, float _speed) {
		// Create content values
		ContentValues values = new ContentValues();
		values.put(KEY_COURSE, String.valueOf(_course));
		values.put(KEY_SPEED, String.valueOf(_speed));
		
		// Create where clause for contact ID
		String whereClause = KEY_NAME + "='"+ _contactID + "'";
		
		// Update table
		return db.update(TABLE_OWNSHIP, values, whereClause, null);
	}
	
	public long addObservation(String _contactID, float _bearing, 
			float _range, String _timeOfObs) {
		ContentValues values = createContentValues(
				_contactID, String.valueOf(_bearing), String.valueOf(_range), _timeOfObs);

		return db.insert(TABLE_OBSERVATIONS, null, values);
	}
	
	public long updateObservation(long _rowIndex, float _bearing, 
			float _range, String _timeOfObs) {
		// Create content values
		ContentValues values = new ContentValues();
		values.put(KEY_BEARING, String.valueOf(_bearing));
		values.put(KEY_RANGE, String.valueOf(_range));
		values.put(KEY_TIME, String.valueOf(_timeOfObs));
		
		// Create where clause for contact ID
		String whereClause = KEY_ID + "="+ _rowIndex;

		// Update table
		return db.update(TABLE_OBSERVATIONS, values, whereClause, null);
	}
	
	public boolean deleteContact(long _rowIndex) {
		return db.delete(TABLE_CONTACTS, KEY_ID + "="+ _rowIndex, null) > 0;
//		db.beginTransaction();
//		try {
//			db.delete(TABLE_CONTACTS, KEY_ID + "="+ _rowIndex, null);
//			db.delete(TABLE_CONTACTS, KEY_ID + "="+ _rowIndex, null);
//			db.delete(TABLE_CONTACTS, KEY_ID + "="+ _rowIndex, null);
//			db.delete(TABLE_OWNSHIP, KEY_ID + "="+ _contactName, null);
//			db.delete(TABLE_OBSERVATIONS, KEY_ID + "="+ _contactName, null);
//			db.delete(TABLE_CONTACTS, KEY_ID + "="+ _contactName, null);
//			
//			db.setTransactionSuccessful();
//		} catch (Exception e) {
//			
//		} finally {
//			db.endTransaction();
//		}
	}
	
	public void deleteContact(String _contactName) {
		// Create where clause for contact ID
		String whereClause = KEY_NAME + "='"+ _contactName + "'";
		
		// Delete data within a transaction and try/catch block
		db.beginTransaction();
		try {
			db.delete(TABLE_OWNSHIP, whereClause, null);
			db.delete(TABLE_OBSERVATIONS, whereClause, null);
			db.delete(TABLE_CONTACTS, whereClause, null);
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			
		} finally {
			db.endTransaction();
		}
	}
	
	public void deleteObservation(long _rowIndex) {
		db.delete(TABLE_OBSERVATIONS, KEY_ID + "="+ _rowIndex, null);
	}
	
	public void clearContacts() {
		db.beginTransaction();
		try {
			db.delete(TABLE_OWNSHIP, null, null);
			db.delete(TABLE_OBSERVATIONS, null, null);
			db.delete(TABLE_CONTACTS, null, null);
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			
		} finally {
			db.endTransaction();
		}
	}
	
	public void clearObservations(String contactID) {
		db.beginTransaction();
		try {
			db.delete(TABLE_OBSERVATIONS, KEY_NAME + "='"+ contactID + "'", null);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			
		} finally {
			db.endTransaction();
		}
	}
	
	private ContentValues createContentValues(String _contactID,
			String _course, String _speed) {
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, _contactID);
		values.put(KEY_COURSE, _course);
		values.put(KEY_SPEED, _speed);
		return values;
	}
	
	private ContentValues createContentValues(String _contactID, 
			String _bearing, String _range, String _timeOfObs) {
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, _contactID);
		values.put(KEY_BEARING, _bearing);
		values.put(KEY_RANGE, _range);
		values.put(KEY_TIME, _timeOfObs);
		return values;
	}

	private static class ContactDBOpenHelper extends SQLiteOpenHelper {
		private final Context helperContext;
		
		public ContactDBOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);	
			this.helperContext = context;
		}
		
		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(TABLE_CONTACTS_CREATE);
			_db.execSQL(TABLE_OBSERVATIONS_CREATE);
			_db.execSQL(TABLE_OWNSHIP_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
		
		public synchronized void close() {
			if (db != null)
				db.close();
			super.close();
		}
	}
}
