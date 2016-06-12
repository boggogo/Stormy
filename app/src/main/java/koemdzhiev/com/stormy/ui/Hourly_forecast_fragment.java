package koemdzhiev.com.stormy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.Utils.Constants;
import koemdzhiev.com.stormy.adapters.HourListAdapter;
import koemdzhiev.com.stormy.weather.Hour;

/**
 * Created by koemdzhiev on 14/12/15.
 */
public class Hourly_forecast_fragment extends Fragment {
    private static final String TAG = "MainActivity";
    private Hour[] mHours;
    private MainActivity mActivity;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    TextView mEmptyTextView;
    ListView mListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = ((MainActivity) getActivity());


    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume - Hourly Fragment called");
        mActivity = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.hourly_forecast_fragment, container, false);
        mEmptyTextView = (TextView) v.findViewById(android.R.id.empty);
        mListView = (ListView) v.findViewById(android.R.id.list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.hourly_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.blue, R.color.orange);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //if there is internet and if the mSwipeRefreshLayout in the current and daily fragments are not already running...

                if (mActivity.isNetworkAvailable()) {
                    if (!mActivity.mDaily_forecast_fragment.mSwipeRefreshLayout.isRefreshing() && !mActivity.mCurrent_forecast_fragment.mSwipeRefreshLayout.isRefreshing()) {
                        if (mActivity.isLocationServicesEnabled()) {
                            if (mActivity.latitude != 0.0 && mActivity.longitude != 0.0) {
                                mActivity.getForecast(mActivity.latitude, mActivity.longitude);
                            } else {
                                mActivity.getLocation();
                            }
                        } else {
                            mActivity.alertForNoLocationEnabled();
                        }
                    } else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(mActivity, "currently refreshing...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mActivity, "No Internet Connection!", Toast.LENGTH_LONG).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        Log.e("Forecast_fragment", "onCreateView");
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG, "OnDestroyView - Hourly Fragment called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "OnDestroy - Hourly Fragment called");
    }

    public void setUpHourlyFragment() {
        //set to null to reset the old one and set a new adapter bellow...
        mListView.setAdapter(null);
        Hour[] hourlyForecast = mActivity.mForecast.getHourlyForecast();
        mHours = Arrays.copyOf(hourlyForecast, hourlyForecast.length, Hour[].class);

        HourListAdapter adapter = new HourListAdapter(mActivity, mHours);
        mListView.setEmptyView(mEmptyTextView);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Hour h = mHours[position];
                String time = h.getHour();
                String temperature = h.getTemperature() + "";
                String summary = h.getSummary();

                Intent goToHourlyMoreInfo = new Intent(mActivity, HourlyMoreInfoActivity.class);
                goToHourlyMoreInfo.putExtra(Constants.HOURLY_KEY,mHours[position]);

                startActivity(goToHourlyMoreInfo);

                String message = String.format("At %s it will be %s and %s", time, temperature, summary);
                //Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
                //play animations
                //YoYo.with(Techniques.Shake).duration(200).playOn(view);
            }
        });

    }
}

