package com.bluetoothmessenger.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.bluetoothmessenger.R;
import com.bluetoothmessenger.utils.Constants;

public class SplashScreenActivity extends Activity {

    // Splash screen timer - 3secs
    private static int SPLASH_TIME_OUT = 3000;

    private boolean isRegisteredUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        init();
        runSplashScreen();
    }

    /* Initialize variables */
    private void init() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isRegisteredUser = preferences.getBoolean(Constants.REGISTERED_DEVICE_NAME, false);
    }

    /* Run a thread and check if the user is already registered at the application */
    private void runSplashScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (isRegisteredUser) {
                    Intent i = new Intent(SplashScreenActivity.this, HomeActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(SplashScreenActivity.this, RegisterActivity.class);
                    startActivity(i);
                }

                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
