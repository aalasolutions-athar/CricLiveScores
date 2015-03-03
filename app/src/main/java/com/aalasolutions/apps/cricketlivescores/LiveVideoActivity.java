package com.aalasolutions.apps.cricketlivescores;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class LiveVideoActivity extends ActionBarActivity {
    String WEBURL = "http://www.eboundservices.com/iframe/new/iframe.php?stream=tensports";
    ProgressBar progress;
    WebView myWebView;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        makeWebView(savedInstanceState);
        makeAdView();
    }

    private void makeAdView() {
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device.
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("355033051433847")
                .build();
        adView = (AdView) findViewById(R.id.adView);
        // Start loading the ad in the background.
        adView.loadAd(adRequest);
        adView.setAdListener(getAdListener(adRequest));
        adView.bringToFront();
    }

    @Override
    protected void onDestroy() {

        //TODO Admob Destroy
        if (adView != null) {
            adView.destroy();
        }
//        if (billingProcessor != null)
//            billingProcessor.release();
        super.onDestroy();

    }

    @Override
    protected void onPause() {

        // on pause turn off the flash
        //TODO adview Pause
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeAdView();
        if (adView != null) {
            adView.resume();
        }
    }

    private AdListener getAdListener(final AdRequest adRequest) {
        return new AdListener() {
            // hide ad block if none could be found
            @Override
            public void onAdFailedToLoad(int errorCode) {
                adView.loadAd(adRequest);
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        break;
                }
            }

            // show ad block if one was found
            @Override
            public void onAdLoaded() {
                findViewById(R.id.adView).setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_live_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home) {
            myWebView.destroy();
            super.onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeWebView(Bundle savedInstanceState) {
        myWebView = (WebView) findViewById(R.id.webview);
        if (savedInstanceState == null) {
            myWebView.loadUrl(WEBURL);
        } else {
            myWebView.restoreState(savedInstanceState);
        }
        websettings();
        //  myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        myWebView.setWebViewClient(new MyWebViewClient());

    }

    private void websettings() {
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        // Set cache size to 8 mb by default. should be more than enough
        // This next one is crazy. It's the DEFAULT location for your app's cache
        // But it didn't work for me without this line
        webSettings.setAppCachePath("/data/data/" + getPackageName() + "/cache");

        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains(WEBURL)) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
            // open URL in an external web browser
            else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
            //            if (Uri.parse(url).getHost().equals("http://khatmenbuwat.org/")) {
            //                // This is my web site, so do not override; let my WebView load the page
            //                return false;
            //            }
            //            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            //            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //            startActivity(intent);
            //            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Toast.makeText(getBaseContext(), "Cannot find video" + description, Toast.LENGTH_SHORT).show();
            //super.onReceivedError(view, errorCode, description, failingUrl);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progress.setVisibility(View.VISIBLE);
        }

        public void onPageFinished(WebView view, String url) {
            progress.setVisibility(View.GONE);
        }

    }
}
