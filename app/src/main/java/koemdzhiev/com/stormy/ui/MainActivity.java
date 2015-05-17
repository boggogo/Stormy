package koemdzhiev.com.stormy.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.weather.Current;


public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private Current mCurrent;
    //default coordinates - Aberdeen, UK
    private double latitude = 57.149717;
    private double longitude = -2.094278;

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
        getCurrentLocation();


        mRefreshImaveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
                getForecast(latitude,longitude);
            }
        });

        getForecast(latitude,longitude);
        Log.d(TAG, "Main UI code is running!");

    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentLocation();
        getForecast(latitude,longitude);
    }

    private void getCurrentLocation() {
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d(MainActivity.class.getSimpleName(), "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);
        myLocation.cancelTimer();
    }

    private void getForecast(double latitude, double longitude) {
        //animations
        YoYo.with(Techniques.FadeIn).duration(1800).playOn(mLocationLabel);
        YoYo.with(Techniques.FadeIn).duration(1600).playOn(mTemperatureLabel);
        YoYo.with(Techniques.FadeIn).duration(1800).playOn(mIconImageView);
        YoYo.with(Techniques.FadeIn).duration(1000).playOn(mSummaryLabel);
        YoYo.with(Techniques.FadeIn).duration(1200).playOn(mHumidityValue);
        YoYo.with(Techniques.FadeIn).duration(1400).playOn(mWindSpeedValue);
        YoYo.with(Techniques.FadeIn).duration(1200).playOn(mPrecipValue);
        YoYo.with(Techniques.FadeIn).duration(1200).playOn(mTimeLabel);

        String API_KEY = "3ed3a1906736c6f6c467606bd1f91e2c";
        String forecast = "https://api.forecast.io/forecast/"+ API_KEY +"/"+ latitude+","+ longitude+"?units=auto";

        if(isNetworkAvailable()) {
            toggleRefresh();

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
                            mCurrent = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught:", e);
                    }
                    catch (JSONException e){
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
        mTemperatureLabel.setText(mCurrent.getTemperature()+"");
        mTimeLabel.setText("At "+ mCurrent.getFormattedTime()+" it will be");
        mHumidityValue.setText(mCurrent.getHumidity() +"%");
        mPrecipValue.setText(mCurrent.getPrecipChange()+"%");
        mSummaryLabel.setText(mCurrent.getSummery());
        mWindSpeedValue.setText(mCurrent.getWindSpeed()+"");
        mLocationLabel.setText(mCurrent.getTimeZone());
        getLocationName();
        Drawable drawable = ContextCompat.getDrawable(this, mCurrent.getIconId());
        mIconImageView.setImageDrawable(drawable);


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

    private void getLocationName(){
        Geocoder geo = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geo.getFromLocation(this.latitude,this.longitude,1);
            if (addressList.isEmpty()){
                //gets the default name from the timeZone
                //that we set in as a local variable
            }else{
                if(addressList.size() > 0){
                    Log.v(MainActivity.class.getSimpleName(),addressList.get(0).getLocality() + ", "+ addressList.get(0).getCountryName()+"");
                    mLocationLabel.setText(addressList.get(0).getLocality() + ", "+ addressList.get(0).getCountryName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
