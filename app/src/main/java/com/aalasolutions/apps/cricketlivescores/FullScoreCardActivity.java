package com.aalasolutions.apps.cricketlivescores;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.aalasolutions.apps.cricketlivescores.classes.JSONParser;
import com.aalasolutions.apps.cricketlivescores.model.MatchDetail;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class FullScoreCardActivity extends ActionBarActivity {
    @InjectView(R.id.title)
    TextView tvtitle;
    @InjectView(R.id.tvTeamName)
    TextView tvTeamName;
    @InjectView(R.id.tvTeamScore)
    TextView tvTeamScore;
    @InjectView(R.id.summary)
    TextView tvSummary;
    @InjectView(R.id.description)
    TextView tvDescription;
    @InjectView(R.id.tvCurrentInning)
    TextView tvCurrentInning;
    @InjectView(R.id.scoreCard)
    TableLayout tableLayout;
    @InjectView(R.id.dataLinearLayout)
    LinearLayout dataLinearLayout;
    @InjectView(R.id.internalLayout)
    LinearLayout internalLayout;

    MatchDetail matchDetail;
    Context applicationContext;
    ProgressDialog pDialog;
    TeamDetails team1 = new TeamDetails();
    TeamDetails team2 = new TeamDetails();
    private AdView adView;
    private String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1709767846664941/2945331709";
    private InterstitialAd interstitial;

    //TODO INTERTITIAL ADVERTISE ca-app-pub-1709767846664941/2945331709
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_score_card);
        matchDetail = new MatchDetail();
        matchDetail.matchTitle = getIntent().getStringExtra("matchtitle");
        matchDetail.matchLink = getIntent().getStringExtra("matchlink");
        ButterKnife.inject(this);
        tvtitle.setText(matchDetail.matchTitle);
        tvSummary.setText(matchDetail.matchLink);
        //Remove // ?CMP=OTC-RSS from the link
        applicationContext = this;
        callAsynchronousTask();
        makeAdView();
        displayInterstitial();
    }

    private AdListener getAdListener() {
        return new AdListener() {
            // hide ad block if none could be found

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("ad failed to load", errorCode + "");
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Log.e("INTERSTITIAL: ", "internal error");
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        Log.e("INTERSTITIAL : ", "invalid request");
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        Log.e("INTERSTITIAL: ", "network error");
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        Log.e("INTERSTITIAL : ", "no fill");
                        break;
                }
            }

            // show ad block if one was found
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                interstitial.show();
            }
        };
    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {

        // Create the interstitial.
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(INTERSTITIAL_AD_UNIT_ID);
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//				    .addTestDevice("355033051433847")
                .build();
        // Create ad request.
//		AdRequest adRequest = new AdRequest.Builder().build();
        // Begin loading your interstitial.
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(getAdListener());

        if (interstitial.isLoaded()) {
            interstitial.show();
        }
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
    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            tableLayout.removeAllViews();
                            tableLayout.setVisibility(View.GONE);
                            dataLinearLayout.removeAllViews();
                            new getMatchDetailScore().execute();

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 60000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_full_score_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void parseMatchCard(XmlPullParser myParser) {
        int event;
        String text = null;
        boolean itemstarted = false;
        boolean partnerships = false;
        boolean trTagStarted = false;
        boolean tdTagStarted = false;
        TableRow tableRow = new TableRow(getApplicationContext());

        TextView textView;
        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:

                        if (name.equals("table")) {
                            itemstarted = true;
                        }
                        if (name.equals("tr")) {
                            trTagStarted = true;
                            tableRow = new TableRow(getApplicationContext());

                        }
                        if (name.equals("td")) {
                            tdTagStarted = true;
                            event = myParser.next();
                            if (event == XmlPullParser.TEXT) {
                                text = myParser.getText();
                            } else if (event == XmlPullParser.START_TAG && myParser.getName().equals("a")) {
                                event = myParser.next();
                                if (event == XmlPullParser.TEXT) {
                                    text = myParser.getText();
                                } else if (event == XmlPullParser.END_TAG) {
                                    continue;
                                }
                            } else if (event == XmlPullParser.START_TAG && myParser.getName().equals("td")) {
                                continue;
                            }

                        }

                        break;
                    case XmlPullParser.TEXT:
                        text = myParser.getText();

                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals("caption") && (text.equals("Partnerships") || text.equals("To bat"))) {
                            partnerships = true;

                        }
                        if (!partnerships) {
                            if (trTagStarted) {
                                if (name.equals("th") || name.equals("td")) {
                                    Log.e("tag:data ", text);
                                    textView = new TextView(getApplicationContext());
                                    textView.setText(text);
                                    textView.setPadding(5, 5, 5, 5);
                                    tableRow.addView(textView);
                                    if (text.equals("Batting")) {
                                        textView = new TextView(getApplicationContext());
                                        textView.setText("  ");
                                        textView.setPadding(5, 5, 5, 5);
                                        tableRow.addView(textView);

                                    }
                                } else if (name.equals("link")) {
                                    matchDetail.matchLink = text;
                                }
                                if (name.equals("item")) {
                                    itemstarted = false;
                                }
                            }

                        }

                        if (name.equals("tr")) {
                            trTagStarted = false;
                            tableLayout.addView(tableRow);
                            if (tableLayout.getVisibility() == View.GONE) {
                                tableLayout.setVisibility(View.VISIBLE);
                            }
                        }
                        if (name.equals("td")) {
                            tdTagStarted = false;
                        }
                        if (name.equals("table")) {
                            partnerships = false;
                            itemstarted = false;
                            TableRow x = new TableRow(getApplicationContext());

                            textView = new TextView(getApplicationContext());
                            textView.setText("  ");
                            textView.setPadding(5, 5, 5, 5);
                            x.addView(textView);
                            tableLayout.addView(x);

                        }
                        break;
                }
                event = myParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getTeamDetails(JSONObject val) throws JSONException {
        JSONArray teams = val.getJSONArray("team");
        team1.team_id = teams.getJSONObject(0).getString("team_id");
        team1.team_name = teams.getJSONObject(0).getString("team_name");
        team1.team_logo = teams.getJSONObject(0).getString("logo_image_path");
        team1.team_short_name = teams.getJSONObject(0).getString("team_short_name");
        team2.team_id = teams.getJSONObject(1).getString("team_id");
        team2.team_name = teams.getJSONObject(1).getString("team_name");
        team2.team_logo = teams.getJSONObject(1).getString("logo_image_path");
        team2.team_short_name = teams.getJSONObject(1).getString("team_short_name");

        tvtitle.setText(team1.team_name + " VS " + team2.team_name);
    }

    private void getMatchCard(JSONObject val) throws JSONException, XmlPullParserException {
        String match_card = val.getString("match_card");
        InputStream stream = new ByteArrayInputStream(match_card.getBytes(Charset.forName("UTF-8")));
        XmlPullParserFactory xmlFactoryObject;
        xmlFactoryObject = XmlPullParserFactory.newInstance();
        XmlPullParser myparser = xmlFactoryObject.newPullParser();

        myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES
                , false);
        myparser.setInput(stream, null);
        parseMatchCard(myparser);
    }

    private void getInnings(JSONObject val, TeamDetails team1, TeamDetails team2) throws JSONException {
        JSONArray innings = val.getJSONArray("innings");
        ArrayList<Inning> inningArrayList = new ArrayList<Inning>();
        for (int i = 0; i < innings.length(); i++) {
            Inning inning = new Inning();
            JSONObject singleinning = innings.getJSONObject(i);
            inning.batting_team_id = singleinning.getString("batting_team_id");
            if (inning.batting_team_id.equals(team1.team_id)) {
                inning.batting_team_name = team1.team_short_name;
                inning.bowling_team_name = team2.team_short_name;
            }
            if (inning.batting_team_id.equals(team2.team_id)) {
                inning.batting_team_name = team2.team_short_name;
                inning.bowling_team_name = team1.team_short_name;
            }
            inning.bowling_team_id = singleinning.getString("bowling_team_id");
            inning.run_rate = singleinning.getString("run_rate");
            inning.runs = singleinning.getString("runs");
            inning.wickets = singleinning.getString("wickets");
            inning.overs = singleinning.getString("overs");
            inning.over_limit = singleinning.getString("over_limit");
            inning.innings_numth = singleinning.getString("innings_numth");
            inning.live_current_name = singleinning.getString("live_current_name");
            if (inning.live_current_name.equals("current innings")) {
                tvCurrentInning.setText(inning.batting_team_name + " playing");
            }
            if (inning.run_rate == "null" || inning.run_rate.equals(null)) {
                inning.run_rate = "0.0";
            }
            inningArrayList.add(inning);
        }
        for (Inning i : inningArrayList) {
            TextView name = new TextView(this);
            TextView data = new TextView(this);
            name.setLayoutParams(tvTeamName.getLayoutParams());
            data.setLayoutParams(tvTeamScore.getLayoutParams());
            name.setText(i.batting_team_name);
            data.setText(" Runs:" + i.runs + " RunRate:" + i.run_rate + " Wickets:" + i.wickets + " Overs:" + i.overs);
            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.HORIZONTAL);
            l.setLayoutParams(internalLayout.getLayoutParams());
            l.addView(name);
            l.addView(data);
            dataLinearLayout.addView(l);
        }
        internalLayout.setVisibility(View.INVISIBLE);

    }

    private class getMatchDetailScore extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(applicationContext);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            JSONParser sh = new JSONParser();
            // Making a request to url and getting response
            final String jsonStr = sh.makeServiceCall(matchDetail.matchLink, JSONParser.GET);
            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        JSONObject val = null;
                        try {
                            val = new JSONObject(jsonStr);

                            //current inning
                            String description = val.getString("description");
                            String status = val.getJSONObject("live").getString("status");
                            //team
                            tvDescription.setText(description);
                            tvSummary.setText(status);
                            getMatchCard(val);
                            //innings
                            getTeamDetails(val);
                            getInnings(val, team1, team2);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */

        }

    }

    private class TeamDetails {
        public String team_id;
        public String team_name;
        public String team_logo;
        public String team_short_name;
    }

    private class Inning {
        public String batting_team_id;
        public String batting_team_name;
        public String bowling_team_id;
        public String bowling_team_name;
        public String run_rate;
        public String runs;
        public String wickets;
        public String overs;
        public String over_limit;
        public String innings_numth;
        public String live_current_name;
    }
}

