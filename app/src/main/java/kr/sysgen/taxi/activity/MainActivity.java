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
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.AddMyPartnerFragment;
import kr.sysgen.taxi.activity.Fragment.BaseFragment;
import kr.sysgen.taxi.activity.Fragment.InitViewTabFragment;
import kr.sysgen.taxi.activity.Fragment.MainActivityFragment;
import kr.sysgen.taxi.activity.Fragment.ShowMyParentFragment;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.service.BeaconListener;
import kr.sysgen.taxi.service.TimerService;
import kr.sysgen.taxi.util.SysgenPreference;

public class MainActivity extends BaseActivity implements ShowMyParentFragment.OnParentListFragmentInteractionListener{
    private final String TAG = MainActivity.class.getSimpleName();

    private String inputParameter;

    private MemberInfo member;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getData();
        setToolbar("");
        requestPermission();
        makeChildrenView(Integer.parseInt(member.getMemberIndex()));

        if(!isBackgroundBeaconServiceRunning(this)){
            setServiceStart(this);
        }
    }

    private void getData(){
        member = new MemberInfo();
        Bundle bundle = getIntent().getExtras();
        int memberIndex = bundle.getInt(getString(R.string.mem_idx));
        member.setMemberIndex(String.valueOf(memberIndex));
        inputParameter = bundle.getString(getString(R.string.parameter));
    }

    private void makeChildrenView(int memberIndex){
        if (inputParameter != null) {
            // Notification 을 클릭 하여 들어 왔을 경우
            fromNotificationThenMakeDialog(inputParameter);
        }

        new GetMyChildrenTask().execute(member.getMemberIndex());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MAP_REQUEST_CODE){
            JSONObject json = new JSONObject();
            try {
                json.put(getString(R.string.is_parent), true);
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void fromNotificationThenMakeDialog(@NonNull String inputParameter){
        if(!(inputParameter.length() > 0))return;
        try {
            JSONObject inputJson = new JSONObject(inputParameter);
            if(inputJson.has(getString(R.string.notification_param))) {
                JSONObject paramJson = inputJson.getJSONObject(getString(R.string.notification_param));

                boolean memState = paramJson.getBoolean(getString(R.string.mem_status));
                String taxiNumber = paramJson.getString(getString(R.string.mem_bus_name));

                if(memState) {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("택시 "+taxiNumber + " 차량에 탑승 하셨습니다. 안심 메시지를 전송 할까요?");
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("문자를 전송 하시겠습니까?")
                            .setMessage(buffer.toString())
                            .setPositiveButton(R.string.text_confirm,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).setNegativeButton(R.string.text_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                    builder.show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);

        if(count == 1){
            toolbar.setTitle(R.string.app_name_kor);
        }
        if(count==0 && fragment instanceof InitViewTabFragment){
            finish();
        }else {
            getSupportFragmentManager().popBackStack();
        }

        List<Fragment> list = getSupportFragmentManager().getFragments();
    }

    @Override
    public void OnParentListFragmentInteractionListener(MemberInfo item) {

    }

    private class GetMyChildrenTask extends AsyncTask<String,Void,String> {
        private ConnectToServer conn;
        private final String jspFile;

        public GetMyChildrenTask() {
            conn = new ConnectToServer(MainActivity.this);
            jspFile = getString(R.string.getMyChildren);
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject paramJson = new JSONObject();

            try {
                paramJson.put(getString(R.string.rl_mem_idx2), params[0]);
            }catch(JSONException e){
                e.printStackTrace();
            }

            return conn.getJson(paramJson.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            InitViewTabFragment fragment = InitViewTabFragment.newInstance(s);
            addFragment(fragment);
        }
    }
}
