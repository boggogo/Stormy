package koemdzhiev.com.stormy.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.weather.Current;
import koemdzhiev.com.stormy.weather.Day;
import koemdzhiev.com.stormy.weather.Forecast;
import koemdzhiev.com.stormy.weather.Hour;


public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private Forecast mForecast;
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    //default coordinates - Aberdeen, UK Lati:57.156866 ; Long:
    private double latitude = 57.1526;
    private double longitude = -2.1100;
    private LocationManager locationManager;

    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
    @InjectView(R.id.locationLabel) TextView mLocationLabel;
    @InjectView(R.id.windSpeedValue) TextView mWindSpeedValue;
    @InjectView(R.id.iconImageView) ImageView mIconImageView;
    @InjectView(R.id.refreshImageView) ImageView mRefreshImaveView;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //-----------MY CODE STARTS HERE-----------------
        ButterKnife.inject(this);
        mProgressBar.setVisibility(View.INVISIBLE);
        mRefreshImaveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getForecast(latitude, longitude);
                getLocation();
            }
        });
        //getForecast(latitude, longitude);
        getLocation();

    }

    private void getForecast(double latitude, double longitude) {
        String API_KEY = "3ed3a1906736c6f6c467606bd1f91e2c";
        String forecast = "https://api.forecast.io/forecast/"+ API_KEY +"/"+ latitude+","+ longitude+"?units=auto";

        if(isNetworkAvailable()) {
            //toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecast)
                    .build();

            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }
                //when the call to the Okhttp library finishes, than calls this method:
                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        //Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                           mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Exception caught:", e);
                    }
                }
            });
        }else{
            //Toast.makeText(this,getString(R.string.network_unavailable_message),Toast.LENGTH_LONG).show();
            WIFIDialogFragment dialog = new WIFIDialogFragment();
            dialog.show(getFragmentManager(), getString(R.string.error_dialog_text));
        }
    }

    private void toggleRefresh() {
        if(mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImaveView.setVisibility(View.INVISIBLE);
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImaveView.setVisibility(View.VISIBLE);
        }
    }
    //updates the dysplay with the data in the CUrrentWeather locaal object
    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        //setting the current weather details to the ui
        mTemperatureLabel.setText(current.getTemperature()+"");
        mTimeLabel.setText("At "+ current.getFormattedTime()+" it will be");
        mHumidityValue.setText(current.getHumidity() +"%");
        mPrecipValue.setText(current.getPrecipChange()+"%");
        mSummaryLabel.setText(current.getSummery());
        mWindSpeedValue.setText(current.getWindSpeed()+"");
        mLocationLabel.setText(current.getTimeZone());
        getLocationName();
        Drawable drawable = ContextCompat.getDrawable(this, current.getIconId());
        mIconImageView.setImageDrawable(drawable);
        //-
        //animations
        YoYo.with(Techniques.FadeIn).duration(300).playOn(mLocationLabel);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(mTemperatureLabel);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(mIconImageView);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(mSummaryLabel);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(mHumidityValue);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(mWindSpeedValue);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(mPrecipValue);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(mTimeLabel);


    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();
        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for(int i = 0;i < data.length();i++){
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);

            days[i] = day;

            Log.v(MainActivity.class.getSimpleName(),days[i].getIcon());
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[]hours = new Hour[data.length()];

        for(int i = 0;i < data.length();i++){
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);

            hours[i] = hour;
        }

        return hours;
    }

    /*
     * throws JSONException, doing it like that, we place the
     * responsability of handaling this exeption to the caller of the method
    */
    private Current getCurrentDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG,"From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");
        Current mCurrent = new Current();
        mCurrent.setHumidity(currently.getDouble("humidity"));
        mCurrent.setTime(currently.getLong("time"));
        mCurrent.setIcon(currently.getString("icon"));
        mCurrent.setPrecipChange(currently.getDouble("precipProbability"));
        mCurrent.setSummery(currently.getString("summary"));
        mCurrent.setTemperature(currently.getDouble("temperature"));
        mCurrent.setTimeZone(timezone);
        mCurrent.setWindSpeed(currently.getDouble("windSpeed"));

        Log.d(TAG, mCurrent.getFormattedTime());
        return mCurrent;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        //contition to check if there is a network and if the device is connected
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDIalogFragment dialog = new AlertDIalogFragment();
        dialog.show(getFragmentManager(),getString(R.string.error_dialog_text));
    }
    //using butter knife to inject onClick listener for the two buttons
    @OnClick(R.id.dailyButton)
    public void startDailyActivity(View view){
        Intent intent = new Intent(this,DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST,mForecast.getDailyForecast());
        startActivity(intent);
    }

    @OnClick(R.id.hourlyButton)
    public void startHourlyActivity(View v){
        Intent intent = new Intent(this,HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST,mForecast.getHourlyForecast());
        startActivity(intent);
    }


//------------------------- MY EXTERNAL CODE BELLOW-------------------------------------------
private void getLocation(){
    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    toggleRefresh();
    if(isNetworkAvailable()){

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000, 1000, new MyLocationListener());
        //toggleRefresh();

    }else{
        WIFIDialogFragment dialog = new WIFIDialogFragment();
        dialog.show(getFragmentManager(), getString(R.string.error_dialog_text));
        toggleRefresh();
    }

}
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            //stop listening to location updates after setting the latitude and lonitude
            getForecast(latitude, longitude);
            locationManager.removeUpdates(this);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    //external my method...
    private void getLocationName(){
        Log.i(TAG,"Lattitude: " + latitude + " | " + "Longitude" + longitude);
        Geocoder geo = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geo.getFromLocation(this.latitude,this.longitude,1);
            if (addressList.isEmpty()){
                //gets the default name from the timeZone
                //that we set in as a local variable
            }else{
                if(addressList.size() > 0){
                    Log.v(MainActivity.class.getSimpleName(), addressList.get(0).getLocality() + ", " + addressList.get(0).getCountryName() + "");
                    mLocationLabel.setText(addressList.get(0).getLocality() + ", " + addressList.get(0).getCountryName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
