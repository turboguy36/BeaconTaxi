package kr.sysgen.taxi.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.InvitationListFragment;
import kr.sysgen.taxi.activity.Fragment.MainFragment;
import kr.sysgen.taxi.activity.Fragment.TabFragments.MyInfoFragment;
import kr.sysgen.taxi.activity.Fragment.TabFragments.PersonalTabFragment;

import kr.sysgen.taxi.activity.Fragment.SettingsFragment;
import kr.sysgen.taxi.activity.Fragment.TaxiInfoFragment;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.service.BeaconListener;
import kr.sysgen.taxi.service.TimerService;
import kr.sysgen.taxi.util.SysgenPreference;

public class SlideMenuActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{
    private final String TAG = SlideMenuActivity.class.getSimpleName();

    /**
     * 왼쪽 메뉴 화면 컨트롤러
     * 메뉴가 열려 있는 동안 뒤로가기 버튼이 눌리면 메뉴를 닫는다.
     */
    private DrawerLayout drawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 퍼미션을 아직 허가 하지 않은 사용자에게 허가 요구하기.
        requestPermission();

        setToolbar("");

        initNavigationDrawer();

        // User Id
        Bundle bundle = getIntent().getExtras();
        int memberIndex = bundle.getInt(getString(R.string.mem_idx));

        new GetMyChildrenTask().execute(memberIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == REQUEST_CODE_GET_CONTACT){
//            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
//            if(fragment instanceof MainFragment){
//                ((MainFragment)fragment).onActivityResult(requestCode, resultCode, data);
//            }
//        }
    }

    public void initNavigationDrawer() {
        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        View header = navigationView.getHeaderView(0);
        header.setOnClickListener(this);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){

            @Override
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
    }

    private boolean isUserLocationTime(){
        SysgenPreference pref = new SysgenPreference(this);
        final String startTime = pref.getString(getString(R.string.location_information_start));
        final String endTime = pref.getString(getString(R.string.location_information_end));

        SimpleDateFormat format = new SimpleDateFormat(getString(R.string.hour_format));
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();

//        Log.i(TAG, "start: " + startTime + " /end: "+endTime);
        try {
            startCalendar.setTime(format.parse(startTime));
        }catch(ParseException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        try{
            endCalendar.setTime(format.parse(endTime));
        }catch(ParseException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String fragmentTag = MainFragment.TAG;

        drawerLayout.closeDrawer(GravityCompat.START);

        switch(item.getItemId()){
            case R.id.notification:
                isUserLocationTime();
                break;
            case R.id.my_page:
//                MyInfoFragment myInfoFragment = MyInfoFragment.newInstance(null, null);
//                addFragment(myInfoFragment, fragmentTag);
                break;
            case R.id.settings:
                SettingsFragment settingsFragment = SettingsFragment.newInstance(null, null);
                addFragment(settingsFragment, SettingsFragment.TAG);
                break;
            case R.id.call_center:
                InvitationListFragment invitationListFragment = InvitationListFragment.newInstance(1);
                addFragment(invitationListFragment, InvitationListFragment.TAG);
                break;
            case R.id.log_out:
                getDialog(R.string.want_to_logout).show();
                break;
        }
        return false;
    }

    private AlertDialog.Builder getDialog(int message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_dialog_alert)
                .setMessage(message)
                .setPositiveButton(R.string.text_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                            }
                        })
                .setNegativeButton(R.string.text_cancel,
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                Do Nothing
                            }
                        });
        return builder;
    }
    private void logout(){
        SysgenPreference sysgenPreference = new SysgenPreference(this);
        sysgenPreference.removeAll();

        finish();

        startActivity(new Intent(this, SplashActivity.class));
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, BeaconListener.class));
    }

    @Override
    public void onClick(View v) {
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            int count = getSupportFragmentManager().getBackStackEntryCount();
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);

            if (count == 0 && fragment instanceof MainFragment) {
                super.onBackPressed();
            } else if(count > 0 && fragment instanceof PersonalTabFragment){
                getSupportFragmentManager().popBackStack(PersonalTabFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else if(count > 0 && fragment instanceof MyInfoFragment){
                getSupportFragmentManager().popBackStack(MyInfoFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else if(fragment instanceof TaxiInfoFragment){
                getSupportFragmentManager().popBackStack(TaxiInfoFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else if(fragment instanceof SettingsFragment){
                getSupportFragmentManager().popBackStack();
            } else if(fragment instanceof InvitationListFragment){
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    private class GetMyChildrenTask extends AsyncTask<Integer,Void,String> {
        private ConnectToServer conn;
        private final String jspFile;

        public GetMyChildrenTask() {
            conn = new ConnectToServer(SlideMenuActivity.this);
            jspFile = getString(R.string.getMyChildren);
        }

        @Override
        protected String doInBackground(Integer... params) {
            JSONObject paramJson = new JSONObject();

            try {
                paramJson.put(getString(R.string.rl_mem_idx2), String.valueOf(params[0]));
            }catch(JSONException e){
                e.printStackTrace();
            }

            return conn.getJson(paramJson.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(!isCancelled()) {
                MainFragment fragment = MainFragment.newInstance(s);
                try {
                    addFragment(fragment);
                }catch(IllegalStateException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
