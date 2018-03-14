package kr.sysgen.taxi.activity;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.UserHistoryFragment;

public class UserHistoryActivity extends AppCompatActivity {
    private final String TAG = UserHistoryActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_slide_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.history_fragment_title));

        String memberIndex = getIntent().getStringExtra(getString(R.string.mem_idx));
        int memberStatus = getIntent().getIntExtra(getString(R.string.mem_status), 0);
        UserHistoryFragment userHistoryFragment = UserHistoryFragment.newInstance(memberIndex, memberStatus);
        addFragment(userHistoryFragment, UserHistoryFragment.TAG);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if(count == 0){
            finish();
        }
    }

    public int addFragment(Fragment fragment, String tag){
        return getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment, fragment)
                .addToBackStack(tag)
                .commit();
    }
}
