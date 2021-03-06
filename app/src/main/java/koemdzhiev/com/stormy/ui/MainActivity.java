package koemdzhiev.com.stormy.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.adapters.ViewPagerAdapter;
import koemdzhiev.com.stormy.ui.dialogs.AlertDIalogFragment;
import koemdzhiev.com.stormy.ui.dialogs.WIFIDialogFragment;
import koemdzhiev.com.stormy.weather.Current;
import koemdzhiev.com.stormy.weather.Day;
import koemdzhiev.com.stormy.weather.Forecast;
import koemdzhiev.com.stormy.weather.Hour;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements Callback {
    public static final String TAG = MainActivity.class.getSimpleName();
    public Forecast mForecast;
    //initiate coordinates to 0.0
    public double latitude = 0.0;
    public double longitude = 0.0;
    public boolean isFirstTimeLaunchingTheApp = true;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Current", "Hourly", "Daily"};
    int Numboftabs = 3;
    Current_forecast_fragment mCurrent_forecast_fragment;
    Hourly_forecast_fragment mHourly_forecast_fragment;
    Daily_forecast_fragment mDaily_forecast_fragment;
    String mCurrentTag;
    String mHourlyTag;
    String mDailyTag;
    LinearLayout mainActivityLayout;
    LocationRequest request;
    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
    ReactiveLocationProvider locationProvider;
    Observable<List<Address>> reverseGeocodeObservable;
    Subscription subscription;
    Subscription onlyFirstTimeSubscription;
    NotAbleToGetWeatherDataTask mNotAbleToGetWeatherDataTask = new NotAbleToGetWeatherDataTask();
    int numOfBackgroundUpdates = 0;
    String locationName = "";
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private LocationManager locationManager;
    private ScheduledFuture<?> mScheduledFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //-----------MY CODE STARTS HERE-----------------
        //check if the user previously has seen the whats new message...
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        if (sharedPref.getInt(getString(R.string.saved_if_whats_new_seen), 1) != 0){
            WhatsNewDialogCreator dialogCreator = new WhatsNewDialogCreator(this, sharedPref);
            dialogCreator.show();
        }

        locationProvider = new ReactiveLocationProvider(this);

        mainActivityLayout = (LinearLayout)findViewById(R.id.main_activity_layout);
        changeWindowTopColor();
        this.mCurrent_forecast_fragment = new Current_forecast_fragment();
        this.mCurrentTag = mCurrent_forecast_fragment.getTag();

        this.mHourly_forecast_fragment = new Hourly_forecast_fragment();
        this.mHourlyTag = mHourly_forecast_fragment.getTag();

        this.mDaily_forecast_fragment = new Daily_forecast_fragment();
        this.mDailyTag = mDaily_forecast_fragment.getTag();

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs, mCurrent_forecast_fragment,
                mHourly_forecast_fragment, mDaily_forecast_fragment);

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
                return ContextCompat.getColor(MainActivity.this, R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        if(isFirstTimeLaunchingTheApp) {
            Log.d(TAG, "onCreate getLocation");
            getLocation();
        }
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "OnDestroy Called!");
        if(subscription != null) {
            subscription.unsubscribe();
        }
        //cancel any left tasks to be done now...
        super.onDestroy();
    }

