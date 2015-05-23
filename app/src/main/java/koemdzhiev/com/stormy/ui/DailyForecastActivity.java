package koemdzhiev.com.stormy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.adapters.DayAdapter;
import koemdzhiev.com.stormy.weather.Day;

public class DailyForecastActivity extends Activity {
    private Day[] mDays;
    @InjectView(android.R.id.list) ListView mListView;
    @InjectView(android.R.id.empty) TextView mEmptyTextView;
    private TextView mLocationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        ButterKnife.inject(this);
        mLocationLabel = (TextView)findViewById(R.id.LocationLabel);

        Intent intent = getIntent();
        String location = intent.getStringExtra(MainActivity.LOCATION_KEY);
        mLocationLabel.setText(location);

        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables,parcelables.length,Day[].class);

        DayAdapter adapter = new DayAdapter(this,mDays);
        mListView.setAdapter(adapter);
        mListView.setEmptyView(mEmptyTextView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dayOfTheWeek = mDays[position].getDayOfTheWeek();
                String condition = mDays[position].getSummary();
                String highTemp = mDays[position].getTemperatureMax() + "";

                String massage = String.format("On %s the high will be %s and it will be %s", dayOfTheWeek, highTemp, condition);
                Toast.makeText(DailyForecastActivity.this, massage, Toast.LENGTH_LONG).show();
            }
        });
    }


}
