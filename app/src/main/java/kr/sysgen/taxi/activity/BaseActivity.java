package kr.sysgen.taxi.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

//import com.perples.recosdk.RECOBeaconManager;
//import com.perples.recosdk.RECOBeaconRegion;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.service.BeaconListener;
import kr.sysgen.taxi.service.TimerService;
import kr.sysgen.taxi.util.SysgenPreference;


/**
 * Created by leehg on 2016-06-16.
 */
public class BaseActivity extends AppCompatActivity {
    private final String TAG = BaseActivity.class.getSimpleName();
    protected Toolbar toolbar;

    public static final int PERMISSION_REQUEST_CODE = 0xAAAA;
    public static final int MAP_REQUEST_CODE = 0xABCD;
    public static final int REQUEST_CODE_GET_CONTACT = 0xAAAD;

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_slide_menu);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if(!isBackgroundBeaconServiceRunning(this)){
            setServiceStart(this);
        }
    }
    public void setToolbarTitle(String title){
        if(this.toolbar != null){
            Log.i(TAG, "setToolbarTitle: " + title);
            this.toolbar.setTitle(title);
        }

    }
    public void setToolbarTitle(int title){
        final String barTitle = getString(title);
        setToolbarTitle(barTitle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {}

    public Toolbar getToolbar() {
        return toolbar;
    }

    public int addFragment(Fragment fragment){
        return getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment, fragment)
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .commit();
    }

    public int replaceFragment(Fragment fragment){
        return getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();
    }

    public int replaceFragment(Fragment fragment, String tag){
        return getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .addToBackStack(tag)
                .commitAllowingStateLoss();
    }

    public int addFragment(Fragment fragment, String tag){
        return getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment, fragment)
                .addToBackStack(tag)
                .commit();
    }
    protected void setToolbar(String title){
        if(toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.icon_small_32);

            toolbar.setTitle(title);
            setSupportActionBar(toolbar);
        }else{
            Log.i(TAG, "toolbar null");
        }
    }

    protected void requestPermission(){
        // Marshmallow 사용자들에게 Bluetooth 사용을 허가 받자
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || this.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                    || this.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                // 권한이 없을 경우

                // 최초 권한 요청인지, 혹은 사용자에 의한 재요청인지 확인
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    //사용자가 임으로 권한을 취소시킨 경우
                    // 권한 재요청
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
                } else{
                    // 최초로 권한을 요청하는 경우(첫실행)
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

    protected boolean isBackgroundBeaconServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if(BeaconListener.class.getName().equals(runningService.service.getClassName())){
                return true;
            }
        }
        return false;
    }
    protected void setServiceStart(Context context){
        Intent monitoringService = new Intent(context, BeaconListener.class);
        startService(monitoringService);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSION_REQUEST_CODE:
                if(!(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    getDialog().show();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //If a user device turns off bluetooth, request to turn it on.
        //사용자가 블루투스를 켜도록 요청합니다.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
    }

    protected AlertDialog.Builder getDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_dialog_alert)
                .setMessage(R.string.please_allow_permission)
                .setPositiveButton(R.string.text_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
        return builder;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SysgenPreference sysgenPreference = new SysgenPreference(this);

        switch(item.getItemId()){
            case R.id.log_out:
                sysgenPreference.removeAll();

                finish();
                startActivity(new Intent(this, SplashActivity.class));

                stopService(new Intent(this, TimerService.class));
                stopService(new Intent(this, BeaconListener.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
