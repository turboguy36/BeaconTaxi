package kr.sysgen.taxi.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leehg on 2016-05-20.
 */
public class ParentInfo {
    private int pId;
    private String name;
    private String phoneNumber;
    private int photo;

    public static final String NAME_TAG = "NAME";
    public static final String PHONE_TAG = "PHONE";
    public static final String PHOTO_TAG = "PHOTO";

    public ParentInfo(){}
    public ParentInfo(String name, String phone){
        this.name = name;
        this.phoneNumber = phone;
    }
    public ParentInfo(String name, String phone, int photo){
        this.name = name;
        this.phoneNumber = phone;
        this.photo = photo;
    }
    public int getpId() {
        return pId;
    }

    public void setpId(int pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put(NAME_TAG, name);
            json.put(PHONE_TAG, phoneNumber);
            json.put(PHOTO_TAG, photo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }
}
