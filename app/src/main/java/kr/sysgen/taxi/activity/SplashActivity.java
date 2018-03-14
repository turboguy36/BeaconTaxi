package kr.sysgen.taxi.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.TaxiInfo;
import kr.sysgen.taxi.fcm.QuickstartPreferences;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.service.database.DBOpenHelper;
import kr.sysgen.taxi.service.database.Databases;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * 어플 실행 했을 때 첫 화면
 *
 * 1. 로그인 프로세스를 실행 한다.
 * 로그인 성공 -> 메인화면으로
 * 로그인 실패 -> 로그인 화면으로 이동
 *
 * 2. 브로드캐스트 리시버를 등록한다.
 *  (토큰키를 얻어 오기 위해 필요하다.)
 *
 * written by leehg
 * 2106-09-06
 */
public class SplashActivity extends AppCompatActivity {
    private final String TAG = SplashActivity.class.getSimpleName();

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private DBOpenHelper mDBOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initDBHelper();
        new GetAllTaxiList().execute();
        login();
    }
    private void login() { // 갖고 있는 정보로 로그인 하기
        SysgenPreference sysgenPreference = new SysgenPreference(this);
        final String memberRandomKey = sysgenPreference.getString(getString(R.string.mem_r_key));
        final String memHpNum = sysgenPreference.getString(getString(R.string.mem_hp_num));
        final String memberIndex = sysgenPreference.getString(getString(R.string.mem_idx));
        final String memberName = sysgenPreference.getString(getString(R.string.mem_name));
        sysgenPreference.showAllPreferences();
        new LoginTask().execute(memberRandomKey, memHpNum, memberName, memberIndex);
    }
    private void initDBHelper(){
        mDBOpenHelper = new DBOpenHelper(getApplicationContext(), Databases.DB_VERSION);
        boolean appVersionCheck = getVersionCheckResult();
        Log.i(TAG, "appVersionCheck: " + appVersionCheck);
        if(appVersionCheck) {
            mDBOpenHelper.onUpgrade(null, 0, 0);
        }
        Log.i(TAG, "initDBHelper() -- end");
    }
    /**
     * LocalBroadcast 리시버를 정의한다. 토큰을 획득하기 위한 READY, GENERATING, COMPLETE 액션에 따라 UI에 변화를 준다.
     */
    public void registBroadcastReceiver() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(QuickstartPreferences.REGISTRATION_READY)) {
                    // 액션이 READY일 경우
                    // mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                    // mInformationTextView.setVisibility(View.GONE);
                } else if (action.equals(QuickstartPreferences.REGISTRATION_GENERATING)) {
                    // 액션이 GENERATING일 경우
                    // mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
                    // mInformationTextView.setVisibility(View.VISIBLE);
                    // mInformationTextView.setText(getString(R.string.registering_message_generating));
                } else if (action.equals(QuickstartPreferences.REGISTRATION_COMPLETE)) {
                    // 액션이 COMPLETE일 경우
                    // mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                    // mRegistrationButton.setText(getString(R.string.registering_message_complete));
                    // mRegistrationButton.setEnabled(false);
                    String token = intent.getStringExtra("token");
                    //  Log.i("Reg Token ID = ", token);
                    //  mInformationTextView.setText(token);
                }
            }
        };
    }

    /**
     * 앱이 실행되어 화면에 나타날때 LocalBroadcastManager에 액션을 정의하여 등록한다.
     */
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_READY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_GENERATING));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

    }

    /**
     * 앱이 화면에서 사라지면 등록된 LocalBoardcast를 모두 삭제한다.
     */
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
    public String getRegistrationId(Context context) {
        StringBuffer token = new StringBuffer();
        InstanceID instanceID = InstanceID.getInstance(context);
        String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
        try {
            token.append(instanceID.getToken(getString(R.string.sender_id), scope, null));
        }catch(IOException e){
            e.printStackTrace();
        }
        return token.toString();
    }

    /**
     *
     */
    private class LoginTask extends AsyncTask<String, Void, String>{
        private ConnectToServer conn;
        private String jspFile;

        public LoginTask(){
            conn = new ConnectToServer(SplashActivity.this);
            jspFile = getString(R.string.login);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... params) {
            String memberRandomKey = params[0];
            String memHpNum = params[1];
            String memberName = params[2];
            String memberIndex = params[3];

            final String tokenId = getRegistrationId(getApplicationContext());

            JSONObject param = new JSONObject();
            try {
                param.put(getString(R.string.mem_idx), memberIndex);
                param.put(getString(R.string.mem_hp_num), memHpNum);
                param.put(getString(R.string.mem_r_key), memberRandomKey);
                param.put(getString(R.string.mem_name), memberName);
                param.put(getString(R.string.mem_token_id), tokenId);
            } catch(JSONException e) {
                e.printStackTrace();
            }

            return conn.getJson(param.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {
                Intent signUpIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(signUpIntent);
                finish();
            } else {
                try {
                    JSONObject resultJSON = new JSONObject(result);

                    int resultCode = resultJSON.getInt(getString(R.string.result_code));

                    if (resultCode == 1) {
                        final int loginResult = resultJSON.getInt(getString(R.string.login_result));
                        if(loginResult == LoginActivity.LOGIN_RESULT_FAIL){ // 로그인 실패
                            Intent signUpIntent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(signUpIntent);
                            finish();
                        }

                        final int relationCode = Integer.parseInt(resultJSON.getString(getString(R.string.relation_code)));
                        final int memberIndex = resultJSON.getInt(getString(R.string.mem_idx));
                        final int memberType = resultJSON.getInt(getString(R.string.mem_type));
                        final String memberName = resultJSON.getString(getString(R.string.mem_name));
                        final String memberPhone = resultJSON.getString(getString(R.string.mem_hp_num));

                        // 만일 User 의 정보가 변경 되었을 경우
                        updateUserInformation(memberName, memberPhone);

                        if(loginResult == LoginActivity.LOGIN_RESULT_OK) { // 로그인 성공
                            startSlideMenuActivity(memberIndex);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                        builder.setTitle(getString(R.string.title_alert))
                                .setMessage(getString(R.string.error_time_out))
                                .setPositiveButton(R.string.text_confirm,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                finish();
                                            }
                                        });
                        builder.show();
                    }
                } catch (JSONException | NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateUserInformation(String userName, String userPhone){
            SysgenPreference pref = new SysgenPreference(getApplicationContext());
            pref.putString(getString(R.string.mem_name), userName);
            pref.putString(getString(R.string.mem_hp_num), userPhone);
        }
        private void startSlideMenuActivity(int memberIndex){
            SysgenPreference sysgenPreference = new SysgenPreference(SplashActivity.this);
            sysgenPreference.putString(getString(R.string.mem_idx), String.valueOf(memberIndex));
            Intent intent = new Intent(SplashActivity.this, SlideMenuActivity.class);
            try {
                if (getIntent().getExtras() != null) {
                    String inputParameter = getIntent().getExtras().getString(getString(R.string.parameter));
                    intent.putExtra(getString(R.string.parameter), inputParameter);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            intent.putExtra(getString(R.string.mem_idx), memberIndex);
            startActivity(intent);
            finish();
        }
    }

    private Boolean getVersionCheckResult(){
        boolean isSameVersion = false;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int version = pInfo.versionCode;
            SysgenPreference pref = new SysgenPreference(getApplicationContext());
            int appVersion = pref.getInt(getString(R.string.app_version));
            if(appVersion == version){
                isSameVersion = true;
            }else{
                pref.putInt(getString(R.string.app_version), version);
            }
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return isSameVersion;
    }
    private class GetAllTaxiList extends AsyncTask<String, Integer, String>{
        private ConnectToServer conn;
        private String jspFile;
        private ProgressBar progress;

        public GetAllTaxiList(){
            jspFile = getString(R.string.getAllTaxiList);
            conn = new ConnectToServer(getApplicationContext());
            progress = (ProgressBar)findViewById(R.id.progressBar);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progress.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... strings) {
            registBroadcastReceiver();
            return conn.getJson(null, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject inputJson = new JSONObject(s);
                JSONArray array = inputJson.getJSONArray(getString(R.string.list));
                int length = array.length();

                for(int i=0; i<length; i++){
                    int value = ((i+1)*100/length);
                    onProgressUpdate(value);

                    JSONObject taxiObj = array.getJSONObject(i);
                    TaxiInfo taxiInfo = TaxiInfo.parseTaxi(taxiObj);
                    if(!mDBOpenHelper.checkExistColumn(Databases.TaxiTable._TABLE, taxiInfo.getMajorNumber(), taxiInfo.getMinorNumber())) {

                        mDBOpenHelper.insertData(Databases.TaxiTable._TABLE, taxiInfo);
                    }
                }
            }catch(JSONException e){
                e.printStackTrace();
            }catch(SQLiteException e){
                e.printStackTrace();
            }
        }
    }
}
