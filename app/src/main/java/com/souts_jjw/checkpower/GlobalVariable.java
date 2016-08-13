package com.souts_jjw.checkpower;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class GlobalVariable extends Application {

    private static Context context;

    private List<String> phoneList = new ArrayList<String>();
    private boolean isCheckWifi = false;
    private boolean isCheckChanging = false;
    private long CheckStatusWaitingTime = 10000;
    private long CallPhoneWaitingTime = 40000;
    private LogDAO logDAO;

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalVariable.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return GlobalVariable.context;
    }

    // 取得撥打電話名單
    public List<String> getPhoneList() {
        return phoneList;
    }

    // 設定撥打電話名單
    public void setPhoneList(List<String> phoneList) {
        this.phoneList = phoneList;
    }

    // 清除撥打電話名單
    public void clearPhoneList() {
        phoneList.clear();
    }

    // 增加號碼至撥打電話名單中
    public void AddPhone(String phone) {
        phoneList.add(phone);
    }

    // 是否檢查Wifi
    public boolean isCheckWifi() {
        return isCheckWifi;
    }

    // 設定是否檢查Wifi
    public void setCheckWifi(boolean checkWifi) {
        isCheckWifi = checkWifi;
    }

    // 是否檢查充電狀態
    public boolean isCheckChanging() {
        return isCheckChanging;
    }

    // 設定是否檢查充電狀態
    public void setCheckChanging(boolean checkChanging) {
        isCheckChanging = checkChanging;
    }

    // 取得檢查間隔時間
    public long getCheckStatusWaitingTime() {
        return CheckStatusWaitingTime;
    }

    // 設定檢查間隔時間
    public void setCheckStatusWaitingTime(long checkStatusWaitingTime) {
        CheckStatusWaitingTime = checkStatusWaitingTime;
    }

    public long getCallPhoneWaitingTime() {
        return CallPhoneWaitingTime;
    }

    public void setCallPhoneWaitingTime(long callPhoneWaitingTime) {
        CallPhoneWaitingTime = callPhoneWaitingTime;
    }

    private void initDAO() {
        if (logDAO == null) {
            logDAO = new LogDAO(context);
        }
    }

    public LogItem insertLog(String content) {
        initDAO();

        LogItem item = new LogItem(content);

        item = logDAO.insert(item);

        return item;
    }

    public List<LogItem> getAllLog() {
        initDAO();

        return logDAO.getAll();
    }

    public boolean deleteAllLog() {
        initDAO();

        return logDAO.deleteAll();
    }

    public List<String> getLogsGroup() {
        initDAO();

        return logDAO.getGroup();
    }

    public List<LogItem> getLogsByDate(String date) {
        return getLogsByDate(date, false);
    }

    public List<LogItem> getLogsByDate(String date, boolean isDesc) {
        initDAO();

        return logDAO.getLogsByDate(date, isDesc);
    }

}
