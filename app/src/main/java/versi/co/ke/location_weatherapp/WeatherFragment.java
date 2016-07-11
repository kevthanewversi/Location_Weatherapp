package versi.co.ke.location_weatherapp;

import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import versi.co.ke.location_weatherapp.Location_AsyncTask;
import versi.co.ke.location_weatherapp.R;

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
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    public WeatherFragment() {
        handler = new Handler();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //String city = "Nairobi";
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField =(TextView)rootView.findViewById(R.id.city_field);
        updatedField =(TextView)rootView.findViewById(R.id.updated_field);
        detailsField =(TextView)rootView.findViewById(R.id.details_field);
        TemperatureField =(TextView)rootView.findViewById(R.id.current_temperature_field);

        //updatetheWeather(city);

        //check if Build version is greater than API 22
        //before requesting runtime permissions



//            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1) {
//
//                if (ContextCompat.checkSelfPermission(MainActivity.this,
//                        Manifest.permission.ACCESS_FINE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//
//                    // Should we show an open_weather_app_IDn explanation?
//                    //returns true if user has not been shown dialog before
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
//                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Log.e("MM", "nAIROOOOOO");
//
//                        // Show an expanation to the user *asynchronously* -- don't block
//                        // this thread waiting for the user's response! After the user
//                        // sees the explanation, try again to request the permission.
//                        Snackbar snackbar = Snackbar.make(rootView, R.string.explanation, Snackbar.LENGTH_SHORT).
//                                setAction(R.string.request, new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        ActivityCompat.requestPermissions(MainActivity.this,
//                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//                                    }
//                                });
//                        snackbar.show();
//
//                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//
//
//                    } else {
//
//                        // No explanation needed, we can request the permission.
//                        Log.e("MM", "nAIROOOOOO1");
//                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//
//                        // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
//                        // app-defined int constant. The callback method gets the
//                        // result of the request.
//                    }
//                }
//            }
//
//            else {
//                try{String city = getcurrentLocation().toString();
//                    Log.e("MM",city);
//                    updatetheWeather(city);
//                }
//                catch(Exception e) {
//                    e.printStackTrace();
//                }
//            }

        return rootView;
    }


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

//                        try{
//                            String city = getcurrentLocation().toString();
//                            Log.e("MM",city);
//                            updatetheWeather(city);
//
//
//                        }
//
//                        catch(Exception e) {
//                            e.printStackTrace();
//                        }

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




    public  String   getcurrentLocation (Double longitude, Double latitude){

        String city = null;



        if (latitude == null){
            Log.e("kev", "LUV");}


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