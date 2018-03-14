package kr.sysgen.taxi.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leehg on 2016-08-12.
 */

public class TaxiUser extends MemberInfo{
    private String latitudeGetOn;
    private String longitudeGetOn;
    private String dateGetOn;
    private String latitudeGetOut;
    private String longitudeGetOut;
    private String dateGetOut;
    private String taxiName;
    private String taxiSCName;
    private String taxiNumber;
//    private String historyIndex;

    public String getLatitudeGetOn() {
        return latitudeGetOn;
    }

    public void setLatitudeGetOn(String latitudeGetOn) {
        this.latitudeGetOn = latitudeGetOn;
    }

    public String getLongitudeGetOn() {
        return longitudeGetOn;
    }

    public void setLongitudeGetOn(String longitudeGetOn) {
        this.longitudeGetOn = longitudeGetOn;
    }

    public String getDateGetOn() {
        return dateGetOn;
    }

    public void setDateGetOn(String dateGetOn) {
        this.dateGetOn = dateGetOn;
    }

    public String getLatitudeGetOut() {
        return latitudeGetOut;
    }

    public void setLatitudeGetOut(String latitudeGetOut) {
        this.latitudeGetOut = latitudeGetOut;
    }

    public String getLongitudeGetOut() {
        return longitudeGetOut;
    }

    public void setLongitudeGetOut(String longitudeGetOut) {
        this.longitudeGetOut = longitudeGetOut;
    }

    public String getDateGetOut() {
        return dateGetOut;
    }

    public void setDateGetOut(String dateGetOut) {
        this.dateGetOut = dateGetOut;
    }


    public String getTaxiName() {
        return taxiName;
    }

    public void setTaxiName(String taxiName) {
        this.taxiName = taxiName;
    }

    public String getTaxiSCName() {
        return taxiSCName;
    }

    public void setTaxiSCName(String taxiSCName) {
        this.taxiSCName = taxiSCName;
    }

    public String getTaxiNumber() {
        return taxiNumber;
    }

    public void setTaxiNumber(String taxiNumber) {
        this.taxiNumber = taxiNumber;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("history_index", getHistoryIndex());
            json.put("mem_idx", getMemberIndex());
        }catch(JSONException e){
            e.printStackTrace();
        }
        return json.toString();
    }
}
