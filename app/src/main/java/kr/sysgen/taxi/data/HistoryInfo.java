package kr.sysgen.taxi.data;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by dev01 on 2016-12-14.
 */

public class HistoryInfo {
    private static final String HISTORY_INDEX = "history_index";
    private static final String LONGITUTE_GET_ON = "longitude_get_on";
    private static final String TAXI_SC_NAME = "taxi_sc_name";
    private static final String TIME_GET_ON = "time_get_on";
    private static final String LATITUDE_GET_ON = "latitude_get_on";
    private static final String TAXI_NUM = "taxi_num";
    private static final String MEM_ADDRESS = "mem_address";
    private static final String MEMBER_INDEX = "mem_idx";

    private int historyIndex;
    private int memberIndex;

    private Date timeGetOn;
    private String timeGetOff;
    private double latituteGetOn;
    private double longitudeGetOn;
    private long latituteGetOff;
    private long longitudeGetOff;

    private int taxiIndex;
    private String taxiScName;
    private String taxiNum;
    private String address;

    public HistoryInfo() {

    }

    public int getHistoryIndex() {
        return historyIndex;
    }

    public void setHistoryIndex(int historyIndex) {
        this.historyIndex = historyIndex;
    }

    public int getMemberIndex() {
        return memberIndex;
    }

    public void setMemberIndex(int memberIndex) {
        this.memberIndex = memberIndex;
    }

    public Date getTimeGetOn() {
        return timeGetOn;
    }

    public void setTimeGetOn(String timeGetOn) {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            this.timeGetOn = sd.parse(timeGetOn);
        }catch(ParseException e){
            e.printStackTrace();
        }
    }

    public String getTimeGetOff() {
        return timeGetOff;
    }

    public void setTimeGetOff(String timeGetOff) {
        this.timeGetOff = timeGetOff;
    }

    public double getLatituteGetOn() {
        return latituteGetOn;
    }

    public void setLatituteGetOn(double latituteGetOn) {
        this.latituteGetOn = latituteGetOn;
    }

    public double getLongitudeGetOn() {
        return longitudeGetOn;
    }

    public void setLongitudeGetOn(double longitudeGetOn) {
        this.longitudeGetOn = longitudeGetOn;
    }

    public long getLatituteGetOff() {
        return latituteGetOff;
    }

    public void setLatituteGetOff(long latituteGetOff) {
        this.latituteGetOff = latituteGetOff;
    }

    public long getLongitudeGetOff() {
        return longitudeGetOff;
    }

    public void setLongitudeGetOff(long longitudeGetOff) {
        this.longitudeGetOff = longitudeGetOff;
    }

    public int getTaxiIndex() {
        return taxiIndex;
    }

    public void setTaxiIndex(int taxiIndex) {
        this.taxiIndex = taxiIndex;
    }

    public String getTaxiScName() {
        return taxiScName;
    }

    public void setTaxiScName(String taxiScName) {
        this.taxiScName = taxiScName;
    }

    public String getTaxiNum() {
        return taxiNum;
    }

    public void setTaxiNum(String taxiNum) {
        this.taxiNum = taxiNum;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static HistoryInfo parseHistory(String input) throws JSONException {

        return parseHistory(new JSONObject(input));
    }



    public static HistoryInfo parseHistory(JSONObject input) {
        HistoryInfo result = new HistoryInfo();
        if (input.has(HISTORY_INDEX)) {
            try {
                result.setHistoryIndex(Integer.parseInt(input.getString(HISTORY_INDEX)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (input.has(LATITUDE_GET_ON)) {
            try {
                result.setLatituteGetOn(Double.parseDouble(input.getString(LATITUDE_GET_ON)));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        if (input.has(LONGITUTE_GET_ON)) {
            try {
                result.setLongitudeGetOn(Double.parseDouble(input.getString(LONGITUTE_GET_ON)));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        if (input.has(TIME_GET_ON)) {
            try {
                result.setTimeGetOn(input.getString(TIME_GET_ON));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (input.has(TAXI_SC_NAME)) {
            try {
                result.setTaxiScName(input.getString(TAXI_SC_NAME));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (input.has(TAXI_NUM)) {
            try {
                result.setTaxiNum(input.getString(TAXI_NUM));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (input.has(MEM_ADDRESS)) {
            try {
                result.setAddress(input.getString(MEM_ADDRESS));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (input.has(MEMBER_INDEX)) {
            try {
                result.setMemberIndex(input.getInt(MEMBER_INDEX));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put(HISTORY_INDEX, getHistoryIndex());
            json.put(MEMBER_INDEX, getMemberIndex());
            json.put(MEM_ADDRESS, getAddress());
            json.put(TAXI_NUM, getTaxiNum());
            json.put(TAXI_SC_NAME, getTaxiScName());
            json.put(LATITUDE_GET_ON, getLatituteGetOn());
            json.put(LONGITUTE_GET_ON, getLongitudeGetOn());
            json.put(TIME_GET_ON, getTimeGetOn());
        }catch (JSONException e){
            e.printStackTrace();
        }
        return json.toString();
    }

}
