package koemdzhiev.com.stormy.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.adapters.ViewPagerAdapter;
import koemdzhiev.com.stormy.weather.Current;

/**
 * Created by koemdzhiev on 14/12/15.
 */
public class Current_forecast_fragment extends Fragment {
    private static final String TAG = "MainActivity";
    private MainActivity mActivity;
    TextView mTimeLabel;
    TextView mTemperatureLabel;
    TextView mHumidityValue;
    TextView mPrecipValue;
    TextView mSummaryLabel;
    TextView mLocationLabel;
    TextView mWindSpeedValue;
    TextView mFeelsLike;
    ImageView mIconImageView;
    ImageView mDegreeImageView;
    public SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = ((MainActivity) getActivity());
//        Log.d(mActivity.getClass().getSimpleName(),"OnCreateFragment");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume - Current Fragment called");
        mActivity = ((MainActivity) getActivity());
        if (!mActivity.mCurrent_forecast_fragment.isAdded()) {
            mActivity.adapter = new ViewPagerAdapter(mActivity.getSupportFragmentManager(), mActivity.Titles, mActivity.Numboftabs, mActivity.mCurrent_forecast_fragment,
                    mActivity.mHourly_forecast_fragment, mActivity.mDaily_forecast_fragment);
            mActivity.pager.setOffscreenPageLimit(3);
            mActivity.pager.setAdapter(mActivity.adapter);
            Log.d(TAG, "OnResume - Current Fragment reattached");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.current_forefast_fragment, container, false);
        mTimeLabel = (TextView) v.findViewById(R.id.timeLabel);
        mTemperatureLabel = (TextView) v.findViewById(R.id.temperatureLabel);
        mHumidityValue = (TextView) v.findViewById(R.id.humidityValue);
        mPrecipValue = (TextView) v.findViewById(R.id.precipValue);
        mSummaryLabel = (TextView) v.findViewById(R.id.summaryLabel);
        mLocationLabel = (TextView) v.findViewById(R.id.locationLabel);
        mWindSpeedValue = (TextView) v.findViewById(R.id.windSpeedValue);
        mFeelsLike = (TextView) v.findViewById(R.id.feels_like_label);
        mIconImageView = (ImageView) v.findViewById(R.id.iconImageView);
        mDegreeImageView = (ImageView) v.findViewById(R.id.degreeImageView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.current_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.blue, R.color.green);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("TAG", "Swiping in current!");
                //if there is internet and if the mSwipeRefreshLayout in the Hourly and daily fragments are not already running...
                if (mActivity.isNetworkAvailable()) {
                    if (!mActivity.mHourly_forecast_fragment.mSwipeRefreshLayout.isRefreshing() && !mActivity.mDaily_forecast_fragment.mSwipeRefreshLayout.isRefreshing()) {
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
        //Start the swipe refresh layout on start up is internet available
        if (mActivity.isNetworkAvailable())
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    Log.d("TAG", "running swiping...");
                }
            });

        return v;
    }


    public void updateDisplay() {
        Current current = mActivity.mForecast.getCurrent();
        //setting the current weather details to the ui
        mTemperatureLabel.setText(current.getTemperature() + "");
        mTimeLabel.setText("At " + current.getFormattedTime() + " it is");
        mHumidityValue.setText(current.getHumidity() + "%");
        mPrecipValue.setText(current.getPrecipChange() + "%");
        mSummaryLabel.setText(current.getSummery());
        mWindSpeedValue.setText(current.getWindSpeed() + "");
        mFeelsLike.setText("Feels like: " + current.getFeelsLike());
        mLocationLabel.setText(mActivity.locationName);
        Drawable drawable = ContextCompat.getDrawable(mActivity, current.getIconId());
        mIconImageView.setImageDrawable(drawable);

        //animations
        YoYo.with(Techniques.FadeIn).duration(200).playOn(mLocationLabel);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(mTemperatureLabel);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(mIconImageView);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(mSummaryLabel);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(mHumidityValue);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(mWindSpeedValue);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(mFeelsLike);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(mPrecipValue);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(mTimeLabel);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }
}

