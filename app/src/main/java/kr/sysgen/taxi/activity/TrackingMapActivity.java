package kr.sysgen.taxi.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.data.TrackingInfo;

/**
 *
 */
public class TrackingMapActivity extends AppCompatActivity implements OnMapReadyCallback{
    private final String TAG = TrackingMapActivity.class.getSimpleName();

    /**
     * Toolbar
     */
    private Toolbar toolbar;

    /**
     *
     */
    private GoogleMap mMap;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private ArrayList<TrackingInfo> locationInfos;

    private Bitmap startPin;
    private Bitmap arrivePin;
    private Bitmap currentPin;

    final int scale_128 = 128;
    private int memStatus = 0;

    private Bitmap resizeDrawable(int icon){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), icon);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, scale_128, scale_128, false);
        return resizedBitmap;
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setView();
        startPin = resizeDrawable(R.drawable.placeholder_departure);
        arrivePin = resizeDrawable(R.drawable.placeholder_arrive);
        currentPin = resizeDrawable(R.drawable.placeholder_current);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        buildGoogleApiClient();
        setToolBar();

        Bundle bundle = getIntent().getBundleExtra(getString(R.string.parameter));
        String parameter = bundle.getString(getString(R.string.tracking_result));

        Log.i(TAG, ">> parameter: " + parameter);

        setLocationInfos(parameter);
    }
    private void setLocationInfos(String parameter){
        try {
            locationInfos = new ArrayList<>();

            JSONObject json = new JSONObject(parameter);
            if(json.has(getString(R.string.mem_status))) {
                memStatus = json.getInt(getString(R.string.mem_status));
            }

            JSONArray array = json.getJSONArray(getString(R.string.tracking_array));

            int length = array.length();
            Log.i(TAG, "array.length(): " + array.length());

            for(int i=0; i<length; i++){
                JSONObject obj = array.getJSONObject(i);
                TrackingInfo locationInfo = new TrackingInfo();

                String memLat = obj.getString(getString(R.string.mem_lat));
                String memLng = obj.getString(getString(R.string.mem_lng));
                String time = obj.getString(getString(R.string.time));

                locationInfo.setLatitude(Double.valueOf(memLat));
                locationInfo.setLongitude(Double.valueOf(memLng));

                SimpleDateFormat sd = new SimpleDateFormat(getString(R.string.date_format));
                try {
                    sd.parse(time);
                    Calendar cal = sd.getCalendar();

                    locationInfo.setTime(sd.format(cal.getTime()));
                }catch (ParseException e){
                    e.printStackTrace();
                }
                locationInfos.add(i, locationInfo);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }catch(NumberFormatException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false); // 회전 금지

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, BaseActivity.PERMISSION_REQUEST_CODE);

        }else {
        }
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
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

    private void updateUI(){
        TrackingInfo locationInfo = new TrackingInfo();
        Log.i(TAG, "locationInfos.size(): " + locationInfos.size());

        for(int i=0; i<locationInfos.size(); i++){
            locationInfo = locationInfos.get(i);
            LatLng currentLocation = new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());
            mMap.addMarker(getMarker(i, currentLocation, locationInfo.getTime()));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude()), 17.0f));
    }

    /**
     *
     * @param i
     * @param currentLocation
     * @param currentTime
     * @return
     */
    private MarkerOptions getMarker(int i, LatLng currentLocation, String currentTime){
        MarkerOptions marker = new MarkerOptions()
                .position(currentLocation)
                .title(currentTime);
        if(i==0){
            // 출발 위치
            marker.icon(BitmapDescriptorFactory.fromBitmap(startPin));
        }else if(i == locationInfos.size()-1){
            if(memStatus == 0) {
                // 마지막 도착 위치
                marker.icon(BitmapDescriptorFactory.fromBitmap(arrivePin));
            }else if(memStatus == 1){
                // 이동중 마지막 위치(현위치)
                marker.icon(BitmapDescriptorFactory.fromBitmap(currentPin));
            }
        }else {
            // 지금까지 이동 해 온 경로
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.tracking_pin));
        }
        return marker;
    }
    private void setToolBar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.icon_small_32);
            toolbar.setTitle(getString(R.string.tracking_title));
            setSupportActionBar(toolbar);
        }
    }
    private void setView(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FloatingActionButton moveCurrentLocationButton = (FloatingActionButton)findViewById(R.id.floating_button);
        moveCurrentLocationButton.setVisibility(View.GONE);
    }
}
