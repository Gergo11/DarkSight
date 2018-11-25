package com.gergo.darksight.Logic;

import android.provider.BaseColumns;

public final class IpContract {

    private IpContract() { }

    public static class IpEntry implements BaseColumns{
        public  static final String TABLE_NAME = "IP_STORE";
        public static final String COLUMN_NAME_IP = "STORE";
    }
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + IpEntry.TABLE_NAME + " (" +
                    IpEntry._ID + " INTEGER PRIMARY KEY," +
                    IpEntry.COLUMN_NAME_IP + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + IpEntry.TABLE_NAME;
}
