package com.tpnet.tpautoverifycode.callback;

/**
 * 权限回调
 * Created by litp on 2017/6/23.
 */

public interface PermissionCallBack {
    
    void onSuccess();

    /**
     * 回调获取短信权限失败
     * @return 为真，则重试。假则返回。
     */
    boolean onFail();
    
}
