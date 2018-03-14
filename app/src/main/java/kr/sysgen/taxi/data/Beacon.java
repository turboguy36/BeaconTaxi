package kr.sysgen.taxi.data;

import android.bluetooth.le.ScanResult;

import com.android.scanner.ScanBLEResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leehg on 2016-09-09.
 */
public class Beacon {
    private final String TAG = Beacon.class.getSimpleName();

    long id;
    String uuid;
    int major;
    int minor;
    int weight;
    String taxiNum;
    int pairMajor;
    int pairMinor;

    /**
     * bytesToHex method
     * Found on the internet
     * http://stackoverflow.com/a/9855338
     */
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public Beacon(){
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getTaxiNum() {
        return taxiNum;
    }

    public void setTaxiNum(String taxiNum) {
        this.taxiNum = taxiNum;
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

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", getId());
            json.put("uuid", getUuid());
            json.put("major", getMajor());
            json.put("minor", getMinor());
            json.put("weight", getWeight());
            json.put("taxiNum", getTaxiNum());
            json.put("pair_major", getPairMajor());
            json.put("pair_minor", getPairMinor());
        }catch(JSONException e){
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     *
     * @param scanResult
     * @return Beacon (Major, Minor, UUID )
     */
    public static Beacon getBeaconInfo(ScanBLEResult scanResult){
        Beacon beacon = new Beacon();

        byte[] scanRecord = scanResult.getScanRecord().getBytes();

        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            //Convert to hex String
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            //Here is your UUID
            String uuid = hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32);

            //Here is your Major value
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            //Here is your Minor value
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

            beacon.setUuid(uuid);
            beacon.setMajor(major);
            beacon.setMinor(minor);

            return beacon;
        }else{
            return null;
        }
    }
    /**
     *
     * @param scanResult
     * @return Beacon (Major, Minor, UUID )
     */
    public static Beacon getBeaconInfo(ScanResult scanResult){
        Beacon beacon = new Beacon();

        byte[] scanRecord = scanResult.getScanRecord().getBytes();

        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            //Convert to hex String
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            //Here is your UUID
            String uuid = hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32);

            //Here is your Major value
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            //Here is your Minor value
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

            beacon.setUuid(uuid);
            beacon.setMajor(major);
            beacon.setMinor(minor);

            return beacon;
        }else{
            return null;
        }
    }
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
