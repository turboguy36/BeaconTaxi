package kr.sysgen.taxi.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import kr.sysgen.taxi.R;

public class GetContactActivity extends AppCompatActivity {
    public static final int ACTIVITY_ID = 6900;
    public static final String TAG = GetContactActivity.class.getSimpleName();

    public Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_contact);

        toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
    }

    public int replaceFragment(Fragment fragment){
        return getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();
    }
}
