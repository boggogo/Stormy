package koemdzhiev.com.stormy;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
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

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;
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
    }

    private void getForecast(double latitude, double longitude) {
        YoYo.with(Techniques.FadeIn).duration(1800).playOn(mLocationLabel);
        YoYo.with(Techniques.FadeIn).duration(1600).playOn(mTemperatureLabel);
        YoYo.with(Techniques.FadeIn).duration(1800).playOn(mIconImageView);
        YoYo.with(Techniques.FadeIn).duration(1000).playOn(mSummaryLabel);
        YoYo.with(Techniques.FadeIn).duration(1200).playOn(mHumidityValue);
        YoYo.with(Techniques.FadeIn).duration(1400).playOn(mWindSpeedValue);
        YoYo.with(Techniques.FadeIn).duration(1200).playOn(mPrecipValue);
        YoYo.with(Techniques.FadeIn).duration(1200).playOn(mTimeLabel);

        String API_KEY = "3ed3a1906736c6f6c467606bd1f91e2c";
        //aberdeen, coordinates

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
                            mCurrentWeather = getCurrentDetails(jsonData);
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

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature()+"");
        mTimeLabel.setText("At "+mCurrentWeather.getFormattedTime()+" it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity() +"%");
        mPrecipValue.setText(mCurrentWeather.getPrecipChange()+"%");
        mSummaryLabel.setText(mCurrentWeather.getSummery());
        mWindSpeedValue.setText(mCurrentWeather.getWindSpeed()+"");
        mLocationLabel.setText(mCurrentWeather.getTimeZone());
        Drawable drawable = ContextCompat.getDrawable(this, mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);


    }

    //throws JSONException, doing it like that, we place the
    // responsability of handaling this exeption to the caller of the method
    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG,"From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");
        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChange(currently.getDouble("precipProbability"));
        currentWeather.setSummery(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);
        currentWeather.setWindSpeed(currently.getDouble("windSpeed"));

        Log.d(TAG,currentWeather.getFormattedTime());
        return currentWeather;
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


}
