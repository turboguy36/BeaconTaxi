package kr.sysgen.taxi.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.database.CursorIndexOutOfBoundsException;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.scanner.BLEScanCallback;
import com.android.scanner.BLEScanner;
import com.android.scanner.ScanBLEResult;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.Beacon;
import kr.sysgen.taxi.service.database.DBOpenHelper;
import kr.sysgen.taxi.service.database.Databases;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 *
 * Created by leehg on 2016-09-07
 */
public class BeaconListener extends Service implements BLEScanCallback {
    private final String TAG = BeaconListener.class.getSimpleName();
    /**
     *
     */
    private BLEScanner bleScanner;
    /**
     *
     */
    final long scanPeriod = 120 * 1000L;
    /**
     *
     */
    final long betweenPeriod = 10 * 1000L;
    /**
     *
     */
    private DBOpenHelper mDBOpenHelper;
    /**
     *
     */
    public Runnable mRunnable = null;
    /**
     *
     */
    public static final int WEIGHT_MAX = 10;

    private final long ONE_SECOND = 1000L;

    private final long ONE_MINUTE = 60 * ONE_SECOND;

    private final long WAKEUP_INTERVAL = 2 * ONE_MINUTE;

    @Override
    public void onCreate() {
        super.onCreate();

        // Beacon 을 찾아내어 onLeScan 에 콜백을 준다.
        setBleScanner();

        // Database 를 접속 하기 위한 객체 생성
        mDBOpenHelper = initDBHelper();

        // 이 서비스가 죽었을 경우 다시 살려 내기 위한 타이머를 등록한다.
        registerRestartAlarm(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onLeScan(ScanBLEResult scanBLEResult) {

        // Scan 된 결과를 비콘 객체로 만든다.
        Beacon incomeBeacon = Beacon.getBeaconInfo(scanBLEResult);
        if(incomeBeacon == null)return;

        // SQLite 와 Server 의 Database 에 등록 된 비콘이 맞는지 판별
        // Beacon 유효성 검사 - Beacon 이 맞는가? (핸즈프리 / 블루투스 스피커 와 같은 기기들이 스캔 되었을 경우 과정 생략
        try{
            Beacon dbBeacon = mDBOpenHelper.getBeacon(Databases.TaxiTable._TABLE, incomeBeacon.getMajor(), incomeBeacon.getMinor());
            if (dbBeacon==null || dbBeacon.getId() == 0) return;

            Log.i(TAG, "dbBeacon: " + dbBeacon);
            SysgenPreference pref = new SysgenPreference(getApplicationContext());
            int memStatus = pref.getInt(getString(R.string.mem_status));
            Log.i(TAG, "memStatus: " + memStatus);

            startTimerService(dbBeacon);
        }catch(NullPointerException ne){
            ne.printStackTrace();
        }catch(IllegalStateException ie){
            ie.printStackTrace();
        }catch(CursorIndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }
    private void startTimerService(Beacon dbBeacon){
        boolean isRunning = isServiceRunning(getApplicationContext(), TimerService.class);
//        Log.i(TAG, "isRunning: " + isRunning);

        Intent timerService = new Intent(getApplicationContext(), TimerService.class);

        if(!isRunning) {
            Bundle bundle = new Bundle();
            bundle.putLong(getString(R.string.beacon_id), dbBeacon.getId());
            bundle.putInt(getString(R.string.taxi_major), dbBeacon.getMajor());
            bundle.putInt(getString(R.string.taxi_minor), dbBeacon.getMinor());
            bundle.putString(getString(R.string.taxi_num), dbBeacon.getTaxiNum());

            timerService.putExtra(getString(R.string.beacon_id), dbBeacon.getId());
            timerService.putExtra(getString(R.string.parameter), bundle);
            startService(timerService);
        }
    }
/*
    private void startTimerService(Beacon dbBeacon, boolean isRestart){
        boolean isRunning = isServiceRunning(getApplicationContext(), TimerService.class);
        Log.i(TAG, "isRunning: " + isRunning);

        Intent timerService = new Intent(getApplicationContext(), TimerService.class);
        if(isRestart){
            stopService(timerService);
        }

        if(!isRunning) {
            Bundle bundle = new Bundle();
            bundle.putLong(getString(R.string.beacon_id), dbBeacon.getId());
            bundle.putInt(getString(R.string.taxi_major), dbBeacon.getMajor());
            bundle.putInt(getString(R.string.taxi_minor), dbBeacon.getMinor());
            bundle.putString(getString(R.string.taxi_num), dbBeacon.getTaxiNum());

            timerService.putExtra(getString(R.string.parameter), bundle);
            startService(timerService);
        }
    }
*/

    @Override
    public void onScanCycleFinish() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new BeaconListener.BeaconServiceV4Binder();
    }

    /**
     *
     */
    private void setBleScanner(){
        this.bleScanner = BLEScanner.createScanner(this.getApplicationContext(), this);
        this.bleScanner.setScanPeriod(scanPeriod);
        this.bleScanner.setBetweenScanPeriod(betweenPeriod);
        this.bleScanner.start();
    }

    public class BeaconServiceV4Binder extends Binder {
        public BeaconServiceV4Binder(){

        }
        public BeaconListener getService(){
            return BeaconListener.this;
        }
    }

    /**
     *
     * @param isOn
     */
    public void registerRestartAlarm(boolean isOn){
        Intent intent = new Intent(BeaconListener.this, RestartReceiver.class);
        intent.setAction(RestartReceiver.ACTION_RESTART_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if(isOn){
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, WAKEUP_INTERVAL, sender);
        }else{
            am.cancel(sender);
        }
    }

    /**
     *
     */
    private DBOpenHelper initDBHelper(){
        return new DBOpenHelper(getApplicationContext(), Databases.DB_VERSION);
    }

    /**
     *
     * @param context
     * @param service
     * @return
     */
    private boolean isServiceRunning(Context context, Class service) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if(service.getName().equals(runningService.service.getClassName())){

                return true;
            }
        }
        return false;
    }
}
