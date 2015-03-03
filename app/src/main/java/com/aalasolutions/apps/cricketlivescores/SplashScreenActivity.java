package com.aalasolutions.apps.cricketlivescores;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;


public class SplashScreenActivity extends ActionBarActivity {

    int SPLASH_DISPLAY_LENGTH = 1000;
    MediaPlayer bgSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        bgSound = MediaPlayer.create(getApplicationContext(), R.raw.crowd);
        bgSound.start();
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        /*
      Duration of wait *
     */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                SplashScreenActivity.this.startActivity(mainIntent);
                SplashScreenActivity.this.finish();
                // transition from splash to main menu
                overridePendingTransition(android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onDestroy() {

        bgSound.release();
        super.onDestroy();
    }
}
