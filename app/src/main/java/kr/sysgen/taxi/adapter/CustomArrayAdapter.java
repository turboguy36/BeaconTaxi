package kr.sysgen.taxi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.TaxiInfo;

/**
 * Created by leehg on 2016-05-19.
 */
public class CustomArrayAdapter extends ArrayAdapter<TaxiInfo> {
    private ViewHolder viewHolder;
    private ArrayList<TaxiInfo> mList;
    private Context mContext;
    private int viewId;
    private LayoutInflater inflater;

    public CustomArrayAdapter(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
        this.viewId = resource;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout)convertView;
        if(view == null){
            viewHolder = new ViewHolder();
            view = (LinearLayout)inflater.inflate(R.layout.list_item_history, null);
            viewHolder.dateText = (TextView)view.findViewById(R.id.text_date);
            viewHolder.taxiInfoText = (TextView)view.findViewById(R.id.text_taxi_num);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.dateText.setText(mList.get(position).getDate());
        viewHolder.taxiInfoText.setText(mList.get(position).getTaxiId());
        return view;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public TaxiInfo getItem(int position) {
        return mList.get(position);
    }

    public void setData(ArrayList<TaxiInfo> data){
        this.mList = data;
    }

    class ViewHolder{
        public TextView dateText;
        public TextView taxiInfoText;
    }
}