//dont kill the app on backpressed
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void getForecast(double latitude, double longitude) {
        //scedule no response from the server task...
        mScheduledFuture = exec.schedule(mNotAbleToGetWeatherDataTask,12, TimeUnit.SECONDS);

        Log.d(TAG, "getForecast initiated...");
        String API_KEY = "3ed3a1906736c6f6c467606bd1f91e2c";
        String forecast = "https://api.forecast.io/forecast/" + API_KEY + "/" + latitude + "," + longitude + "?units=si";

        if (isNetworkAvailable()) {
//            mCurrent_forecast_fragment.toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecast)
                    .build();

            Call call = client.newCall(request);

            call.enqueue(this);
        } else {
            toggleSwipeRefreshLayoutsOff();
            alertForNoInternet();
            Log.d(TAG, "Alert No Internet" + 220);
            //is there is no internet cancel the noResponseFromServer task
            Log.d(TAG, "No internet _ scheduledFuture is CANCELED");
            mScheduledFuture.cancel(true);
        }
    }

    public void toggleSwipeRefreshLayoutsOff() {
        if(mHourly_forecast_fragment.mSwipeRefreshLayout != null && mCurrent_forecast_fragment.mSwipeRefreshLayout !=null && mDaily_forecast_fragment.mSwipeRefreshLayout !=null) {
            mHourly_forecast_fragment.mSwipeRefreshLayout.setRefreshing(false);
            mCurrent_forecast_fragment.mSwipeRefreshLayout.setRefreshing(false);
            mDaily_forecast_fragment.mSwipeRefreshLayout.setRefreshing(false);
        }
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
            day.setSunriseTime(jsonDay.getLong("sunriseTime"));
            day.setSunsetTime(jsonDay.getLong("sunsetTime"));
            day.setWindSpeed(jsonDay.getDouble("windSpeed"));
            day.setPressure(jsonDay.getDouble("pressure"));

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
            hour.setWindSpeed(jsonHour.getDouble("windSpeed"));
            hour.setPressure(jsonHour.getDouble("pressure"));
            double visib = jsonHour.getDouble("visibility");
            Log.d(TAG,"VISIBILITY: " + visib);

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
        //convert the meters per second to km per hour and round up to 2 decimal places...
        double windSpeedInKMPH = Math.round(currently.getDouble("windSpeed") * 3.6);
        mCurrent.setWindSpeed(windSpeedInKMPH);
        Log.d(TAG, "Wind speed: " + windSpeedInKMPH);
        mCurrent.setFeelsLike(Math.round(currently.getDouble("apparentTemperature")));
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
        Log.d(TAG,"getLocation initiated...");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (isNetworkAvailable()) {
            //check if the if the location services are enabled
            if( !isLocationServicesEnabled()) {
                alertForNoLocationEnabled();
            }else {
                requestLocationPermission();
            }

        } else {
            alertForNoInternet();
            Log.d(TAG, "Alert No Internet" + 366);
        }
    }

    public void requestLocationPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getString(R.string.alert_title_no_location_permission))
                        .setMessage(getString(R.string.alert_message_no_location_permission))
                        .setPositiveButton(getString(R.string.alert_possitive_button_no_location_permission), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                            }
                        }).setNeutralButton(getString(R.string.alert_negative_button_no_location_permission), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showSnackbarForLocationPermission();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();


            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else{
//            PERMISSION IS PREVIOUSLY GRANDED
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    startLocationUpdates();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showSnackbarForLocationPermission();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showSnackbarForLocationPermission() {
        mCurrent_forecast_fragment.mSwipeRefreshLayout.setRefreshing(false);
        mCurrent_forecast_fragment.mLocationLabel.setText("Location request denied!");
        final RelativeLayout rootView = (RelativeLayout) findViewById(R.id.fragmentRoot);
        Snackbar.make(rootView, "Allow location permission", Snackbar.LENGTH_INDEFINITE).setAction("Allow", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrent_forecast_fragment.mSwipeRefreshLayout.setRefreshing(true);
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }).show();
    }

    private void startLocationUpdates() {
        LocationRequest oneTimeOnStartRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setInterval(0);
        onlyFirstTimeSubscription = locationProvider.getUpdatedLocation(oneTimeOnStartRequest)
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        Log.d(TAG, "Getting first location updates...");
                        MainActivity.this.latitude = location.getLatitude();
                        MainActivity.this.longitude = location.getLongitude();

                        reverseGeocodeObservable = locationProvider
                                .getReverseGeocodeObservable(Locale.ENGLISH, location.getLatitude(), location.getLongitude(), 1);
                        getLocationName();
                        //check, only on create get location calls getForecast...
                        if (isFirstTimeLaunchingTheApp) {
                            getForecast(latitude, longitude);
                        }

                        onlyFirstTimeSubscription.unsubscribe();

                    }
                });
        //start background updates
        if(request == null){
            request = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setSmallestDisplacement(900)
                    .setFastestInterval(1 * 60 * 1000)
                    .setInterval(60 * 60 * 1000);
            //subscribe for background location updates...
            subscription = locationProvider.getUpdatedLocation(request)
                    .subscribe(new Subscriber<Location>() {
                        @Override
                        public void onNext(Location location) { /*Handle the location updates*/
                            Log.d(TAG, "Getting Background updates...");
                            MainActivity.this.latitude = location.getLatitude();
                            MainActivity.this.longitude = location.getLongitude();
                            numOfBackgroundUpdates++;

                            reverseGeocodeObservable = locationProvider
                                    .getReverseGeocodeObservable(Locale.ENGLISH, location.getLatitude(), location.getLongitude(), 1);
                            getLocationName();

                        }

                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "OnError Background updates");
                        }
                    });
        }
    }

    public boolean isLocationServicesEnabled() {
        return (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    public void alertForNoLocationEnabled() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.network_not_found_title);  // network not found
        builder.setMessage(R.string.network_not_found_message); // Want to enable?
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                toggleSwipeRefreshLayoutsOff();
//                code that returns the user when he/she turns location on
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        long fortySecondsFromNow = System.currentTimeMillis() + 40 * 1000;
                        while ((System.currentTimeMillis() < fortySecondsFromNow)
                                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.setAction(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            startActivity(intent);
                            //Do what u want
                        }

                    }
                });
