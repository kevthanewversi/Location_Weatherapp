package versi.co.ke.location_weatherapp;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {
    public static final String OPEN_WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    String city = "Nairobi";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new WeatherFragment(),"WEATHER")
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class WeatherFragment extends Fragment {

        Handler handler;
        TextView cityField;
        TextView updatedField;
        TextView detailsField;
        TextView currentTemperatureField;
        TextView weatherIcon;

        public WeatherFragment() {
            handler = new Handler();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            String city = "Nairobi";
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            cityField =(TextView)rootView.findViewById(R.id.city_field);
            updatedField =(TextView)rootView.findViewById(R.id.updated_field);
            detailsField =(TextView)rootView.findViewById(R.id.details_field);
            currentTemperatureField =(TextView)rootView.findViewById(R.id.current_temperature_field);
            weatherIcon =(TextView)rootView.findViewById(R.id.weather_icon);
            updatetheWeather(city);

            return rootView;
        }

        public void  updatetheWeather(final String  city) {
            //run this on a new thread to lighten the load on the main thread
            //UI thread skips 32 frames so it seems we need to do more of the work in background
            new Thread(){
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

                JSONObject weather = jObj.getJSONArray("weather").getJSONObject(0);
                JSONObject main = jObj.getJSONObject("main");
                detailsField.setText(weather.getString("description").toUpperCase(Locale.ENGLISH) + "\n"
                        + "Humidity : " + main.getString("humidity") + "%" + "\n" );

                currentTemperatureField.setText(String.format("%.2f",main.getDouble("temp")) + "C");
                //display date weather info was last updated on
                DateFormat dateFormat = DateFormat.getDateTimeInstance();
                String updatedOn = dateFormat.format(new Date(jObj.getLong("dt")*1000));
                updatedField.setText(updatedOn);

                //display weather icon too



            }
            catch(Exception e){

            }


        }
    }
}
