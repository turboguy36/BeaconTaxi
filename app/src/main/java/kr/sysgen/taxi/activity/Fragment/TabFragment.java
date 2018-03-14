package kr.sysgen.taxi.activity.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.TabFragments.PersonalTabFragment;
import kr.sysgen.taxi.adapter.TabAdapter;

/**
 * Created by dev01 on 2016-10-27.
 */

public class TabFragment extends Fragment {

    public static TabFragment newInstance(String param){
        TabFragment fragment = new TabFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout baseView = (RelativeLayout)inflater.inflate(R.layout.fragment_main, container, false);
        ViewPager viewPager = (ViewPager)baseView.findViewById(R.id.viewpager);
        TabAdapter adapter = new TabAdapter(getActivity().getSupportFragmentManager());

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout)baseView.findViewById(R.id.tabs_layout);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.taxi_icon);
        return baseView;
    }
}
