package com.spatialind.imoboard;

import com.spatialind.imoboard.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * A class representing the user preferences screen (activity).
 * 
 * @author BillF
 */
public class UserPreferences extends PreferenceActivity {
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
	}
}
