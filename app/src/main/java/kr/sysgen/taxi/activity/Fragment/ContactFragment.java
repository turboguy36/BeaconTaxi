package kr.sysgen.taxi.activity.Fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.adapter.ContactRecyclerViewAdapter;
import kr.sysgen.taxi.data.ContactInfo;

import java.util.ArrayList;

public class ContactFragment extends Fragment implements View.OnClickListener{
    private final String TAG = ContactFragment.class.getSimpleName();

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    ViewHolder viewHolder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactFragment() {
    }

    public static ContactFragment newInstance(int columnCount) {
        ContactFragment fragment = new ContactFragment();
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
    ArrayList<ContactInfo> list;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        viewHolder = new ViewHolder(view);

        view.setOnClickListener(this);
        RecyclerView recyclerView = viewHolder.listView;

        Log.i(TAG, "onCreateView");
        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Log.i(TAG, "instanceof RecyclerView - " + mColumnCount);

            Context context = view.getContext();


            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            ContactInfo contactInfo01 = new ContactInfo("lee", "010-0000-0001");
            ContactInfo contactInfo02 = new ContactInfo("lee", "010-0000-0002");
            ContactInfo contactInfo03 = new ContactInfo("lee", "010-0000-0003");
            list = new ArrayList();
            list.add(contactInfo01);
            list.add(contactInfo02);
            list.add(contactInfo03);

            viewHolder.setMyAdapter(new ContactRecyclerViewAdapter(list));
            recyclerView.setAdapter(viewHolder.getMyAdapter());
        }
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick");

        list.remove(2);
        viewHolder.getMyAdapter().setData(list);
        viewHolder.getMyAdapter().notifyDataSetChanged();
    }
    private class RefreshTask extends AsyncTask<String, Void, ArrayList<ContactInfo>>{
        @Override
        protected void onPostExecute(ArrayList<ContactInfo> contactInfos) {
            super.onPostExecute(contactInfos);
        }

        @Override
        protected ArrayList<ContactInfo> doInBackground(String... params) {
            return null;
        }
    }
    class ViewHolder {
        final RecyclerView listView;
        ContactRecyclerViewAdapter myAdapter;
        public ViewHolder(View view){
            listView = (RecyclerView)view.findViewById(R.id.list);
        }

        public ContactRecyclerViewAdapter getMyAdapter() {
            return myAdapter;
        }

        public void setMyAdapter(ContactRecyclerViewAdapter myAdapter) {
            this.myAdapter = myAdapter;
        }
    }
}
