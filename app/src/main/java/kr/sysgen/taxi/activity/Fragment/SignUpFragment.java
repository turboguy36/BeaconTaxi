package kr.sysgen.taxi.activity.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
//import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.BaseActivity;
import kr.sysgen.taxi.activity.LoginActivity;
import kr.sysgen.taxi.activity.SignUpActivity;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.BitmapControlUtil;
import kr.sysgen.taxi.util.PhoneNumberUtil;
import kr.sysgen.taxi.util.SysgenAsyncTask;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener{

    private final String TAG = SignUpFragment.class.getSimpleName();

    public final static int RESULT_LOAD_IMG = 0xFFF;

    private static final String MEM_TYPE_PARAM = "MEM_TYPE";

    private OnFragmentInteractionListener mListener;

    //    private View baseView; // 현재 Fragment Layout
    private ViewHolder viewHolder;

    private Context mContext;

    private BitmapControlUtil bitmapUtil;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment SignUpFragment.
     */

    public static SignUpFragment newInstance(int param1) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View baseView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        setHasOptionsMenu(true);
        viewHolder = new ViewHolder(baseView);
        {
            viewHolder.selectImageButton.setOnClickListener(this);
        }
        { // Name Text
            viewHolder.nameEditText.setFocusable(true);
            viewHolder.nameEditText.performClick();
            viewHolder.nameEditText.requestFocus();
        }
        {
            viewHolder.phoneNumberHead.addTextChangedListener(getNextFocusTextWatcher(viewHolder.phoneNumberMiddle, 3));
            viewHolder.phoneNumberMiddle.addTextChangedListener(getNextFocusTextWatcher(viewHolder.phoneNumberTail, 4));
            viewHolder.phoneNumberMiddle.setOnFocusChangeListener(this);
            viewHolder.phoneNumberTail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 4) {
                        hideKeyBoard(mContext, viewHolder.phoneNumberTail);
                    }
                }
            });
        }
        try{
            PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil(getContext());
            final String phoneNumber = phoneNumberUtil.getUSIMPhoneNumber();
            if(phoneNumber != null) {
                String dashedPhoneNumber = phoneNumberUtil.getDashedPhoneNumber(phoneNumber);
                if (dashedPhoneNumber != null) {
                    String[] phoneNumbers = phoneNumbers = dashedPhoneNumber.split(PhoneNumberUtil._DASH);
                    viewHolder.phoneNumberHead.setText(phoneNumbers[0]);
                    viewHolder.phoneNumberMiddle.setText(phoneNumbers[1]);
                    viewHolder.phoneNumberTail.setText(phoneNumbers[2]);
                    viewHolder.nameEditText.requestFocus();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        {
            viewHolder.maleRadioButton.setOnClickListener(this);
            viewHolder.femaleRadioButton.setOnClickListener(this);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE)){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, BaseActivity.PERMISSION_REQUEST_CODE);
                }else{
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, BaseActivity.PERMISSION_REQUEST_CODE);
                }
            }
        }
        return baseView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case BaseActivity.PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    TelephonyManager telephonyManager = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);

                    PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil(getContext());
                    String myTelephoneNumber = phoneNumberUtil.getUSIMPhoneNumber();
                    String dashPhoneNumber = phoneNumberUtil.getDashedPhoneNumber(myTelephoneNumber);

                    String [] phoneNumbers = new String[3];
                    if(dashPhoneNumber != null){
                        phoneNumbers = dashPhoneNumber.split(PhoneNumberUtil._DASH);
                    }
                    viewHolder.phoneNumberHead.setText(phoneNumbers[0]);
                    viewHolder.phoneNumberMiddle.setText(phoneNumbers[1]);
                    viewHolder.phoneNumberTail.setText(phoneNumbers[2]);
                }else{

                }
                break;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_signup, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.confirm_signup:
                item.setEnabled(false);

                final String name = viewHolder.nameEditText.getEditableText().toString();
                final String phone_head = viewHolder.phoneNumberHead.getEditableText().toString();
                final String phone_middle = viewHolder.phoneNumberMiddle.getEditableText().toString();
                final String phone_tail = viewHolder.phoneNumberTail.getEditableText().toString();
                final String phoneNumber = new StringBuffer().append(phone_head).append('-').append(phone_middle).append('-').append(phone_tail).toString();
                final int isMale = viewHolder.radioGroup.getCheckedRadioButtonId() == R.id.male_radio_button ? 0 : 1;
                final String region = (String) viewHolder.sidoSpinner.getSelectedItem();

                if(viewHolder.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.title_alert)
                            .setMessage(R.string.warning_message_empty)
                            .setPositiveButton(R.string.text_confirm,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                    builder.show();
                }else{
                    getDialog(phoneNumber, name, String.valueOf(isMale), region).show();
                }
                item.setEnabled(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modify_photo_button:
                loadImagefromGallery();
                break;
            case R.id.male_radio_button:
                break;
            case R.id.female_radio_button:
                break;
            case R.id.download_iamge:
                downloadImageFromServer();
                break;
        }
    }

    private void downloadImageFromServer(){
        new GetImageTask().execute();
    }
    private class GetImageTask extends AsyncTask<String, Void, String>{
        private String jspFile = "/media/getImg.jsp";
        private ConnectToServer conn;
        public GetImageTask(){
            conn = new ConnectToServer(mContext);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return conn.getJson(null, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i(TAG, s);
        }
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus)showKeyBoard(mContext, v);
//        else hideKeyBoard(mContext, v);
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

    public void loadImagefromGallery() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        getActivity().startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_LOAD_IMG && data != null) {
            bitmapUtil = new BitmapControlUtil(mContext);
            String imagePath = bitmapUtil.getImagePath(data);
            Bitmap bitmap = bitmapUtil.getBitmap(imagePath);
            if (imagePath != null) {
                viewHolder.imageView.setImageBitmap(bitmap);
                viewHolder.imageView.setTag(bitmapUtil.getFileName(imagePath));
            }

            SendImageTask sendImageTask = new SendImageTask();
            sendImageTask.execute(bitmapUtil.convertBitmapIntoString(bitmap));
        }
    }

    private class ViewHolder {
        public final ImageView imageView;
        public final Button selectImageButton;
        public final EditText nameEditText;
        public final EditText phoneNumberHead;
        public final EditText phoneNumberMiddle;
        public final EditText phoneNumberTail;
        public final RadioButton maleRadioButton;
        public final RadioButton femaleRadioButton;
        public final RadioGroup radioGroup;
        public final Spinner sidoSpinner;
        public final Button signUpButton;
        public final Button downloadImage;
        public final ImageView targetImageView;
        private EditText[] getEditTextViews(){
            EditText[] result = {nameEditText, phoneNumberHead, phoneNumberMiddle, phoneNumberTail};
            return result;
        }
        public ViewHolder(View baseView) {
            imageView = (ImageView) baseView.findViewById(R.id.user_photo_view);
            selectImageButton = (Button) baseView.findViewById(R.id.modify_photo_button);
            nameEditText = (EditText) baseView.findViewById(R.id.edittext_user_name);
            phoneNumberHead = (EditText) baseView.findViewById(R.id.edittext_phone_number_head);
            phoneNumberMiddle = (EditText) baseView.findViewById(R.id.edittext_phone_number_middle);
            phoneNumberTail = (EditText) baseView.findViewById(R.id.edittext_phone_number_tail);
            maleRadioButton = (RadioButton) baseView.findViewById(R.id.male_radio_button);
            femaleRadioButton = (RadioButton) baseView.findViewById(R.id.female_radio_button);
            radioGroup = (RadioGroup) baseView.findViewById(R.id.radio_text_box);
            sidoSpinner = (Spinner) baseView.findViewById(R.id.sido_spinner);
            signUpButton = (Button) baseView.findViewById(R.id.sign_up_button);
            targetImageView = (ImageView) baseView.findViewById(R.id.target_image);
            downloadImage = (Button) baseView.findViewById(R.id.download_iamge);
        }

        public Boolean isEmpty(){
            boolean result = false;

            EditText[] editTextViews = getEditTextViews();
            for(EditText view : editTextViews){
                if(isEmpty(view)){
                    view.requestFocus();
                    showKeyBoard(mContext, view);
                    result = true;
                    break;
                }
            }

            return result;
        }
        public Boolean isEmpty(EditText view){
            return (view.getText().length() > 0) ? false : true;
        }
    }

    public AlertDialog.Builder getDialog(final String ... params) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.text_sign_up)
                .setMessage(R.string.msg_dialog_confirm_again)
                .setPositiveButton(R.string.text_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        new SignUpTask().execute(params);

                        JSONObject param = new JSONObject();
                        final String inputPhoneNumber = params[0].trim();
                        final String inputName = params[1].trim();

                        try {
                            param.put(getString(R.string.mem_hp_num), inputPhoneNumber);
                            param.put(getString(R.string.mem_name), inputName);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        SysgenAsyncTask asyncTask = new SysgenAsyncTask(getContext());
                        asyncTask.setProgressDialog();
                        asyncTask.setJspFile(getString(R.string.isExistUser));
                        asyncTask.setListener(new SysgenAsyncTask.Listener() {
                            @Override
                            public void onResult(String result) {

                                try {
                                    JSONObject resultJson = new JSONObject(result);
                                    if(resultJson.has(getString(R.string.mem_idx))){
                                        String memberIndex = resultJson.getString(getString(R.string.mem_idx));
                                        getAlertDialog().show();
                                    }else{
                                        new SignUpTask().execute(params);
                                    }
                                }catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        asyncTask.execute(param.toString());
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder;
    }
    private AlertDialog.Builder getAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.title_dialog_alert))
                .setMessage(getString(R.string.message_signed_up_user))
                .setPositiveButton(getString(R.string.go_to_login_activity),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        })
                .setNegativeButton(getString(R.string.back_and_change_information),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        return builder;
    }
    public View.OnKeyListener getOnEnterKeyListener(final View nextFocusView) {
        return new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    nextFocusView.requestFocus();
                }
                return false;
            }
        };
    }

    private class SignUpTask extends AsyncTask<String, Void, String> {
        private SysgenPreference sysgenPreference;
        private ProgressDialog progress;
        private ConnectToServer conn;
        private final String jspFile;// = "/tm/join_result.jsp";

        public SignUpTask() {
            conn = new ConnectToServer(mContext);
//            editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            progress = new ProgressDialog(mContext);
            jspFile = getString(R.string.join_result);
            sysgenPreference = new SysgenPreference(mContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage(getString(R.string.please_wait));
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
//            final String param = params[0];
            JSONObject param = new JSONObject();
            final String inputPhoneNumber = params[0].trim();
            final String inputName = params[1].trim();
            final String inputSex = params[2].trim();
            final String inputRegion = params[3].trim();
            try {
                param.put(getString(R.string.mem_hp_num), inputPhoneNumber);
                param.put(getString(R.string.mem_name), inputName);
                param.put(getString(R.string.mem_token_id), ((SignUpActivity) getActivity()).getRegistrationId(getContext()));
                param.put(getString(R.string.mem_os_ver), ((SignUpActivity) getActivity()).getAndroidVersion());
                param.put(getString(R.string.mem_sex), inputSex);
                param.put(getString(R.string.mem_region), inputRegion);

                sysgenPreference.putString(getString(R.string.mem_hp_num), inputPhoneNumber);
                sysgenPreference.putString(getString(R.string.mem_name), inputName);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return conn.getJson(param.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject resultJson = new JSONObject(s);
                final String memberRandomKey = resultJson.getString(getString(R.string.mem_r_key));
                final String result = resultJson.getString(getString(R.string.result_code));
                final String signUpMemberId = resultJson.getString(getString(R.string.mem_idx));

                final int resultCode = Integer.parseInt(result);
                if (resultCode == ConnectToServer.RESULT_OK && memberRandomKey.length() > 0) {
                    SysgenPreference sysgenPreference = new SysgenPreference(mContext);
                    sysgenPreference.putString(getString(R.string.mem_idx), signUpMemberId);
                    sysgenPreference.putString(getString(R.string.mem_r_key), memberRandomKey);

                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle(R.string.title_complete)
                            .setMessage(R.string.message_complete)
                            .setPositiveButton(R.string.text_confirm,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            PickMyPartnerFragment fragment = PickMyPartnerFragment.newInstance(signUpMemberId, null);
                                            ((SignUpActivity) getActivity()).replaceFragment(fragment, PickMyPartnerFragment.TAG);
                                        }
                                    });
                            dialog.show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            progress.dismiss();
        }
    }

    private class SendImageTask extends AsyncTask<String, Void, String> {
        private ConnectToServer conn;
        private final String jspFile;

        public SendImageTask() {
            conn = new ConnectToServer(mContext);
            jspFile = getString(R.string.upload_image);
        }

        @Override
        protected String doInBackground(String... params) {
            final String param = params[0];
            Log.i(TAG, "param.length(): " +param.length());
            return conn.getJson(param, jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i(TAG, ">>" + s);
        }
    }

    private Boolean showKeyBoard(Context context, View view){
        InputMethodManager inputMethodManager=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }
    private Boolean hideKeyBoard(Context context, View view){
        InputMethodManager inputMethodManager=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_FORCED);
    }
}
