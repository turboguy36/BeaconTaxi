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
import java.util.Iterator;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.TaxiInfo;

/**
 * Created by leehg on 2016-07-07.
 */
public class DriversAdapter extends ArrayAdapter<TaxiInfo> {
    private final String TAG = DriversAdapter.class.getSimpleName();

    private ArrayList< TaxiInfo> list;
    private ViewHolder viewHolder;
    private LayoutInflater inflater;
    private int viewId;

    public DriversAdapter(Context context, int resource) {
        super(context, resource);

        this.viewId = resource;
        this.inflater = LayoutInflater.from(context);
        this.list = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            TaxiInfo taxiInfo = this.list.get(position);

            viewHolder = new ViewHolder();

            convertView = (RelativeLayout)inflater.inflate(viewId, null);
            viewHolder.imageView = (ImageView)convertView.findViewById(R.id.driver_photo);
            viewHolder.driverName = (TextView)convertView.findViewById(R.id.driver_name);
            viewHolder.taxiNumber = (TextView)convertView.findViewById(R.id.text_taxi_number);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        TaxiInfo taxiInfo = this.getItem(position);

        viewHolder.driverName.setText("택시기사 : "+taxiInfo.getDriverName());
        viewHolder.taxiNumber.setText("택시번호 : "+ taxiInfo.getTaxiNumber());

        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public TaxiInfo getItem(int position) {
        return list.get(position);
    }

    public void addData(TaxiInfo value){
        if(contains(value))return;
        else this.list.add(value);
    }
    private boolean contains(TaxiInfo input){
        int inputMinor = input.getMinorNumber();
        boolean isContains = false;
        Iterator<TaxiInfo> iterator = this.list.iterator();
        while(iterator.hasNext()){
            try {
                TaxiInfo taxi = iterator.next();
                if (inputMinor == taxi.getMinorNumber()) {
                    isContains = true;
                }
            }catch(Exception e){
                e.printStackTrace();
                break;
            }
        }
        return isContains;
    }
    static class ViewHolder {
        public ImageView imageView;
        public TextView driverName;
        public TextView taxiNumber;
    }
}
