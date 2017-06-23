package com.tpnet.tpautoverifycode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import com.tpnet.tpautoverifycode.callback.PermissionCallBack;
import com.tpnet.tpautoverifycode.callback.SmsCallBack;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * 自动填写短信中的验证码
 * Created by litp on 2017/6/23.
 */
public class AutoVerifyCode {

    
    private String TAG =  getClass().getSimpleName();
    
    private Context mContext;
    
    private AutoCodeConfig mConfig;

    private VerifyCodeHandler mHandler;
    
    private SmsCallBack mSmsCallBack;  //短信内容回调
    
    private PermissionCallBack mPermissionCallBack; //权限回调

    private Intent serviceIntent;


    private class VerifyCodeHandler extends Handler {
        
        //软引用
        private Reference<TextView> mTextView;

   
        VerifyCodeHandler(TextView codeView ) {
            super();
            if(codeView == null){
                throw new IllegalArgumentException("target view is null");
            }
            this.mTextView = new SoftReference<>(codeView);
        }
        
        VerifyCodeHandler() {
            if(this.mTextView == null && mSmsCallBack == null){
                throw new IllegalArgumentException("target view and smscallback is null,Set at least one");
            }
        }
      
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TextView mAuthCode = mTextView.get();
            
            
            switch (msg.what) {
                case ReadSmsService.OBSERVER_SMS_CODE_MSG:
                case ReadSmsService.RECEIVER_SMS_CODE_MSG:

                    if (mSmsCallBack != null) {
                        mSmsCallBack.onGetCode((String) msg.obj);
                    }
                    if (mAuthCode != null /*&& mAuthCode.getText().toString().isEmpty()*/) {
                        mAuthCode.setText((String) msg.obj);
                        return;
                    }
                    break;
                case ReadSmsService.RECEIVER_SENDER_MSG:
                    if (mSmsCallBack != null) {
                        mSmsCallBack.onGetSender((String) msg.obj);
                    }

                    break;  
                case ReadSmsService.RECEIVER_SMS_MSG:
                    if (mSmsCallBack != null) {
                        mSmsCallBack.onGetMessage((String) msg.obj);
                    }

                    break;
                case GetPermissionActivity.AUTOCODE_REQUEST_PERMISSION_SUCCESS:
                    //成功申请权限
                    if(mPermissionCallBack != null){
                        mPermissionCallBack.onSuccess();
                    }
                    restart();
                    break;
                case GetPermissionActivity.AUTOCODE_REQUEST_PERMISSION_FAIL:
                    //申请权限失败
                    if(mPermissionCallBack != null){
                        mPermissionCallBack.onFail();
                    }
                    break;
                default:
                    break;
            }
        }


        public void release() {
            mTextView.clear();
            this.mTextView = null;
        }
    }

    private static AutoVerifyCode INSTANCE;

    private AutoVerifyCode() {}

    public static AutoVerifyCode getInstance() {
        if (INSTANCE == null) {
            synchronized(AutoVerifyCode.class){
                INSTANCE = new AutoVerifyCode();
            }
        }
        return INSTANCE;
    }

    public AutoVerifyCode with(Context context) {
        mContext = context;
        return this;
    }

    public AutoVerifyCode config(AutoCodeConfig config) {
        if (mContext == null) {
            throw new NullPointerException("mContext is null.Please call with(Context) first.");
        }
        mConfig = config;
        return this;
    }


    public AutoVerifyCode smsCallback(SmsCallBack callback) {
        this.mSmsCallBack = callback;
        return this;
    }

    public AutoVerifyCode permissionCallback(PermissionCallBack callBack){
        this.mPermissionCallBack = callBack;
        return this;
    }


    /**
     * 设置验证码到哪个View
     * @param codeView
     */
    public AutoVerifyCode into(TextView codeView) {
        mHandler = new VerifyCodeHandler(codeView);
        return this;
    }


    /**
     * 开始监听短信
     */
    public void start(){
        if(mHandler == null){
            mHandler = new VerifyCodeHandler();
        }
        if (mConfig == null) {
            mConfig = new AutoCodeConfig.Builder().build();
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            restart();
        } else {
            Log.e(TAG, "no Permission,start to request Permission");
            //申请权限，会回调
            mContext.startActivity(new Intent(mContext,GetPermissionActivity.class));
        }
    }


    /**
     * 重新注册，获取权限之后
     */
    private void restart(){
        if(serviceIntent != null){
            mContext.stopService(serviceIntent);
        }
        startReadSmsService();
    }
 

    /**
     * 开启短信验证码处理的服务
     */
    private void startReadSmsService() {

        serviceIntent = new Intent(mContext, ReadSmsService.class);
        serviceIntent.putExtra(ReadSmsService.EXTRAS_MESSAGER, new Messenger(mHandler));
        serviceIntent.putExtra(ReadSmsService.EXTRAS_CONFIG, mConfig);
        mContext.startService(serviceIntent);
    }
    
    protected  Handler getHandler(){
        return mHandler;
    }
     
    
    public void release(){
        if(serviceIntent != null){
            mContext.stopService(serviceIntent);
        }
        if(mHandler != null){
            mHandler.release();
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        mPermissionCallBack = null;
        mSmsCallBack = null;
        mConfig = null;
        mContext = null;
        INSTANCE = null;
    }

 
    
}
