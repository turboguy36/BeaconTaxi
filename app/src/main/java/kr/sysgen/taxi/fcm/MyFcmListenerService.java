package kr.sysgen.taxi.fcm;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.reco.RecoPushUtil;
import kr.sysgen.taxi.service.BeaconListener;
import kr.sysgen.taxi.util.SysgenPreference;

/**
 * Created by dev01 on 2016-10-19.
 */

public class MyFcmListenerService extends FirebaseMessagingService {
    private final String TAG = MyFcmListenerService.class.getSimpleName();

    public static final int TYPE_CHECK_IN = 0;
    public static final int TYPE_CHECK_OUT = 1;
    public static final int TYPE_SET_PARENT = 2;
    public static final int TYPE_TURN_ON_BLUETOOTH = 3;
    public static final int TYPE_INVITE_CHILD = 4;

    private final String TYPE_KEY = "type";
    private final String TITLE_KEY = "title";
    private final String MESSAGE_KEY = "message";

    /**
     * key : type, title, message
     *
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map data = remoteMessage.getData();

        JSONObject paramJson = new JSONObject();
        try {
            JSONObject json = new JSONObject();
            json.put(getString(R.string.from_push_tag), true);
            paramJson.put(getString(R.string.parameter), json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String title = (String) data.get(TITLE_KEY);
        String message = (String) data.get(MESSAGE_KEY);
        String strType = (String) data.get(TYPE_KEY);

        int type = Integer.parseInt(strType);

        if (type == TYPE_TURN_ON_BLUETOOTH) {
            // 보호자가 어플리케이션을 동작 시켰는데,
            // 나의 블루투스를 켜고 싶다.
            turnOnBluetooth();
            setServiceStart(getApplicationContext());
        } else if(type == TYPE_INVITE_CHILD) {
            // 보호자가 나를 초대 하였다.
            try {
                long time = System.currentTimeMillis();

                JSONArray jsonArray = getJSONArrayFromPreference();

                String memberIndex = getMemberIndexFromServer(message);
                JSONObject invitorJson = new JSONObject();
                invitorJson.put(getString(R.string.mem_idx), memberIndex);
                invitorJson.put(getString(R.string.time), time);

                jsonArray.put(invitorJson);

                putJSONArrayToPreference(jsonArray);
            } catch(JSONException e) {
                e.printStackTrace();
            }
        } else if(type == TYPE_SET_PARENT){
            // 초대 수락 메시지가 도착 했다.
            new RecoPushUtil(getApplicationContext()).popupNotification(title, message, paramJson.toString(), type);
        } else {
            // 탑승 혹은 하차 메시지가 도착 했다.
            new RecoPushUtil(getApplicationContext()).popupNotification(title, message, paramJson.toString(), type);
        }
    }
    private void putJSONArrayToPreference(JSONArray jsonArray) throws JSONException {
        SysgenPreference pref = new SysgenPreference(getApplicationContext());
        JSONObject prefJson = new JSONObject();
        prefJson.put(getString(R.string.invitation_user_index_array), jsonArray);
        pref.putString(getString(R.string.invitation_user_index_object), prefJson.toString());
    }
    private JSONArray getJSONArrayFromPreference() throws JSONException{
        SysgenPreference pref = new SysgenPreference(getApplicationContext());
        JSONArray jsonArray = new JSONArray();

        String invitationArrayString = pref.getString(getString(R.string.invitation_user_index_object));

        if(invitationArrayString!=null && invitationArrayString.length()>0){
            JSONObject json = new JSONObject(invitationArrayString);
            jsonArray = json.getJSONArray(getString(R.string.invitation_user_index_array));
        }

        return jsonArray;
    }
    private String getMemberIndexFromServer(String input)throws JSONException{
        JSONObject json = new JSONObject(input);
        String memberIndex = json.getString(getString(R.string.rl_mem_idx1));
        return memberIndex;
    }

    /**
     *
     * @param context
     */
    private void setServiceStart(Context context){
        Intent monitoringService = new Intent(context, BeaconListener.class);
        boolean serviceRunning = isBackgroundBeaconServiceRunning(getApplicationContext());

        if(!serviceRunning) {
            startService(monitoringService);
        }
    }

    /**
     *
     */
    private void turnOnBluetooth(){
        BluetoothManager mBluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter.isEnabled()){
            Log.i(TAG, "bluetooth enabled: true");
        }else{
            Log.i(TAG, "bluetooth enabled: false");
        }
        mBluetoothAdapter.enable();
    }
    private boolean isBackgroundBeaconServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if(BeaconListener.class.getName().equals(runningService.service.getClassName())){
                return true;
            }
        }
        return false;
    }
}