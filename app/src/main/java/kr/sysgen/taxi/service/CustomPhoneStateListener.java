package kr.sysgen.taxi.service;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by dev01 on 2016-11-23.
 */

public class CustomPhoneStateListener extends PhoneStateListener {
    private static final String TAG = CustomPhoneStateListener.class.getSimpleName();

    private Context context;

    public CustomPhoneStateListener(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                //when Idle i.e no call
                TimerService.IS_PHONE_CALL = false;

                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //when Off hook i.e in call
                //Make intent and start your service here
                TimerService.IS_PHONE_CALL = true;

                break;
            case TelephonyManager.CALL_STATE_RINGING:
                //when Ringing

                break;
            default:
                break;
        }
    }
}
