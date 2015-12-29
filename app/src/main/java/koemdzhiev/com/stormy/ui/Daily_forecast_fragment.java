package koemdzhiev.com.stormy.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import koemdzhiev.com.stormy.adapters.DayAdapter;
import koemdzhiev.com.stormy.weather.Day;

/**
 * Created by koemdzhiev on 14/12/15.
 */
public class Daily_forecast_fragment extends Fragment {
    private Day[] mDays;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    ListView mListView;
    TextView mEmptyTextView;
    TextView mDeveloperEmail;
    TextView mAppVersion;
    private MainActivity mActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.daily_forecast_fragment,container,false);
        mListView = (ListView)v.findViewById(android.R.id.list);
        mEmptyTextView = (TextView)v.findViewById(android.R.id.empty);
        mAppVersion = (TextView)v.findViewById(R.id.appVersion);
        mSwipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.daily_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.blue, R.color.orange);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //if there is internet and if the mSwipeRefreshLayout in the current and hourly fragments are not already running...
                if (mActivity.isNetworkAvailable()) {
                    if (!mActivity.mHourly_forecast_fragment.mSwipeRefreshLayout.isRefreshing() && !mActivity.mCurrent_forecast_fragment.mSwipeRefreshLayout.isRefreshing()) {
                        if (mActivity.isLocationServicesEnabled()) {
                            if (mActivity.latitude != 0.0 && mActivity.longitude != 0.0) {
                                mActivity.getForecast(mActivity.latitude, mActivity.longitude);
                            } else {
                                mActivity.getLocation();
                            }
                        }else{
                            mActivity.alertForNoLocationEnabled();
                        }
                    }else{
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(mActivity, "currently refreshing...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mActivity, "No Internet Connection!", Toast.LENGTH_LONG).show();
                    mActivity.toggleSwipeRefreshLayoutsOff();
                }
            }
        });
        PackageInfo pInfo = null;
        try {
            pInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final String version = pInfo.versionName;
        mDeveloperEmail = (TextView)v.findViewById(R.id.developer_email);
        mDeveloperEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:koemdjiev@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + ", version: " + version);

                startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
            }
        });

        mAppVersion.setText("v " + version);
        mAppVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mActivity, "Number of background updates: " + mActivity.numOfBackgroundUpdates, Toast.LENGTH_LONG).show();
            }
        });
        mAppVersion.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mActivity.numOfBackgroundUpdates = 0;
                Toast.makeText(mActivity,"numberOfUpdates reseted to 0",Toast.LENGTH_LONG).show();
                return true;
            }
        });
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mActivity = ((MainActivity)getActivity());
    }

    public void setUpDailyFragment(){
//set to null to reset the old one and set a new adapter bellow...
        mListView.setAdapter(null);
        Day[] myDayArr = mActivity.mForecast.getDailyForecast();
        mDays = Arrays.copyOf(myDayArr, myDayArr.length, Day[].class);

        DayAdapter adapter = new DayAdapter(mActivity,mDays);
        mListView.setAdapter(adapter);
        mListView.setEmptyView(mEmptyTextView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dayOfTheWeek = mDays[position].getDayOfTheWeek();
                String condition = mDays[position].getSummary();
                String highTemp = mDays[position].getTemperatureMax() + "";
                String massage = String.format("On %s the high will be %s and it will be %s", dayOfTheWeek, highTemp, condition);
                Toast.makeText(mActivity, massage, Toast.LENGTH_LONG).show();
                //play animations
                YoYo.with(Techniques.Shake).duration(200).playOn(view);

            }
        });
    }
}
