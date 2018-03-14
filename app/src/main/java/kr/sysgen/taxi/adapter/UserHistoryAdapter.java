package kr.sysgen.taxi.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.ShowMyParentFragment;
import kr.sysgen.taxi.activity.Fragment.TaxiUserHistoryFragment;
import kr.sysgen.taxi.activity.Fragment.UserHistoryFragment;
import kr.sysgen.taxi.data.HistoryInfo;
import kr.sysgen.taxi.data.MemberInfo;
import kr.sysgen.taxi.data.TaxiUser;

//import kr.sysgen.taxi.activity.Fragment.dummy.DummyContent.DummyItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link MemberInfo} and makes a call to the
 * specified {@link ShowMyParentFragment.OnParentListFragmentInteractionListener}.
 */
public class UserHistoryAdapter extends RecyclerView.Adapter<UserHistoryAdapter.ViewHolder> {
    private final String TAG = UserHistoryAdapter.class.getSimpleName();
    private final Context mContext;
    private final List<HistoryInfo> mValues;
    private UserHistoryFragment myParentFragment;

    public UserHistoryAdapter(Context context, Fragment fragment, List<HistoryInfo> items) {
        this.mContext = context;
        this.mValues = items;
        this.myParentFragment = (UserHistoryFragment)fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_taxi_user, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);

        holder.address.setText(holder.mItem.getAddress());

        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM월dd일");
        holder.date.setText(dateFormatter.format(holder.mItem.getTimeGetOn()));
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH시mm분");
        holder.time.setText(timeFormatter.format(holder.mItem.getTimeGetOn()));

        holder.taxiNumber.setText(holder.mItem.getTaxiNum());
//        holder.taxiCompany.setText(holder.mItem.getTaxiScName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myParentFragment.onItemClicked(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public interface OnItemClickListener{
        public boolean onItemClicked(HistoryInfo historyInfo);
    }
    public void swap(ArrayList<HistoryInfo> inputData){
        mValues.clear();
        mValues.addAll(inputData);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView address;
        public final TextView date;
        public final TextView time;
        public final TextView taxiNumber;
//        public final TextView taxiCompany;
        public HistoryInfo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            address = (TextView) view.findViewById(R.id.address);
            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time) ;
            taxiNumber = (TextView) view.findViewById(R.id.taxi_number);
//            taxiCompany = (TextView) view.findViewById(R.id.taxi_company);
        }
    }
}
