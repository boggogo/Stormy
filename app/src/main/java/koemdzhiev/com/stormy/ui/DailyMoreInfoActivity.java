package koemdzhiev.com.stormy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.Utils.Constants;
import koemdzhiev.com.stormy.weather.Day;

public class DailyMoreInfoActivity extends AppCompatActivity {
    private static final String TAG = DailyMoreInfoActivity.class.getSimpleName();
    private Day chosenDay;
    private ImageView mWeatherIcon;
    private TextView mSummaryTextView;
    private TextView mSunrise;
    private TextView mSunset;
    private TextView mWindSpeed;
    private TextView mPressure;
    private TextView mTemperature;
    private ScrollView mScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_more_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mWeatherIcon = (ImageView) findViewById(R.id.weatherIcon);
        mSummaryTextView = (TextView) findViewById(R.id.summaryTextView);
        mSunrise = (TextView) findViewById(R.id.sunrise_time);
        mSunset = (TextView) findViewById(R.id.sunset_time);
        mWindSpeed = (TextView) findViewById(R.id.wind_speed);
        mPressure = (TextView) findViewById(R.id.pressure);
        mTemperature = (TextView) findViewById(R.id.temperature);
        mScrollView = (ScrollView)findViewById(R.id.dailyScrollView);
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent i = new Intent(DailyMoreInfoActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                return true;
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        Intent intent = getIntent();
        String dayOfTheWeek = intent.getStringExtra(Constants.DAY_OF_WEEK);
        Bundle extras = intent.getExtras();
        chosenDay = (Day) extras.getParcelable(Constants.DAY_KEY);
        getSupportActionBar().setTitle(dayOfTheWeek);

        mWeatherIcon.setImageResource(chosenDay.getIconId());
        mSummaryTextView.setText(chosenDay.getSummary());
        mSunrise.setText(chosenDay.getFormattedSunRiseTime() + "");
        mSunset.setText(chosenDay.getFormattedSunSetTime() + "");
        mWindSpeed.setText(chosenDay.getWindSpeed() + " m/s");
        mPressure.setText(chosenDay.getPressure() + " millibars");
        mTemperature.setText(chosenDay.getTemperatureMax() + " C");

//        Log.d(TAG, "Summary: " + chosenDay.getSummary());
//        Log.d(TAG, "Time: " + chosenDay.getTime());
//        Log.d(TAG, "TimeZone: " + chosenDay.getTimezone());
//        Log.d(TAG, "TempMax: " + chosenDay.getTemperatureMax());
//        Log.d(TAG, "IconID: " + chosenDay.getIconId());
//
//        Log.d(TAG, "Sunrise time: " + chosenDay.getFormattedSunRiseTime());
//        Log.d(TAG, "Sunset time: " + chosenDay.getFormattedSunSetTime());
//        Log.d(TAG, "Wind Speed: " + chosenDay.getWindSpeed() + " m/s");
//        Log.d(TAG, "Pressure: " + chosenDay.getPressure() + " millibars");

    }


}
