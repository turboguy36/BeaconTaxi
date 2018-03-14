package kr.sysgen.taxi.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.regex.Pattern;

/**
 * USIM 에서 전화번호를 받아와 처리하는 클래스
 *
 * Created by leehg on 2016-08-08.
 */
public class PhoneNumberUtil {
    private final String TAG = PhoneNumberUtil.class.getSimpleName();

    public static final String _DASH = "-";

    private Context mContext;

    /**
     * 생성자
     * @param context
     */
    public PhoneNumberUtil(Context context){
        this.mContext = context;
    }

    /**
     * Telephony Manager 로부터 핸드폰 번호를 받아 온다.
     * @return
     */
    public String getUSIMPhoneNumber(){
        TelephonyManager telephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);

        if(telephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
            return null;
        }else{
            return telephonyManager.getLine1Number();
        }
    }
    public String getDashedPhoneNumber(String phoneNumber) {
        Log.i(TAG, "phoneNumber: " + phoneNumber);
        String regEx = "(\\d{3})(\\d{3,4})(\\d{4})";
        if(!Pattern.matches(regEx, phoneNumber)) return null;
        return phoneNumber.replaceAll(regEx, "$1-$2-$3");
    }
    public String[] getSplitPhoneNumberArray(String number){
        return number.split(_DASH);
    }
    public String[] getSplitPhoneNumberArray(){
        String usimPhoneNumber = getUSIMPhoneNumber();
        if(usimPhoneNumber != null){
            return getSplitPhoneNumberArray(getDashedPhoneNumber(usimPhoneNumber));
        }else{
            return null;
        }
    }
    public String getDashedPhoneNumber(){
        String usimPhoneNumber = getUSIMPhoneNumber();
        if(usimPhoneNumber != null) {
            return getDashedPhoneNumber(usimPhoneNumber);
        }else{
            return null;
        }
    }
}
