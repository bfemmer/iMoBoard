package com.spatialind.imoboard;

import com.spatialind.imoboard.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class InfoActivity extends Activity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        ImageView image = (ImageView)findViewById(R.id.dbBlackImage);
        image.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// TODO
			}
        });
    }
}