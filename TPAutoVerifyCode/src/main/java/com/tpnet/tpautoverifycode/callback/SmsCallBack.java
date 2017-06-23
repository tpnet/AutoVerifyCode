package com.tpnet.tpautoverifycode.callback;

import android.support.annotation.Nullable;

/**
 * 短信消息回调
 * Created by litp on 2017/6/23.
 */

public abstract class SmsCallBack {
    
    public void onGetMessage(String mess){}
    
    public void onGetCode(String code){}
    
    public void onGetSender(@Nullable String phoneNumber){}
    
}
