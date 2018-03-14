package kr.sysgen.taxi.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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

import org.json.JSONException;
import org.json.JSONObject;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.network.ConnectToServer;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener {

    private final String TAG = MapsActivity.class.getSimpleName();
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
    protected Boolean mRequestingLocationUpdates = false;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private String memberIndex;

    private MemberInfo memberInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Bundle bundle = getIntent().getBundleExtra(getString(R.string.parameter));
            String parameter = bundle.getString(getString(R.string.member_info));

            JSONObject inputJson = new JSONObject(parameter);
            memberIndex = inputJson.getString(getString(R.string.mem_idx));
//            String latitude = inputJson.getString(getString(R.string.mem_lat));
//            String longitude = inputJson.getString(getString(R.string.mem_lng));

            memberInfo = MemberInfo.parseMemberInfo(inputJson);

//            Double dLatitude = Double.parseDouble(latitude);
//            Double dLonDouble = Double.parseDouble(longitude);

        }catch(NullPointerException e){
            e.printStackTrace();
        }catch(JSONException e){
            e.printStackTrace();
        }

        setContentView(R.layout.activity_maps);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.icon_small_white_32);
            toolbar.setTitle(memberInfo.getMemberName() + getString(R.string.maps_activity_title));
            setSupportActionBar(toolbar);
        } else {
            Log.i(TAG, "toolbar null");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton moveCurrentLocationButton = (FloatingActionButton)findViewById(R.id.floating_button);
        moveCurrentLocationButton.setOnClickListener(this);
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
    }

    private void lcationNotFound() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(memberInfo.getMemberName() + " 님의 위치 정보를 받아 올 수 없습니다.")
                .setPositiveButton("뒤로가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false); // 회전 금지

        executeGetUserLocation(memberInfo.getMemberIndex());

//        updateUI(memberInfo.getLatitude(), memberInfo.getLongitude(), memberInfo.getMemberName());

        JSONObject paramObj = new JSONObject();
        try {
            paramObj.put(getString(R.string.mem_idx), memberIndex);
        }catch(JSONException e){
            e.printStackTrace();
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
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
    public void onConnected(@Nullable Bundle bundle) { }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.i(TAG, "onLocationChanged");
//
//        mCurrentLocation = location;
//        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }
    private void updateUI(double currentLatitude, double currentLongitude, String userName){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(userName).append(" 님의 ").append(getString(R.string.maps_activity_title));
        if(userName.length() > 0) {
            toolbar.setTitle(stringBuilder.toString());
        }

        double radius = 100.0;
        int strokeColor = getResources().getColor(R.color.map_stroke);
        int shadeColor = getResources().getColor(R.color.map_shade);

        mMap.clear();
        LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title(getString(R.string.current_location)));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocation)
                .zoom(17)
                .tilt(40)
                .build();
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 17.0f));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.floating_button:
                executeGetUserLocation(memberInfo.getMemberIndex());
                break;
        }
    }
    private void executeGetUserLocation(String memIdx){
//        SysgenPreference pref = new SysgenPreference(this);
//        String memIdx = pref.getString(getString(R.string.mem_idx));

        JSONObject json = new JSONObject();
        try {
            json.put(getString(R.string.mem_idx), memIdx);
        }catch(JSONException e){
            e.printStackTrace();
        }
        new GetUserLocationTask().execute(json.toString());
    }
    /**
     * input : mem_idx
     */
    private class GetUserLocationTask extends AsyncTask<String, Void, String>{
        private ConnectToServer conn;
        private String jspFile;
        private ProgressDialog progressDialog;

        public GetUserLocationTask(){
            this.conn = new ConnectToServer(getApplicationContext());
            this.jspFile = getString(R.string.getUserLocation);
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            final String param = params[0];

            return conn.getJson(param, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            Log.i(TAG, s);

            String latitudeStr;
            String longitudeStr;
            String userName = "";
            double latitude = 0.0d;
            double longitude = 0.0d;

            try {
                JSONObject resultJson = new JSONObject(s);
//                userName = resultJson.getString(getString(R.string.mem_name));
                latitudeStr = resultJson.getString(getString(R.string.mem_lat));
                longitudeStr = resultJson.getString(getString(R.string.mem_lng));
                latitude = Double.valueOf(latitudeStr);
                longitude = Double.valueOf(longitudeStr);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NumberFormatException e){
                lcationNotFound();
            }


            updateUI(latitude, longitude, userName);
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        this.setResult(RESULT_OK);
        this.finish();
    }
}
