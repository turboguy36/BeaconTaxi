package kr.sysgen.taxi.activity.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.MainActivity;
import kr.sysgen.taxi.activity.MyLocationMapActivity;
import kr.sysgen.taxi.adapter.GridAdapter;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChildModeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChildModeFragment extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{
    public static final String TAG = ChildModeFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private ViewHolder viewHolder;

    private String myId;
    private MemberInfo memberInfo;

    public ChildModeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChildModeFragment.
     */
    public static ChildModeFragment newInstance(String param1, String param2) {
        ChildModeFragment fragment = new ChildModeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SysgenPreference sysgenPreference = new SysgenPreference(getContext());
        myId = sysgenPreference.getString(getString(R.string.mem_idx));
        memberInfo = new MemberInfo();
        memberInfo.setMemberIndex(myId);

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProgressDialog progress = new ProgressDialog(getContext());
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage(getString(R.string.please_wait));
        progress.show();

        View view = inflater.inflate(R.layout.fragment_child_mode, container, false);
        viewHolder = new ViewHolder(view);
        viewHolder.swipeRefreshLayout.setOnRefreshListener(this);
        new GetMyInfoTask().execute(myId);

        GridView gridView = (GridView)view.findViewById(R.id.gridView);
        String[] items = getResources().getStringArray(R.array.grid_button_group);
        GridAdapter adapter = new GridAdapter(getContext(), R.layout.item_grid_buttons, items);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);

        progress.dismiss();
        return view;
    }
    private AlertDialog.Builder makeDialog(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.text_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
        return builder;
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private AlertDialog.Builder makeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("비상연락")
                .setMessage("보호자 에게 전화 할까요?")
                .setPositiveButton(getString(R.string.text_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(position){
            case 0:
                int status = (Integer)viewHolder.messageView.getTag(R.string.mem_status);
                if(status == MemberInfo.STATUS_IN_TAXI) {
                    TaxiInfoFragment fragment = TaxiInfoFragment.newInstance(memberInfo.getTaxiIndex());
                    ((MainActivity) getActivity()).addFragment(fragment, TaxiInfoFragment.TAG);
                }else{
                    String title = getString(R.string.title_alert);
                    String message = new StringBuffer().append(new SysgenPreference(getContext()).getString(getString(R.string.mem_name))).append(getString(R.string.message_not_find_car)).toString();

                    makeDialog(title, message).show();
                }
                break;
            case 1:

                Intent intent = new Intent(getContext(), MyLocationMapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.mem_name), memberInfo.getMemberName());
                bundle.putDouble(getString(R.string.mem_lat), memberInfo.getLatitude());
                bundle.putDouble(getString(R.string.mem_lng), memberInfo.getLongitude());
                bundle.putString(getString(R.string.mem_idx), memberInfo.getMemberIndex());

                intent.putExtra("bundle", bundle);
                getActivity().startActivity(intent);
                break;
            case 2:
                ShowMyParentFragment showMyParentFragment = ShowMyParentFragment.newInstance(1);
                ((MainActivity)getActivity()).addFragment(showMyParentFragment, ShowMyParentFragment.TAG);
                break;
            case 3:
                makeDialog().show();
                break;
        }
    }

    @Override
    public void onRefresh() {
        new GetMyInfoTask().execute(myId);
    }

    class ViewHolder{
        TextView messageView;
        GridView gridView;
        SwipeRefreshLayout swipeRefreshLayout;
        public ViewHolder(View baseView){
            this.gridView = (GridView)baseView.findViewById(R.id.gridView);
            this.messageView = (TextView)baseView.findViewById(R.id.user_tab_message);
            this.swipeRefreshLayout = (SwipeRefreshLayout)baseView.findViewById(R.id.swipeToRefresh);
        }
    }

    class GetMyInfoTask extends AsyncTask<String, Void, String>{
        private final ConnectToServer conn;
        private String jspFile;

        public GetMyInfoTask(){
            conn = new ConnectToServer(getContext());
            jspFile = getString(R.string.getMyInfo);
        }

        @Override
        protected String doInBackground(String... params) {
            final String myId = params[0];
            JSONObject paramJson = new JSONObject();
            try {
                paramJson.put(getString(R.string.mem_idx), myId);
            }catch(JSONException e){
                e.printStackTrace();
            }

            return conn.getJson(paramJson.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSONObject jsonResult = null;
            try {
                jsonResult = new JSONObject(s);
            }catch(JSONException e){
                e.printStackTrace();
            }
            try {
                if (jsonResult != null) {
                    setView(jsonResult);
                }
            }catch(IllegalStateException e){
                e.printStackTrace();
            }
            if(viewHolder.swipeRefreshLayout.isRefreshing()){
                viewHolder.swipeRefreshLayout.setRefreshing(false);
            }
        }
        private void setView(JSONObject jsonResult) throws IllegalStateException{
            int result = 0;
            int status = 0;
            String myName = null;

            if(jsonResult.has(getString(R.string.result_code))) {
                try {
                    result = Integer.parseInt(jsonResult.getString(getString(R.string.result_code)));
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            if(result == ConnectToServer.RESULT_OK){
                if(jsonResult.has(getString(R.string.mem_status))){
                    try {
                        status = Integer.parseInt(jsonResult.getString(getString(R.string.mem_status)));
                    }catch(JSONException | NumberFormatException e){
                        e.printStackTrace();
                    }
                }

                try {
                    myName = jsonResult.getString(getString(R.string.mem_name));
                    memberInfo.setMemberName(myName);
                }catch(JSONException e){
                    e.printStackTrace();
                }
                if(myName != null){
                    StringBuffer message = new StringBuffer();
                    if(status == MemberInfo.STATUS_OUT_TAXI) {
                        message.append(myName).append(getString(R.string.message_not_find_car));
                    }else if(status == MemberInfo.STATUS_IN_TAXI){
                        message.append(myName).append(getString(R.string.message_found_car));
                    }
                    viewHolder.messageView.setText(message.toString());
                    viewHolder.messageView.setTag(R.string.mem_status, status);
                }

                try{
                    String latitude = jsonResult.getString(getString(R.string.mem_lat));
                    String longitude = jsonResult.getString(getString(R.string.mem_lng));
                    String taxiIndex = jsonResult.getString(getString(R.string.taxi_idx));

                    if(latitude != null) {
                        memberInfo.setLatitude(Double.parseDouble(latitude));
                    }
                    if(longitude != null) {
                        memberInfo.setLongitude(Double.parseDouble(longitude));
                    }
                    memberInfo.setTaxiIndex(taxiIndex);
                }catch(JSONException | NumberFormatException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
