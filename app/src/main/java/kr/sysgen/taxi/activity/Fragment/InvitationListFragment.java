package kr.sysgen.taxi.activity.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import kr.sysgen.taxi.activity.SlideMenuActivity;
import kr.sysgen.taxi.adapter.InvitationAdapter;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.network.ConnectToServer;
import kr.sysgen.taxi.util.SysgenPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class InvitationListFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = InvitationListFragment.class.getSimpleName();

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private ViewHolder viewHolder;

    // 되돌아 갈 때 Title Bar 변경 하기 위해 직전화면 Title 저장한다.
    private String fragmentTitleStore;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InvitationListFragment() {
    }

    public static InvitationListFragment newInstance(int columnCount) {
        InvitationListFragment fragment = new InvitationListFragment();
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
        try {
            fragmentTitleStore = ((SlideMenuActivity) getActivity()).getToolbar().getTitle().toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invitation_list, container, false);
        viewHolder = new ViewHolder(view);

        ((SlideMenuActivity) getActivity()).setToolbarTitle(getString(R.string.title_invitation_toolbar));

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            SysgenPreference pref = new SysgenPreference(getContext());

            String invitationUsersString = pref.getString(getString(R.string.invitation_user_index_object));

            try {
                JSONObject json = new JSONObject(invitationUsersString);
                new GetUserInfoTask().execute(json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((SlideMenuActivity) getActivity()).setToolbarTitle(fragmentTitleStore);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onClick(View v) {
        MemberInfo memberInfo = (MemberInfo) v.getTag();
        makeDialog(memberInfo).show();
    }

    private AlertDialog makeDialog(final MemberInfo memberInfo) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("초대 수락 하기")
                .setMessage(memberInfo.getMemberName() + " 님을 보호자로 지정 합니다.")
                .setPositiveButton(getString(R.string.text_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SendPermitTask().execute(memberInfo);
                    }
                })
                .setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return dialog;
    }

    /**
     * 초대 메시지에 수락 한다.
     * 초대 완료가 되면, 화면에 있는 초대메시지를 삭제한다.
     *
     * @param : 내 아이디와, 상대방 아이디
     */
    private class SendPermitTask extends AsyncTask<MemberInfo, Void, String> {
        private ConnectToServer conn;
        private String jspFile;
        SysgenPreference pref;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            conn = new ConnectToServer(getContext());
            jspFile = getString(R.string.setParent);
            pref = new SysgenPreference(getContext());
        }


        /**
         * 초대 수락 후 보호자 ID 값 받아오기
         */
        @Override
        protected String doInBackground(MemberInfo... params) {
            pref.getString(getString(R.string.mem_idx));

            MemberInfo memberInfo = params[0];
            JSONObject json = new JSONObject();
            try {
                json.put(getString(R.string.mem_idx), pref.getString(getString(R.string.mem_idx)));
                json.put(getString(R.string.rl_mem_idx2), memberInfo.getMemberIndex());
                json.put(getString(R.string.mem_parent_name), memberInfo.getMemberName());
                json.put(getString(R.string.mem_name), pref.getString(getString(R.string.mem_name)));
                json.put(getString(R.string.mem_hp_num), pref.getString(getString(R.string.mem_hp_num)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return conn.getJson(json.toString(), jspFile);
        }

        /**
         * SysgenPreference 에 있는 key:invitation_user_index_object 값을 가져온다
         * 그 안에서 보호자ID 값을 확인하고, 수락이 완료된 보호자의 초대정보는 삭제한다.
         *
         * @param parentId : 보호자 ID {rl_mem_idx2}
         */
        @Override
        protected void onPostExecute(String parentId) {
            super.onPostExecute(parentId);
            // call by reference
            InvitationAdapter adapter = (InvitationAdapter)viewHolder.recyclerView.getAdapter();
            ArrayList<MemberInfo> memberInfos = adapter.getData();

            if(afterConfirmInvitationTask(parentId, memberInfos)) {
                adapter.setData(memberInfos);
                adapter.notifyDataSetChanged();
            }
        }

        /**
         *
         * @param parentId : 내가 수락한 초대메시지의 보호자 ID
         * @param memberInfos : RecyclerView 에 보여질 리스트
         * @return 삭제 성공 여부
         */
        private Boolean afterConfirmInvitationTask(String parentId, ArrayList<MemberInfo> memberInfos){
            boolean result = false;

            try {
                JSONObject json = new JSONObject(parentId);
                String rlMemIdx = json.getString(getString(R.string.rl_mem_idx2));
                SysgenPreference pref = new SysgenPreference(getContext());
                final String invitationObjectStr = pref.getString(getString(R.string.invitation_user_index_object));
                JSONObject invitationObject = new JSONObject(invitationObjectStr);
                JSONArray invitationArray = invitationObject.getJSONArray(getString(R.string.invitation_user_index_array));
                for(int i=0; i<invitationArray.length(); i++){
                    JSONObject invitorInfo = (JSONObject)invitationArray.get(i);
                    String invitorIndex = invitorInfo.getString(getString(R.string.mem_idx));
                    if(invitorIndex.equals(rlMemIdx)){
                        invitationArray.remove(i);
                    }
                }
                Iterator<MemberInfo> iter = memberInfos.iterator();
                while(iter.hasNext()){
                    MemberInfo memberInfo = iter.next();
                    if(memberInfo.getMemberIndex().equals(rlMemIdx)){
                        result = memberInfos.remove(memberInfo);
                        break;
                    }
                }
                invitationObject.put(getString(R.string.invitation_user_index_array), invitationArray);
                pref.putString(getString(R.string.invitation_user_index_object), invitationObject.toString());
            }catch(JSONException e){
                e.printStackTrace();
            }

            return result;
        }
    }

    /**
     * 나에게 초대 메시지를 보낸 User 들의 정보를 얻어온다.
     * 화면에 User 들의 정보를 보여주기 위해 List Adapter 를 셋팅한다.
     *
     * @param : {JSONObject : { JSONArray ... }}
     */
    private class GetUserInfoTask extends AsyncTask<String, Void, ArrayList<MemberInfo>> {
        private ConnectToServer conn;
        private String jspFile;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            conn = new ConnectToServer(getContext());
            jspFile = getString(R.string.getUsersInfo);
        }

        @Override
        protected ArrayList<MemberInfo> doInBackground(String... params) {
            HashMap<Integer, String> invitorMap = new HashMap<>();

            try {
                final String param = params[0];

                JSONObject inputJson = new JSONObject(param);

                JSONArray array = inputJson.getJSONArray(getString(R.string.invitation_user_index_array));
                for (int i = 0; i < array.length(); i++) {
                    JSONObject member = array.getJSONObject(i);
                    int memberIndex = Integer.parseInt(member.getString(getString(R.string.mem_idx)));
                    String invitationTime = member.getString(getString(R.string.time));
                    invitorMap.put(memberIndex, invitationTime);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final String json = conn.getJson(params[0], jspFile);

            ArrayList<MemberInfo> memberInfos = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(json);

                JSONArray jsonArray = jsonObject.getJSONArray(getString(R.string.invitation_user_index_array));
                for (int i = 0; i < jsonArray.length(); i++) {
                    MemberInfo memberInfo = MemberInfo.parseMemberInfo((String) jsonArray.get(i));
                    memberInfo.setDate(invitorMap.get(Integer.parseInt(memberInfo.getMemberIndex())));
                    memberInfos.add(memberInfo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return memberInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<MemberInfo> memberInfos) {
            super.onPostExecute(memberInfos);

            viewHolder.recyclerView.setAdapter(new InvitationAdapter(InvitationListFragment.this, memberInfos));
        }
    }

    class ViewHolder {
        final public View baseView;
        final public RecyclerView recyclerView;

        public ViewHolder(View view) {
            this.baseView = view;
            this.recyclerView = (RecyclerView) view.findViewById(R.id.list);
        }
    }
}
