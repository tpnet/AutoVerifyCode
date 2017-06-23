package com.tpnet.tpautoverifycode.callback;

/**
 * 收到短信接口回调
 * Created by litp on 2017/6/23.
 */

public interface GetMessageListener {
    
    boolean onGetMessageInfo(String sender,String message);
    
}
