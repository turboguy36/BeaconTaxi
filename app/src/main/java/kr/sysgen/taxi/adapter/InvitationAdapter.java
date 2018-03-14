package kr.sysgen.taxi.adapter;

import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.InvitationListFragment;
import kr.sysgen.taxi.data.MemberInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {
    private ArrayList<MemberInfo> mValues;
    private InvitationListFragment parentFragment;
    private static final int EMPTY_VIEW = 0xA;
    public InvitationAdapter(Fragment fragment, ArrayList<MemberInfo> items) {
        this.parentFragment = (InvitationListFragment)fragment;
        mValues = items;
    }

    public void setData(ArrayList<MemberInfo> items){
        mValues = items;
    }
    public ArrayList<MemberInfo> getData(){
        return mValues;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        if(viewType == EMPTY_VIEW){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_empty_view, parent, false);
            return new EmptyViewHolder(view);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(mValues.size() > 0) {
            long date = Long.valueOf(mValues.get(position).getDate());

            DateFormat dateFormat = new DateFormat();
            String strDate = dateFormat.format(parentFragment.getString(R.string.date_format), date).toString();

            ((CardView) holder.mView).setCardElevation(2.0f);
            ((CardView) holder.mView).setContentPadding(5, 5, 5, 15);
            holder.mItem = mValues.get(position);

            holder.parentName.setText(mValues.get(position).getMemberName());
            holder.invitationDate.setText(strDate);
            holder.parentPhoneNumber.setText(mValues.get(position).getMemberPhoneNumber());

            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setTag(holder.mItem);
                    parentFragment.onClick(v);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mValues.size() == 0){
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mValues.size()>0 ? mValues.size() : 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView parentName;
        public final TextView parentPhoneNumber;
        public final TextView invitationDate;
        public final Button acceptButton;

        public MemberInfo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            parentName = (TextView) view.findViewById(R.id.parent_name);
            parentPhoneNumber = (TextView) view.findViewById(R.id.parent_phone_number);
            invitationDate = (TextView) view.findViewById(R.id.invitation_date);
            acceptButton = (Button) view.findViewById(R.id.button_accept);
        }
    }
    public class EmptyViewHolder extends ViewHolder{

        public EmptyViewHolder(View itemView) {
            super(itemView);

        }
    }
}
