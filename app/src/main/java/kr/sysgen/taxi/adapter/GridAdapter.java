package kr.sysgen.taxi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import kr.sysgen.taxi.R;

/**
 * Created by leehg on 2016-07-27.
 */
public class GridAdapter extends BaseAdapter{
    private final Context mContext;
    private int layout;
    private String[] items;

    public GridAdapter(Context c, int layout, String[] array) {
        this.mContext = c;
        this.layout = layout;
        this.items = array;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public String getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(this.layout, null);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView)convertView.findViewById(R.id.item_text_view);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.title.setText(getItem(position));

        return convertView;
    }
    class ViewHolder {
        TextView title;
    }

}
