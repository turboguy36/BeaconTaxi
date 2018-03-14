package kr.sysgen.taxi.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;

import kr.sysgen.taxi.R;

/**
 * Shared Preference 를 여러 장소에서 사용 하다 보면
 * 어디에 어떤 변수를 저장 했는지, 어떻게 꺼내 쓰는지 알기 힘들다.
 *
 * Created by leehg on 2016-07-22.
 */
public class SysgenPreference {
    private final String TAG = SysgenPreference.class.getSimpleName();

    private Context mContext;
    private SharedPreferences pref;

    public SysgenPreference(Context context){
        this.mContext = context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }
    public SharedPreferences getPreferences(){
        return pref;
    }
    public void remove(String key){
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key).commit();
    }
    public Boolean removeAll(){
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        return editor.commit();
    }
    public Boolean putString(String key, String value){
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(key, value);
        return editor.commit();
    }
    public Boolean putLong(String key, Long value){
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        return editor.commit();
    }
    public Long getLong(String key){
        return pref.getLong(key, 0);
    }
    public String getString(String key){
        return pref.getString(key, null);
    }
    public Boolean removeLong(String key){
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        return editor.commit();
    }
    public Boolean putInt(String key, int value){
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        return editor.commit();
    }
    public int getInt(String key){
        return pref.getInt(key, 0);
    }

    public void showAllPreferences(){
        Map<String, ?> allPrefs = pref.getAll();
        Iterator<?> iter = allPrefs.entrySet().iterator();
        while(iter.hasNext()){
            Log.i(TAG, iter.next().toString());
        }
    }
}