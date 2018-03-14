package kr.sysgen.taxi.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.SplashActivity;
import kr.sysgen.taxi.configure.AppConfig;
import kr.sysgen.taxi.data.Beacon;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.data.TrackingInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.reco.RecoPushUtil;
import kr.sysgen.taxi.service.database.DBOpenHelper;
import kr.sysgen.taxi.service.database.Databases;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * Created by leehg on 2016-09-23.
 */
public class TimerService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String TAG = TimerService.class.getSimpleName();

    public Runnable mRunnable = null;

    private final long second = 1000L;

    private long beaconId;

    private DBOpenHelper mDBOpenHelper;

    private SysgenPreference pref;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
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
    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    /**
     *
     */
    final long betweenPeriod = 10 * second;
    /**
     *
     */
    private BluetoothLeScanner bluetoothLeScanner;
    /**
     *
     */
    private BluetoothAdapter bluetoothAdapter;
    /**
     *
     */
    private final int LOCATION_TRIGGER = 5;
    /**
     *
     */
    private int ticker = LOCATION_TRIGGER;
    /**
     *
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.KOREA);
    /**
     *
     */
    private int historyIndex = 0;
    /**
     *
     */
    private Handler mHandler;
    /**
     *
     */
    public static boolean IS_PHONE_CALL = false;
    /**
     *
     */
    private boolean isSameCar = false;
    /**
     *
     */
    private Runnable countRunnable;
    /**
     *
     */
    int numberOfCount = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        pref = new SysgenPreference(getApplicationContext());
        initDBHelper();
        buildGoogleApiClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setBleScanner();
        try {
            beaconId = intent.getLongExtra(getString(R.string.beacon_id), 0L);
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        verifyBeacon(intent);
        return START_STICKY;
    }


    private void verifyBeacon(final Intent intent){
        final Handler countHandler = new Handler();
        countRunnable = new Runnable() {
            @Override
            public void run() {
                if(isSameCar && (numberOfCount == 0)){
                    countHandler.removeCallbacks(countRunnable);
                    controlForegroundService(intent);
                }else if(numberOfCount-- > 0){
                    countHandler.postDelayed(countRunnable, betweenPeriod);
                }else if(!isSameCar){
                    countHandler.removeCallbacks(countRunnable);
                    onDestroy();
                }
            }
        };
        countHandler.postDelayed(countRunnable, betweenPeriod);
    }
    private void controlForegroundService(Intent intent){
        mHandler = new Handler();
        if (intent != null && intent.getExtras() != null) {
            try {
                historyIndex = intent.getIntExtra(getString(R.string.history_idx), -1);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            try {
                Beacon beacon = mDBOpenHelper.getBeacon(Databases.TaxiTable._TABLE, beaconId);

                if(historyIndex > 0 && beaconId > 0){
                    restartForegroundService(beaconId, beacon);
                }else if (beaconId > 0) {
                    startForegroundService(beacon);
                } else {
                    stopForegroundService();
                }
            } catch (CursorIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 처음 택시를 탔을 때
     * @param beacon
     */
    private void startForegroundService(Beacon beacon){
        makeForegroundService(beacon.getTaxiNum(), false);
        setTimerHandler();
        didEnterRegion(beacon.getMajor(), beacon.getMinor());
    }

    /**
     * 택시 타다가 서비스가 혼자 죽었을 때
     *
     * @param beaconId
     * @param beacon
     */
    private void restartForegroundService(long beaconId, Beacon beacon){
        makeForegroundService(beacon.getTaxiNum(), false);
        setTimerHandler();
        mDBOpenHelper.updateWeight(Databases.TaxiTable._TABLE, beaconId, BeaconListener.WEIGHT_MAX);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        int weight = 0;
        try {
            weight = mDBOpenHelper.getWeight(Databases.TaxiTable._TABLE, beaconId);
        }catch(CursorIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        try {
            mHandler.removeCallbacks(mRunnable);
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        // 택시에서 하차 하지도 않았는데, 서비스가 혼자 죽었을 때
        if(weight > 0){
            Intent intent = new Intent();
            intent.setAction(TimerService.class.getSimpleName());
            intent.putExtra(getString(R.string.history_idx), historyIndex);
            intent.putExtra(getString(R.string.beacon_id), beaconId);
            sendBroadcast(intent);
        }else{
            // 정상 종료
            stopForegroundService();
            if (mGoogleApiClient.isConnected()) {
                stopLocationUpdates();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TimerService.BeaconServiceV4Binder();
    }

    /**
     *
     * @param taxiNum
     * @param isRestart
     */
    private void makeForegroundService(String taxiNum, boolean isRestart) {
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent splashActivityIntent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, AppConfig.NOTIFICATION_ID.FOREGROUND_SERVICE, splashActivityIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle(getString(R.string.app_name_kor))
                .setContentText(taxiNum + getString(R.string.get_on_taxi_message))
                .setSmallIcon(R.drawable.icon_small_white_32)
                .setOngoing(true);

        if(isRestart) {
            builder.setSound(defaultSoundUri)
                    .setVibrate(new long[]{500, 100, 500, 100});
        }

        startForeground(AppConfig.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
    }
    private boolean isUserLocationTime(){
        final String startTime = pref.getString(getString(R.string.location_information_start));
        final String endTime = pref.getString(getString(R.string.location_information_end));

        SimpleDateFormat format = new SimpleDateFormat(getString(R.string.hour_format));
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(format.parse(startTime));
        }catch(ParseException e){
            e.printStackTrace();
        }

        int hourStart = calendar.get(Calendar.HOUR_OF_DAY);
        int minStart = calendar.get(Calendar.MINUTE);

        return false;
    }
    /**
     * Foreground Service 가 실행 되는 동안 계속 해서 사용자의 위치 서비스를 전송 한다.
     * 사용자가 택시에서 하차 하는지 판별 하는 부분이기도 하다.
     */
    private void setTimerHandler() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if(ticker-- == 0) {
                    // 위치 서비스 업데이트 및 전송
                    if (mCurrentLocation != null) {
                        double memLat = mCurrentLocation.getLatitude();
                        double memLng = mCurrentLocation.getLongitude();

                        startLocationUpdates();

                        final String memIdx = pref.getString(getString(R.string.mem_idx));
                        JSONObject paramJson = new JSONObject();
                        try {
                            paramJson.put(getString(R.string.mem_idx), memIdx);
                            paramJson.put(getString(R.string.mem_lat), memLat);
                            paramJson.put(getString(R.string.mem_lng), memLng);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // flag
                        new UpdateMyLocation().execute(paramJson.toString());
                        ticker = LOCATION_TRIGGER;
                    }
                }

                // 택시에서 내리는 중인지 판별, 하차 시키기
                int weight = 0;
                try {
                    weight = mDBOpenHelper.getWeight(Databases.TaxiTable._TABLE, beaconId);
                }catch(CursorIndexOutOfBoundsException e){
                    e.printStackTrace();
                }

                if (weight > 0) {
                    // 택시에 탑승함. 혹은 타고 있는 상태

                    pref.putLong(getString(R.string.beacon_id), beaconId);
                    // 가중치를 주기 마다 떨어뜨린다.
                    if(!IS_PHONE_CALL) {
                        mDBOpenHelper.updateWeight(Databases.TaxiTable._TABLE, beaconId, --weight);
                    }
                    mHandler.postDelayed(mRunnable, betweenPeriod);
                } else {
                    // 택시에서 하차 했다.
                    stopForegroundService();
                    pref.removeLong(getString(R.string.beacon_id));
                    didExitRegion();
                }
            }
        };
        mHandler.postDelayed(mRunnable, betweenPeriod);
    }

    private void initDBHelper() {
        mDBOpenHelper = new DBOpenHelper(getApplicationContext(), Databases.DB_VERSION);
    }

    private void stopForegroundService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected");
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
        float distanceInMeters = location.distanceTo(mCurrentLocation);

        if(distanceInMeters > 50) {
            mCurrentLocation = location;
        }
    }

    private void scanCallbackResult(ScanResult scanBLEResult) throws NullPointerException{
        Beacon incomeBeacon = Beacon.getBeaconInfo(scanBLEResult);

        if(incomeBeacon==null){
            return;
        }

        Beacon dbIncomeBeacon = mDBOpenHelper.getBeacon(Databases.TaxiTable._TABLE, incomeBeacon.getMajor(), incomeBeacon.getMinor());

        isSameCar = (beaconId == dbIncomeBeacon.getId())?true:false;

        long onTaxiId = pref.getLong(getString(R.string.beacon_id));
        if(numberOfCount > 0){
            // Do Nothing
        }else if(onTaxiId > 0){ // 이미 탑승한 사용자
            Beacon onTaxiBeacon = mDBOpenHelper.getBeacon(Databases.TaxiTable._TABLE, onTaxiId);

            if(onTaxiBeacon.getId() == dbIncomeBeacon.getId()){
                //같은 비콘 일 때 (ID 가 같을 때)
                mDBOpenHelper.updateWeight(Databases.TaxiTable._TABLE, onTaxiBeacon.getId(), BeaconListener.WEIGHT_MAX);
            }else if(onTaxiBeacon.getPairMajor() == dbIncomeBeacon.getMajor() && onTaxiBeacon.getPairMinor()==dbIncomeBeacon.getMinor()){
                // 다른 비콘 이지만 같은 택시 일 때 (Pair 값이 같다)
                mDBOpenHelper.updateWeight(Databases.TaxiTable._TABLE, onTaxiBeacon.getId(), BeaconListener.WEIGHT_MAX);
            }
        }
    }

    public class BeaconServiceV4Binder extends Binder {
        public BeaconServiceV4Binder() {

        }

        public TimerService getService() {
            return TimerService.this;
        }
    }

    private void didEnterRegion(int taxiMajor, int taxiMinor){
//        Log.i(TAG, "didEnterRegion");

        mDBOpenHelper.updateWeight(Databases.TaxiTable._TABLE, beaconId, BeaconListener.WEIGHT_MAX);

        final String randomKey = pref.getString(getString(R.string.mem_r_key));
        final String memPrimaryKey = pref.getString(getString(R.string.mem_idx));

        JSONObject sendParam = new JSONObject();
        try {
            sendParam.put(getString(R.string.mem_r_key), randomKey);
            sendParam.put(getString(R.string.mem_idx), memPrimaryKey);
            sendParam.put(getString(R.string.taxi_major), String.valueOf(taxiMajor));
            sendParam.put(getString(R.string.taxi_minor), String.valueOf(taxiMinor));
            if(mCurrentLocation != null){
                sendParam.put(getString(R.string.mem_lat), mCurrentLocation.getLatitude());
                sendParam.put(getString(R.string.mem_lng), mCurrentLocation.getLongitude());
                sendParam.put(getString(R.string.mem_address), TrackingInfo.getAddress(getApplicationContext(), mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            }
        }catch(JSONException e){
            e.printStackTrace();
        }

        if(sendParam != null) {
            new SendOnBoardTask().execute(sendParam.toString());
        }
    }

    /**
     * 택시에서 내렸다
     */
    private void didExitRegion(){
        final String randomKey = pref.getString(getString(R.string.mem_r_key));
        final String memberIndex = pref.getString(getString(R.string.mem_idx));

        JSONObject json = new JSONObject();
        try {
            json.put(getString(R.string.mem_r_key), randomKey);
            json.put(getString(R.string.mem_idx), memberIndex);
            if(mCurrentLocation != null) {
                json.put(getString(R.string.mem_lat), mCurrentLocation.getLatitude());
                json.put(getString(R.string.mem_lng), mCurrentLocation.getLongitude());
                json.put(getString(R.string.mem_address), TrackingInfo.getAddress(getApplicationContext(), mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendExitTaxiTask().execute(json.toString());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
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
//
//        // Sets the desired interval for active location updates. This interval is
//        // inexact. You may not receive updates at all if no location sources are available, or
//        // you may receive them slower than requested. You may also receive updates faster than
//        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

//        // Sets the fastest rate for active location updates. This interval is exact, and your
//        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            // int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }
    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
//        // It is a good practice to remove location requests when the activity is in a paused or
//        // stopped state. Doing so helps battery performance and is especially
//        // recommended in applications that request frequent location updates.
//
//        // The final argument to {@code requestLocationUpdates()} is a LocationListener
//        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void setBleScanner(){
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if(bluetoothLeScanner != null){
            bluetoothLeScanner.startScan(mScanCallback);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            try {
                scanCallbackResult(result);
            }catch(NullPointerException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult result : results) {
                try {
                    scanCallbackResult(result);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };
    private class SendOnBoardTask extends AsyncTask<String, Void, String> {
        private ConnectToServer conn;
        private String jspFile;

        public SendOnBoardTask() {
            conn = new ConnectToServer(getApplicationContext());
            jspFile = getString(R.string.check_in);
        }

        @Override
        protected String doInBackground(String... params) {
            String param = params[0];

            return conn.getJson(param, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject json = new JSONObject(s);
                historyIndex = json.getInt(getString(R.string.history_idx));

            }catch(JSONException e){
                e.printStackTrace();
            }
            pref.putInt(getString(R.string.mem_status), MemberInfo.STATUS_IN_TAXI);
        }
    }

    private class SendExitTaxiTask extends AsyncTask<String, Void, String> {
        private ConnectToServer conn;
        private String jspPage;

        public SendExitTaxiTask() {
            conn = new ConnectToServer(getApplicationContext());
            jspPage = getString(R.string.check_out);
        }

        @Override
        protected String doInBackground(String... params) {
            final String param = params[0];
            return conn.getJson(param, jspPage);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            final String pushTitle = getString(R.string.title_push_out);
            final String pushMsg = getString(R.string.message_push_out);

            JSONObject outputJson = new JSONObject();
            try {
                JSONObject json = new JSONObject();
                boolean isOnTaxi = false;
                json.put(getString(R.string.mem_status), isOnTaxi);

                outputJson.put(getString(R.string.notification_param), json);
            } catch (JSONException e) {
                e.getStackTrace();
            }

            RecoPushUtil pushUtil = new RecoPushUtil(getApplicationContext());
            pushUtil.popupNotification(pushTitle, pushMsg, outputJson.toString(), RecoPushUtil.TYPE_ENTER_TAXI);

            pref.putInt(getString(R.string.mem_status), MemberInfo.STATUS_OUT_TAXI);
        }
    }
    private class UpdateMyLocation extends AsyncTask<String, Void, String>{
        private ConnectToServer conn;
        private String jspPage;

        public UpdateMyLocation(){
            conn = new ConnectToServer(getApplicationContext());
            jspPage = getString(R.string.setMyLocation);
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "update location");

            return conn.getJson(params[0], jspPage);
        }
    }
}
