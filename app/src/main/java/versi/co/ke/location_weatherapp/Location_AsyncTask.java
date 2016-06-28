package versi.co.ke.location_weatherapp;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by root on 6/28/16.
 */
public class Location_AsyncTask  extends AsyncTask<String,String,String>{
    Activity activity;
    double longitude;
    double latitude;


    public Location_AsyncTask(Activity activity,double longitude,double latitude) {
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    //find the city from latitude and longitude
    @Override
    protected String doInBackground(String... strings) {
        String city = "";
        Geocoder geocoder = new Geocoder(activity, Locale.ENGLISH);

        try{
            List<Address> list = geocoder.getFromLocation(latitude,longitude,1);
            if (list !=null & list.size()>0){
                Address address = list.get(0);
                city = address.getLocality();
            }
        }

        catch (IOException e){
            e.printStackTrace();
        }
        return city;
    }

    @Override
    protected void onPostExecute(String city) {
        super.onPostExecute(city);

    }
}
