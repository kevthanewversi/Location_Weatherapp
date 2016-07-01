package versi.co.ke.location_weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
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


public class MainActivity extends ActionBarActivity {
    public static final String OPEN_WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new WeatherFragment(), "WEATHER")
                    .commit();

            //WeatherFragment weatherFragment = (WeatherFragment)getSupportFragmentManager().findFragmentByTag("WEATHER");
            //weatherFragment.
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

    //I preffered working with an in class fragment instead of an external fragment
    //for simplicity purposes
    public class WeatherFragment extends Fragment {

        //it's a support fragment(app.v4) to support older devices
        Handler handler;
        TextView cityField;
        TextView updatedField;
        TextView detailsField;
        TextView TemperatureField;
        //String city = "Nairobi";
        Double longitude,latitude;
        LocationListener locationListener;

        public WeatherFragment() {
            handler = new Handler();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //String city = "Nairobi";
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            cityField =(TextView)rootView.findViewById(R.id.city_field);
            updatedField =(TextView)rootView.findViewById(R.id.updated_field);
            detailsField =(TextView)rootView.findViewById(R.id.details_field);
            TemperatureField =(TextView)rootView.findViewById(R.id.current_temperature_field);

            //updatetheWeather(city);

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {


                // Should we show an explanation?
                //returns true if user has not been shown dialog before
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.e("MM","nAIROOOOOO");

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    

                } else {

                    // No explanation needed, we can request the permission.
                    Log.e("MM","nAIROOOOOO1");
                   requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }

            return rootView;
        }

//        public void onViewCreated(View view, Bundle savedInstanceState)
//        {
//
//
//
//        }

        //the callback method that checks if the user conformed to the app's ACCESS_FINE_LOCATION permission request
        //here you can a suitable action based on the whether the user granted the permission or not
        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               String permissions[], int[] grantResults) {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted, yay! Do the
                        // location-related task you need to do.

                        try{
                            String city = getcurrentLocation().toString();
                            Log.e("MM",city);
                            updatetheWeather(city);


                        }

                        catch(Exception e) {
                            e.printStackTrace();
                        }

                    } else {

                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return;
                }

                // other 'case' lines to check for other
                // permissions this app might request
            }
        }




        public String  getcurrentLocation (){

            String city = null;

            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,10,locationListener);
            final Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            longitude = myLocation.getLongitude();
            latitude = myLocation.getLatitude();

            ///call Async task to get city location
            Location_AsyncTask location_asyncTask = new Location_AsyncTask(getActivity(),longitude,latitude);
            location_asyncTask.execute();
            try {
                //get the city location from the asyncTask result
                city = location_asyncTask.get().toString();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }


            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    longitude = myLocation.getLongitude();
                    latitude = myLocation.getLatitude();
                }


                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };


            return city;
        }

        public void  updatetheWeather(final String  city) {
            //run this on a new thread to lighten the load on the main thread
            new Thread(){
                //UI thread skips 32 frames so it seems we need to do more of the work in background
                public void run(){
                    final JSONObject jObj = MainActivity.fetchJSON(getActivity(),city);
                    if(jObj == null){
                        handler.post(new Runnable(){
                            public void run(){
                                Toast.makeText(getActivity(),
                                        getActivity().getString(R.string.place_not_found),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        handler.post(new Runnable(){
                            public void run(){
                                displayWeather(jObj);
                            }
                        });
                    }
                }
            }.start();
        }

        public void displayWeather(JSONObject jObj) {
            try{
                cityField.setText( jObj.getString("name").toUpperCase(Locale.ENGLISH) + ", " +
                        jObj.getJSONObject("sys").getString("country"));
                //the weather tag is an array in the JSON file containing several elements (JSON objects)
                JSONObject weather = jObj.getJSONArray("weather").getJSONObject(0);
                JSONObject main = jObj.getJSONObject("main");
                detailsField.setText(weather.getString("description").toUpperCase(Locale.ENGLISH) + "\n"
                        + "Humidity : " + main.getString("humidity") + "%" + "\n" );

                TemperatureField.setText(String.format("%.2f",main.getDouble("temp")) + "C");
                //display the date weather info was last updated on
                DateFormat dateFormat = DateFormat.getDateTimeInstance();
                //jObj.getLong  fetches the date and timeinfo from the virtual json file
                String updatedOn = dateFormat.format(new Date(jObj.getLong("dt")*1000));
                //set the date and time info to a text field
                updatedField.setText(updatedOn);

            }
            catch(Exception e){

            }


        }
    }
}
