package kr.sysgen.taxi.activity.Fragment;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.TabFragments.PersonalTabFragment;
import kr.sysgen.taxi.adapter.TabAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public static final String TAG = MainActivityFragment.class.getSimpleName();
//    private TextView monitoringView;
    private String parameter;
    private final static String ARG_PARAM = "ARG_PARAM";

    public static MainActivityFragment newInstance(String param){
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        fragment.setArguments(args);
        return fragment;
    }
    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parameter = getArguments().getString(ARG_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout baseView = (RelativeLayout)inflater.inflate(R.layout.fragment_main, container, false);

        ViewPager viewPager = (ViewPager)baseView.findViewById(R.id.viewpager);
        TabAdapter adapter = new TabAdapter(getActivity().getSupportFragmentManager());

        try {
            PersonalTabFragment personalTabFragment = PersonalTabFragment.newInstance(null);

//            ParentModeFragment parentModeFragment = ParentModeFragment.newInstance(1);
            ChildModeFragment childModeFragment = ChildModeFragment.newInstance(null, null);

            adapter.addFragment(personalTabFragment, "보호자모드");
//            adapter.addFragment(parentModeFragment, "보호자모드");
            adapter.addFragment(childModeFragment, "탑승자모드");
            viewPager.setAdapter(adapter);
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        TabLayout tabLayout = (TabLayout)baseView.findViewById(R.id.tabs_layout);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);

        return baseView;
    }
}