package kr.sysgen.taxi.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leehg on 2016-05-19.
 */
public class TaxiInfo {
    private String date;
    private String taxiId;
    private String driverName;
    private String taxiNumber;
    private int minorNumber;
    private int majorNumber;
    private String uuid;
    private int pairMajor;
    private int pairMinor;
    private String getOnTaxiAddress;
    private String getOffTaxiAddress;

    private final String name = "name";
    private final String number = "number";
    private final String taxi = "taxi";
    private final String minor = "minor";
    private static final String BOADING_DATE = "date";
    private static final String TAXI_MAJOR = "taxi_major";
    private static final String TAXI_UUID = "taxi_uuid";
    private static final String TAXI_MINOR = "taxi_minor";
    private static final String TAXI_NUM = "taxi_num";
    private static final String PAIR_BEACON = "pair_beacon";
    private static final String PAIR_MAJOR = "pair_major";
    private static final String PAIR_MINOR = "pair_minor";
    public static final String MEM_ADDRESS = "mem_address";
    public static final String ADDRESS_GET_OFF = "address_get_off";

    public TaxiInfo(){

    }
    public TaxiInfo(String date, String texiId) {
        this.date = date;
        this.taxiId = texiId;
    }

    public TaxiInfo(int minor){
        this.minorNumber = minor;
    }

    public void setDate(String date){this.date = date;}

    public String getDate() {
        return date;
    }

    public String getTaxiId() {
        return taxiId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getTaxiNumber() {
        return taxiNumber;
    }

    public void setTaxiNumber(String taxiNumber) {
        this.taxiNumber = taxiNumber;
    }

    public int getMinorNumber() {
        return minorNumber;
    }

    public void setMinorNumber(int minorNumber) {
        this.minorNumber = minorNumber;
    }

    public int getMajorNumber() {
        return majorNumber;
    }

    public void setMajorNumber(int majorNumber) {
        this.majorNumber = majorNumber;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getPairMajor() {
        return pairMajor;
    }

    public void setPairMajor(int pairMajor) {
        this.pairMajor = pairMajor;
    }

    public int getPairMinor() {
        return pairMinor;
    }

    public void setPairMinor(int pairMinor) {
        this.pairMinor = pairMinor;
    }

    public String getGetOnTaxiAddress() {
        return getOnTaxiAddress;
    }

    public void setGetOnTaxiAddress(String getOnTaxiAddress) {
        this.getOnTaxiAddress = getOnTaxiAddress;
    }

    public String getGetOffTaxiAddress() {
        return getOffTaxiAddress;
    }

    public void setGetOffTaxiAddress(String getOffTaxiAddress) {
        this.getOffTaxiAddress = getOffTaxiAddress;
    }

    @Override
    public String toString() {
        String result = new String();
        try {
            JSONObject json = new JSONObject();
            json.put(name, this.driverName);
            json.put(number, this.taxiNumber);
            json.put(minor, this.minorNumber);
            json.put(PAIR_MAJOR, this.pairMajor);
            json.put(PAIR_MINOR, this.pairMinor);
            json.put(BOADING_DATE, this.date);
            json.put(MEM_ADDRESS, this.getOnTaxiAddress);
            json.put(ADDRESS_GET_OFF, this.getOffTaxiAddress);

            result = new JSONObject().put(taxi, json).toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        return result;
    }
    public static TaxiInfo parseTaxi(JSONObject input){
        TaxiInfo result = new TaxiInfo();
        try {
            result.setMajorNumber(input.getInt(TAXI_MAJOR));
            result.setMinorNumber(input.getInt(TAXI_MINOR));
            result.setTaxiNumber(input.getString(TAXI_NUM));
            result.setUuid(input.getString(TAXI_UUID));
            result.setGetOnTaxiAddress(input.getString(MEM_ADDRESS));
            result.setGetOffTaxiAddress(input.getString(ADDRESS_GET_OFF));

            JSONObject pairBeacon = input.getJSONObject(PAIR_BEACON);
            if(pairBeacon != null) {
                if(pairBeacon.has(TAXI_MAJOR)) {
                    result.setPairMajor(pairBeacon.getInt(TAXI_MAJOR));
                }
                if(pairBeacon.has(TAXI_MINOR)) {
                    result.setPairMinor(pairBeacon.getInt(TAXI_MINOR));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return result;
    }
}
