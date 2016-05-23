package koemdzhiev.com.stormy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.Utils.Constants;
import koemdzhiev.com.stormy.weather.Hour;

public class HourlyMoreInfoActivity extends AppCompatActivity {
    private static final String TAG = HourlyMoreInfoActivity.class.getSimpleName();
    private TextView mSummaryTextView;
    private TextView mWindSpeed;
    private TextView mPressure;
    private Hour chosenHour;
    private ImageView mWeatherIcon;
    private TextView mTemperature;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_more_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mWeatherIcon = (ImageView) findViewById(R.id.weatherIcon);
        mSummaryTextView = (TextView) findViewById(R.id.summaryTextView);
        mWindSpeed = (TextView) findViewById(R.id.wind_speed);
        mPressure = (TextView) findViewById(R.id.pressure);
        mTemperature = (TextView) findViewById(R.id.temperature);
        mScrollView = (ScrollView) findViewById(R.id.finishThisActivity);
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent i = new Intent(HourlyMoreInfoActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                return true;
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        chosenHour = (Hour) extras.getParcelable(Constants.HOURLY_KEY);
        mSummaryTextView.setText(chosenHour.getSummary());
        mWindSpeed.setText(chosenHour.getWindSpeed() + " m/s");
        mPressure.setText(chosenHour.getPressure() + " millibars");
        mWeatherIcon.setImageResource(chosenHour.getIconId());
        mTemperature.setText(chosenHour.getTemperature() + " C");


        getSupportActionBar().setTitle("Weather details for " + chosenHour.getHour());

    }

}
