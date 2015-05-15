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

import java.io.IOException;


public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String API_KEY = "3ed3a1906736c6f6c467606bd1f91e2c";
        double latitude = 37.8267;
        double longtitude = -122.423;
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

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        Log.v(TAG, response.body().string());
                        if (response.isSuccessful()) {

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught:", e);
                    }
                }
            });
        }else{
            //Toast.makeText(this,getString(R.string.network_unavailable_message),Toast.LENGTH_LONG).show();
            WIFIDialogFragment dialog = new WIFIDialogFragment();
            dialog.show(getFragmentManager(),getString(R.string.error_dialog_text));
        }
        Log.d(TAG, "Main UI code is running!");


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
