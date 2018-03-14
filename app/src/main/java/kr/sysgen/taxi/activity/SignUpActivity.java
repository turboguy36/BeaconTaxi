package kr.sysgen.taxi.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.SignUpFragment;
import kr.sysgen.taxi.util.SysgenPreference;

public class SignUpActivity extends BaseActivity {
    private final String TAG = SignUpActivity.class.getSimpleName();
    public static final String PROPERTY_REG_ID = "registration_id";
    public Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.icon_small_white_32);
            toolbar.setTitle(getString(R.string.title_of_sign_up));
            setSupportActionBar(toolbar);
        }
        SysgenPreference sysgenPreference = new SysgenPreference(this);
        sysgenPreference.removeAll();

        if(savedInstanceState == null){
            SignUpFragment signUpFragment = SignUpFragment.newInstance();
            replaceFragment(signUpFragment);
        }
    }

    public String getAndroidVersion(){
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    public int replaceFragment(Fragment fragment){
        return getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment, fragment)
                .commit();
    }

    public int replaceFragment(Fragment fragment, String tag){
        return getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .addToBackStack(tag)
                .commit();
    }

    public String getRegistrationId(Context context) {
        StringBuilder token = new StringBuilder();
        InstanceID instanceID = InstanceID.getInstance(this);
        String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
        try {
            token.append(instanceID.getToken(getString(R.string.sender_id), scope, null));
        }catch(IOException e){
            e.printStackTrace();
        }
        return token.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if(requestCode == SignUpFragment.RESULT_LOAD_IMG) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if(requestCode == BaseActivity.PERMISSION_REQUEST_CODE){
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
