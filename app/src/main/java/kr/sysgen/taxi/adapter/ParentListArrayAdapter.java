package kr.sysgen.taxi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.ParentInfo;

/**
 * Created by leehg on 2016-05-19.
 */
public class ParentListArrayAdapter extends ArrayAdapter<ParentInfo> {
    private ViewHolder viewHolder;
    private ArrayList<ParentInfo> mList;
    private Context mContext;
    private int viewId;
    private LayoutInflater inflater;

    public ParentListArrayAdapter(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
        this.viewId = resource;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout view = (RelativeLayout)convertView;
        if(view == null){
            viewHolder = new ViewHolder();
            view = (RelativeLayout)inflater.inflate(R.layout.list_item_parents, null);
            viewHolder.nameText = (TextView)view.findViewById(R.id.parent_name);
            viewHolder.phoneText = (TextView)view.findViewById(R.id.parent_phone);
            viewHolder.photoView = (ImageView)view.findViewById(R.id.parent_photo);

            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.nameText.setText(mList.get(position).getName());
        viewHolder.phoneText.setText(mList.get(position).getPhoneNumber());
        viewHolder.photoView.setImageResource(mList.get(position).getPhoto());
        return view;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public ParentInfo getItem(int position) {
        return mList.get(position);
    }

    public void setData(ArrayList<ParentInfo> data){
        this.mList = data;
    }

    public boolean addData(ParentInfo data){
        return this.mList.add(data);
    }
    class ViewHolder{
        public TextView nameText;
        public TextView phoneText;
        public ImageView photoView;
    }
}
