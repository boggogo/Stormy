package koemdzhiev.com.stormy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.Utils.Constants;
import koemdzhiev.com.stormy.weather.Day;

public class DailyMoreInfoActivity extends AppCompatActivity {
    private static final String TAG = DailyMoreInfoActivity.class.getSimpleName();
    private Day chosenDay;
    private ImageView mWeatherIcon;
    private TextView mSummaryTextView;

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

        Intent intent = getIntent();
        String dayOfTheWeek = intent.getStringExtra(Constants.DAY_OF_WEEK);
        Bundle extras = intent.getExtras();
        chosenDay = (Day) extras.getParcelable(Constants.DAY_KEY);
        getSupportActionBar().setTitle(dayOfTheWeek);

        mWeatherIcon.setImageResource(chosenDay.getIconId());
        mSummaryTextView.setText(chosenDay.getSummary());

        Log.d(TAG, "Summary: " + chosenDay.getSummary());
        Log.d(TAG, "Time: " + chosenDay.getTime());
        Log.d(TAG, "TempMax: " + chosenDay.getTemperatureMax());
        Log.d(TAG, "IconID: " + chosenDay.getIconId());

    }


}
