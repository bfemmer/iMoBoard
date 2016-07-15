package com.spatialind.imoboard;

import com.spatialind.imoboard.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class ContactTabActivity extends TabActivity {
	private String contactName;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tab);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    Bundle extras = getIntent().getExtras();
        if(extras !=null) {
        	contactName = extras.getString("TARGET");
        } else {
        	contactName = "xxx";
        }
        
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, ManeuveringBoardActivity.class);
	    intent.putExtra("TARGET", contactName);
	    
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("moboard").setIndicator(
	    		this.getResources().getString(R.string.tab_moboard_string),
                res.getDrawable(R.layout.ic_tab_moboard))
            .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the second tab
	    intent = new Intent().setClass(this, ContactActivity.class);
	    intent.putExtra("TARGET", contactName);
	    spec = tabHost.newTabSpec("target").setIndicator(
	    		this.getResources().getString(R.string.tab_los_string),
                res.getDrawable(R.layout.ic_tab_target))
            .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the third tab
	    intent = new Intent().setClass(this, ObservationListActivity.class);
	    intent.putExtra("TARGET", contactName);
	    spec = tabHost.newTabSpec("observations").setIndicator(
	    		this.getResources().getString(R.string.tab_obs_string),
	                      res.getDrawable(R.layout.ic_tab_obs))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Set current tab to Maneuvering Board
	    tabHost.setCurrentTab(0);
	}
}
