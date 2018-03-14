package kr.sysgen.taxi.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import fr.quentinklein.slt.LocationTracker;
//import fr.quentinklein.slt.TrackerSettings;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.MemberInfo;

/**
 * 실시간으로 현재 위치를 받아와 화면에서 보여준다.
 */
public class MyLocationMapActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String TAG = MyLocationMapActivity.class.getSimpleName();

    /**
     * Toolbar
     */
    private Toolbar toolbar;

    private GoogleMap mMap;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
//    protected Boolean mRequestingLocationUpdates = false;
    /**
     * Time when the location was updated represented as a String.
     */
//    protected String mLastUpdateTime;

//    private LocationTracker tracker;
    /**
     * 10 초마다
     * 100 미터 마다
     * GPS 정보를 받아 온다.
     * 집에 가는 내내 나의 경로를 부모님/선생님 에게 전달
     */
//    int seconds = 10;
//    int updateTime = 1000;
//    int distance = 10;

    private MemberInfo memberInfo;

    private ProgressDialog progressDialog;

    private final int PERMISSION_REQUEST_CODE = 0xAAAA;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        showProgress();
        setMemberInfo();
        setTollBar();
        setView();

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.floating_button:
                if(mCurrentLocation!=null && mGoogleApiClient.isConnected()) {
                    updateUI(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), memberInfo.getMemberName());
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.my_location:
                if (memberInfo != null) {
                    new GetMyLocation().execute(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false); // 회전 금지

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, BaseActivity.PERMISSION_REQUEST_CODE);

        }else {
            Log.i(TAG, "setMyLocationEnabled");
            mMap.setMyLocationEnabled(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
//        tracker.stopListening();
        super.onStop();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }else {
            LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient, mLocationRequest, this);
        }
    }
    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                // int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mCurrentLocation!= null && mGoogleApiClient.isConnected()) {
                updateUI(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), memberInfo.getMemberName());
                startLocationUpdates();
                Log.i(TAG, "onConnected");
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateUI(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), memberInfo.getMemberName());
        Log.i(TAG, "onLocationChanged");
    }

    private void updateUI(double currentLatitude, double currentLongitude, String userName){
//        Log.i(TAG, "currentLatitude: "+ currentLatitude + "/currentLongitude: " + currentLongitude + " /userName: " + userName);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(userName).append(" 님의 ").append(getString(R.string.maps_activity_title));
        toolbar.setTitle(stringBuilder.toString());

//        double radius = 100.0;
//        int strokeColor = getResources().getColor(R.color.map_stroke);
//        int shadeColor = getResources().getColor(R.color.map_shade);

        mMap.clear();
        LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title(getString(R.string.current_location)));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocation)
                .zoom(17)
                .tilt(40)
                .build();
//        CircleOptions circleOptions = new CircleOptions().center(currentLocation).radius(100.0).fillColor(shadeColor).strokeColor(strokeColor);
//        mMap.addCircle(circleOptions);
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 17.0f));

        progressDialog.dismiss();
    }

    private void showProgress(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
    }
    private void setMemberInfo(){
        Bundle bundle = getIntent().getBundleExtra("bundle");
        memberInfo = new MemberInfo();

        try {
            final String userName = bundle.getString(getString(R.string.mem_name));
            final String memIdx = bundle.getString(getString(R.string.mem_idx));
            memberInfo.setMemberIndex(memIdx);
            memberInfo.setMemberName(userName);
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }
    private void setTollBar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.icon_small_white_32);
            toolbar.setTitle(getString(R.string.maps_activity_title));
            setSupportActionBar(toolbar);
        } else {
            Log.i(TAG, "toolbar null");
        }
    }
    private void setView(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FloatingActionButton moveCurrentLocationButton = (FloatingActionButton)findViewById(R.id.floating_button);
        moveCurrentLocationButton.setOnClickListener(this);
    }

    private class GetMyLocation extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            double latitude = params[0];
            double longitude = params[1];

            return showCurrentAddress(latitude, longitude);
        }

        @Override
        protected void onPostExecute(String s) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MyLocationMapActivity.this);
            builder.setTitle("현재위치").setMessage(s).show();

            super.onPostExecute(s);
        }
        private String showCurrentAddress(double latitude, double longitude) {
            StringBuilder result = new StringBuilder();

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addressList = new ArrayList<>();
            try{
                addressList = geocoder.getFromLocation(latitude, longitude, 1);
            }catch(IOException e){
                e.printStackTrace();
            }

            if (geocoder.isPresent()) {
                Address returnAddress = addressList.get(0);

                String locality = returnAddress.getLocality();
                String city = returnAddress.getCountryName();
                String thorougfare = returnAddress.getThoroughfare();
                String subThorougfare = returnAddress.getSubThoroughfare();

                result.append(city).append(' ').append(locality).append(' ').append(thorougfare).append(' ').append(subThorougfare);
            }

            return result.toString();
        }
    }
}
