package kr.sysgen.taxi.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.data.ContactInfo;
import kr.sysgen.taxi.util.ImageHelper;

/**
 * Created by leehg on 2016-05-19.
 */
public class ContactArrayAdapter extends ArrayAdapter<ContactInfo> {
    private final String TAG= ContactArrayAdapter.class.getSimpleName();

    private ViewHolder viewHolder;
    private ArrayList<ContactInfo> mList;
    private Context mContext;
    private int viewId;
    private LayoutInflater inflater;

    public ContactArrayAdapter(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
        this.viewId = resource;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactInfo cInfo = mList.get(position);
        TextView nameText;
        TextView phoneNumberText;
        ImageView photoView;

        if(convertView == null){
            convertView = inflater.inflate(R.layout.list_item_contacts, parent, false);

            nameText = (TextView)convertView.findViewById(R.id.text_name);
            phoneNumberText = (TextView)convertView.findViewById(R.id.text_phone_number);
            photoView = (ImageView) convertView.findViewById(R.id.image_view);
            convertView.setTag(new ViewHolder(nameText, phoneNumberText, photoView));
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
            nameText = viewHolder.nameText;
            phoneNumberText = viewHolder.phoneNumberText;
            photoView = viewHolder.photoView;
        }

        nameText.setText(cInfo.getName());
        phoneNumberText.setText(cInfo.getPhone());

        Log.i(TAG, cInfo.getName() + " : " + cInfo.getPhoto());

        if(cInfo.getPhoto() != null) {
            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(cInfo.getPhoto()));
                Bitmap roundedImage = ImageHelper.getRoundedCornerBitmap(bm, 64);
                photoView.setImageBitmap(roundedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            photoView.setImageDrawable(mContext.getDrawable(R.drawable.avartar_02));
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public ContactInfo getItem(int position) {
        return mList.get(position);
    }

    public void setData(ArrayList<ContactInfo> data){
        this.mList = data;
    }

    class ViewHolder{

        public TextView nameText;
        public TextView phoneNumberText;
        public ImageView photoView;

        public ViewHolder(TextView nameText, TextView phoneNumberText, ImageView photoView){
            this.nameText = nameText;
            this.phoneNumberText = phoneNumberText;
            this.photoView = photoView;
        }
    }
}
