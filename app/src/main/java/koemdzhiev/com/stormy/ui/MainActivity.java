package koemdzhiev.com.stormy.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

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

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.adapters.ViewPagerAdapter;
import koemdzhiev.com.stormy.weather.Current;
import koemdzhiev.com.stormy.weather.Day;
import koemdzhiev.com.stormy.weather.Forecast;
import koemdzhiev.com.stormy.weather.Hour;


public class MainActivity extends AppCompatActivity {
    private static final int TURN_ON_LOCATION_REQUEST = 1;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Current","Hourly","Daily"};
    int Numboftabs =3;
    Current_forecast_fragment mCurrent_forecast_fragment;
    Hourly_forecast_fragment mHourly_forecast_fragment;
    Daily_forecast_fragment mDaily_forecast_fragment;

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String LOCATION_KEY = "location_key";
    public Forecast mForecast;
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    //default coordinates - Gotse Delchev, UK Lati:57.156866 ; Long:
    private double latitude = 41.5667;
    private double longitude = 23.7333;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //-----------MY CODE STARTS HERE-----------------
        changeWindowTopColor();
        this.mCurrent_forecast_fragment = new Current_forecast_fragment();
        this.mHourly_forecast_fragment = new Hourly_forecast_fragment();
        this.mDaily_forecast_fragment = new Daily_forecast_fragment();
        getLocation();

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs,mCurrent_forecast_fragment,
                mHourly_forecast_fragment,mDaily_forecast_fragment);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(MainActivity.this,R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);


    }

    private void getForecast(double latitude, double longitude) {
        String API_KEY = "3ed3a1906736c6f6c467606bd1f91e2c";
        String forecast = "https://api.forecast.io/forecast/" + API_KEY + "/" + latitude + "," + longitude + "?units=auto";

        if (isNetworkAvailable()) {
//            mCurrent_forecast_fragment.toggleRefresh();

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
//                            mCurrent_forecast_fragment.toggleRefresh();
                            toggleSwipeRefreshLayoutsOff();
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
//                            mCurrent_forecast_fragment.toggleRefresh();
                            toggleSwipeRefreshLayoutsOff();
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
                                  mCurrent_forecast_fragment.updateDisplay();
                                    mHourly_forecast_fragment.setUpHourlyFragment();
                                    mDaily_forecast_fragment.setUpDailyFragment();

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
        } else {
//            mCurrent_forecast_fragment.toggleRefresh();
            toggleSwipeRefreshLayoutsOff();
//            Toast.makeText(this,getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
            alertForNoInternet();
        }
    }

    private void toggleSwipeRefreshLayoutsOff() {
        mHourly_forecast_fragment.mSwipeRefreshLayout.setRefreshing(false);
        mCurrent_forecast_fragment.mSwipeRefreshLayout.setRefreshing(false);
        mDaily_forecast_fragment.mSwipeRefreshLayout.setRefreshing(false);
    }

    public void alertForNoInternet() {
        WIFIDialogFragment dialog = new WIFIDialogFragment();
        dialog.show(getFragmentManager(), getString(R.string.error_dialog_text));
    }


    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();
        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);

            days[i] = day;

            Log.v(MainActivity.class.getSimpleName(), days[i].getIcon());
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for (int i = 0; i < data.length(); i++) {
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
    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

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

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        //contition to check if there is a network and if the device is connected
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDIalogFragment dialog = new AlertDIalogFragment();
        dialog.show(getFragmentManager(), getString(R.string.error_dialog_text));
    }



    //------------------------- MY EXTERNAL CODE BELLOW-------------------------------------------
    public void getLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (isNetworkAvailable()) {
//            mCurrent_forecast_fragment.toggleRefresh();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //check if the if the location services are enabled
            if( !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                alertForNoLocationEnabled();
            }else {

                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0, 0, new MyLocationListener());
            }

        } else {
            alertForNoInternet();
        }
    }

    private void alertForNoLocationEnabled() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.network_not_found_title);  // network not found
        builder.setMessage(R.string.network_not_found_message); // Want to enable?
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                toggleSwipeRefreshLayoutsOff();
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                toggleSwipeRefreshLayoutsOff();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            //stop listening to location updates after setting the latitude and lonitude
            getForecast(latitude, longitude);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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
    public void getLocationName(){
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
                    mCurrent_forecast_fragment.mLocationLabel.setText(addressList.get(0).getLocality() + ", " + addressList.get(0).getCountryName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //

    private void changeWindowTopColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.ColorPrimaryDark));
        }
    }

}
