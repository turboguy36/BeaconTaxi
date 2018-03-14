package kr.sysgen.taxi.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.Fragment.GetContactActivityFragment;
import kr.sysgen.taxi.data.ContactInfo;
import kr.sysgen.taxi.util.ImageHelper;

/**
 * Created by leehg on 2016-08-02.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private final String TAG = GetContactActivityFragment.TAG;
    private ArrayList<ContactInfo> items;

    private GetContactActivityFragment parentFragment;

    public ContactAdapter(Fragment fragment, ArrayList<ContactInfo> items){
        this.items = items;

        this.parentFragment = (GetContactActivityFragment)fragment;
    }

    public void setData(ArrayList<ContactInfo> items){
        this.items = items;
    }

    public void updateList(ArrayList<ContactInfo> items){
//        this.items.clear();
        this.items = items;
        Log.i(TAG, "this.items: " + this.items.size());
//        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_contacts, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ContactInfo contactInfo = items.get(position);
        holder.name.setText(contactInfo.getName());
        holder.phone.setText(contactInfo.getPhone());

        Bitmap bitmap = null;

        if(contactInfo.getPhoto() != null) {
            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(parentFragment.getContext().getContentResolver(), Uri.parse(contactInfo.getPhoto()));
                bitmap = ImageHelper.getRoundedCornerBitmap(bm, 64);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            bitmap = holder.getEmptyPhoto();
        }
        holder.image.setImageBitmap(bitmap);

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.onClicked(contactInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnClickListener{
        public void onClicked(ContactInfo contactInfo);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView name;
        public final TextView phone;
        public final ImageView image;
        public final Button button;
        private Bitmap emptyPhoto;

        public ViewHolder(View view){
            super(view);
            this.view = view;
            this.name = (TextView)view.findViewById(R.id.text_name);
            this.phone = (TextView)view.findViewById(R.id.text_phone_number);
            this.image = (ImageView)view.findViewById(R.id.image_view);
            this.button = (Button)view.findViewById(R.id.button);

            setEmptyPhoto();
        }

        private void setEmptyPhoto(){
            Bitmap icon = BitmapFactory.decodeResource(parentFragment.getResources(), R.drawable.avartar_01);
            Bitmap roundedImage = ImageHelper.getRoundedCornerBitmap(icon, 64);
            this.emptyPhoto = roundedImage;
        }

        public Bitmap getEmptyPhoto() {
            return this.emptyPhoto;
        }
    }
}
