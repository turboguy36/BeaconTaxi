package kr.sysgen.taxi.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leehg on 2016-05-20.
 */
public class ContactInfo {
//    private int cId;
    private String contactId;
    private String name;
    private String phone;
    private String photo;
    private String phone_01;

    public static final String CONTACTID_TAG = "CONTACT_ID";
//    public static final String CID_TAG = "CID";
    public static final String NAME_TAG = "NAME";
    public static final String PHONE_TAG = "PHONE";
    public static final String PHOTO_TAG = "PHOTO";

    public ContactInfo(){};

    public ContactInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public ContactInfo(String name, String phone, String photo){
        this.photo = photo;
        this.name = name;
        this.phone = phone;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }
//    public int getcId() {
//        return cId;
//    }
//
//    public void setcId(int cId) {
//        this.cId = cId;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
//            json.put(CID_TAG, cId);
            json.put(CONTACTID_TAG, getContactId());
            json.put(NAME_TAG, name);
            json.put(PHONE_TAG, phone);
            json.put(PHOTO_TAG, photo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
