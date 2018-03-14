package kr.sysgen.taxi.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.configure.AppConfig;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.PhoneNumberUtil;
import kr.sysgen.taxi.util.SysgenPreference;

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    private final String TAG = LoginActivity.class.getSimpleName();

    public static final int LOGIN_RESULT_OK = 0;
    public static final int LOGIN_RESULT_FAIL = -1;

    private ViewHolder viewHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        viewHolder = new ViewHolder();

        viewHolder.loginButton.setOnClickListener(this);
        viewHolder.signUpButton.setOnClickListener(this);

        requestPermission();

    }

    /**
     * 허가 받아야 할 퍼미션
     *
     * 1. READ_PHONE_STATE - 내 전화번호 긁어 오기 위해
     * 2. ACCESS_FINE_LOCATION - 보호자에게 내 위치 전송 하기 위해
     * 3. READ_CONTACTS - 자녀추가 화면에서 연락처 받아 오기 위해
     * 4. SEND_SMS - 자녀추가 화면에서 초대 메시지 보내기 위해
     */
    @TargetApi(21)
    protected void requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.READ_CONTACTS,
                                    Manifest.permission.SEND_SMS
                            },
                            BaseActivity.PERMISSION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.READ_CONTACTS,
                                    Manifest.permission.SEND_SMS
                            },
                            BaseActivity.PERMISSION_REQUEST_CODE);
                }
            } else {
                setPhoneNumberView();
            }
        } else {
            setPhoneNumberView();
        }
    }
    private void setPhoneNumberView(){
        PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil(this);
        String phoneNumber = phoneNumberUtil.getDashedPhoneNumber();
        if (phoneNumber != null && phoneNumber.length() > 0) {

            viewHolder.phoneNumberView.setText(phoneNumber);
            viewHolder.phoneNumberView.setEnabled(false);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case BaseActivity.PERMISSION_REQUEST_CODE:
                try {
                    int result = grantResults[0];

                    if (grantResults.length > 0 && result == PackageManager.PERMISSION_GRANTED) {
                        setPhoneNumberView();
                    } else if (result == PackageManager.PERMISSION_DENIED) {

                    }
                }catch(ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                }finally {
                    return;
                }
        }
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.sign_in_button:
                String userName = viewHolder.userNameView.getText().toString().trim();
                String userPhoneNumber = viewHolder.phoneNumberView.getText().toString().trim();

                if(!(userName.length() > 0)){
                    makeDialog(viewHolder.userNameView);
                }else if(!(userPhoneNumber.length() > 0)){
                    makeDialog(viewHolder.phoneNumberView);
                }else{
                    String userIndex = (String) v.getTag(R.string.mem_idx);
                    PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil(this);

                    if(!userPhoneNumber.contains(PhoneNumberUtil._DASH)){
                        userPhoneNumber = phoneNumberUtil.getDashedPhoneNumber(userPhoneNumber);
                    }

                    new UserLoginTask().execute(userName, userPhoneNumber, userIndex);
                }
                break;
            case R.id.text_sign_up:
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
    private void makeDialog(final View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_alert)
                .setMessage(R.string.warning_message_empty)
                .setPositiveButton(R.string.text_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                view.requestFocus();
                            }
                        });
        builder.show();
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, String> {

        private ConnectToServer conn;
        private String jspFile;

        public UserLoginTask(){
            conn = new ConnectToServer(getApplicationContext());
            jspFile = getString(R.string.login_no_rkey);

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            viewHolder.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            final String userName = params[0];
            final String phoneNumber = params[1];
            final String userIndex = params[2];

            JSONObject paramJson = new JSONObject();
            try {
                paramJson.put(getString(R.string.mem_idx), userIndex);
                paramJson.put(getString(R.string.mem_name), URLEncoder.encode(userName, AppConfig.ENCODING));
                paramJson.put(getString(R.string.mem_hp_num), phoneNumber);
            }catch(JSONException e){
                e.printStackTrace();
            }catch(UnsupportedEncodingException e){
                e.printStackTrace();
            }
            return conn.getJson(paramJson.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            int resultCode = 99;
            MemberInfo member = new MemberInfo();
            try {
                JSONObject resultJson = new JSONObject(result);
                resultCode = resultJson.getInt(getString(R.string.result_code));
                if(resultCode == 1){
                    String memberRandomKey = resultJson.getString(getString(R.string.mem_r_key));
                    String memberPhoneNumber = resultJson.getString(getString(R.string.mem_hp_num));

                    SysgenPreference preference = new SysgenPreference(getApplicationContext());
                    preference.putString(getString(R.string.mem_r_key), memberRandomKey);
                    preference.putString(getString(R.string.mem_hp_num), memberPhoneNumber);
                    finish();
                    startActivity(new Intent(LoginActivity.this, SplashActivity.class));
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            viewHolder.progressBar.setVisibility(View.GONE);
        }
    }


    class ViewHolder {
        // UI references.
        private EditText userNameView;
        private EditText phoneNumberView;
        private Button loginButton;
        private ProgressBar progressBar;
        private TextView signUpButton;

        public ViewHolder(){
            progressBar = (ProgressBar) findViewById(R.id.login_progress);
            phoneNumberView = (EditText) findViewById(R.id.phone_number_head);
            userNameView = (EditText) findViewById(R.id.user_name);
            loginButton = (Button)findViewById(R.id.sign_in_button);
            signUpButton = (TextView)findViewById(R.id.text_sign_up);
        }
    }
}

