package com.souts_jjw.checkpower;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.Date;

public class PowerCutService extends Service {

    private GlobalVariable globalVariable = null;
    private boolean serviceRunning = false;
    private boolean serviceStatus = false;

    CheckThread task;
    Thread t;

    @Override
    public IBinder onBind(Intent intent) {
        return new PowerCutBinder();
    }

    public class PowerCutBinder extends Binder {
        PowerCutService getService() {
            return PowerCutService.this;
        }

        public void PauseService() {
            globalVariable.insertLog("暫停電力監控");

            task.stopThread();
            t.interrupt();
        }

        public void ResumeService() {
            globalVariable.insertLog("重新開始電力監控");
            AppUnity.Wait(10 * 1000);

            task = new CheckThread();
            t = new Thread(task);
            t.start();
        }
    }

    public class CheckThread implements Runnable {
        private boolean isRunning = true;

        @Override
        public void run() {
            boolean isChanged = false;
            try {
                while (isRunning) {

                    globalVariable.insertLog("等待間隔時間");
                    AppUnity.Wait(globalVariable.getCheckStatusWaitingTime() * 1000);

                    if (!isRunning) {
                        continue;
                    }

                    globalVariable.insertLog("檢查網路連線是否還是wifi");
                    // 檢查網路連線是否還是wifi
                    if (!AppUnity.isWifi(getApplicationContext())) {
                        isChanged = true;
                    }

                    globalVariable.insertLog("檢查是否還在充電");
                    // 檢查是否充電
                    if (!AppUnity.isCharging(getApplicationContext())) {
                        isChanged = true;
                    }

                    globalVariable.insertLog("檢查狀態是有改變並且執行相關程序");
                    if (isChanged && statusCallback != null) {
                        statusCallback.statusChanged(isChanged);
                    }
                }

            } catch (Exception e) {
                globalVariable.insertLog(e.getMessage());
            }
        }

        public void stopThread() {
            this.isRunning = false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        serviceRunning = true;
        serviceStatus = true;

        globalVariable = (GlobalVariable)getApplicationContext();

        task = new CheckThread();
        t = new Thread(task);
        t.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        task.stopThread();
        t.interrupt();

        super.onDestroy();
    }

    StatusCallback statusCallback = null;

    public StatusCallback getStatusCallback() {
        return statusCallback;
    }

    public void setStatusCallback(StatusCallback statusCallback) {
        this.statusCallback = statusCallback;
    }

    public interface StatusCallback {
        void statusChanged(boolean isChanged);
    }

}
