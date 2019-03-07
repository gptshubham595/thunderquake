package com.shubham.thunderquake;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;


public class QuakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Quake>> {
    ListView listView;
        private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?";
    TextView emptyView, emptyDesc;
    ImageView emptyImageView;
    ProgressBar progressBar;
    SwipeRefreshLayout mySwipeRefreshLayout;
    LoaderManager loaderManager;
    String myLat, myLon, myRadius;
    Dialog dialog;
    public static String uri_string;
    ConnectivityManager connMgr;
    NetworkInfo networkInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String themes = sharedPrefs.getString(
                getString(R.string.settings_themes_key),
                getString(R.string.settings_themes_default));
        switch (themes) {
            case "dark":
                setTheme(R.style.AppTheme);

                break;
            case "light":
                setTheme(R.style.AppTheme_Light);
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake);
        dialog = new Dialog(this);
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {

            dialog.setContentView(R.layout.instruction_dialog);

            dialog.show();

            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                    .putBoolean("isFirstRun", false).apply();
        }

        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.pbar);
        emptyView = (TextView) findViewById(R.id.empty_view);
        emptyDesc = (TextView) findViewById(R.id.empty_view_desc);
        emptyImageView = (ImageView) findViewById(R.id.empty_imageview);
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        listView.setEmptyView(emptyView);
        listView.setEmptyView(emptyImageView);
        listView.setEmptyView(emptyDesc);
        loaderManager = getLoaderManager();
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loaderManager.restartLoader(0, null, QuakeActivity.this);
                    }
                }
        );


        if (googleServicesAvailable()) {
            checkConnection();
            if (networkInfo != null && networkInfo.isConnected()) {
                loaderManager.initLoader(0, null, QuakeActivity.this);
            } else {
                progressBar.setVisibility(View.GONE);
                emptyView.setText(R.string.noInternet);
                emptyDesc.setText(R.string.emptyDesc);
                emptyImageView.setImageResource(R.drawable.no_connection);
            }

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Intent intent = new Intent(QuakeActivity.this, MapsActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(QuakeActivity.this, "No Internet Connection. Please Check your connectivity and try again.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkConnection()
    {
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
    }
    public void dismiss(View view) {
        dialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
		if (id == R.id.action_intro) {
            Intent settingsIntent = new Intent(this, DosDonts.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_contact) {
            Intent settingsIntent = new Intent(this, Contact.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_about) {
            Intent aboutIntent = new Intent(this, About.class);
            startActivity(aboutIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class QuakeLoader extends AsyncTaskLoader<ArrayList<Quake>> {
        String mUrl;

        public QuakeLoader(Context context, String url) {
            super(context);
            mUrl = url;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public ArrayList<Quake> loadInBackground() {
            return Utils.extractEarthquakes(mUrl);
        }
    }

    @Override
    public Loader<ArrayList<Quake>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        String location = sharedPrefs.getString(
                getString(R.string.settings_location_key),
                getString(R.string.settings_location_default));
        String speclocation = sharedPrefs.getString(
                getString(R.string.settings_speclocation_key),
                getString(R.string.settings_speclocation_default));
        String speccountlocation = sharedPrefs.getString(
                getString(R.string.settings_speccountlocation_key),
                getString(R.string.settings_speccountlocation_default));
        String limit = sharedPrefs.getString(
                getString(R.string.settings_limit_key),
                getString(R.string.settings_limit_default));
        switch (location) {
            case "all":
                myLat = "0";
                myLon = "0";
                myRadius = "180";
                break;
            case "asia":
                myLat = "34.047863";
                myLon = "100.619655";
                myRadius = "40";
                break;
            case "australia":
                myLat = "-25.274398";
                myLon = "133.77513599999997";
                myRadius = "19";
                break;
            case "africa":
                myLat = "-8.783195";
                myLon = "34.50852299999997";
                myRadius = "30";
                break;
            case "europe":
                myLat = "54.525961";
                myLon = "15.255119";
                myRadius = "30";
                break;
            case "north_america":
                myLat = "54.525961";
                myLon = "-105.255119";
                myRadius = "55";
                break;
            case "south_america":
                myLat = "-35.675147";
                myLon = "-71.54296899999997";
                myRadius = "40";
                break;
        }
        switch (speclocation) {
            case "None":
                if(speccountlocation.equals("None")){
                go(location);}else{
                    go3(location,speccountlocation);
                }
                break;
            case "Assam":
                myLat = "26.2006";
                myLon = "92.9376";
                myRadius = "4";
                break;
            case "Jammu Kashmir":
                myLat = "34.083656";
                myLon = "74.797371";
                myRadius = "6";
                break;
            case "Delhi":
                myLat = "28.7041";
                myLon = "77.1025";
                myRadius = "3";
                break;
            case "Maharastra":
                myLat = "18.5204";
                myLon = "7.8567";
                myRadius = "5";
                break;
            case "Tamil Nadu":
                myLat = "11.1271";
                myLon = "78.6569";
                myRadius = "5";
                break;
            case "Kerala":
                myLat = "10.8505";
                myLon = "76.2711";
                myRadius = "4";
                break;
            case "Kolkata":
                myLat = "22.5726";
                myLon = "88.3639";
                myRadius = "4";
                break;
            case "Bihar":
                myLat = "25.0961";
                myLon = "85.3131";
                myRadius = "5";
                break;

        }
        switch (speccountlocation) {
            case "None":
                if(speclocation.equals("None"))
                {go(location);}
                else{go2(location,speclocation);}
                break;

            case "Japan":
                myLat = "36.2048";
                myLon = "138.2529";
                myRadius = "10";
                break;
            case "Nepal":
                myLat = "28.3949";
                myLon = "84.1240";
                myRadius = "8";
                break;
            case "India":
                myLat = "20.5937";
                myLon = "78.9629";
                myRadius = "15";
                break;
            case "Ecuador":
                myLat = "1.8312";
                myLon = "78.1834";
                myRadius = "10";
                break;
            case "Philippines":
                myLat = "12.8797";
                myLon = "121.7740";
                myRadius = "20";
                break;
            case "Pakistan":
                myLat = "30.3753";
                myLon = "69.3451";
                myRadius = "15";
                break;
		    case "El Salvador":
                myLat = "13.7942";
                myLon = "88.8965";
                myRadius = "10";
                break;
            case "Mexico":
                myLat = "23.6345";
                myLon = "102.5528";
                myRadius = "15";
                break;
            case "Turkey":
                myLat = "38.9637";
                myLon = "35.2433";
                myRadius = "10";
                break;
            case "Indonesia":
                myLat = "0.7893";
                myLon = "113.9213";
                myRadius = "19";
                break;
        }
        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("minmagnitude", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);
        uriBuilder.appendQueryParameter("latitude", myLat);
        uriBuilder.appendQueryParameter("longitude", myLon);
        uriBuilder.appendQueryParameter("maxradius", myRadius);
        uriBuilder.appendQueryParameter("limit", limit);
        Log.i("URL", uriBuilder.toString());
        uri_string = uriBuilder.toString();
        return new QuakeLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Quake>> loader, ArrayList<Quake> earthquakes) {
        emptyView.setText(R.string.emptyText);
        emptyDesc.setText(R.string.emptyview_desc);
        mySwipeRefreshLayout.setRefreshing(false);
        emptyImageView.setImageResource(R.drawable.happy_earth);
        if (earthquakes != null && !earthquakes.isEmpty()) {
            updateUi(earthquakes);
            emptyImageView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Quake>> loader) {
    }

private void go(String location){
    switch (location) {
        case "all":
            myLat = "0";
            myLon = "0";
            myRadius = "180";
            break;
        case "asia":
            myLat = "34.047863";
            myLon = "100.619655";
            myRadius = "40";
            break;
        case "australia":
            myLat = "-25.274398";
            myLon = "133.77513599999997";
            myRadius = "19";
            break;
        case "africa":
            myLat = "-8.783195";
            myLon = "34.50852299999997";
            myRadius = "30";
            break;
        case "europe":
            myLat = "54.525961";
            myLon = "15.255119";
            myRadius = "30";
            break;
        case "north_america":
            myLat = "54.525961";
            myLon = "-105.255119";
            myRadius = "55";
            break;
        case "south_america":
            myLat = "-35.675147";
            myLon = "-71.54296899999997";
            myRadius = "40";
            break;
    }

}
    private void go2(String location,String speclocation){
        switch (speclocation) {
            case "None":
                go(location);
                break;
            case "Assam":
                myLat = "26.2006";
                myLon = "92.9376";
                myRadius = "4";
                break;
            case "Jammu Kashmir":
                myLat = "34.083656";
                myLon = "74.797371";
                myRadius = "6";
                break;
            case "Delhi":
                myLat = "28.7041";
                myLon = "77.1025";
                myRadius = "3";
                break;
            case "Maharastra":
                myLat = "18.5204";
                myLon = "7.8567";
                myRadius = "5";
                break;
            case "Tamil Nadu":
                myLat = "11.1271";
                myLon = "78.6569";
                myRadius = "5";
                break;
            case "Kerala":
                myLat = "10.8505";
                myLon = "76.2711";
                myRadius = "4";
                break;
            case "Kolkata":
                myLat = "22.5726";
                myLon = "88.3639";
                myRadius = "4";
                break;
            case "Bihar":
                myLat = "25.0961";
                myLon = "85.3131";
                myRadius = "5";
                break;

        }

    }
    private void go3(String location,String speccountlocation){
        switch (speccountlocation) {
            case "None":
                go(location);
                break;

            case "Japan":
                myLat = "36.2048";
                myLon = "138.2529";
                myRadius = "10";
                break;
            case "Nepal":
                myLat = "28.3949";
                myLon = "84.1240";
                myRadius = "8";
                break;
            case "India":
                myLat = "20.5937";
                myLon = "78.9629";
                myRadius = "15";
                break;
            case "Ecuador":
                myLat = "1.8312";
                myLon = "78.1834";
                myRadius = "10";
                break;
            case "Philippines":
                myLat = "12.8797";
                myLon = "121.7740";
                myRadius = "20";
                break;
            case "Pakistan":
                myLat = "30.3753";
                myLon = "69.3451";
                myRadius = "15";
                break;
            case "El Salvador":
                myLat = "13.7942";
                myLon = "88.8965";
                myRadius = "10";
                break;
            case "Mexico":
                myLat = "23.6345";
                myLon = "102.5528";
                myRadius = "15";
                break;
            case "Turkey":
                myLat = "38.9637";
                myLon = "35.2433";
                myRadius = "10";
                break;
            case "Indonesia":
                myLat = "0.7893";
                myLon = "113.9213";
                myRadius = "19";
                break;
        }
    }
    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot connect to Google Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void updateUi(ArrayList<Quake> quakeArrayList) {

        final QuakeAdapter quakeAdapter = new QuakeAdapter(this, quakeArrayList);
        listView.setAdapter(quakeAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Quake currentQuake = quakeAdapter.getItem(position);
                Intent intent = new Intent(QuakeActivity.this, QuakeDetailsActivity.class);
                intent.putExtra("title", currentQuake.getTitle());
                intent.putExtra("mag", currentQuake.getMagnitude());
                intent.putExtra("date", currentQuake.getDate());
                intent.putExtra("latitude", currentQuake.getLatitude());
                intent.putExtra("longitude", currentQuake.getLongitude());
                intent.putExtra("depth", currentQuake.getDepth());
                intent.putExtra("felt", currentQuake.getFelt());
                startActivity(intent);
            }
        });
    }
}
