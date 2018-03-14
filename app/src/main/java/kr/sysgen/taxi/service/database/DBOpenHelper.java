package kr.sysgen.taxi.service.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import kr.sysgen.taxi.data.Beacon;
import kr.sysgen.taxi.data.TaxiInfo;

/**
 * Created by leehg on 2016-09-07.
 */
public class DBOpenHelper extends SQLiteOpenHelper{
    private final String TAG = DBOpenHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "beacons.db";

    public static SQLiteDatabase mDB;
    private Context mContext;

    public DBOpenHelper(Context context, int database_version) {
        super(context, DATABASE_NAME, null, database_version);
        this.mContext = context;
        mDB = getWritableDatabase();
    }

    /*public void createBeaconTable(){
        mDB.execSQL(Databases.BeaconTable._CREATE);
    }*/
    public void createTaxiTable() {
        mDB.execSQL(Databases.TaxiTable._CREATE);
    }
    /**
     *
     * @param tableName
     * @param uuid
     * @param major
     * @param minor
     * @return
     */
    public long insertColumn(String tableName, String uuid, int major, int minor, int weight) throws SQLiteException{
        ContentValues values = new ContentValues();
        values.put(Databases.TaxiTable.UUID, uuid);
        values.put(Databases.TaxiTable.MAJOR, major);
        values.put(Databases.TaxiTable.MINOR, minor);
        values.put(Databases.TaxiTable.WEIGHT, weight);
        return mDB.insert(tableName, null, values);
    }

    /**
     *
     * @param tableName
     * @param major
     * @param minor
     * @return
     */
    public boolean checkExistColumn(String tableName, int major, int minor) throws SQLiteException {
        boolean result = false;

        String query = "SELECT * FROM "+ tableName + " WHERE taxi_major =? and taxi_minor=?";
        Cursor cursor = mDB.rawQuery(query, new String[]{String.valueOf(major), String.valueOf(minor)});

        if(cursor.getCount() > 0){
            result = true;
        }else {
            result = false;
        }
        cursor.close();
        return result;
    }
    public boolean isOnTaxi(String tableName, int major, int minor) throws SQLiteException, CursorIndexOutOfBoundsException{
        boolean result = false;
        String query = "SELECT weight FROM " + tableName + " WHERE taxi_major=? and taxi_minor=?";
        Cursor cursor = mDB.rawQuery(query, new String[]{String.valueOf(major), String.valueOf(minor)});

        return result;
    }
    public long getId(String tableName, int major, int minor) throws SQLiteException, CursorIndexOutOfBoundsException{
        String query = "SELECT _id FROM " + tableName + " where taxi_major=? and taxi_minor=?";
        Cursor cursor = mDB.rawQuery(query, new String[]{String.valueOf(major), String.valueOf(minor)});
        if(cursor != null)cursor.moveToFirst();

        return cursor.getLong(cursor.getColumnIndex(cursor.getColumnName(0)));
    }

    public int getWeight(String tableName, long id) throws CursorIndexOutOfBoundsException{
        final String getWeightQuery = "Select weight From " + tableName + " Where _id=?";
        Cursor cursor = mDB.rawQuery(getWeightQuery, new String[]{String.valueOf(id)});
        if(cursor != null)cursor.moveToFirst();

        final int result = cursor.getInt(0);

        cursor.close();

        return result;
    }
    public Beacon getBeacon(String tableName, int major, int minor) throws CursorIndexOutOfBoundsException{
//        int result = -1;
        String query = "Select * From " + tableName + " Where taxi_major=? and taxi_minor=?";
        Cursor cursor = mDB.rawQuery(query, new String[]{String.valueOf(major), String.valueOf(minor)});
        if(cursor != null)cursor.moveToFirst();
        if(cursor == null || cursor.getCount()==0){
            Log.i(TAG, "query null");
            return null;
        }
        Beacon beacon = new Beacon();
        beacon.setId(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(0))));
        beacon.setUuid(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1))));
        beacon.setTaxiNum(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2))));
        beacon.setMajor(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(3))));
        beacon.setMinor(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(4))));
        beacon.setPairMajor(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(5))));
        beacon.setPairMinor(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(6))));
        beacon.setWeight(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(7))));

        return beacon;
    }
    public synchronized boolean updateWeight(String tableName, long id, int weight) throws SQLiteException, CursorIndexOutOfBoundsException{
        final String whereClause = "_id="+id;
        ContentValues values = new ContentValues();
        values.put(Databases.TaxiTable.WEIGHT, weight);

        return mDB.update(tableName, values, whereClause, null) > 0;
    }

    public boolean deleteField(String tableName, long id){
        final String query = "_id = ?";;
        int affectedCols = mDB.delete(tableName, query, new String[]{String.valueOf(id)});
        if(affectedCols > 0)return true;
        else return false;
    }

    public Beacon getBeacon(String tableName, long id) throws CursorIndexOutOfBoundsException{
        if(!(id > 0))return null;

        final String query = "SELECT * FROM " + tableName + " WHERE _ID = " + id;
        Cursor cursor = mDB.rawQuery(query, new String[]{});
        if(cursor != null)cursor.moveToFirst();

        Beacon beacon = new Beacon();
        beacon.setId(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(0))));
        beacon.setUuid(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1))));
        beacon.setTaxiNum(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2))));
        beacon.setMajor(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(3))));
        beacon.setMinor(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(4))));
        beacon.setPairMajor(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(5))));
        beacon.setPairMinor(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(6))));
        beacon.setWeight(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(7))));

        return beacon;
    }
    public String showAllData(String tableName) throws SQLiteException {
        StringBuilder result = new StringBuilder();

        String query = "SELECT * FROM " +tableName;
        Cursor cursor = mDB.rawQuery(query, new String[]{});
        if(cursor != null)cursor.moveToFirst();
        do {
             final String fieldInfo = cursor.getColumnName(0) + " : "+cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(0)))
                    + " / " + cursor.getColumnName(1) + " : " + cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1)))
                    + " / " + cursor.getColumnName(2) + " : "+ cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2)))
                    + " / " + cursor.getColumnName(3) + " : "+ cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(3)))
                    + " / " + cursor.getColumnName(4) + " : "+ cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(4)))
                    + " / " + cursor.getColumnName(5) + " : "+ cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(5)))
                     + "\n";
            result.append(fieldInfo);
        } while (cursor.moveToNext());

        return result.toString();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int before, int current) {
        Log.i(TAG, "version : " + before + " -> " + current);
        mDB.execSQL("DROP TABLE IF EXISTS " + Databases.TaxiTable._TABLE);
//        createBeaconTable();
        createTaxiTable();
    }
    public long insertData(String tableName, TaxiInfo taxiInfo) throws SQLiteException{
        ContentValues values = new ContentValues();

        values.put(Databases.TaxiTable.TAXI_NUM, taxiInfo.getTaxiNumber());
        values.put(Databases.TaxiTable.UUID, taxiInfo.getUuid());
        values.put(Databases.TaxiTable.MAJOR, taxiInfo.getMajorNumber());
        values.put(Databases.TaxiTable.MINOR, taxiInfo.getMinorNumber());
        values.put(Databases.TaxiTable.PAIR_MAJOR, taxiInfo.getPairMajor());
        values.put(Databases.TaxiTable.PAIR_MINOR, taxiInfo.getPairMinor());
        values.put(Databases.TaxiTable.WEIGHT, 0);
        return mDB.insert(tableName, null, values);
    }
}
