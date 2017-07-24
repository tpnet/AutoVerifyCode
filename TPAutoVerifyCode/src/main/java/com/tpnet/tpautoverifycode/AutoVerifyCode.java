package com.tpnet.tpautoverifycode;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import com.tpnet.tpautoverifycode.callback.OnInputCompleteListener;
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
    
    private AutoVerifyCodeConfig mConfig;

    private VerifyCodeHandler mHandler;
    
    private SmsCallBack mSmsCallBack;  //短信内容回调
    
    private OnInputCompleteListener mOnInputComplete;  //设置文本完毕回调
    
    private PermissionCallBack mPermissionCallBack; //权限回调

    private Intent mServiceIntent;
    
    private String mCurrText;  //防止多次设置文本，


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
                throw new IllegalArgumentException("target view and smscallback is null,Must set at least one");
            }
        }
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
         
            
            switch (msg.what) {
                case ReadSmsService.OBSERVER_SMS_CODE_MSG:
                case ReadSmsService.RECEIVER_SMS_CODE_MSG:
                  
                    setText(mTextView.get(),(String) msg.obj);
                    
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
                        if( mPermissionCallBack.onFail()){
                            start();
                        }
                    }
                    break;
                default:
                    break;
            }
        }


        private void release() {
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

    public AutoVerifyCode config(AutoVerifyCodeConfig config) {
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

    public AutoVerifyCode inputCompleteCallback(OnInputCompleteListener callback) {
        this.mOnInputComplete = callback;
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
     * 设置验证码到哪个View
     * @param id
     */
    public AutoVerifyCode into(@IdRes int id) {
        if(mContext == null){
            throw new IllegalArgumentException("请先调用with方法设置activity的上下文对象");
        }
        TextView textView = (TextView) ((Activity)mContext).findViewById(id);
        mHandler = new VerifyCodeHandler(textView);
        return this;
    }


    /**
     * 开始监听短信
     */
    public AutoVerifyCode start(){
        if(mHandler == null){
            mHandler = new VerifyCodeHandler();
        }
        if (mConfig == null) {
            mConfig = new AutoVerifyCodeConfig.Builder().build();
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            restart();
        } else {
            Log.e(TAG, "no Permission,start to request Permission");
            //申请权限，会回调
            mContext.startActivity(new Intent(mContext,GetPermissionActivity.class));
        }
        
        return this;
    }


    /**
     * 重新注册，获取权限之后
     */
    private void restart(){
        if(mServiceIntent != null){
            mContext.stopService(mServiceIntent);
        }
        startReadSmsService();
    }
 

    /**
     * 开启短信验证码处理的服务
     */
    private void startReadSmsService() {

        mServiceIntent = new Intent(mContext, ReadSmsService.class);
        mServiceIntent.putExtra(ReadSmsService.EXTRAS_CONFIG, mConfig);
        mContext.startService(mServiceIntent);
    }
    
    
    private synchronized void setText(TextView view,String code){

        if(code.equals(mCurrText)){
            return;
        }
        

        if (mSmsCallBack != null) {
            mSmsCallBack.onGetCode(code);
        }
        if (view != null /*&& mAuthCode.getText().toString().isEmpty()*/) {
            view.setText(code);

            if(view.getText().toString().equals(code) && mOnInputComplete != null){
                mOnInputComplete.onInputComplete(code);
            }
            
            mCurrText = code;
 
        }
    }
    
    protected  Handler getHandler(){
        return mHandler;
    }

    /**
     * 释放内存
     */
    public void release(){
        if(mServiceIntent != null){
            mContext.stopService(mServiceIntent);
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
