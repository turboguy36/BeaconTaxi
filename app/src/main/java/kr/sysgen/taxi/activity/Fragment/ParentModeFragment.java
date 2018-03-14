package kr.sysgen.taxi.activity.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.MainActivity;
import kr.sysgen.taxi.activity.MapsActivity;
import kr.sysgen.taxi.adapter.EmptyAdapter;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.SysgenPreference;

import java.util.ArrayList;

/**
 */
public class ParentModeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = ParentModeFragment.class.getSimpleName();

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount;
    private View.OnClickListener mListener;

    private ViewHolder viewHolder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ParentModeFragment() {
    }

    public static ParentModeFragment newInstance(int columnCount) {
        ParentModeFragment fragment = new ParentModeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        viewHolder = new ViewHolder(view);

        viewHolder.swipeRefreshLayout.setOnRefreshListener(this);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
        }

        SysgenPreference sysgenPreference = new SysgenPreference(getContext());
        final String myIndex = sysgenPreference.getString(getString(R.string.mem_idx));

        new GetMyChildrenTask().execute(myIndex);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh() {
        SysgenPreference sysgenPreference = new SysgenPreference(getContext());
        final String myIndex = sysgenPreference.getString(getString(R.string.mem_idx));

        new GetMyChildrenTask().execute(myIndex);
    }

//    @Override
//    public boolean onItemClickListener(final MemberInfo memberInfo) {
//        Log.i(TAG, memberInfo.toString());
//
//        if(memberInfo.getStatus() == MemberInfo.STATUS_IN_TAXI){
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            builder.setTitle("탑승중 사용자")
//                    .setSingleChoiceItems(R.array.dialog_choice, -1, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            switch (which){
//                                case 0:
//                                    TaxiUserHistoryFragment fragment = TaxiUserHistoryFragment.newInstance(memberInfo.getMemberName(), memberInfo.toString());
//                                    ((MainActivity)getActivity()).addFragment(fragment, TaxiUserHistoryFragment.TAG);
//
//                                    break;
//                                case 1:
//                                    Intent intent = new Intent(getContext(), MapsActivity.class);
//
//                                    Bundle bundle = new Bundle();
//                                    bundle.putString(getString(R.string.mem_idx), memberInfo.getMemberIndex());
//                                    bundle.putString(getString(R.string.member_info), memberInfo.toString());
//                                    intent.putExtra(getString(R.string.parameter), bundle);
//
//                                    startActivity(intent);
//                                    break;
//                                case 2:
//
//                                    break;
//                            }
//                            dialog.dismiss();
//                        }
//                    }).show();
//
//
//        }else if(memberInfo.getStatus() == MemberInfo.STATUS_OUT_TAXI){
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            builder.setTitle("하차 한 사용자")
//                    .setSingleChoiceItems(R.array.dialog_choice_off, -1, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            switch (which){
//                                case 0:
//                                    TaxiUserHistoryFragment fragment = TaxiUserHistoryFragment.newInstance(memberInfo.getMemberName(), memberInfo.toString());
//                                    ((MainActivity)getActivity()).addFragment(fragment, TaxiUserHistoryFragment.TAG);
//
//                                    break;
//
//                                case 1:
//
//                                    break;
//                            }
//                            dialog.dismiss();
//                        }
//                    }).show();
//        }
//        return true;
//    }

    private class GetMyChildrenTask extends AsyncTask<String,Void,String>{
        private ConnectToServer conn;
        private final String jspFile;

        public GetMyChildrenTask(){
            conn = new ConnectToServer(getContext());
            jspFile = getString(R.string.getMyChildren);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject paramJson = new JSONObject();

            try {
                paramJson.put(getString(R.string.rl_mem_idx2), params[0]);
            }catch(JSONException e){
                e.printStackTrace();
            }
            return conn.getJson(paramJson.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ArrayList<MemberInfo> items = new ArrayList<>();

            try {
                JSONObject inputJson = new JSONObject(s);
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
                    }
                    items.add(memberInfo);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            if(items.size() == 0){
                viewHolder.recyclerView.setAdapter(new EmptyAdapter());
            }else {
//                viewHolder.recyclerView.setAdapter(new ChildrenListAdapter(getContext(), items, ParentModeFragment.this));
            }
            viewHolder.swipeRefreshLayout.setRefreshing(false);
        }
    }

    class ViewHolder{
        public RecyclerView recyclerView;
        public SwipeRefreshLayout swipeRefreshLayout;
        public ViewHolder(View view){
            recyclerView = (RecyclerView)view.findViewById(R.id.list);
            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeToRefresh);
        }
    }
}
