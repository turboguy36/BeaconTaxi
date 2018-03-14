package kr.sysgen.taxi.activity.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Iterator;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.GetContactActivity;
import kr.sysgen.taxi.adapter.ContactAdapter;
import kr.sysgen.taxi.data.ContactInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.SysgenPreference;

import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.SearchView;

import org.json.JSONException;
import org.json.JSONObject;

public class GetContactActivityFragment extends Fragment implements ContactAdapter.OnClickListener {
    public static final String TAG = GetContactActivityFragment.class.getSimpleName();

    private SysgenPreference pref;
    private ViewHolder viewHolder;

    public GetContactActivityFragment() {
    }

    public static GetContactActivityFragment getInstance() {
        return new GetContactActivityFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new SysgenPreference(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_get_contact, container, false);
        getActivity().setTitle(getString(R.string.title_get_contact));
        viewHolder = new ViewHolder(view);

        setList();

        setSearchView();

        return view;
    }

    private void setList() {
        ArrayList<ContactInfo> contactInfos = getPhone(getContext());

        viewHolder.setContactInfos(contactInfos);

        if (contactInfos.size() > 0) {
            ContactAdapter adapter = new ContactAdapter(this, contactInfos);

            viewHolder.listView.setAdapter(adapter);
            viewHolder.setAdapter(adapter);
        }
    }

    private void setSearchView(){
        viewHolder.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    viewHolder.getAdapter().updateList(viewHolder.getContactInfos());
                    viewHolder.getAdapter().notifyDataSetChanged();
                } else {
                    new FindSameNameTask().execute(newText);
                }
                return false;
            }
        });

        viewHolder.searchView.setFocusable(true);
        viewHolder.searchView.setIconified(false);
        viewHolder.searchView.requestFocusFromTouch();
        viewHolder.searchView.setQueryHint(getString(R.string.search_hint));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 디바이스에 있는 연락처 목록을 가져온다.
     *
     * @param context
     * @return
     */
    public ArrayList<ContactInfo> getPhone(Context context) {
        ArrayList<ContactInfo> contactList = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();

        String[] PROJECTION = new String[]{
                ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID};

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String filter = "" + ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0 and " + Phone.TYPE + "=" + Phone.TYPE_MOBILE;
        String order = ContactsContract.Contacts.DISPLAY_NAME + " ASC";

        Cursor phoneCur = cr.query(uri, PROJECTION, filter, null, order);
        while (phoneCur.moveToNext()) {
            ContactInfo contactInfo = new ContactInfo();

            contactInfo.setName(phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            contactInfo.setPhone(phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            contactInfo.setPhoto(phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)));
            contactInfo.setContactId(phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID)));

            contactList.add(contactInfo);
        }
        phoneCur.close();

        return contactList;
    }

    private void finishActivityWithData(ContactInfo info) {
        Intent data = new Intent();
        data.putExtra(GetContactActivity.TAG, info.toString());

        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public void onClicked(final ContactInfo info) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.text_confirm))
                .setMessage(info.getName() + getString(R.string.want_to_invite_child))
                .setPositiveButton(R.string.text_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String receiverPhoneNumber = info.getPhone();
                        String myName = pref.getString(getString(R.string.mem_name));

                        new SendInvitationTask().execute(myName, receiverPhoneNumber);

                        finishActivityWithData(info);
                    }
                }).setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    /**
     * 초대 문자를 보낸다.
     */
    private class SendInvitationTask extends AsyncTask<String, Void, String> {
        private ConnectToServer conn;
        private String jspFile;

        public SendInvitationTask() {
            jspFile = getString(R.string.sendInvitation);
            conn = new ConnectToServer(getContext());
        }

        @Override
        protected String doInBackground(String... params) {
            final String myName = params[0];
            final String receiverPhoneNumber = params[1];

            ArrayList<String> messageList = new ArrayList<>();

            final String appUri = getString(R.string.app_url_pre) + getContext().getPackageName();
            StringBuffer sb = new StringBuffer();
            sb.append('<')
                    .append(getString(R.string.app_name_kor))
                    .append('>')
                    .append('\n')
                    .append(myName)
                    .append(getString(R.string.message_invitation));

            final String messageBody = sb.toString();

            messageList.add(messageBody);
            messageList.add(Uri.parse(appUri).toString());

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendMultipartTextMessage(
                    receiverPhoneNumber //받는 사람 번호
                    , null //service center
                    , messageList //문자내용
                    , null // sendIntent may null
                    , null // deliveryIntent may null
            );
            JSONObject json = new JSONObject();

            String memberIndex = pref.getString(getString(R.string.mem_idx));
            try {
                json.put(getString(R.string.mem_idx), memberIndex);
                json.put(getString(R.string.mem_hp_num), receiverPhoneNumber);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return conn.getJson(json.toString(), jspFile);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    /**
     * 이름 / 전화번호 포함 되어 있는지를 검색한다.
     */
    private class FindSameNameTask extends AsyncTask<String, Void, ArrayList<ContactInfo>> {

        @Override
        protected ArrayList<ContactInfo> doInBackground(String... params) {
            ArrayList<ContactInfo> result = new ArrayList<>();

            String userInput = params[0];
            Iterator<ContactInfo> iter = viewHolder.getContactInfos().iterator();
            while (iter.hasNext()) {
                ContactInfo contactInfo = iter.next();

                String contactName = contactInfo.getName();
                if (contactName.contains(userInput)) {
                    result.add(contactInfo);
                }

                String contactNumber = contactInfo.getPhone();
                if (contactNumber.contains(userInput)) {
                    result.add(contactInfo);
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<ContactInfo> array) {
            super.onPostExecute(array);
            viewHolder.getAdapter().updateList(array);
            viewHolder.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     *
     */
    class ViewHolder {
        public final RelativeLayout baseView;
        public final SearchView searchView;
        public final RecyclerView listView;

        private ContactAdapter adapter;
        private ArrayList<ContactInfo> contactInfos;

        public ViewHolder(View view) {
            baseView = (RelativeLayout) view;
            searchView = (SearchView) baseView.findViewById(R.id.search_view_contact);
            listView = (RecyclerView) baseView.findViewById(R.id.contacts_list);
        }

        public ArrayList<ContactInfo> getContactInfos() {
            return this.contactInfos;
        }

        public ContactAdapter getAdapter() {
            return adapter;
        }

        public void setAdapter(ContactAdapter adapter) {
            this.adapter = adapter;
        }

        public void setContactInfos(ArrayList<ContactInfo> contactInfos) {
            this.contactInfos = contactInfos;
        }
    }
}
