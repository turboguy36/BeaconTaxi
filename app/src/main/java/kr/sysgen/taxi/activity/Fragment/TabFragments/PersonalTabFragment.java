package kr.sysgen.taxi.activity.Fragment.TabFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.HistoryFragment;
import kr.sysgen.taxi.activity.Fragment.TaxiInfoFragment;
import kr.sysgen.taxi.activity.GetContactActivity;
import kr.sysgen.taxi.activity.SlideMenuActivity;
import kr.sysgen.taxi.activity.TrackingMapActivity;
import kr.sysgen.taxi.activity.UserHistoryActivity;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.data.TaxiInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * Created by leehg on 2016-10-26.
 */
public class PersonalTabFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {
    public static final String TAG = PersonalTabFragment.class.getSimpleName();

    private static final String MEMBER_INFO_TAG = "member_info_tag";

    private MemberInfo memberInfo;

    private ViewHolder viewHolder;

    // 탑승한 사용자의 위치를 표시 하기 위한 맵 구성 : OnMapReadyCallback 을 implement 해야 한다.
    private GoogleMap mMap;

    // AsyncTask : 평상시(탑승 하지 않은 사용자) 화면 구성
    private GetUserTaxiHistoryTask getUserTaxiHistoryTask;

    // AsyncTask : 탑승한 사용자의 현재 위치를 지도에 표시한다.
    private GetUserLocation getUserLocation;

    // (현위치) 지도 핀
    private Bitmap currentPin;
    private Bitmap pinOn;
    private Bitmap pinOff;
    private boolean isOn = true;
    private Handler handler;
    private Runnable runnable;
    /**
     *
     * @param info : MainFragment 로 부터 내자녀 정보(MemberInfo 를 받는다)
     * @return : 생성된 객체
     */
    public static PersonalTabFragment newInstance(MemberInfo info) {
        PersonalTabFragment fragment = new PersonalTabFragment();
        Bundle args = new Bundle();
        if (info != null) {
            args.putString(MEMBER_INFO_TAG, info.toString());
            fragment.setArguments(args);
        }
        return fragment;
    }

    /**
     * 전역변수 memberInfo 에 MainFragment 에서 받아온 정보를 Parsing 한다.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            String memberInfoString = getArguments().getString(MEMBER_INFO_TAG);
            memberInfo = MemberInfo.parseMemberInfo(memberInfoString);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 내 자녀 사용자의 현재 상태를 화면에 보여준다.
     * a. 탑승 하지 않은
     * b. 탑승 하여 이동중인
     * c. 자녀 추가하기 화면
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_tab, container, false);
        viewHolder = new ViewHolder(view);
        viewHolder.setClickListener();

        currentPin = resizeDrawable(R.drawable.placeholder_current);

        if (memberInfo == null) {
            // c. 자녀 추가하기 화면
            makeLastTab();
        } else {
            if (memberInfo.getStatus() == 1 && Integer.parseInt(memberInfo.getHistoryIndex()) > 0) {
                // b. 탑승 하여 이동중인
                makeTaxiUserLayout();
            } else {
                // a. 탑승 하지 않은
                makeAidleStateUserLayout();
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "super.onResume();");
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "super.onResume();"+ memberInfo.getMemberName());

        if(pinOn == null) {
            pinOn = resizeDrawable(R.drawable.tracking_pin_on);
        }
        if(pinOff ==null) {
            pinOff = resizeDrawable(R.drawable.tracking_pin);
        }
        if(handler == null && runnable!=null) {
            handler = new Handler();
            handler.postDelayed(runnable, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "super.onPause();" + memberInfo.getMemberName());
        if(runnable != null && handler!=null) {
            handler.removeCallbacks(runnable);
        }
    }
*/

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            // 현재 보여지는 User 의 위치상태를 받아와 새로고침
            getUserLocation = new GetUserLocation(false);
            getUserLocation .execute(memberInfo.getMemberIndex());
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * 택시에 탑승한 사용자 화면 구성
     */
    private void makeTaxiUserLayout(){
        viewHolder.personView.setImageDrawable(getContext().getDrawable(R.drawable.avartar_01));

        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        replaceFragment(R.id.map_layout, supportMapFragment);
        supportMapFragment.getMapAsync(this);

        viewHolder.mapBigger.setVisibility(View.VISIBLE);
        viewHolder.currentLocation.setVisibility(View.VISIBLE);
        viewHolder.taxiInfo.setText(getString(R.string.taxi_information));
    }
    /**
     * 평상시(탑승 하지 않은 사용자) 화면 구성
     */
    private void makeAidleStateUserLayout(){
        viewHolder.personView.setImageDrawable(getContext().getDrawable(R.drawable.avartar_01));

        getUserTaxiHistoryTask = new GetUserTaxiHistoryTask(getContext());
        getUserTaxiHistoryTask.execute(memberInfo.getMemberIndex(), memberInfo.getHistoryIndex());

        viewHolder.taxiInfo.setVisibility(View.VISIBLE);
        viewHolder.taxiInfo.setText(getString(R.string.show_tracking));
    }
    /**
     * 마지막 탭의 자녀 추가하기 화면
     */
    private void makeLastTab(){
        viewHolder.textBox.setVisibility(View.VISIBLE);
        viewHolder.textView.setText(getString(R.string.please_add_child));
        viewHolder.phoneView.setVisibility(View.INVISIBLE);
        viewHolder.smsView.setVisibility(View.INVISIBLE);
        viewHolder.personView.setImageDrawable(getContext().getDrawable(R.drawable.content_new_black));

        viewHolder.taxiInfo.setVisibility(View.GONE);
        viewHolder.onTaxi.setVisibility(View.GONE);
    }
    /**
     * Fragment 내부에 자녀 Fragment 를 생성
     *
     * @param layout
     * @param fragment
     * @return
     */
    private int replaceFragment(int layout, Fragment fragment)throws IllegalStateException{
        return getChildFragmentManager().beginTransaction().replace(layout, fragment).commit();
    }

