package kr.sysgen.taxi.activity.Fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.MainActivity;
import kr.sysgen.taxi.activity.TrackingMapActivity;
import kr.sysgen.taxi.adapter.UserHistoryAdapter;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.data.TaxiUser;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaxiUserHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaxiUserHistoryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = TaxiUserHistoryFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "mem_name";
    private static final String ARG_PARAM2 = "memberInfo";

    private String memberName;
    private String memberInfo;

    private ViewHolder viewHolder;

    private ProgressDialog progressDialog;

    public TaxiUserHistoryFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userName 화면에 보여지는 사람의 이름
     * @param memberInfo 화면에 보여지는 사용자의 정보
     *
     * @return A new instance of fragment TaxiUserHistoryFragment.
     */
    public static TaxiUserHistoryFragment newInstance(String userName, String memberInfo) {
        TaxiUserHistoryFragment fragment = new TaxiUserHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, userName);
        args.putString(ARG_PARAM2, memberInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memberName = getArguments().getString(ARG_PARAM1);
            memberInfo = getArguments().getString(ARG_PARAM2);
        }

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getToolbar().setTitle(memberName + " 님의 택시 탑승 이력");

        View view = inflater.inflate(R.layout.fragment_taxi_user_info, container, false);
        viewHolder = new ViewHolder(view);
        viewHolder.refreshLayout.setOnRefreshListener(this);

        new GetUserHistoryTask().execute(memberInfo);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.log_out);
        item.setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onItemClicked(MemberInfo memberInfo) {
//        new GetTrackingInfo().execute(memberInfo.toString());
//
//        return false;
//    }

    @Override
    public void onRefresh() {
        final String memIdx = new SysgenPreference(getContext()).getString(getString(R.string.mem_idx));
        JSONObject json = new JSONObject();
        try {
            json.put(getString(R.string.mem_idx), memIdx);
        }catch(JSONException e){
            e.printStackTrace();
        }

        if(viewHolder.refreshLayout.isRefreshing()){
            viewHolder.refreshLayout.setRefreshing(false);
        }
        new GetUserHistoryTask().execute(memberInfo);
    }

    class GetUserHistoryTask extends AsyncTask<String, Void, String>{
        private ConnectToServer conn;
        private String jspFile;

        public GetUserHistoryTask(){
            this.conn = new ConnectToServer(getContext());
            this.jspFile = getString(R.string.getUserHistory);
        }

        @Override
        protected String doInBackground(String... params) {
            final String param = params[0];

            return conn.getJson(param, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            ArrayList<TaxiUser> taxiUsers = new ArrayList<>();

            try {
                JSONObject resultJson = new JSONObject(s);
                JSONArray array = resultJson.getJSONArray(getString(R.string.list));
                int arrayLength = array.length();

                for(int i=0; i<arrayLength; i++){
                    TaxiUser taxiUser = new TaxiUser();

                    JSONObject historyJson = array.getJSONObject(i);
                    String timeGetOn = historyJson.getString(getString(R.string.time_get_on));
                    String latitudeGetOn = historyJson.getString(getString(R.string.latitude_get_on));
                    String longitudeGetOn = historyJson.getString(getString(R.string.longitude_get_on));
                    String taxiNum = historyJson.getString(getString(R.string.taxi_num));
                    String taxiSCName = historyJson.getString(getString(R.string.taxi_sc_name));
                    String taxiName = historyJson.getString(getString(R.string.taxi_name));
                    String historyIndex = historyJson.getString(getString(R.string.history_idx));
                    String memberIndex = historyJson.getString(getString(R.string.mem_idx));

                    SimpleDateFormat sd = new SimpleDateFormat(getString(R.string.date_format));
                    sd.parse(timeGetOn);
                    Calendar calendar = sd.getCalendar();

                    SimpleDateFormat formatter = new SimpleDateFormat("MM월dd일 HH시mm분");

                    taxiUser.setDateGetOn(formatter.format(calendar.getTime()));
                    taxiUser.setLatitudeGetOn(latitudeGetOn);
                    taxiUser.setLongitudeGetOn(longitudeGetOn);
                    taxiUser.setTaxiName(taxiName);
                    taxiUser.setTaxiNumber(taxiNum);
                    taxiUser.setTaxiSCName(taxiSCName);
                    taxiUser.setHistoryIndex(historyIndex);
                    taxiUser.setMemberIndex(memberIndex);

                    taxiUsers.add(taxiUser);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }

            if(viewHolder.refreshLayout.isRefreshing()){
                viewHolder.refreshLayout.setRefreshing(false);
            }

            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    }

    class ViewHolder {
        public SwipeRefreshLayout refreshLayout;
        public RecyclerView recyclerView;
        public ViewHolder(View view){
            refreshLayout = (SwipeRefreshLayout)view;
            recyclerView = (RecyclerView)refreshLayout.findViewById(R.id.list);
        }
    }
}
