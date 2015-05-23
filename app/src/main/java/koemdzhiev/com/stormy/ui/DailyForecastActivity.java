package koemdzhiev.com.stormy.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.adapters.DayAdapter;
import koemdzhiev.com.stormy.weather.Day;

public class DailyForecastActivity extends ListActivity {
    private Day[] mDays;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);


        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables,parcelables.length,Day[].class);

        DayAdapter adapter = new DayAdapter(this,mDays);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String dayOfTheWeek = mDays[position].getDayOfTheWeek();
        String condition = mDays[position].getSummary();
        String highTemp = mDays[position].getTemperatureMax()+"";

        String massage = String.format("On %s the high will be %s and it will be %s",dayOfTheWeek,highTemp,condition);
        Toast.makeText(this,massage,Toast.LENGTH_LONG).show();
    }
}
