package com.souts_jjw.checkpower;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PhoneUnity {

    public static String TAG = PhoneUnity.class.getSimpleName();

    // 掛斷電話
    public static void endCall(Context context) {
        try {
            Object telephonyObject = getTelephonyObject(context);
            if (null != telephonyObject) {
                Class telephonyClass = telephonyObject.getClass();

                Method endCallMethod = telephonyClass.getMethod("endCall");
                endCallMethod.setAccessible(true);

                endCallMethod.invoke(telephonyObject);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static Object getTelephonyObject(Context context) {
        Object telephonyObject = null;
        try {
            // 初始化iTelephony
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            // Will be used to invoke hidden methods with reflection
            // Get the current object implementing ITelephony interface
            Class telManager = telephonyManager.getClass();
            Method getITelephony = telManager.getDeclaredMethod("getITelephony");
            getITelephony.setAccessible(true);
            telephonyObject = getITelephony.invoke(telephonyManager);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return telephonyObject;
    }

    // 通過反射呼叫的方法，接聽電話，該方法只在android 2.3之前的系統上有效
    private static void answerRingingCallWithReflect(Context context) {
        try {
            Object telephonyObject = getTelephonyObject(context);
            if (null != telephonyObject) {
                Class telephonyClass = telephonyObject.getClass();
                Method endCallMethod = telephonyClass.getMethod("answerRingingCall");
                endCallMethod.setAccessible(true);

                endCallMethod.invoke(telephonyObject);
                // ITelephony iTelephony = (ITelephony) telephonyObject;
                // iTelephony.answerRingingCall();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    // 偽造一個有線耳機插入，並按接聽鍵的廣播，讓系統開始接聽電話
    private static void answerRingingCallWithBroadcast(Context context){
        AudioManager localAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //判断是否插上了耳机
        boolean isWiredHeadsetOn = localAudioManager.isWiredHeadsetOn();
        if (!isWiredHeadsetOn) {
            Intent headsetPluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
            headsetPluggedIntent.putExtra("state", 1);
            headsetPluggedIntent.putExtra("microphone", 0);
            headsetPluggedIntent.putExtra("name", "");
            context.sendBroadcast(headsetPluggedIntent);

            Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT,keyEvent);
            context.sendOrderedBroadcast(meidaButtonIntent, null);

            Intent headsetUnpluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
            headsetUnpluggedIntent.putExtra("state", 0);
            headsetUnpluggedIntent.putExtra("microphone", 0);
            headsetUnpluggedIntent.putExtra("name", "");
            context.sendBroadcast(headsetUnpluggedIntent);

        } else {
            Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT,keyEvent);
            context.sendOrderedBroadcast(meidaButtonIntent, null);
        }
    }

    // 接聽電話
    public static void answerRingingCall(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {	//2.3或2.3以上系统
            answerRingingCallWithBroadcast(context);
        } else {
            answerRingingCallWithReflect(context);
        }
    }

    // 撥打電話
    public static void callPhone(Context context, String phoneNumber) {
        if(!TextUtils.isEmpty(phoneNumber)){
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ phoneNumber));
                // http://chroya.iteye.com/blog/724804
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(callIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 撥打電話，只出現號碼畫面，但不撥打
    public static void dialPhone(Context context, String phoneNumber){
        if(!TextUtils.isEmpty(phoneNumber)){
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ phoneNumber));
                context.startActivity(callIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
