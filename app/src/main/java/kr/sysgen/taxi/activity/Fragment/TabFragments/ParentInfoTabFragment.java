package kr.sysgen.taxi.activity.Fragment.TabFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.GetContactActivity;
import kr.sysgen.taxi.adapter.ParentListArrayAdapter;
import kr.sysgen.taxi.data.ContactInfo;
import kr.sysgen.taxi.data.ParentInfo;

/**
 * Created by leehg on 2016-05-19.
 */
public class ParentInfoTabFragment extends Fragment implements View.OnClickListener{
    private final String TAG = ParentInfoTabFragment.class.getSimpleName();
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.tab_parent_info, null);
        listView = (ListView)view.findViewById(R.id.listview_parents);

        ParentListArrayAdapter adapter = new ParentListArrayAdapter(getContext(), R.layout.list_item_parents);
        adapter.setData(makeTempData());
        listView.setAdapter(adapter);

        FloatingActionButton actionButton = (FloatingActionButton)view.findViewById(R.id.floating_button);
        actionButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private ArrayList<ParentInfo> makeTempData(){
        ArrayList<ParentInfo> result = new ArrayList<>();

        ParentInfo pInfo_01 = new ParentInfo("엄마", "010-7946-6542", R.drawable.avartar_03);
        ParentInfo pInfo_02 = new ParentInfo("아빠", "010-7561-5312", R.drawable.avartar_01);
        ParentInfo pInfo_03 = new ParentInfo("오빠", "010-4561-3791", R.drawable.avartar_02);
        result.add(pInfo_01);
        result.add(pInfo_02);
        result.add(pInfo_03);

        return result;
    }

    @Override
    public void onClick(View v) {
//        getActivity().startActivity(new Intent(getContext(), GetContactActivity.class));
        this.startActivityForResult(new Intent(getContext(), GetContactActivity.class), GetContactActivity.ACTIVITY_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final String extra = data.getStringExtra(GetContactActivity.TAG);
        Log.i(TAG, "requestCode: " + requestCode + "/resultCode: " +resultCode +" /data: " + data.getStringExtra(GetContactActivity.TAG));
        ParentInfo parentInfo = new ParentInfo();
        try {
            JSONObject json = new JSONObject(extra);
//            json.get(ContactInfo.CID_TAG);
            parentInfo.setName(json.getString(ContactInfo.NAME_TAG));
            parentInfo.setPhoneNumber(json.getString(ContactInfo.PHONE_TAG));
            parentInfo.setPhoto(R.drawable.avartar_01);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ParentListArrayAdapter adapter = ((ParentListArrayAdapter)listView.getAdapter());
        adapter.addData(parentInfo);
        adapter.notifyDataSetChanged();
    }
}
