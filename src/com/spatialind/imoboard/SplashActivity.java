package com.spatialind.imoboard;

import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;
import com.spatialind.imoboard.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.view.Window;
import android.widget.Toast;

public class SplashActivity extends Activity {
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh/JAm05FboXP2owmUF1Z1+mivay0/JPcXY2PK76u9+sEA09nN1BgU7J5teNzaZrK/NOzmoRNwQ/CjZj1zBDtm0eZaI5ZCPf1JLl9+BvTzE45y+2jcykrYLRPCBzVGK9gu7fRK5iATNazy5u9KygwukwP/aDs9qviPddd0pGY0jbmmbuIf08GcVKSKU02fTPJs8YzfXHpGMzPxKAwGtpige9DIGV2Gu0Vgi5sFEuF2CS9+1rn2eRs/6qWbL5YsiDKK7R9tR8wDBqEeMvaemohNGWi8lCi4AEtVgyWOe5IGKBNdh5EDa1V/Y7Dh2sfV+OKfi+4cU3vIUuNWMWORmI2nwIDAQAB";

    // Generate your own 20 random bytes, and put them here.
    private static final byte[] SALT = new byte[] {
        21, 34, 97, -101, -13, -56, 27, -41, 67, 16, -29, 83, -30, -108, -72, 16, -62, -49, 97,
        -55
    };
    
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    
    // A handler on the UI thread.
    private Handler mHandler;
	private Handler delayHandler = new Handler();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup progress bar before calling setContentView
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        
        setContentView(R.layout.splash);
        
        mHandler = new Handler();

        // Try to use more data here. ANDROID_ID is a single point of attack.
        String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        // Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        
        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(
            this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), deviceId)),
            BASE64_PUBLIC_KEY);
        
        // Perform license check
//        doLicenseCheck();
        
        // This activity will only display for 2.2 seconds.
        // Fire singleshot to start contact list activity.
        delayHandler.removeCallbacks(delayTimeTask);
        delayHandler.postDelayed(delayTimeTask, 2200);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mChecker.onDestroy();
    }
    
    private Runnable delayTimeTask = new Runnable() {
	   public void run() {
		// Launch map activity
       	Intent intent = new Intent(SplashActivity.this, 
				ContactListActivity.class);
		startActivity(intent);
		
		// This prevents the splash activity from resuming when the Back
		// button is clicked/pressed from the map activity.
		SplashActivity.this.finish();
	   }
	};
	
	private void doLicenseCheck() {
        setProgressBarIndeterminateVisibility(true);
        mChecker.checkAccess(mLicenseCheckerCallback);
    }
	
	private void displayResult(final String result) {
        mHandler.post(new Runnable() {
            public void run() {
                setProgressBarIndeterminateVisibility(false);
                
//                // Display a short toast if a license error exists 
//                if (result.equals("License Error")) {
//                	Toast tst = Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT);
//                    tst.show();
//                }
//                
//                if (result.equals("Unlicensed")) {
//                	Toast tst = Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT);
//                    tst.show();
//                }
            }
        });
    }
	
	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow() {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            
//            displayResult("Licensed");
            
            // Launch contact list activity
           	Intent intent = new Intent(SplashActivity.this, 
    				ContactListActivity.class);
    		startActivity(intent);
    		
    		// This prevents the splash activity from resuming when the Back
    		// button is clicked/pressed from the map activity.
    		SplashActivity.this.finish();
        }

        public void dontAllow() {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            
            displayResult("Unlicensed");
            
            // Should not allow access. In most cases, the app should assume
            // the user has access unless it encounters this. If it does,
            // the app should inform the user of their unlicensed ways
            // and then either shut down the app or limit the user to a
            // restricted set of features.
            
            // Launch unlicensed activity
           	Intent intent = new Intent(SplashActivity.this, 
           			UnlicensedActivity.class);
    		startActivity(intent);
    		
    		// This prevents the splash activity from resuming when the Back
    		// button is clicked/pressed from the map activity.
    		SplashActivity.this.finish();
        }

        public void applicationError(ApplicationErrorCode errorCode) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // Somehow, a mistake was made by the programmer (i.e. me)
            // while setting up or calling the license checker library.
            // This problem should be fixed, but don't penalize the
            // customer for it ... go ahead and let the program run.
            
            //String result = String.format(getString(R.string.application_error), errorCode);
            displayResult("License Error");
            
            // Launch contact list activity
           	Intent intent = new Intent(SplashActivity.this, 
    				ContactListActivity.class);
    		startActivity(intent);
    		
    		// This prevents the splash activity from resuming when the Back
    		// button is clicked/pressed from the map activity.
    		SplashActivity.this.finish();
        }
    }
}