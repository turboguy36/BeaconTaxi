package kr.sysgen.taxi.activity.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.SplashActivity;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PickMyPartnerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PickMyPartnerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PickMyPartnerFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = PickMyPartnerFragment.class.getSimpleName();
    private Context mContext;

    private static final String MEM_IDX_KEY = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String myMemIndex;

    private OnFragmentInteractionListener mListener;

    ViewHolder viewHolder;

    public PickMyPartnerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PickMyPartnerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PickMyPartnerFragment newInstance(String param1, String param2) {
        PickMyPartnerFragment fragment = new PickMyPartnerFragment();
        Bundle args = new Bundle();
        args.putString(MEM_IDX_KEY, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        if (getArguments() != null) {
            myMemIndex = getArguments().getString(MEM_IDX_KEY);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_my_partner, container, false);
        viewHolder = new ViewHolder(view);

        viewHolder.phoneNumberHead.addTextChangedListener(getNextFocusTextWatcher(viewHolder.phoneNumberMiddle, 3));
        viewHolder.phoneNumberMiddle.addTextChangedListener(getNextFocusTextWatcher(viewHolder.phoneNumberTail, 4));

        setHasOptionsMenu(true);

        view.setOnClickListener(this);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_next, menu);
    }
    private String getParentInfoEditText(){
        JSONObject json = new JSONObject();

        String userName = viewHolder.nameEditText.getText().toString().trim();
        final String phone_head = viewHolder.phoneNumberHead.getEditableText().toString();
        final String phone_middle = viewHolder.phoneNumberMiddle.getEditableText().toString();
        final String phone_tail = viewHolder.phoneNumberTail.getEditableText().toString();
        final String phoneNumber = new StringBuffer().append(phone_head).append('-').append(phone_middle).append('-').append(phone_tail).toString();

        try {
            json.put(getString(R.string.mem_name), userName);
            json.put(getString(R.string.mem_hp_num), phoneNumber);
            json.put(getString(R.string.mem_idx), myMemIndex);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    private String getParentInfoEditText(int parentId){
        JSONObject json = new JSONObject();
        SysgenPreference sysgenPreference = new SysgenPreference(getContext());
        final String myName = sysgenPreference.getString(getString(R.string.mem_name));

        final String parentUserName = viewHolder.nameEditText.getText().toString().trim();
        final String phone_head = viewHolder.phoneNumberHead.getEditableText().toString();
        final String phone_middle = viewHolder.phoneNumberMiddle.getEditableText().toString();
        final String phone_tail = viewHolder.phoneNumberTail.getEditableText().toString();
        final String phoneNumber = new StringBuffer().append(phone_head).append('-').append(phone_middle).append('-').append(phone_tail).toString();

        try {
            json.put(getString(R.string.mem_name), myName);
            json.put(getString(R.string.mem_hp_num), phoneNumber);
            json.put(getString(R.string.mem_idx), myMemIndex);
            json.put(getString(R.string.rl_mem_idx2), parentId);
            json.put(getString(R.string.mem_parent_name), parentUserName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:

                final String param = getParentInfoEditText();
                if(viewHolder.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.title_alert)
                            .setMessage(R.string.warning_message_empty)
                            .setPositiveButton(R.string.text_confirm,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                            .setNegativeButton(R.string.text_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                    builder.show();
                }else {
                    viewHolder.phoneNumberTail.clearFocus();
                    new IsExistUserTask().execute(param);
                }
                break;
            case R.id.skip:
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle(R.string.title_skip).setMessage(R.string.warning_message_skip)
                        .setPositiveButton(getString(R.string.text_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                                Intent splashIntent = new Intent(getContext(), SplashActivity.class);
                                getActivity().startActivity(splashIntent);
                            }
                        })
                        .setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

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
        void onFragmentInteraction(Uri uri);
    }

    private class ViewHolder {
        public final EditText nameEditText;
        public final EditText phoneNumberHead;
        public final EditText phoneNumberMiddle;
        public final EditText phoneNumberTail;

        /**
         * 현재 View 에서 필수 입력 사항을 지정한다.
         *
         * @return
         */
        private EditText[] getEditViews(){
            EditText[] editViews = {nameEditText, phoneNumberHead, phoneNumberMiddle, phoneNumberTail};
            return editViews;
        }

        public ViewHolder(View baseView) {
            nameEditText = (EditText) baseView.findViewById(R.id.edittext_user_name);
            phoneNumberHead = (EditText) baseView.findViewById(R.id.edittext_phone_number_head);
            phoneNumberMiddle = (EditText) baseView.findViewById(R.id.edittext_phone_number_middle);
            phoneNumberTail = (EditText) baseView.findViewById(R.id.edittext_phone_number_tail);
        }

        /**
         * 현재 View 에 존재하는 모든 EditView 를
         * 전체적으로 검사한다.
         *
         * @return 비어 있다면 TRUE 를 RETURN 한다.
         */
        public Boolean isEmpty(){
            boolean result = false;
            EditText[] editViews = getEditViews();
            for(EditText view : editViews){
                if(isEmpty(view)){
                    view.requestFocus();

                    showKeyBoard(mContext, view);
                    result = true;
                    break;
                }
            }
            return result;
        }

        /**
         * 특정 EditText 가 비어 있는지 확인 한다.
         *
         * @param view
         * @return
         */
        public Boolean isEmpty(EditText view){
            return (view.getText().length() > 0) ? false : true;
        }
    }

    private void showAlert(final int parentId, String parentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.title_alert)
                .setMessage(parentName + " 님을 보호자로 지정 하겠습니까?")
                .setPositiveButton(R.string.text_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String param = getParentInfoEditText(parentId);
                                hideKeyBoard(mContext, viewHolder.phoneNumberTail);
                                new SetParentTask().execute(param);
                            }
                        })
                .setNegativeButton(R.string.text_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        builder.show();
    }

    private void showAlertFail(int message, int resultCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.title_alert)
                .setMessage(message)
                .setPositiveButton(R.string.text_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
    private void showAlertFail(int message){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.title_alert)
                .setMessage(message)
                .setPositiveButton(R.string.text_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    /**
     * 내가 요청한 사용자가
     * 이미 가입 되어 있는지
     * 확인 하는 작업
     */
    private class IsExistUserTask extends AsyncTask<String, Void, String>{
        private ConnectToServer conn;
        private final String jspFile;

        public IsExistUserTask(){
            conn = new ConnectToServer(mContext);
            jspFile = getString(R.string.isExistUser);
        }
        @Override
        protected String doInBackground(String... params) {
            final String param = params[0];

            return conn.getJson(param, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            int existUserId = 0;
            String existUserName = null;
            try {
                JSONObject json = new JSONObject(s);
                int resultCode = json.getInt(getString(R.string.result_code));
                if(resultCode == 1){
                    if(json.has(getString(R.string.mem_idx))) {
                        existUserId = json.getInt(getString(R.string.mem_idx));
                        existUserName = json.getString(getString(R.string.mem_name));

                        showAlert(existUserId, existUserName);
                    }else{
                        showAlertFail(R.string.message_not_exist_user, resultCode);
                    }
                }else if(resultCode >= HttpURLConnection.HTTP_BAD_REQUEST){
                    final int errorMessage = json.getInt(getString(R.string.error_message));
                    showAlertFail(errorMessage);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            hideKeyBoard(mContext, viewHolder.phoneNumberTail);
        }
    }

    /**
     * 보모 사용자를 지정 하는 작업
     */
    private class SetParentTask extends AsyncTask<String, Void, String> {
        private final ConnectToServer conn;
        private final String jspFile;
        private ProgressDialog progress;

        public SetParentTask() {
            conn = new ConnectToServer(mContext);
            jspFile = getString(R.string.setParent);
            progress = new ProgressDialog(mContext);
            progress.setCancelable(true);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String param = params[0];

            return conn.getJson(param, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progress.dismiss();
            int resultCode = -1;

            try {
                JSONObject resultJson = new JSONObject(s);

                String resultString = resultJson.getString(getString(R.string.result_code));
                resultCode = Integer.valueOf(resultString);
            }catch(JSONException e){
                e.printStackTrace();
            }

            if (resultCode == 1) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle(R.string.text_success).setMessage(R.string.message_set_parent_complete)
                        .setPositiveButton(getString(R.string.text_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                                Intent splashIntent = new Intent(getContext(), SplashActivity.class);
                                getActivity().startActivity(splashIntent);

                                dialog.dismiss();
                            }
                        });
                dialog.show();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle(R.string.text_fail).setMessage(R.string.message_user_not_found)
                        .setPositiveButton(getString(R.string.text_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialog.show();
            }
        }
    }
    public TextWatcher getNextFocusTextWatcher(final EditText nextFocusView, final int length) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == length) {
                    nextFocusView.requestFocus();
                }
            }
        };
    }
    private Boolean showKeyBoard(Context context, View view){
        InputMethodManager inputMethodManager=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }
    private Boolean hideKeyBoard(Context context, View view){
        InputMethodManager inputMethodManager=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}
