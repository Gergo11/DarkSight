package com.gergo.darksight.Logic;

import android.provider.BaseColumns;

public final class IpContract {

    public IpContract() { }

    public IpContract(int id, String ip) {
        this.id = id;
        this.ip = ip;
    }

    public  static final String TABLE_NAME = "IP_STORE";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_IP = "ip";

        private int id;
        private String ip;

    public static final String CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_IP + " TEXT)";


    public static final String DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
