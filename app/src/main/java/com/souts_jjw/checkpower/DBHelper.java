package com.souts_jjw.checkpower;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // 資料庫名稱
    public static final String DATABASE_NAME = "logdata.db";
    // 資料庫版本
    public static final int VERSION = 1;
    //
    private static SQLiteDatabase database;

    // 建構子，不需要修改
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new DBHelper(context, DATABASE_NAME, null, VERSION).getWritableDatabase();
        }

        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 建立應用程式需要的表格
        System.out.println("建立表格");
        sqLiteDatabase.execSQL(LogDAO.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // 刪除原有表格
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LogDAO.TABLE_NAME);
        // 呼叫onCreate建立新版的表格
        onCreate(sqLiteDatabase);
    }
}
