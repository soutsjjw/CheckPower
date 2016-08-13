package com.souts_jjw.checkpower;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongToIntFunction;

public class LogDAO {
    // 表格名稱
    public static final String TABLE_NAME = "Logs";
    // 表格編號欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其他欄位名稱
    public static final String DATE_COLUMN = "date";
    public static final String DATETIME_COLUMN = "datetime";
    public static final String CONTENT_COLUMN = "content";

    // 建立表格SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DATE_COLUMN + " TEXT NOT NULL, " +
                    DATETIME_COLUMN + " INTEGER NOT NULL, " +
                    CONTENT_COLUMN + " TEXT NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子
    public LogDAO(Context context) {
        this.db = DBHelper.getDatabase(context);
    }

    // 關閉資料庫
    public void close() {
        db.close();
    }

    // 新增
    public LogItem insert(LogItem item) {
        ContentValues cv = new ContentValues();

        cv.put(DATE_COLUMN, item.getDate());
        cv.put(DATETIME_COLUMN, item.getDatetime());
        cv.put(CONTENT_COLUMN, item.getContent());

        long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        item.setId(id);

        // 回傳結果
        return item;
    }

    // 更新，應該用不到
    public boolean update(LogItem item) {
        ContentValues cv = new ContentValues();

        cv.put(DATE_COLUMN, item.getLocaleDate());
        cv.put(DATETIME_COLUMN, item.getDatetime());
        cv.put(CONTENT_COLUMN, item.getContent());

        String where = KEY_ID + "=" + item.getId();

        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除紀錄檔，應該用不到
    public boolean delete(long id) {
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    // 刪除所有紀錄檔
    public boolean deleteAll() {
        try {
            db.delete(TABLE_NAME, "1=1", null);

            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<LogItem> getAll() {
        List<LogItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        while(cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public List<LogItem> getLogsByDate(String date) {
        return getLogsByDate(date, false);
    }

    public List<LogItem> getLogsByDate(String date, boolean isDesc) {
        String where = DATE_COLUMN + "= '" + date + "'";
        String orderby = DATETIME_COLUMN + (isDesc ? " DESC" : "");

        List<LogItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, orderby);

        while(cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public List<LogItem> getAllDesc() {
        List<LogItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, DATETIME_COLUMN + " DESC");

        while(cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public List<String> getGroup() {
        List<String> result = new ArrayList<>();
        Cursor cursor = db.query(true, TABLE_NAME, new String[] { DATE_COLUMN }, null, null, DATE_COLUMN, null, DATE_COLUMN + " desc", null);

        while(cursor.moveToNext()) {
            result.add(cursor.getString(0));
        }

        cursor.close();
        return result;
    }

    public LogItem get(long id) {
        LogItem item = null;

        String where = KEY_ID + "=" + id;

        Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);

        if (result.moveToFirst()) {
            item = getRecord(result);
        }

        result.close();
        return item;
    }

    public LogItem getRecord(Cursor cursor) {
        LogItem result = new LogItem();

        result.setId(cursor.getLong(0));
        result.setDate(cursor.getString(1));
        result.setDatetime(cursor.getLong(2));
        result.setContent(cursor.getString(3));

        return result;
    }

    public int getCount() {
        int result = 0;

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }

}
