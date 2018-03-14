package kr.sysgen.taxi.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.sysgen.taxi.R;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by leehg on 2016-09-20.
 */
public class RestartReceiver extends BroadcastReceiver {
    private final String TAG = RestartReceiver.class.getSimpleName();
    static public final String ACTION_RESTART_SERVICE = "RestartReceiver.restart";    // 값은 맘대로

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION_RESTART_SERVICE)){
            boolean isRunning = isBackgroundBeaconServiceRunning(context, BeaconListener.class.getName());
            if(!isRunning) {
                Log.i(TAG, "restart service");
                Intent beaconListenerIntent = new Intent(context, BeaconListener.class);
                context.startService(beaconListenerIntent);
            }else{
                Log.i(TAG, "already started service");
            }
        }else if(intent.getAction().equals(TimerService.class.getSimpleName())){
            Log.i(TAG, "action: " + intent.getAction());

            int historyIndex = intent.getIntExtra(context.getString(R.string.history_idx), -1);
            long beaconId = intent.getLongExtra(context.getString(R.string.beacon_id), -1);

            Log.i(TAG, "beaconId: " + beaconId);

            Intent timerServiceIntent = new Intent(context, TimerService.class);
            timerServiceIntent.putExtra(context.getString(R.string.history_idx), historyIndex);
            timerServiceIntent.putExtra(context.getString(R.string.beacon_id), beaconId);
            context.startService(timerServiceIntent);
        }
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

    private boolean isBackgroundBeaconServiceRunning(Context context, String listenerName) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {

            if(listenerName.equals(runningService.service.getClassName())){
                return true;
            }
        }
        return false;
    }
}
