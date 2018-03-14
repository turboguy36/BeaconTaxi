package kr.sysgen.taxi.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leehg on 2016-07-08.
 */
public class MemberInfo {
    public static final int STATUS_OUT_TAXI = 0;
    public static final int STATUS_IN_TAXI = 1;

    private String memberIndex;
    private String memberName;
    private String memberPhoneNumber;
    private int status;
    private double latitude;
    private double longitude;
    private String date;
    private String taxiIndex;
    private String historyIndex;

    public String getTaxiIndex() {
        return taxiIndex;
    }

    public void setTaxiIndex(String taxiIndex) {
        this.taxiIndex = taxiIndex;
    }

    public MemberInfo(){
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMemberPhoneNumber() {
        return memberPhoneNumber;
    }

    public void setMemberPhoneNumber(String memberPhoneNumber) {
        this.memberPhoneNumber = memberPhoneNumber;
    }

    public String getMemberIndex() {
        return memberIndex;
    }

    public void setMemberIndex(String meIndex) {
        this.memberIndex = meIndex;
    }

    public String getHistoryIndex() {
        return historyIndex;
    }

    public void setHistoryIndex(String historyIndex) {

        this.historyIndex = historyIndex;
    }

    @Override
    public String toString() {
        JSONObject result = new JSONObject();
        try {
            result.put("mem_name", getMemberName());
            result.put("mem_idx", getMemberIndex());
            result.put("mem_phone", getMemberPhoneNumber());
            result.put("mem_status", getStatus());
            result.put("mem_lng", getLongitude());
            result.put("mem_lat", getLatitude());
            result.put("mem_loc_data", getDate());
            result.put("history_index", getHistoryIndex());
        }catch(JSONException e){
            e.printStackTrace();
        }
        return result.toString();
    }
    static final String MEM_IDX = "mem_idx";
    static final String MEM_NAME = "mem_name";
    static final String MEM_PHONE = "mem_phone";
    static final String MEM_HP_NUM = "mem_hp_num";
    static final String MEM_STATUS = "mem_status";
    static final String MEM_LAT = "mem_lat";
    static final String MEM_LNG = "mem_lng";
    static final String HISTORY_INDEX = "history_index";

    public static MemberInfo parseMemberInfo(JSONObject input) throws JSONException{
        MemberInfo result = new MemberInfo();
        if(input.has(MEM_IDX)) {
            result.setMemberIndex(input.getString(MEM_IDX));
        }
        if(input.has(MEM_NAME)) {
            result.setMemberName(input.getString(MEM_NAME));
        }
        if (input.has(MEM_PHONE)) {
            result.setMemberPhoneNumber(input.getString(MEM_PHONE));
        }else if(input.has(MEM_HP_NUM)){
            result.setMemberPhoneNumber(input.getString(MEM_HP_NUM));
        }
        if (input.has(MEM_STATUS)) {
            result.setStatus(input.getInt(MEM_STATUS));
        }
        if (input.has(MEM_LAT)) {
            try {
                String latitude = input.getString(MEM_LAT);
                if(latitude != null) {
                    result.setLatitude(Double.valueOf(latitude));
                }
            }catch(NumberFormatException e){
//                e.printStackTrace();
                result.setLatitude(0.0);
            }
        }
        if (input.has(MEM_LNG)) {
            try{
                String longitude = input.getString(MEM_LNG);
                if(longitude != null) {
                    result.setLongitude(Double.valueOf(longitude));
                }
            }catch(NumberFormatException e){
//                e.printStackTrace();
                result.setLatitude(0.0);
            }
        }
        if (input.has(HISTORY_INDEX)) {
            result.setHistoryIndex(input.getString(HISTORY_INDEX));
        }
        return result;
    }
    public static MemberInfo parseMemberInfo(String input) throws JSONException{
        JSONObject jsonObject = new JSONObject(input);
        return parseMemberInfo(jsonObject);
    }
}
