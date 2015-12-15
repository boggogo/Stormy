package koemdzhiev.com.stormy.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.adapters.HourAdapter;
import koemdzhiev.com.stormy.weather.Hour;

/**
 * Created by koemdzhiev on 14/12/15.
 */
public class Hourly_forecast_fragment extends Fragment {
    private Hour[] mHours;
    private MainActivity mActivity;
    //inject the RecyclerView as member variable
    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = ((MainActivity) getActivity());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.hourly_forecast_fragment,container,false);

        ButterKnife.inject(this, v);

        return v;
    }

    public void setUpHourlyFragment(){
        if (mActivity.mForecast != null) {
            Hour[] hourlyForecast = mActivity.mForecast.getHourlyForecast();
            mHours = Arrays.copyOf(hourlyForecast, hourlyForecast.length, Hour[].class);
        }
        HourAdapter adapter = new HourAdapter(mActivity,mHours);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mRecyclerView.setLayoutManager(layoutManager);
        //if dealing with fixed size data, it is recommended to do the following...
        mRecyclerView.setHasFixedSize(true);
    }
}

