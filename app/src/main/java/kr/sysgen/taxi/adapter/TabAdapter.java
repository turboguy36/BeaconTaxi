package kr.sysgen.taxi.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.MemberInfo;

/**
 * Created by leehg on 2016-05-19.
 */
public class TabAdapter extends FragmentPagerAdapter {
    private final String TAG = TabAdapter.class.getSimpleName();

    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();

    public TabAdapter(FragmentManager fm){
        super(fm);
    }
    public TabAdapter(FragmentManager fm, Context context){
        super(fm);
//        this.mContext = context;
//        icons = new ArrayList<>();
    }
    public void setData(ArrayList<MemberInfo> data){}

    public void addFragment(Fragment fragment, String title){
        mFragments.add(fragment);
        mFragmentTitles.add(title);
        Log.i(TAG, "title: " + title);
    }
    public void addFragment(Fragment fragment){
        mFragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public void clear(){
        mFragments.clear();
    }
    public List<Fragment> getLists(){
        return this.mFragments;
    }
}
