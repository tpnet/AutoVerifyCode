package com.tpnet.tpautoverifycode;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * 申请短信权限Activity
 * Created by litp on 2017/6/23.
 */

public class GetPermissionActivity extends Activity {

 
    protected static final int AUTOCODE_REQUEST_PERMISSION_CODE = 0x666;

    //获取短信权限成功
    protected static final int AUTOCODE_REQUEST_PERMISSION_SUCCESS = 0x667;
    
    //获取短信权限失败
    protected static final int AUTOCODE_REQUEST_PERMISSION_FAIL = 0x668;
    
    /**
     * 简单处理了短信权限
     */
    private void getPermission(String permissios) {
        if (ActivityCompat.checkSelfPermission(this,permissios) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {permissios}, AUTOCODE_REQUEST_PERMISSION_CODE);
        }else{
            AutoVerifyCode.getInstance().getHandler().sendEmptyMessage(AUTOCODE_REQUEST_PERMISSION_SUCCESS);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //申请短信权限
        getPermission(Manifest.permission.READ_SMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 
        if (requestCode == AUTOCODE_REQUEST_PERMISSION_CODE) {
            if (grantResults.length != 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //拒绝了权限
                    AutoVerifyCode.getInstance().getHandler().sendEmptyMessage(AUTOCODE_REQUEST_PERMISSION_FAIL);
                }else{
                    //允许了权限
                    AutoVerifyCode.getInstance().getHandler().sendEmptyMessage(AUTOCODE_REQUEST_PERMISSION_SUCCESS);
                }
            }
        }
        
        finish();
    }
}
