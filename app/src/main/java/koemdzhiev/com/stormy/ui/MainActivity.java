package koemdzhiev.com.stormy.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.location.LocationRequest;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.adapters.ViewPagerAdapter;
import koemdzhiev.com.stormy.weather.Current;
import koemdzhiev.com.stormy.weather.Day;
import koemdzhiev.com.stormy.weather.Forecast;
import koemdzhiev.com.stormy.weather.Hour;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Current", "Hourly", "Daily"};
    int Numboftabs = 3;
    Current_forecast_fragment mCurrent_forecast_fragment;
    Hourly_forecast_fragment mHourly_forecast_fragment;
    Daily_forecast_fragment mDaily_forecast_fragment;

    public static final String TAG = MainActivity.class.getSimpleName();
    public Forecast mForecast;
    //initiate coordinates to 0.0
    public double latitude = 0.0;
    public double longitude = 0.0;
    private LocationManager locationManager;
    public boolean isFirstTimeLaunchingTheApp = true;
    LinearLayout mainActivityLayout;
    LocationRequest request;
    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture<?> mScheduledFuture;
    ReactiveLocationProvider locationProvider;
    Observable<List<Address>> reverseGeocodeObservable;
    Subscription subscription;
    Subscription onlyFirstTimeSubscription;
    NotAbleToGetWeatherDataTask mNotAbleToGetWeatherDataTask = new NotAbleToGetWeatherDataTask();
    int numOfBackgroundUpdates = 0;
    String locationName = "";
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

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

        request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setSmallestDisplacement(900)
                .setFastestInterval(1 * 60 * 1000)
                .setInterval(60 * 60 * 1000);
        locationProvider = new ReactiveLocationProvider(this);
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
                                .getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);
                        getLocationName();

                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG,"OnError Background updates");
                    }
                });

        mainActivityLayout = (LinearLayout)findViewById(R.id.main_activity_layout);
        changeWindowTopColor();
        this.mCurrent_forecast_fragment = new Current_forecast_fragment();
        this.mHourly_forecast_fragment = new Hourly_forecast_fragment();
        this.mDaily_forecast_fragment = new Daily_forecast_fragment();

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
        subscription.unsubscribe();
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
                            toggleSwipeRefreshLayoutsOff();
                        }
                    });
                    //on response from the server cansel the noResponseFromServer task
//on response from the server cansel the noResponseFromServer task
                    Log.d(TAG,"OnFailure_ scheduledFuture is CANCELED");
                    mScheduledFuture.cancel(true);
                    alertUserAboutError();
                }

                //when the call to the Okhttp library finishes, than calls this method:
                @Override
                public void onResponse(Response response) throws IOException {
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
            });
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
        //convert the meters per second to km per hour and round up to 2 decimal places...
        NumberFormat formatter = new DecimalFormat("#0.00");
        double windSpeedInKMPH = Math.round(currently.getDouble("windSpeed") * 3.6);
        mCurrent.setWindSpeed(windSpeedInKMPH);
        Log.d(TAG, "Wind speed: " + windSpeedInKMPH);
        mCurrent.setFeelsLike(currently.getDouble("apparentTemperature"));
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
                                .getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);
                        getLocationName();
                        //check, only on create get location calls getForecast...
                        if (isFirstTimeLaunchingTheApp) {
                            getForecast(latitude, longitude);
                        }

                        onlyFirstTimeSubscription.unsubscribe();

                    }
                });

            }

        } else {
            alertForNoInternet();
            Log.d(TAG, "Alert No Internet" + 366);
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
                .subscribeOn(Schedulers.io())               // use I/O thread to query for addresses
                .observeOn(AndroidSchedulers.mainThread())// return result in main android thread to manipulate UI
                .subscribe(new Subscriber<List<Address>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG,"OnError Geocode");
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

    class NotAbleToGetWeatherDataTask implements Runnable {

        @Override
        public void run() {
            alertForServerError();
            toggleSwipeRefreshLayoutsOff();
        }
    }

    private void alertForServerError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.server_error_title);
        builder.setMessage(R.string.no_response_from_server_message);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
