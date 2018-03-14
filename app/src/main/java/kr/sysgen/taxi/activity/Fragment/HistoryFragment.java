package kr.sysgen.taxi.activity.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.TrackingMapActivity;
import kr.sysgen.taxi.data.TaxiInfo;
import kr.sysgen.taxi.network.ConnectToServer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    public static final String TAG = HistoryFragment.class.getSimpleName();

    private static final String DATE_TAG = "DATE";
    private static final String TAXI_NUM_TAG = "TAXI_NUM";

    private String taxiNum;
    private String taxiDate;
    private String getOnAddress;
    private String getOffAddress;

    private ViewHolder viewHolder;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param date Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    public static HistoryFragment newInstance(String taxiNum, String date, String getOnAddress, String getOffAddress) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        if(date!=null) {
            args.putString(DATE_TAG, date);
        }
        if(taxiNum !=null) {
            args.putString(TAXI_NUM_TAG, taxiNum);
        }
        if(getOnAddress != null) {
            args.putString(TaxiInfo.MEM_ADDRESS, getOnAddress);
        }
        if(getOffAddress != null) {
            args.putString(TaxiInfo.ADDRESS_GET_OFF, getOffAddress);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            taxiNum = getArguments().getString(TAXI_NUM_TAG);
            taxiDate = getArguments().getString(DATE_TAG);
            getOnAddress = getArguments().getString(TaxiInfo.MEM_ADDRESS);
            getOffAddress = getArguments().getString(TaxiInfo.ADDRESS_GET_OFF);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        viewHolder = new ViewHolder(view);

        if(taxiNum != null) {
            viewHolder.taxiNum.setText("택시번호: " + taxiNum);
        }else{
            viewHolder.taxiNum.setText("아직 탑승 기록이 없습니다.");
        }
        if(taxiDate != null) {
            viewHolder.date.setText("탑승시간: " + taxiDate);
        }
        if(getOnAddress != null) {
            viewHolder.getOnAddress.setText("탑승위치: " + getOnAddress);
        }
        if(getOffAddress != null) {
            viewHolder.getOffAddress.setText("하차위치: " + getOffAddress);
        }
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
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
    class ViewHolder{
        public TextView taxiNum;
        public TextView date;
        public TextView getOnAddress;
        public TextView getOffAddress;

        public Button showRoute;
        public ViewHolder(View baseView){
            this.taxiNum = (TextView)baseView.findViewById(R.id.history_taxi_num);
            this.date = (TextView)baseView.findViewById(R.id.history_date);
            this.getOnAddress = (TextView)baseView.findViewById(R.id.history_get_on_address);
            this.getOffAddress = (TextView)baseView.findViewById(R.id.history_get_off_address);

            this.showRoute = (Button)baseView.findViewById(R.id.button_tracking);
        }
    }
}