//                  end of the above code
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


    //external my method...
    public void getLocationName(){
        reverseGeocodeObservable
                .subscribeOn(Schedulers.io())// use I/O thread to query for addresses
                .observeOn(AndroidSchedulers.mainThread())// return result in main android thread to manipulate UI
                .subscribe(new Subscriber<List<Address>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG,"OnError Geocode");
                        locationName = sharedPref.getString(getString(R.string.location_name_on_error),"unknown");
                    }

                    @Override
                    public void onNext(List<Address> addresses) {
                        if(addresses.size() > 0) {
                            Address address = addresses.get(0);
                            Log.v(MainActivity.class.getSimpleName(),"Locality: "+address.getLocality() + ", CountryName " + address.getCountryName());
                            //check if cityName is null...
                            String cityName = "unknown";
                            if(address.getLocality() != null) {
                                cityName = address.getLocality();
                                //if it is, try to get the subAdmin Area instead...
                            }else if (address.getSubAdminArea() != null){
                                cityName = address.getSubAdminArea();
                            }
                            String countryName = address.getCountryName();
                            editor.putString(getString(R.string.location_name_on_error),cityName + ", " + countryName);
                            editor.apply();
                            locationName = getString(R.string.location_name, cityName, countryName);
                        }
                    }
                });
    }

    private void changeWindowTopColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.ColorPrimaryDark));
        }
    }

    private void alertForServerError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.server_error_title);
        builder.setMessage(R.string.no_response_from_server_message);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onFailure(Call call, IOException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleSwipeRefreshLayoutsOff();
            }
        });
        //on response from the server cansel the noResponseFromServer task
//on response from the server cansel the noResponseFromServer task
        Log.d(TAG,"OnFailure_ scheduledFuture is CANCELED");
        mScheduledFuture.cancel(true);
        alertUserAboutError();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleSwipeRefreshLayoutsOff();
            }
        });
        try {
            String jsonData = response.body().string();
            if (response.isSuccessful()) {
                mForecast = parseForecastDetails(jsonData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "isSuccessful - run on UNI threth (update display)...");
                        mCurrent_forecast_fragment.updateDisplay();
                        mHourly_forecast_fragment.setUpHourlyFragment();
                        mDaily_forecast_fragment.setUpDailyFragment();
                        toggleSwipeRefreshLayoutsOff();
                        //set the isFirstTime to true so that the next refresh wont get location
                        isFirstTimeLaunchingTheApp = false;

                    }
                });


            } else {
                alertUserAboutError();
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Exception caught:", e);
        }
        //on response from the server cansel the noResponseFromServer task
        Log.d(TAG,"OnResponse_ scheduledFuture is CANCELED");
        mScheduledFuture.cancel(true);
    }

    class NotAbleToGetWeatherDataTask implements Runnable {

        @Override
        public void run() {
            alertForServerError();
            toggleSwipeRefreshLayoutsOff();
        }
    }


}
