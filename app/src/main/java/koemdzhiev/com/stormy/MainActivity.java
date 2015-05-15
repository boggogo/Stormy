package koemdzhiev.com.stormy;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //-----------MY CODE STARTS HERE------------------
        String API_KEY = "3ed3a1906736c6f6c467606bd1f91e2c";
        //aberdeen, coordinates
        double latitude = 57.1531;
        double longtitude = -2.0928;
        String forecast = "https://api.forecast.io/forecast/"+ API_KEY +"/"+ latitude+","+ longtitude;

        if(isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecast)
                    .build();

            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }
                //when the call to the Okhttp library finishes, than calls this method:
                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        //Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            //do sth with the forecast details
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
        Log.d(TAG, "Main UI code is running!");


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
