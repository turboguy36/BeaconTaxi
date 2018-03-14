package kr.sysgen.taxi.activity.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.TabFragments.PersonalTabFragment;
import kr.sysgen.taxi.activity.MainActivity;
import kr.sysgen.taxi.adapter.TabAdapter;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * Created by dev01 on 2016-11-04.
 */

public class InitViewTabFragment extends BaseFragment implements View.OnClickListener{
    private final String TAG = InitViewTabFragment.class.getSimpleName();

    static final String INPUT_PARAM = "INPUT_PARAM";
    private String PARAM_RESULT;
    private ViewHolder viewHolder;
    private ArrayList<MemberInfo> memberList;

    public InitViewTabFragment() {
    }

    public static InitViewTabFragment newInstance(String input) {
        InitViewTabFragment fragment = new InitViewTabFragment();
        Bundle args = new Bundle();
        args.putString(INPUT_PARAM, input);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        PARAM_RESULT = getArguments().getString(INPUT_PARAM);
        memberList = parseMemberInfoList(PARAM_RESULT);

        try {
            ((MainActivity) getActivity()).setToolbarTitle(memberList.get(0).getMemberName());
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        viewHolder = new ViewHolder(inflater.inflate(R.layout.fragment_main, container, false));
        viewHolder.setBackground();
        setView(memberList);

        return viewHolder.baseView;
    }

    private void setView(final ArrayList<MemberInfo> s){
        TabAdapter adapter = new TabAdapter(getActivity().getSupportFragmentManager(), getContext());

        for(MemberInfo info: s){
            PersonalTabFragment personalTabFragment = PersonalTabFragment.newInstance(info);
            adapter.addFragment(personalTabFragment, info.getMemberName());
        }

        viewHolder.viewPager.setAdapter(adapter);
        viewHolder.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((MainActivity)getActivity()).setToolbarTitle(s.get(position).getMemberName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewHolder.tabLayout.setupWithViewPager(viewHolder.viewPager);

        viewHolder.progressBar.setVisibility(View.GONE);
        try {
            setupTabIcons();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    private void setupTabIcons()throws  NullPointerException{
        viewHolder.tabLayout.getTabAt(0).setIcon(R.drawable.avartar_03);
        viewHolder.tabLayout.getTabAt(1).setIcon(R.drawable.avartar_01);
        viewHolder.tabLayout.getTabAt(2).setIcon(R.drawable.avartar_02);
        viewHolder.tabLayout.getTabAt(3).setIcon(R.drawable.avartar_03);
    }

    private ArrayList<MemberInfo> parseMemberInfoList(String input){
        ArrayList<MemberInfo> items = new ArrayList<>();
        try {
            JSONObject inputJson = new JSONObject(input);
            JSONArray jsonArray = inputJson.getJSONArray(getString(R.string.list));

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject item = jsonArray.getJSONObject(i);
                MemberInfo memberInfo = new MemberInfo();

                String memberIndex = item.getString(getString(R.string.mem_idx));
                String memberName = item.getString(getString(R.string.mem_name));
                String memberPhoneNumber = item.getString(getString(R.string.mem_hp_num));
                String memberState = item.getString(getString(R.string.mem_status));
                if(item.has(getString(R.string.mem_lat))) {
                    String memberLatitude = item.getString(getString(R.string.mem_lat));
                    memberInfo.setLatitude(Double.parseDouble(memberLatitude));
                }
                if(item.has(getString(R.string.mem_lng))) {
                    String memberLongitude = item.getString(getString(R.string.mem_lng));
                    memberInfo.setLongitude(Double.parseDouble(memberLongitude));
                }

                memberInfo.setMemberIndex(memberIndex);
                memberInfo.setMemberName(memberName);
                memberInfo.setMemberPhoneNumber(memberPhoneNumber);

                try{
                    memberInfo.setStatus(Integer.parseInt(memberState));
                }catch(NumberFormatException e){
                    memberInfo.setStatus(0);
                    e.printStackTrace();
                }
                items.add(memberInfo);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart() ");
    }

//    @Override
//    public boolean onItemClickListener(MemberInfo memberInfo) {
//        return super.onItemClickListener(memberInfo);
//    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "clicked v: " + v);
    }
}
