package koemdzhiev.com.stormy.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.adapters.DayAdapter;
import koemdzhiev.com.stormy.weather.Day;

/**
 * Created by koemdzhiev on 14/12/15.
 */
public class Daily_forecast_fragment extends Fragment {
    private Day[] mDays;
    @InjectView(android.R.id.list)
    ListView mListView;
    @InjectView(android.R.id.empty)
    TextView mEmptyTextView;
    private TextView mLocationLabel;
    private MainActivity mActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.daily_forecast_fragment,container,false);
        ButterKnife.inject(this, v);

        mLocationLabel = (TextView)v.findViewById(R.id.LocationLabel);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mActivity = ((MainActivity)getActivity());
    }

    public void setUpDailyFragment(){

        String location = mActivity.mCurrent_forecast_fragment.mLocationLabel.getText().toString();
        mLocationLabel.setText(location);

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
