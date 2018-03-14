package kr.sysgen.taxi.activity.Fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.SlideMenuActivity;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, View.OnTouchListener{
    public static final String TAG = SettingsFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String START_TIME = "start_time";
//    private static final String END_TIME = "end_time";

    //
    private ViewHolder viewHolder;

    // 되돌아 갈 때 Title Bar 변경
    private String fragmentTitleStore;

    // 설정을 저장 할 저장소
    private SysgenPreference pref;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new SysgenPreference(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        viewHolder = new ViewHolder(view);
        fragmentTitleStore = ((SlideMenuActivity) getActivity()).getToolbar().getTitle().toString();

        ((SlideMenuActivity) getActivity()).setToolbarTitle(getString(R.string.fragment_setting_title));

        try {
            final String startTime = pref.getString(getString(R.string.location_information_start));

            if(startTime== null || startTime.equalsIgnoreCase("null")) {
                viewHolder.startTime.setText("09:00 AM");
            }else{
                viewHolder.startTime.setText(stringToCalendarDate(startTime));
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        try {
            final String endTime = pref.getString(getString(R.string.location_information_end));

            if(endTime == null || endTime.equalsIgnoreCase("null")) {
                viewHolder.endTime.setText("06:00 PM");
            }else{
                viewHolder.endTime.setText(stringToCalendarDate(endTime));
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        return view;
    }

    private String getCalendar(int hourOfDay, int minute){
        boolean isAm = true;// ture: 오전, false : 오후
        final int noon = 12;
        final int ten = 10;
        final int zero = 0;
        final char _blank = ' ';
        final char _colon =':';

        if(hourOfDay > noon){
            // 24 시간제를 12 시간제로
            hourOfDay -= noon;
            isAm = false;
        }
        StringBuffer time = new StringBuffer();
        if (hourOfDay < ten) {
            // 한자리 숫자 앞에 0 붙여줘서, 두자리로 만들자
            time.append(zero).append(hourOfDay);
        }else{
            time.append(hourOfDay);
        }
        time.append(_colon);
        if (minute < ten) {
            // 한자리 숫자 앞에 0 붙여줘서, 두자리로 만들자
            time.append(zero).append(minute);
        }else{
            time.append(minute);
        }
        time.append(_blank);

        if (isAm) {
            time.append(getString(R.string.title_am));
        } else {
            time.append(getString(R.string.title_pm));
        }
        return time.toString();
    }

    /**
     *
     * @param input
     * @return
     */
    private String stringToCalendarDate(String input){
        SimpleDateFormat format = new SimpleDateFormat(getString(R.string.hour_format));
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(input));
        }catch(ParseException e){
            e.printStackTrace();
        }

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return getCalendar(hourOfDay, minute);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((SlideMenuActivity) getActivity()).setToolbarTitle(fragmentTitleStore);
    }

    private String getTimeString(int hour, int minute){
        Log.i(TAG, "hour: " + hour + " /minute: " + minute);

        StringBuffer stringBuffer = new StringBuffer();
        if(hour < 10){
            stringBuffer.append("AM").append(0).append(hour);
        } else if(hour <= 12) {
            stringBuffer.append("AM").append(hour);
        } else if(hour > 12) {
            stringBuffer.append("PM").append(hour-12);
        }

        stringBuffer.append(':');

        if(minute < 10){
            stringBuffer.append(0);
        }
        stringBuffer.append(minute);

        return stringBuffer.toString();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.confirm_button:
                try {
                    Pair<Integer, Integer> startTimePair = (Pair<Integer, Integer>) viewHolder.startTime.getTag();
                    Pair<Integer, Integer> endTimePair = (Pair<Integer, Integer>) viewHolder.endTime.getTag();

                    if (startTimePair != null && endTimePair != null) {
                        String startTime = getTimeString(startTimePair.first, startTimePair.second);
                        String endTime = getTimeString(endTimePair.first,endTimePair.second);

                        getConfirmDialog(startTime, endTime).show();
                    }else{
                        String startTime = viewHolder.startTime.getText().toString();
                        String endTime = viewHolder.endTime.getText().toString();

                        getConfirmDialog(startTime, endTime).show();
                    }
                }catch(ClassCastException e){
                    e.printStackTrace();
                }
                break;
        }
    }

    private AlertDialog getConfirmDialog(final String start, final String end){
        final String message = new StringBuffer().append(getString(R.string.message_dialog_confirm)).append('\n').append(start).append('~').append(end).toString();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(getString(R.string.title_dialog_confirm)).setMessage(message)
                .setPositiveButton(getString(R.string.text_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        pref.putString(getString(R.string.location_information_start), start);
                        pref.putString(getString(R.string.location_information_end), end);
                        dialog.dismiss();
                    }
                }).setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return dialog.create();
    }

    /**
     *
     * @param editText
     * @param hour
     * @param min
     * @return
     */
    private TimePickerDialog getTimeDialog(final EditText editText, int hour, final int min){
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog, new TimePickerDialog.OnTimeSetListener(){

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                editText.setTag(new Pair<>(hourOfDay, minute));
                final String time = getCalendar(hourOfDay, minute);
                editText.setText(time);
            }
            // 초기 설정값
        }, hour, min, false);
        timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        switch(editText.getId()){
            case R.id.start_time_input:
                timePickerDialog.setTitle(getString(R.string.title_dialog_start));
                break;
            case R.id.end_time_input:
                timePickerDialog.setTitle(getString(R.string.title_dialog_end));
                break;
        }
        return timePickerDialog;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            SimpleDateFormat format = new SimpleDateFormat(getString(R.string.hour_format));
            Calendar calendar = Calendar.getInstance();

            switch (v.getId()) {
                case R.id.start_time_input:
                    final String startTime = viewHolder.startTime.getText().toString();
                    try {
                        calendar.setTime(format.parse(startTime));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    getTimeDialog(viewHolder.startTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)).show();
                    break;
                case R.id.end_time_input:
                    try {
                        final String endTime = viewHolder.endTime.getText().toString();
                        calendar.setTime(format.parse(endTime));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    getTimeDialog(viewHolder.endTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)).show();
                    break;
            }
        }
        return false;
    }

    class ViewHolder {
        final public View baseView;
        final Button confirmButton;
        final EditText startTime;
        final EditText endTime;

        public ViewHolder(View baseView) {
            this.baseView = baseView;
            this.startTime = (EditText)baseView.findViewById(R.id.start_time_input);
            this.endTime = (EditText)baseView.findViewById(R.id.end_time_input);
            this.confirmButton = (Button)baseView.findViewById(R.id.confirm_button);

            this.baseView.setOnClickListener(SettingsFragment.this);
            this.confirmButton.setOnClickListener(SettingsFragment.this);
            this.startTime.setOnTouchListener(SettingsFragment.this);
            this.endTime.setOnTouchListener(SettingsFragment.this);
        }
    }
}
