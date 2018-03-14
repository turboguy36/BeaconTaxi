package kr.sysgen.taxi.activity.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.TrackingMapActivity;
import kr.sysgen.taxi.activity.UserHistoryActivity;
import kr.sysgen.taxi.adapter.UserHistoryAdapter;
import kr.sysgen.taxi.data.HistoryInfo;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.data.TrackingInfo;
import kr.sysgen.taxi.network.ConnectToServer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link UserHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserHistoryFragment extends Fragment implements UserHistoryAdapter.OnItemClickListener{
    public static final String TAG = UserHistoryFragment.class.getSimpleName();

    private static final String MEM_INDEX_TAG = "mem_idx";
    private static final String MEM_STATUS = "mem_status";

    private String memberIndex;
    private int memberStatus;

    private ViewHolder viewHolder;

    public UserHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param memIndex
     * @return A new instance of fragment UserHistoryFragment.
     */
    public static UserHistoryFragment newInstance(String memIndex) {
        UserHistoryFragment fragment = new UserHistoryFragment();
        Bundle args = new Bundle();
        args.putString(MEM_INDEX_TAG, memIndex);

        fragment.setArguments(args);
        return fragment;
    }
    public static UserHistoryFragment newInstance(String memIndex, int memStatus) {
        UserHistoryFragment fragment = new UserHistoryFragment();
        Bundle args = new Bundle();
        args.putString(MEM_INDEX_TAG, memIndex);
        args.putInt(MEM_STATUS, memStatus);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memberIndex = getArguments().getString(MEM_INDEX_TAG);
            memberStatus = getArguments().getInt(MEM_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_history, container, false);
        viewHolder = new ViewHolder(view);

        new GetUserHistoryTask(getContext()).execute(memberIndex);

//        UserHistoryActivity activity = ((UserHistoryActivity)getActivity());
//        if(activity != null) {
//            Log.i(TAG, "UserHistoryActivity null");
//        }

        return view;
    }

    @Override
    public boolean onItemClicked(HistoryInfo historyInfo) {
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put(getString(R.string.mem_idx), String.valueOf(historyInfo.getMemberIndex()));
            paramJson.put(getString(R.string.history_idx), String.valueOf(historyInfo.getHistoryIndex()));
        }catch(JSONException e){
            e.printStackTrace();
        }
        new GetTrackingInfo().execute(paramJson.toString());
        return false;
    }

    private class GetUserHistoryTask extends AsyncTask<String, Integer, ArrayList<HistoryInfo>>{
        private ProgressDialog progressDialog;
        private String jspFile;
        private ConnectToServer conn;
        private Context mContext;

        public GetUserHistoryTask(Context context){
            this.mContext = context;
            this.jspFile = context.getString(R.string.getUserHistory);
            this.conn = new ConnectToServer(context);
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("정보를 가져오고 있습니다.");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progressValue = values[0];

            progressDialog.setProgress(progressValue);
        }

        @Override
        protected ArrayList<HistoryInfo> doInBackground(String... params) {
            String memberIndex = params[0];
            JSONObject json = new JSONObject();
            try {
                json.put(getString(R.string.mem_idx), memberIndex);
            }catch (JSONException e){
                e.printStackTrace();
            }
            String result = conn.getJson(json.toString(), jspFile);

            ArrayList<HistoryInfo> historyInfos = new ArrayList<>();
            try {
                JSONObject resultJson = new JSONObject(result);
                JSONArray historyArray = resultJson.getJSONArray(getString(R.string.list));
                int length = historyArray.length();
                for(int i=0; i<length; i++){
                    JSONObject historyJson = (JSONObject) historyArray.get(i);
                    HistoryInfo historyInfo = HistoryInfo.parseHistory(historyJson);

                    historyInfos.add(historyInfo);

                    int value = Math.round(((float)i/(float)length) * 100.0f);
                    publishProgress(value);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }

            return historyInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<HistoryInfo> s) {
            super.onPostExecute(s);

            UserHistoryAdapter adapter = new UserHistoryAdapter(getContext(), UserHistoryFragment.this, s);
            viewHolder.recyclerView.setAdapter(adapter);

            progressDialog.dismiss();
        }

    }
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
    class ViewHolder {
        public RecyclerView recyclerView;
        public ViewHolder(View view){
            recyclerView = (RecyclerView)view.findViewById(R.id.list);
        }
    }
}
