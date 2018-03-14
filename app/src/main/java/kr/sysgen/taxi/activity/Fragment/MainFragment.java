package kr.sysgen.taxi.activity.Fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.TabFragments.PersonalTabFragment;
import kr.sysgen.taxi.activity.GetContactActivity;
import kr.sysgen.taxi.activity.SlideMenuActivity;
import kr.sysgen.taxi.adapter.TabAdapter;
import kr.sysgen.taxi.data.MemberInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements ViewPager.OnPageChangeListener{
    public static final String TAG = MainFragment.class.getSimpleName();

    private ViewHolder viewHolder;
    private String PARAM_RESULT;
    private static final String ARG_PARAM1 = "param1";
    private ArrayList<MemberInfo> memberList;
    private TabAdapter adapter;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new TabAdapter(getActivity().getSupportFragmentManager(), getContext());

        PARAM_RESULT = getArguments().getString(ARG_PARAM1);

        memberList = parseMemberInfoList(PARAM_RESULT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if(savedInstanceState != null) {
//            final String savedState = savedInstanceState.getString(TAG);
//            Log.i(TAG, "savedState: " + savedState);
//        } else {
//            Log.i(TAG, "savedState null");
//        }

        viewHolder = new ViewHolder(inflater.inflate(R.layout.fragment_main, container, false));
        viewHolder.setBackground();

        viewHolder.viewPager.addOnPageChangeListener(this);
        viewHolder.tabLayout.setupWithViewPager(viewHolder.viewPager);

        try {
            viewHolder.progressBar.setVisibility( View.GONE );
            setViewPagerAdapter();
            setupTabIcons(memberList);
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        try {
            ((SlideMenuActivity) getActivity()).setToolbarTitle(memberList.get(0).getMemberName());
        }catch(IndexOutOfBoundsException e){
            ((SlideMenuActivity) getActivity()).setToolbarTitle(getString(R.string.please_add_child));
            e.printStackTrace();
        }

        return viewHolder.baseView;
    }


    /**
     * 내 자녀 리스트를 탭 형식으로 만든다.
     */
    private void setViewPagerAdapter(){
        if(adapter.getLists().size() == 0) {
            for (int i = 0; i < memberList.size(); i++) {
                try {
                    MemberInfo info = memberList.get(i);
                    PersonalTabFragment personalTabFragment = PersonalTabFragment.newInstance(info);
                    adapter.addFragment(personalTabFragment, info.getMemberName());
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            // 탭 마지막에 추가 버튼을 만든다.
            adapter.addFragment(addEmptyFragment());
            viewHolder.viewPager.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();

        }
    }
    private Fragment addEmptyFragment(){
        return PersonalTabFragment.newInstance(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ((SlideMenuActivity) getActivity()).setToolbarTitle(R.string.please_add_child);

        if(requestCode == SlideMenuActivity.REQUEST_CODE_GET_CONTACT && resultCode == Activity.RESULT_OK) {
            int currentPosition = memberList.size()-1;

            viewHolder.tabLayout.setScrollPosition(currentPosition, 0.0f, true);
            viewHolder.viewPager.setCurrentItem(currentPosition, true);
        }
    }

    private void setupTabIcons(ArrayList<MemberInfo> memberList)throws  NullPointerException{
        for(int i=0; i<=memberList.size(); i++) {
            int drawableId = (i == memberList.size()) ? R.drawable.content_new_black : R.drawable.avartar_03;
            viewHolder.tabLayout.getTabAt(i).setIcon(drawableId);
        }
    }

    /**
     * JSON to MemberInfo List
     *
     * @param "SlideMenuActivity" 의 GetMyChildrenTask 에서 넘긴 파라미터 input(JSON 형태의 String)
     * @return ArrayList 형태의 MemberInfo 개체
     */
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

                String historyIndex = item.getString(getString(R.string.history_idx));
                memberInfo.setHistoryIndex(historyIndex);

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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // TODO: NOTING 손으로 페이지를 왼쪽 오른쪽 넘길 동안 offSet 변화가 callback 되어 들어온다.
    }

    @Override
    public void onPageSelected(int position) {
        try {
            ((SlideMenuActivity) getActivity()).setToolbarTitle(memberList.get(position).getMemberName());
        }catch(IndexOutOfBoundsException e){
            ((SlideMenuActivity) getActivity()).setToolbarTitle(getString(R.string.please_add_child));
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "super.onPause();");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "super.onDestroyView();");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "super.onSaveInstanceState(outState);");
        outState.putString(TAG, "hi there");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "super.onResume();");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "super.onStart();");

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "super.onAttach(context);");
    }

    protected class ViewHolder{
        View baseView;
        ViewPager viewPager;
        TabLayout tabLayout;
        ProgressBar progressBar;

        public ViewHolder(View baseView){
            this.baseView = baseView;
            viewPager = (ViewPager)baseView.findViewById(R.id.viewpager);
            tabLayout = (TabLayout)baseView.findViewById(R.id.tabs_layout);
            progressBar = (ProgressBar)baseView.findViewById(R.id.progress_bar);
        }
        public void setBackground(){
            BitmapDrawable bg = new BitmapDrawable(getResources(), blur(getContext(), BitmapFactory.decodeResource(getResources(), R.drawable.city_view), 20));
            baseView.setBackground(bg);
        }
        private Bitmap blur(Context context, Bitmap sentBitmap, int radius) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

                final RenderScript rs = RenderScript.create(context);
                final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
                final Allocation output = Allocation.createTyped(rs, input.getType());
                final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                script.setRadius(radius); //0.0f ~ 25.0f
                script.setInput(input);
                script.forEach(output);
                output.copyTo(bitmap);
                return bitmap;
            }else{
                return null;
            }
        }
    }

}
