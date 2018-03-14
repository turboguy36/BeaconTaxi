package kr.sysgen.taxi.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kr.sysgen.taxi.R;

/**
 * Created by leehg on 2016-08-02.
 */
public class EmptyAdapter extends RecyclerView.Adapter<EmptyAdapter.ViewHolder> {

    public EmptyAdapter(){}
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_empty, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(holder.textView != null) {
            holder.textView.setText(R.string.have_no_child);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView textView;

        public ViewHolder(View view){
            super(view);
            this.view = view;
            this.textView = (TextView)view.findViewById(R.id.empty_item_text_view);
        }
    }
}
