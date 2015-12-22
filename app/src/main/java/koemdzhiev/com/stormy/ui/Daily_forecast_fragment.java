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
                    if (!mActivity.mCurrent_forecast_fragment.mSwipeRefreshLayout.isRefreshing() && !mActivity.mHourly_forecast_fragment.mSwipeRefreshLayout.isRefreshing()) {
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

        mAppVersion.setText("v "+version);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mActivity = ((MainActivity)getActivity());
    }

    public void setUpDailyFragment(){

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
