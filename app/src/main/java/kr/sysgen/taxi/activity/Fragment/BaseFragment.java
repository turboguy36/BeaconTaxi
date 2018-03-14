package kr.sysgen.taxi.activity.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.MainActivity;
import kr.sysgen.taxi.activity.MapsActivity;
import kr.sysgen.taxi.data.MemberInfo;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link BaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BaseFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    private String mParam1;

    private final String TAG = BaseFragment.class.getSimpleName();

    public BaseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment BaseFragment.
     */
    public static BaseFragment newInstance(String param1) {
        BaseFragment fragment = new BaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }
/*

    @Override
    public boolean onItemClickListener(final MemberInfo memberInfo) {
        if(memberInfo.getStatus() == MemberInfo.STATUS_IN_TAXI){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("탑승중 사용자")
                    .setSingleChoiceItems(R.array.dialog_choice, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    TaxiUserHistoryFragment fragment = TaxiUserHistoryFragment.newInstance(memberInfo.getMemberName(), memberInfo.toString());
                                    ((MainActivity)getActivity()).addFragment(fragment, TaxiUserHistoryFragment.TAG);

                                    break;
                                case 1:
                                    Intent intent = new Intent(getContext(), MapsActivity.class);

                                    Bundle bundle = new Bundle();
                                    bundle.putString(getString(R.string.mem_idx), memberInfo.getMemberIndex());
                                    bundle.putString(getString(R.string.member_info), memberInfo.toString());
                                    intent.putExtra(getString(R.string.parameter), bundle);

                                    startActivity(intent);
                                    break;
                                case 2:

                                    break;
                            }
                            dialog.dismiss();
                        }
                    }).show();


        }else if(memberInfo.getStatus() == MemberInfo.STATUS_OUT_TAXI){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("하차 한 사용자")
                    .setSingleChoiceItems(R.array.dialog_choice_off, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    TaxiUserHistoryFragment fragment = TaxiUserHistoryFragment.newInstance(memberInfo.getMemberName(), memberInfo.toString());
                                    ((MainActivity)getActivity()).addFragment(fragment, TaxiUserHistoryFragment.TAG);

                                    break;

                                case 1:

                                    break;
                            }
                            dialog.dismiss();
                        }
                    }).show();
        }
        return true;
    }
*/

    protected class ViewHolder{
        View baseView;
        ViewPager viewPager;
        TabLayout tabLayout;
        ProgressBar progressBar;

        public ViewHolder(View baseView){
            this.baseView = baseView;
            viewPager = (ViewPager)baseView.findViewById(R.id.viewpager);
            tabLayout = (TabLayout)baseView.findViewById(R.id.tabs_layout);
            progressBar = (ProgressBar)baseView.findViewById(R.id.progress_bar);
        }
        public void setBackground(){
            BitmapDrawable bg = new BitmapDrawable(getResources(), blur(getContext(), BitmapFactory.decodeResource(getResources(), R.drawable.city_view), 20));
            baseView.setBackground(bg);
        }
        private Bitmap blur(Context context, Bitmap sentBitmap, int radius) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

                final RenderScript rs = RenderScript.create(context);
                final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
                        Allocation.USAGE_SCRIPT);
                final Allocation output = Allocation.createTyped(rs, input.getType());
                final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                script.setRadius(radius); //0.0f ~ 25.0f
                script.setInput(input);
                script.forEach(output);
                output.copyTo(bitmap);
                return bitmap;
            }else{
                return null;
            }
        }
    }
}
