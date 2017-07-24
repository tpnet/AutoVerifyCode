package com.tpnet.tpautoverifycode;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.annotation.Nullable;

/**
 * 短信验证码读取服务
 * Created by litp on 2017/6/23.
 */
public class ReadSmsService extends Service {

    private static final String TAG = ReadSmsService.class.getSimpleName();


    // 接收到短信时的action
    private static final String SMS_RECEIVED_ACTION = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;

    //API>=19,可直接使用Telephony.Sms.CONTENT_URI
    private static final String SMS_URI = "content://sms";

    static final String[] PROJECTION = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
    };

  
    public static final String EXTRAS_CONFIG = "CONFIG";


    //广播方式接收到验证码
    public static final int RECEIVER_SMS_CODE_MSG = 0x123;

    //从本地数据库获取到验证码
    public static final int OBSERVER_SMS_CODE_MSG = 0x456;

    //整个短信消息内容
    public static final int RECEIVER_SMS_MSG = 0x789;
    
    //发送者
    public static final int RECEIVER_SENDER_MSG = 0X899;

 
    private AutoVerifyCodeConfig mConfig;

    long lastTimeofCall = 0L;    //最后一次数据库回调的时间

    /**
     * 内容观察者，观察数据库短信的变化
     */
    private ContentObserver mReadSmsObserver;

    /**
     * 短信广播接收者
     */
    private BroadcastReceiver mSmsReceiver;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

     
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                mConfig = new AutoVerifyCodeConfig.Builder().build();
            } else {
                mConfig = bundle.getParcelable(EXTRAS_CONFIG);
            }
        }
        HandlerMessage mHandlerMessage = new HandlerMessage(mConfig);
        mReadSmsObserver =  new SmsObserver(this, mHandlerMessage);
        mSmsReceiver = new SmsReceiver(mHandlerMessage);
        
        register();
        
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
    }

 
    /**
     * 注册消息监听
     */
    private void register() {
        registerReceiver();
        registerObserver();
    }

    /**
     * 注册广播接收者
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(SMS_RECEIVED_ACTION);
        //filter.addAction(SMS_RECEIVED_ACTION);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mSmsReceiver, filter);
    }

    /**
     * 注册内容观察者
     */
    private void registerObserver() {
        getContentResolver().registerContentObserver(Uri.parse(SMS_URI), true, mReadSmsObserver);
    }

    /**
     * 注销广播接收者，内容观察者
     */
    private void unRegister() {
        unRegisterReceiver();
        unRegisterObserver();
    }

    /**
     * 注销广播接收者
     */
    private void unRegisterReceiver() {
        if (mSmsReceiver == null) return;
        unregisterReceiver(mSmsReceiver);
        mSmsReceiver = null;
    }

    /**
     * 注销内容观察者
     */
    private void unRegisterObserver() {
        if (mReadSmsObserver == null) return;

        getContentResolver().unregisterContentObserver(mReadSmsObserver);
        mReadSmsObserver = null;
    }

 

}
