package koemdzhiev.com.stormy.ui;

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

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.Arrays;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.adapters.HourListAdapter;
import koemdzhiev.com.stormy.weather.Hour;

/**
 * Created by koemdzhiev on 14/12/15.
 */
public class Hourly_forecast_fragment extends Fragment {
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.hourly_forecast_fragment,container,false);
        mEmptyTextView = (TextView)v.findViewById(android.R.id.empty);
        mListView = (ListView)v.findViewById(android.R.id.list);
        mSwipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.hourly_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green,R.color.blue,R.color.orange);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //if there is internet and if the mSwipeRefreshLayout in the current and daily fragments are not already running...

                    if (mActivity.isNetworkAvailable()) {
                        if (!mActivity.mCurrent_forecast_fragment.mSwipeRefreshLayout.isRefreshing() && !mActivity.mDaily_forecast_fragment.mSwipeRefreshLayout.isRefreshing()) {
                            mActivity.getLocation();
                        } else {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(mActivity, "currently refreshing...", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mActivity, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        Log.e("Forecast_fragment", "onCreateView");
        return v;
    }

    public void setUpHourlyFragment(){
        if (mActivity.mForecast != null) {
//            Toast.makeText(mActivity, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
            Hour[] hourlyForecast = mActivity.mForecast.getHourlyForecast();
            mHours = Arrays.copyOf(hourlyForecast, hourlyForecast.length, Hour[].class);

            HourListAdapter adapter = new HourListAdapter(mActivity, mHours);
            mListView.setEmptyView(mEmptyTextView);
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Hour h = mHours[position];
                    String time = mActivity.mCurrent_forecast_fragment.mTimeLabel.getText().toString();
                    String temperature = mActivity.mCurrent_forecast_fragment.mTemperatureLabel.getText().toString();
                    String summary = mActivity.mCurrent_forecast_fragment.mSummaryLabel.getText().toString();
                    String message = String.format("At %s it will be %s and %s",time,temperature,summary);
                    Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
                    //play animations
                    YoYo.with(Techniques.Shake).duration(200).playOn(view);
                }
            });
        }
    }
}

