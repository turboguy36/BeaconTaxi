package kr.sysgen.taxi.service.database;

import android.provider.BaseColumns;

/**
 * Created by leehg on 2016-09-07.
 */
public class Databases {
    public static final int DB_VERSION = 1;

    public static final class TaxiTable implements BaseColumns {
        public static final String _TABLE = "taxis";

        public static final String UUID = "taxi_uuid";
        public static final String MAJOR = "taxi_major";
        public static final String MINOR = "taxi_minor";
        public static final String TAXI_NUM = "taxi_num";
        public static final String WEIGHT = "weight";
        public static final String PAIR_MAJOR = "pair_major";
        public static final String PAIR_MINOR = "pair_minor";

        public static final String _CREATE =
                "create table " + _TABLE + "("
                        + _ID + " integer primary key autoincrement, "
                        + UUID + " text not null, "
                        + TAXI_NUM + " text , "
                        + MAJOR + " integer not null, "
                        + MINOR + " integer not null, "
                        + PAIR_MAJOR + " integer not null, "
                        + PAIR_MINOR + " integer not null, "
                        + WEIGHT + " integer);";
    }
}
