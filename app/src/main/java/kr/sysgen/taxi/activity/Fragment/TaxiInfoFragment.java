package kr.sysgen.taxi.activity.Fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.network.ConnectToServer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TaxiInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TaxiInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaxiInfoFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = TaxiInfoFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ViewHolder viewHolder;

    public TaxiInfoFragment() {
        // Required empty public constructor
    }

    public static TaxiInfoFragment newInstance(String param1) {
        TaxiInfoFragment fragment = new TaxiInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TaxiInfoFragment.
     */
    public static TaxiInfoFragment newInstance(String param1, String param2) {
        TaxiInfoFragment fragment = new TaxiInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taxi_info, container, false);
        view.setOnClickListener(this);
        viewHolder = new ViewHolder(view);

        new GetMyTaxiInfoTask().execute(mParam1);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick");
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class GetMyTaxiInfoTask extends AsyncTask<String, Void, String>{
        private ConnectToServer conn;
        private String jspFile;
        private ProgressDialog progressDialog;

        public GetMyTaxiInfoTask(){
            conn = new ConnectToServer(getContext());
            jspFile = getString(R.string.getTaxiInfo);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            final String taxiIndex = params[0];
            Log.i(TAG, "taxi idx: " + taxiIndex);

            JSONObject json = new JSONObject();
            try {
                json.put(getString(R.string.taxi_idx), taxiIndex);
            }catch(JSONException e){
                e.printStackTrace();
            }

            return conn.getJson(json.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String taxiIndex = new String();
            String taxiCompany = new String();

            try {
                JSONObject json = new JSONObject(s);
                taxiIndex = json.getString(getString(R.string.taxi_num));
                taxiCompany = json.getString(getString(R.string.taxi_sc_name));
            }catch(JSONException e){
                e.printStackTrace();
            }

            viewHolder.taxiIndex.setText(taxiIndex);
            viewHolder.taxiCompany.setText(taxiCompany);

            progressDialog.dismiss();
        }
    }
    class ViewHolder {
        TextView taxiIndex;
        TextView taxiCompany;

        public ViewHolder(View view){
            taxiIndex = (TextView)view.findViewById(R.id.text_taxi_id);
            taxiCompany = (TextView) view.findViewById(R.id.text_taxi_company);
        }
    }
}
