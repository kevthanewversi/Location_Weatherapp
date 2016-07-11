package versi.co.ke.location_weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String OPEN_WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=";

    GoogleApiClient mGoogleApiClient;
    WeatherFragment weatherFragment;
    Double latitude,longitude;
    Location myLocation;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container,new WeatherFragment(), "WEATHER")
                    .commit();

            //WeatherFragment weatherFragment = (WeatherFragment)getSupportFragmentManager().findFragmentByTag("WEATHER");
            //weatherFragment.
            weatherFragment = (WeatherFragment)getSupportFragmentManager().findFragmentById(R.id.frag_weather);

            if (weatherFragment == null)
            {
                Log.e("VVV","VVVVV");
            }
        }
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }




    }

    public static JSONObject fetchJSON( Context context,String city){
        JSONObject data = new JSONObject();

        try{
            //append location info to url
            URL url = new URL (new StringBuilder(OPEN_WEATHER_URL).append(city).toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //add api key to url
            conn.addRequestProperty("x-api-key",
                    context.getString(R.string.open_weather_app_ID));
            //get input stream of adata form url
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            StringBuffer buffer = new StringBuffer();
            String temp = " ";

            //loop through the reader while adding the data to a string
            while((temp = reader.readLine())  != null){
                buffer.append(temp).append("\n");
                //close the buffered reader
                //reader.close();
            }
            data = new JSONObject(buffer.toString());
            //supposed to return 200 as all successful GET requests do
            if (data.getInt("cod") != 200){
                return null;
            }

        }



        catch(Exception e){
            e.printStackTrace();
        }
        return data;
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try{
        myLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);}

        catch (Exception e ){
            e.printStackTrace();
        }

        if (myLocation != null) {
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();
            Log.e(latitude.toString(),longitude.toString());
           weatherFragment.getcurrentLocation(longitude,latitude);
        }

        else{
            Log.e("Bbbb","0000");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
