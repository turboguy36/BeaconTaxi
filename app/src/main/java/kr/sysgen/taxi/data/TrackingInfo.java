package kr.sysgen.taxi.data;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by dev01 on 2016-12-14.
 */

public class TrackingInfo {
    private int memIdx;
    private int historyIndex;
    private String time;
    private double latitude;
    private double longitude;

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("mem_idx", memIdx);
            json.put("history_index", historyIndex);
            json.put("time", time);
            json.put("mem_lat", latitude);
            json.put("mem_lng", longitude);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return json.toString();
    }

    public int getMemIdx() {
        return memIdx;
    }

    public void setMemIdx(int memIdx) {
        this.memIdx = memIdx;
    }

    public int getHistoryIndex() {
        return historyIndex;
    }

    public void setHistoryIndex(int historyIndex) {
        this.historyIndex = historyIndex;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public static String getAddress(Context mContext, double lat, double lng){
        String getOnAddress = "위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            if(addresses!=null && addresses.size()>0){
                Address address = addresses.get(0);
                String locationAddress = address.getAddressLine(0).toString();
                final int countryNameLength = address.getCountryName().length();
                final String newAddress = locationAddress.substring(countryNameLength, locationAddress.length());
                getOnAddress = newAddress;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return getOnAddress;
    }
}
