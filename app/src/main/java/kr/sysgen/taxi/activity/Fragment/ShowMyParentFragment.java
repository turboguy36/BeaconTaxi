package kr.sysgen.taxi.activity.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.BaseActivity;
import kr.sysgen.taxi.activity.MainActivity;
import kr.sysgen.taxi.adapter.ParentViewAdapter;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnParentListFragmentInteractionListener}
 * interface.
 */
public class ShowMyParentFragment extends Fragment implements ParentViewAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{
    public static final String TAG = ShowMyParentFragment.class.getSimpleName();

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnParentListFragmentInteractionListener mListener;
    private ViewHolder viewHolder;
    private String myIndex;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShowMyParentFragment() {
    }

    public static ShowMyParentFragment newInstance(int columnCount) {
        ShowMyParentFragment fragment = new ShowMyParentFragment();
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
        ((BaseActivity)getActivity()).getToolbar().setTitle(R.string.title_show_my_parent_fragment);

        View view = inflater.inflate(R.layout.fragment_show_my_parent, container, false);
        viewHolder = new ViewHolder(view);

        viewHolder.swipeRefreshLayout.setOnRefreshListener(this);
        SysgenPreference sysgenPreference = new SysgenPreference(getContext());
        myIndex = sysgenPreference.getString(getString(R.string.mem_idx));

        new GetMyParentsTask().execute(myIndex);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnParentListFragmentInteractionListener) {
            mListener = (OnParentListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_parent, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_parent:

                makeDialog().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private AlertDialog.Builder makeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.add_parent))
                .setMessage(getString(R.string.dialog_title_add_parent))
                .setPositiveButton(R.string.text_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AddMyPartnerFragment addMyPartnerFragment = AddMyPartnerFragment.newInstance(myIndex, null);
                                ((MainActivity) getActivity()).addFragment(addMyPartnerFragment, AddMyPartnerFragment.TAG);
                            }
                        })
                .setNegativeButton(R.string.text_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
        return builder;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private AlertDialog.Builder makeDialog(String title, String message, DialogInterface.OnClickListener positiveListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.text_confirm, positiveListener);
        return builder;
    }
    /*private AlertDialog.Builder makeDialog(String title, String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener){
        AlertDialog.Builder builder = makeDialog(title, message, positiveListener);
        builder.setNegativeButton(R.string.text_cancel, negativeListener);

        return builder;
    }*/

    @Override
    public boolean onItemClicked(MemberInfo memberInfo) {
        final String memberIndex = memberInfo.getMemberIndex();

        final String dialogMessage = new StringBuffer().append(memberInfo.getMemberName()).append(getString(R.string.want_to_delete)).toString();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle(R.string.title_alert)
                .setMessage(dialogMessage)
                .setPositiveButton(R.string.delete,
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                JSONObject paramJson = new JSONObject();
                                try {
                                    SysgenPreference sysgenPreference = new SysgenPreference(getContext());
                                    final String rlMemIdx1 = sysgenPreference.getString(getString(R.string.mem_idx));
                                    paramJson.put(getString(R.string.rl_mem_idx2), memberIndex);
                                    paramJson.put(getString(R.string.rl_mem_idx1), rlMemIdx1);
                                }catch(JSONException e){
                                    e.printStackTrace();
                                }
                                new DeleteParentTask().execute(paramJson.toString());
                            }
                        })
                .setNegativeButton(R.string.text_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
        return false;
    }

    @Override
    public void onRefresh() {
        new GetMyParentsTask(false).execute(myIndex);
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
    public interface OnParentListFragmentInteractionListener {
        void OnParentListFragmentInteractionListener(MemberInfo item);
    }

    private class GetMyParentsTask extends AsyncTask<String, Void, String> {
        private ProgressDialog progress;
        private ConnectToServer conn;
        private String jspFile;
        boolean showProgress = true;

        public GetMyParentsTask(){
            conn = new ConnectToServer(getContext());
            jspFile = getString(R.string.getMyParents);
        }
        public GetMyParentsTask(boolean showProgress){
            conn = new ConnectToServer(getContext());
            jspFile = getString(R.string.getMyParents);

            this.showProgress = showProgress;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(getContext());
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage(getString(R.string.please_wait));
            if(showProgress) {
                progress.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put(getString(R.string.rl_mem_idx1), params[0]);
            }catch(JSONException e){
                e.printStackTrace();
            }

            return conn.getJson(jsonObject.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            ArrayList<MemberInfo> arrayList = parseMemberInfo(s);

            viewHolder.recyclerView.setAdapter(new ParentViewAdapter(ShowMyParentFragment.this, arrayList, mListener));

            if(progress.isShowing()) {
                progress.dismiss();
            }

            if(viewHolder.swipeRefreshLayout.isRefreshing()) {
                viewHolder.swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
    private class DeleteParentTask extends AsyncTask<String, Void, String>{
        private ConnectToServer conn;
        private String jspFile;
        private ProgressDialog progressDialog;

        public DeleteParentTask(){
            conn = new ConnectToServer(getContext());
            jspFile = getString(R.string.deleteParent);
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
            String param = params[0];

            return conn.getJson(param, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            ArrayList<MemberInfo> memberInfos = parseMemberInfo(s);
            ((ParentViewAdapter)viewHolder.recyclerView.getAdapter()).swap(memberInfos);

            progressDialog.dismiss();
        }
    }

    /**
     *
     * @param inputJson
     * @return
     */
    private ArrayList<MemberInfo> parseMemberInfo(String inputJson){
        JSONObject resultJson;
        JSONArray jsonArray;
        ArrayList<MemberInfo> arrayList = new ArrayList<>();

        try {
            resultJson = new JSONObject(inputJson);
            jsonArray = resultJson.getJSONArray(getString(R.string.list));
            int length = jsonArray.length();

            for(int i=0; i<length; i++){
                MemberInfo member = new MemberInfo();
                JSONObject object = jsonArray.getJSONObject(i);
                final String memberName = object.getString(getString(R.string.mem_name));
                final String memberPhoneNumber = object.getString(getString(R.string.mem_hp_num));
                final String memIndex = object.getString(getString(R.string.mem_idx));
                member.setMemberName(memberName);
                member.setMemberPhoneNumber(memberPhoneNumber);
                member.setMemberIndex(memIndex);
                arrayList.add(member);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }

        return arrayList;
    }
    class ViewHolder{
        public RecyclerView recyclerView;
        public SwipeRefreshLayout swipeRefreshLayout;

        public ViewHolder(View view){

            swipeRefreshLayout = (SwipeRefreshLayout)view;
            recyclerView = (RecyclerView)view.findViewById(R.id.list);
        }
    }
}
