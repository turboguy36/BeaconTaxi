package kr.sysgen.taxi.service.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import kr.sysgen.taxi.data.TaxiInfo;

/**
 * Created by leehg on 2016-09-19.
 */
public class TaxiDBOpenHelper extends SQLiteOpenHelper{
    private final String TAG = TaxiDBOpenHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "beacons.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private Context mContext;

    public TaxiDBOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        mDB.execSQL("DROP TABLE IF EXISTS " + Databases.TaxiTable._TABLE);
        createTaxiTable();
    }
    public void createTaxiTable() {
        mDB.execSQL(Databases.TaxiTable._CREATE);
    }

    public long insertData(String tableName, TaxiInfo taxiInfo) throws SQLiteException{
        ContentValues values = new ContentValues();
        values.put(Databases.TaxiTable.TAXI_NUM, taxiInfo.getTaxiNumber());
        values.put(Databases.TaxiTable.UUID, taxiInfo.getUuid());
        values.put(Databases.TaxiTable.MAJOR, taxiInfo.getMajorNumber());
        values.put(Databases.TaxiTable.MINOR, taxiInfo.getMinorNumber());
        return mDB.insert(tableName, null, values);
    }

    public void showAllData(String tableName) throws SQLiteException {
        String query = "SELECT * FROM " +tableName;
        Cursor cursor = mDB.rawQuery(query, new String[]{});
        if(cursor != null)cursor.moveToFirst();
        while(cursor.moveToNext()){
            Log.i(TAG, ">>"+cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(4))));
        }
    }
}
