package kr.sysgen.taxi.adapter;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.ShowMyParentFragment;
//import kr.sysgen.taxi.activity.Fragment.dummy.DummyContent.DummyItem;
import kr.sysgen.taxi.data.MemberInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link MemberInfo} and makes a call to the
 * specified {@link ShowMyParentFragment.OnParentListFragmentInteractionListener}.
 */
public class ParentViewAdapter extends RecyclerView.Adapter<ParentViewAdapter.ViewHolder> {
    private final List<MemberInfo> mValues;
    private final ShowMyParentFragment.OnParentListFragmentInteractionListener mListener;
    private ShowMyParentFragment myParentFragment;

    public ParentViewAdapter(Fragment fragment, List<MemberInfo> items, ShowMyParentFragment.OnParentListFragmentInteractionListener listener) {
        this.mValues = items;
        this.mListener = listener;
        this.myParentFragment = (ShowMyParentFragment)fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_my_parent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getMemberName());
        holder.mContentView.setText(mValues.get(position).getMemberPhoneNumber());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myParentFragment.onItemClicked(mValues.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public interface OnItemClickListener{
        public boolean onItemClicked(MemberInfo memberInfo);
    }
    public void swap(ArrayList<MemberInfo> inputData){
        mValues.clear();
        mValues.addAll(inputData);
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public MemberInfo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
