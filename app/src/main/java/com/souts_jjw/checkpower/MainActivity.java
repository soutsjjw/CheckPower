package com.souts_jjw.checkpower;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Switch swCheckWifi;
    Switch swCheckCharging;
    SeekBar sbCheckSecond;
    EditText etPhoneNumber;
    TextView tvShowCheckSecond;
    Button btnStartService;
    Button btnStopService;
    Button btnAddPhoneNumber;
    ListView listView;

    PhoneAdapter adapter = null;

    private Intent intent = null;
    PowerCutServiceConn powerCutServiceConn;
    PowerCutService.PowerCutBinder binder = null;

    GlobalVariable globalVariable = null;

    TelephonyManager telephonyMgr = null;
    // 撥打電話用
    TeleListener teleListener = null;
    // 監聽來電用
    IncomingListener incomingListener = null;

    private MenuItem view_log, delete_log;

    private void initView() {
        etPhoneNumber = (EditText)findViewById(R.id.etPhoneNumber);
        swCheckWifi = (Switch)findViewById(R.id.swCheckWifi);
        swCheckCharging = (Switch)findViewById(R.id.swCheckCharging);

        btnStartService = (Button)findViewById(R.id.btnStartService);
        btnStartService.setOnClickListener(startServiceClickListener);

        btnStopService = (Button)findViewById(R.id.btnStopService);
        btnStopService.setOnClickListener(stopServiceClickListener);

        btnAddPhoneNumber = (Button)findViewById(R.id.btnAddPhoneNumber);
        btnAddPhoneNumber.setOnClickListener(adddServiceClickListener);

        tvShowCheckSecond = (TextView)findViewById(R.id.tvShowCheckSecond);
        sbCheckSecond = (SeekBar)findViewById(R.id.sbCheckSecond);
        sbCheckSecond.setOnSeekBarChangeListener(changeCheckStatusWaitingTime);

        listView = (ListView)findViewById(R.id.list);
    }

    private void initData() {
        globalVariable = (GlobalVariable)getApplicationContext();

        SharedPreferences settings = getSharedPreferences(Constant.SHAREDPREFERENCES, 0);
        String phoneNumbers = settings.getString("phoneNumbers", "");

        List<String> list = new ArrayList<String>();

        for (String p : phoneNumbers.split(",")) {
            globalVariable.AddPhone(p);
            list.add(p);
        }

        adapter = new PhoneAdapter(getApplicationContext(), list);
        listView.setAdapter(adapter);

        boolean isCheckWifi = settings.getBoolean("checkWifi", false);
        swCheckWifi.setChecked(isCheckWifi);
        globalVariable.setCheckWifi(isCheckWifi);

        boolean isCheckCharging = settings.getBoolean("checkCharging", false);
        swCheckCharging.setChecked(isCheckCharging);
        globalVariable.setCheckChanging(isCheckCharging);

        int checkStatusWaitingTime = settings.getInt("checkStatusWaitingTime", 10);
        sbCheckSecond.setProgress(checkStatusWaitingTime - 10);
        tvShowCheckSecond.setText(String.valueOf(checkStatusWaitingTime));
    }

    private void setData() {
        SharedPreferences settings = getSharedPreferences(Constant.SHAREDPREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();

        String phoneNumbers = "";
        for(String p: adapter.getList()) {
            phoneNumbers += p + ",";
        }
        editor.putString("phoneNumbers", phoneNumbers);

        editor.putBoolean("checkWifi", swCheckWifi.isChecked());
        editor.putBoolean("checkCharging", swCheckCharging.isChecked());
        editor.putInt("checkStatusWaitingTime", sbCheckSecond.getProgress() + 10);

        globalVariable.setPhoneList(adapter.getList());
        globalVariable.setCheckWifi(swCheckWifi.isChecked());
        globalVariable.setCheckChanging(swCheckCharging.isChecked());

        globalVariable.setCheckStatusWaitingTime(sbCheckSecond.getProgress() + 10);

        editor.apply();
    }

    private void setEnabled(boolean enabled) {
        if (enabled) {
            btnStartService.setVisibility(View.VISIBLE);
            btnStopService.setVisibility(View.GONE);

            btnAddPhoneNumber.setEnabled(true);
            btnAddPhoneNumber.setBackground(getResources().getDrawable(R.drawable.btn_blue));

            swCheckWifi.setEnabled(true);
            swCheckCharging.setEnabled(true);

            etPhoneNumber.setEnabled(true);
            listView.setEnabled(true);

            sbCheckSecond.setEnabled(true);

            view_log.setEnabled(true);
            delete_log.setEnabled(true);
        } else {
            btnStartService.setVisibility(View.GONE);
            btnStopService.setVisibility(View.VISIBLE);

            btnAddPhoneNumber.setEnabled(false);
            btnAddPhoneNumber.setBackground(getResources().getDrawable(R.drawable.btn_black));

            swCheckWifi.setEnabled(false);
            swCheckCharging.setEnabled(false);

            etPhoneNumber.setEnabled(false);
            listView.setEnabled(false);

            sbCheckSecond.setEnabled(false);

            view_log.setEnabled(false);
            delete_log.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();

        intent = new Intent(MainActivity.this, PowerCutService.class);
        powerCutServiceConn = new PowerCutServiceConn();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (btnStartService.getVisibility() == View.VISIBLE) {
                ConfirmExit();
            } else {
                Toast.makeText(MainActivity.this, "程序執行中，請先停止監聽再離開！", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        view_log = menu.findItem(R.id.view_log);
        delete_log = menu.findItem(R.id.delete_log);

        return true;
    }

    public void ConfirmExit() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.Ask)
                .setMessage(R.string.IsExit)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private Button.OnClickListener startServiceClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.Ask)
                    .setMessage(R.string.IsStartService)
                    .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean isPass = true;
                            // 檢查是否已經設定電話
                            if (adapter.getList().size() == 0) {
                                isPass = false;
                            }

                            // 檢查兩個Switch其中之一是否開啟
                            if (!swCheckWifi.isChecked() && !swCheckCharging.isChecked()) {
                                isPass = false;
                            }

                            // 檢查網路連線是否已經是wifi
                            if (swCheckWifi.isChecked() && !AppUnity.isWifiEnabled(getApplicationContext())) {
                                isPass = false;
                            }

                            // 檢查是否已經是充電狀態
                            if (swCheckCharging.isChecked() && !AppUnity.isCharging(getApplicationContext())) {
                                isPass = false;
                            }

                            if (isPass) {
                                // 儲存參數
                                setData();

                                setEnabled(false);

                                Toast.makeText(getApplicationContext(), "開始檢查…", Toast.LENGTH_SHORT).show();

                                if (telephonyMgr == null) {
                                    telephonyMgr = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                                }

                                incomingListener = new IncomingListener(MainActivity.this, globalVariable.getPhoneList());
                                telephonyMgr.listen(incomingListener, PhoneStateListener.LISTEN_CALL_STATE);

                                createNotification(true);

                                globalVariable.insertLog("啟動電力監控");

                                bindService(intent, powerCutServiceConn, Context.BIND_AUTO_CREATE);
                            } else {
                                Toast.makeText(MainActivity.this, "請確定已增加電話號碼、手機已連接至Wifi或是處於充電狀態！", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }
    };

    private Button.OnClickListener stopServiceClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            // 先停止服務
            binder.PauseService();

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.Ask)
                    .setMessage(R.string.IsStopService)
                    .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopService();
                            createNotification(false);

                            globalVariable.insertLog("停止電力監控");
                        }
                    })
                    .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            binder.ResumeService();
                        }
                    })
                    .show();
        }
    };

    private void stopService() {
        setEnabled(true);

        Toast.makeText(getApplicationContext(), "停止檢查…", Toast.LENGTH_SHORT).show();

        if (telephonyMgr != null) {

            if (teleListener != null) {
                telephonyMgr.listen(teleListener, PhoneStateListener.LISTEN_NONE);
                teleListener = null;
            }

            if (incomingListener != null) {
                telephonyMgr.listen(incomingListener, PhoneStateListener.LISTEN_NONE);
                incomingListener = null;
            }
        }

        try {
            //停止服務
            unbindService(powerCutServiceConn);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Button.OnClickListener adddServiceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!etPhoneNumber.getText().toString().equals("")) {
                adapter.add(etPhoneNumber.getText().toString());

                etPhoneNumber.setText("");
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener changeCheckStatusWaitingTime = new SeekBar.OnSeekBarChangeListener() {
        int progress = 10;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
            progress = progresValue + 10;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            tvShowCheckSecond.setText(String.valueOf(progress));
        }
    };

    class PowerCutServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (PowerCutService.PowerCutBinder)iBinder;
            binder.getService().setStatusCallback(new PowerCutService.StatusCallback() {
                @Override
                public void statusChanged(boolean isChanged) {
                    if (isChanged) {
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(Constant.STATUS_IS_CHANGED, isChanged);
                        msg.setData(bundle);

                        handler.sendMessage(msg);
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean isChanged = msg.getData().getBoolean(Constant.STATUS_IS_CHANGED);

            if (isChanged) {
                binder.PauseService();

                globalVariable.insertLog("狀態改變，將進行電話通知");

                telephonyMgr.listen(incomingListener, PhoneStateListener.LISTEN_NONE);
                incomingListener = null;

                teleListener = new TeleListener(MainActivity.this, globalVariable.getPhoneList());
                telephonyMgr.listen(teleListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    };

    private void stopPhoneStateListener() {
        globalVariable.insertLog("停止撥打電話狀態監聽");

        telephonyMgr.listen(teleListener, PhoneStateListener.LISTEN_NONE);
        teleListener = null;

        globalVariable.insertLog("開始來電電話狀態監聽");

        incomingListener = new IncomingListener(MainActivity.this, globalVariable.getPhoneList());
        telephonyMgr.listen(incomingListener, PhoneStateListener.LISTEN_CALL_STATE);

        AppUnity.Wait(5000);

        binder.ResumeService();
    }

    private void createNotification(boolean isStartService) {
        String num = String.valueOf((int)(Math.random()*10)+1);
        int notifyID = 1;
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = null;

        if (isStartService) {
            int priority = Notification.PRIORITY_MAX;
            notification = new Notification.Builder(getApplicationContext()).setSmallIcon(getNotificationIcon()).setContentTitle(getString(R.string.app_name)).setContentText(getString(R.string.startServiceNofity)).setPriority(priority).build();
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        } else
            notification = new Notification.Builder(getApplicationContext()).setSmallIcon(getNotificationIcon()).setContentTitle(getString(R.string.app_name)).setContentText(getString(R.string.stopServiceNofity)).build();


        notificationManager.notify(notifyID, notification);
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.icon_silhouette : R.mipmap.ic_launcher;
    }

    class TeleListener extends PhoneStateListener {
        private MainActivity activity;
        private List<String> phoneList;
        private Integer currentIndex = 0;
        private String num;

        public TeleListener() {
            num = String.valueOf((int)(Math.random()*10)+1);
        }

        public TeleListener(MainActivity activity, List<String> phoneList) {
            this.activity = activity;
            this.phoneList = phoneList;

            num= String.valueOf((int)(Math.random()*10)+1);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                // 當處於待機狀態中
                case TelephonyManager.CALL_STATE_IDLE:
                    if (currentIndex >= phoneList.size()) {
                        globalVariable.insertLog("名單輪詢完畢，即將結束停止電話狀態監聽");
                        activity.stopPhoneStateListener();
                    } else {
                        AppUnity.Wait(5000);

                        if (!phoneList.get(currentIndex).equals("")) {
                            globalVariable.insertLog("撥打電話" + phoneList.get(currentIndex) + "中-");
                            PhoneUnity.callPhone(MainActivity.this, phoneList.get(currentIndex));
                        }
                    }

                    currentIndex++;

                    break;
                // 當處於正在撥號出去或者是正在通話中
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    globalVariable.insertLog("正在撥號或是通話中");

                    AppUnity.Wait(globalVariable.getCallPhoneWaitingTime());

                    globalVariable.insertLog("即將掛斷電話");
                    PhoneUnity.endCall(MainActivity.this);

                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                default:
                    break;
            }
        }
    }

    class IncomingListener extends PhoneStateListener {
        private MainActivity activity;
        private List<String> phoneList;

        public IncomingListener(MainActivity activity, List<String> phoneList) {
            this.activity = activity;
            this.phoneList = phoneList;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                // 外部撥號，而且還沒有接聽
                case TelephonyManager.CALL_STATE_RINGING:
                    boolean isPass = false;

                    // 檢查來電號碼是否在名單內
                    for(String phoneNumber : phoneList) {
                        if (phoneNumber.equals(incomingNumber)) {
                            isPass = true;
                            break;
                        }
                    }

                    // 來電號碼在名單內
                    if (isPass) {
                        PhoneUnity.endCall(MainActivity.this);
                        // 停止監聽
                        activity.stopService();
                    }

                    break;
                default:
                    break;
            }
        }


    }

    public void clickMenuItem(MenuItem item) {
        int itemId = item.getItemId();

        switch(itemId) {
            case R.id.view_log:
                Intent intent = new Intent(MainActivity.this, LogsActivity.class);

                startActivity(intent);
                break;
            case R.id.delete_log:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.Ask)
                        .setMessage("刪除所有紀錄檔？")
                        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "刪除所有紀錄檔" + (globalVariable.deleteAllLog() ? "成功" : "失敗"), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                break;
        }
    }
}