    @Override
    public void onClick(View v) {
        SysgenPreference pref = new SysgenPreference(getContext());
        switch (v.getId()) {
            case R.id.button_person:
                if (memberInfo == null) {
                    Intent intent = new Intent(getContext(), GetContactActivity.class);

                    getActivity().startActivityForResult(intent, SlideMenuActivity.REQUEST_CODE_GET_CONTACT);
                } else {
                    Log.i(TAG, memberInfo.toString());
                }
                break;
            case R.id.button_sms:
                //
                String myName = pref.getString(getString(R.string.mem_name));
                Uri uri = Uri.parse("smsto:" + memberInfo.getMemberPhoneNumber());
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                final String sendMessage = new StringBuffer()
                        .append(myName)
                        .append(" 님이 택시에 탑승 하셨습니다.")
                        .toString();
                intent.putExtra("sms_body", sendMessage);
                startActivity(intent);
                break;
            case R.id.button_call:
                Uri callUri = Uri.parse("tel:" + memberInfo.getMemberPhoneNumber());
                Intent callIntent = new Intent(Intent.ACTION_DIAL, callUri);
                startActivity(callIntent);
                break;
            case R.id.show_route:
                if(memberInfo.getStatus() == 1) { // 탑승중
                    getUserLocation = new GetUserLocation(true);
                    getUserLocation.execute(memberInfo.getMemberIndex());
                }else{
                    // 하차중 -> 최근경로보기
                    JSONObject tagJson = (JSONObject) v.getTag();

                    Intent trackingMapIntent = new Intent(getContext(), TrackingMapActivity.class);
                    Bundle bundle = new Bundle();
                    try {
                        bundle.putString(getString(R.string.tracking_result), tagJson.toString());
                        trackingMapIntent.putExtra(getString(R.string.parameter), bundle);
                        getActivity().startActivity(trackingMapIntent);
                    }catch(NullPointerException e){
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.open_map_bigger:
                // 택시 탑승자의 이동 경로를 전체적으로 크게 본다
                new GetTrackingInfo().execute(memberInfo.toString());
                break;
            case R.id.current_location:
                // 맵이 이동 되었을 경우 핀의 가운데로 맵을 이동
                moveCamera(memberInfo.getLatitude(), memberInfo.getLongitude());
                break;
            case R.id.show_history:
                // 그 동안 타고 내린 택시 정보를 열람
                Intent activityIntent = new Intent (getActivity(), UserHistoryActivity.class);
                activityIntent.putExtra(getString(R.string.mem_idx), memberInfo.getMemberIndex());
                activityIntent.putExtra(getString(R.string.mem_status), memberInfo.getStatus());
                startActivity(activityIntent);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // 택시 탑승자의 현재 위치 표시
        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        updateMap(memberInfo.getLatitude(), memberInfo.getLongitude());
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // 중요!! Fragment 전환시 AsyncTask 가 Background 로 계속 진행 되는 것을 막는다.
        if(getUserTaxiHistoryTask != null) {
            getUserTaxiHistoryTask.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (viewHolder!=null && viewHolder.mapView != null) {
            ViewGroup parent = (ViewGroup) viewHolder.mapView.getParent();
            if (parent != null) {
                parent.removeView(viewHolder.mapView);
            }
        }
    }

    private Bitmap resizeDrawable(int icon){
        final int scale_60 = 60;
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), icon);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, scale_60, scale_60, false);
        return resizedBitmap;
    }

    private void updateMap(double latitude, double longitude) throws NullPointerException{
        if(mMap != null) {
            if (latitude == 0.0d && longitude == 0.0d) {
                viewHolder.noLocationView.setVisibility(View.VISIBLE);

            } else {
                try {
                    mMap.clear();
                }catch(IllegalStateException e){
                    e.printStackTrace();
                }
                LatLng currentLoc = new LatLng(latitude, longitude);
                MarkerOptions current = new MarkerOptions()
//                        .icon(BitmapDescriptorFactory.fromBitmap(currentPin))
                        .position(currentLoc)
                        .title(getString(R.string.current_location));
                final Marker marker = mMap.addMarker(current);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17.0f));
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(currentPin));

//                runnable =new Runnable() {
//                    @Override
//                    public void run() {
//                        if(marker != null) {
//                            if (isOn) {
//                                try {
//                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(pinOff));
//                                }catch(IllegalArgumentException e){
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                try{
//                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(pinOn));
//                                }catch(IllegalArgumentException e){
//                                    e.printStackTrace();
//                                }
//                            }
//                            isOn = !isOn;
//                            handler.postDelayed(this, 1000);
//                        }
//                    }
//                };
//
//                if(handler != null) {
//                    handler.postDelayed(runnable, 500);
//                }
            }
        }
    }

    private void moveCamera(double latitude, double longitude){
        if(mMap != null) {
            LatLng currentLoc = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17.0f));
        }
    }

    /**
     * 현재 탑승 하고 있는 사용자의
     * 탑승-> 현위치 까지 경로를 보고 싶을 때
     * [TrackingInfo List] 를 받아와 TrackingMapActivity 에서 보여준다.
     */
    private class GetTrackingInfo extends AsyncTask<String, Void, String> {
        private String jspFile;
        private ConnectToServer conn;
        private ProgressDialog progressDialog;

        public GetTrackingInfo() {
            jspFile = getString(R.string.getTracking);
            conn = new ConnectToServer(getActivity());
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.please_wait));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            final String param = params[0];
            return conn.getJson(param, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Intent intent = new Intent(getContext(), TrackingMapActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.tracking_result), s);
            intent.putExtra(getString(R.string.parameter), bundle);

            progressDialog.dismiss();

            getActivity().startActivity(intent);
        }
    }

    /**
     * 1-1. Fragment 가 처음 시작 할 때 탑승자의 위치를 Map 에 보여준다.
     * 1-2. 현위치 새로고침
     *   2. 유저의 택시정보 조회
     */
    class GetUserLocation extends AsyncTask<String, Void, String>{
        private final ConnectToServer conn;
        private String jspFile;

        private final boolean isStartTaxiInfo; // true : 택시 정보를 열람 || false : 현재 위치 새로고침

        public GetUserLocation(boolean startTaxiInfo) {
            conn = new ConnectToServer(getContext());
            jspFile = getString(R.string.getMyInfo);
            this.isStartTaxiInfo = startTaxiInfo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected String doInBackground(String... params) {
            final String myId = params[0];
            JSONObject paramJson = new JSONObject();
            try {
                paramJson.put(getString(R.string.mem_idx), myId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return conn.getJson(paramJson.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                MemberInfo result = MemberInfo.parseMemberInfo(s);
                int status = result.getStatus();
                double lat = result.getLatitude();
                double lng = result.getLongitude();
                String historyIndex = result.getHistoryIndex();

                if(status > 0 && lat>0 && lng>0){
                    memberInfo.setStatus(status);
                    memberInfo.setLatitude(lat);
                    memberInfo.setLongitude(lng);
                    memberInfo.setHistoryIndex(historyIndex);
                }
                if(!isCancelled()) {
                    updateMap(memberInfo.getLatitude(), memberInfo.getLongitude());
                }
            }catch(JSONException e){
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }

            if(isStartTaxiInfo && !isCancelled()) {
                openTaxiInfoFragment(s);
            }
        }
        private void openTaxiInfoFragment(String s) {
            try {
                JSONObject json = new JSONObject(s);
                String taxiIdx = json.getString(getString(R.string.taxi_idx));

                TaxiInfoFragment taxiInfoFragment = TaxiInfoFragment.newInstance(taxiIdx);
                ((SlideMenuActivity) getActivity()).addFragment(taxiInfoFragment, TaxiInfoFragment.TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 마지막으로 탑승 했던 택시의 기록을 가져온다.
     */
    private class GetUserTaxiHistoryTask extends AsyncTask<String, Void, String>{
        private String jspFile;
        private ConnectToServer conn;
        private Context mContext;

        public GetUserTaxiHistoryTask(Context context){
            conn = new ConnectToServer(context);
            jspFile = getString(R.string.getUserTaxiInfoHistory);
            this.mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            final String memberIndex = params[0];
            final String historyIndex = params[1];

            JSONObject json = new JSONObject();
            try {
                if(!getUserTaxiHistoryTask.isCancelled()) {
                    json.put(mContext.getString(R.string.mem_idx), memberIndex);
                    json.put(mContext.getString(R.string.history_idx), historyIndex);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }

            return conn.getJson(json.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(!getUserTaxiHistoryTask.isCancelled()) {
                try {
                    TaxiInfo lastTaxiInfo = new TaxiInfo();

                    JSONObject json = new JSONObject(s);
                    if(json.has(getString(R.string.taxi_num))) {
                        String taxiNumber = json.getString(getString(R.string.taxi_num));
                        lastTaxiInfo.setTaxiNumber(taxiNumber);
                    }
                    if(json.has(getString(R.string.time_get_on))) {
                        String timeGetOn = json.getString(getString(R.string.time_get_on));
                        lastTaxiInfo.setDate(timeGetOn);
                    }
                    if(json.has(getString(R.string.mem_address))){
                        String getOnAddress = json.getString(getString(R.string.mem_address));
                        lastTaxiInfo.setGetOnTaxiAddress(getOnAddress);
                    }
                    if(json.has(getString(R.string.address_get_off))){
                        String getOffAddress = json.getString(getString(R.string.address_get_off));
                        lastTaxiInfo.setGetOffTaxiAddress(getOffAddress);
                    }

                    {   // 마지막 탑승 했던 택시 경로 보여주기 위해 TAG 저장
                        // [최근경로보기] 버튼 클릭 했을 때 필요한 정보
                        JSONArray array = json.getJSONArray(getString(R.string.list));
                        JSONObject tagJson = new JSONObject();
                        tagJson.put(getString(R.string.tracking_array), array);
                        viewHolder.taxiInfo.setTag(tagJson);
                    }

                    HistoryFragment fragment = HistoryFragment.newInstance(lastTaxiInfo.getTaxiNumber(), lastTaxiInfo.getDate(), lastTaxiInfo.getGetOnTaxiAddress(), lastTaxiInfo.getGetOffTaxiAddress());
                    replaceFragment(R.id.map_layout, fragment);
                }catch(JSONException e){
                    e.printStackTrace();
                }catch(IllegalStateException e){
                    e.printStackTrace();
                }
            }
        }
    }

    class ViewHolder {
        public View baseView;
        public ImageView personView;
        public ImageView smsView;
        public ImageView phoneView;
        public TextView textView;
        public FrameLayout textBox;
        public FrameLayout mapView;
        public Button taxiInfo;
        public Button onTaxi;
        public Button mapBigger;
        public Button currentLocation;
        public FrameLayout noLocationView;

        public ViewHolder(View view) {
            this.baseView = view;
            personView = (ImageView) baseView.findViewById(R.id.button_person);
            smsView = (ImageView) baseView.findViewById(R.id.button_sms);
            phoneView = (ImageView) baseView.findViewById(R.id.button_call);
            textBox = (FrameLayout) baseView.findViewById(R.id.text_box);
            textView = (TextView) baseView.findViewById(R.id.text_view);
            mapView = (FrameLayout) baseView.findViewById(R.id.map_layout);
            taxiInfo = (Button) baseView.findViewById(R.id.show_route);
            onTaxi = (Button) baseView.findViewById(R.id.show_history);
            mapBigger = (Button) baseView.findViewById(R.id.open_map_bigger);
            currentLocation = (Button)baseView.findViewById(R.id.current_location);
            noLocationView = (FrameLayout) baseView.findViewById(R.id.no_location_message);
        }

        public void setClickListener() {
            personView.setOnClickListener(PersonalTabFragment.this);
            smsView.setOnClickListener(PersonalTabFragment.this);
            phoneView.setOnClickListener(PersonalTabFragment.this);
            taxiInfo.setOnClickListener(PersonalTabFragment.this);
            onTaxi.setOnClickListener(PersonalTabFragment.this);
            mapBigger.setOnClickListener(PersonalTabFragment.this);
            currentLocation.setOnClickListener(PersonalTabFragment.this);
            noLocationView.setOnClickListener(PersonalTabFragment.this);
        }
    }
}