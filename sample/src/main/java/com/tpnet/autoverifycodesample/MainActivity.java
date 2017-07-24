package com.tpnet.autoverifycodesample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tpnet.autoverifycode.R;
import com.tpnet.tpautoverifycode.AutoVerifyCodeConfig;
import com.tpnet.tpautoverifycode.AutoVerifyCode;
import com.tpnet.tpautoverifycode.callback.OnInputCompleteListener;
import com.tpnet.tpautoverifycode.callback.PermissionCallBack;
import com.tpnet.tpautoverifycode.callback.SmsCallBack;

public class MainActivity extends AppCompatActivity {

  
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
  
    }



    public void open(View v){
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    /**
     * 简单的4到6位的数字验证码获取,不回调
     * @param v
     */
    public void simple(View v){
 
        AutoVerifyCode.getInstance()
                .with(MainActivity.this)
                .into((EditText) findViewById(R.id.et_code))  //要输入的编辑框
                .start();       //开始
    }


    /**
     * 多种属性设置
     * @param v
     */
    public void complex(View v){
        AutoVerifyCodeConfig config = new AutoVerifyCodeConfig.Builder()
                .codeLength(6) // 验证码长度
                .smsCodeType(AutoVerifyCodeConfig.CODE_TYPE_NUMBER)  //验证码类型
                .smsSenderStart("650") // 验证码发送者号码的前几位数字
                .smsSender("6505551212") // 验证码发送者的号码
                .smsBodyStartWith("【守护APP】") // 设置验证码短信开头文字，固定可以设置
                .smsBodyContains("重置") // 设置验证码短信内容包含文字，每个功能包含不一样，例如注册、重置密码
                .build();

        AutoVerifyCode.getInstance()
                .with(MainActivity.this)
                .config(config)  //验证码选项配置
                .smsCallback(new MessageCallBack())  //短信内容回调
                .permissionCallback(new PerCallBack())  //短信短信回调
                .inputCompleteCallback(new OnInputCompleteListener() {
                    @Override
                    public void onInputComplete(String text) {
                        //自动输入完毕，可以进行登录等等操作
                        Log.e("@@","自动输入验证码完成"+text);

                    }
                })
                .into((EditText) findViewById(R.id.et_code))  //要输入的View
                .start();       //开始
    }
    
  


    /**
     * 获取短信回调接口
     * 
     */
    class MessageCallBack extends SmsCallBack{
        @Override
        public void onGetCode(String code) {
            Log.e("@@","验证码为："+code);
        }

        @Override
        public void onGetMessage(String mess) {
            Log.e("@@","短信内容为："+mess);

        }

        @Override
        public void onGetSender(@Nullable String phoneNumber) {
            Log.e("@@","发送者为："+phoneNumber);

        }
    }
    
    
    
    class PerCallBack implements PermissionCallBack{

        @Override
        public void onSuccess() {
            //获取短信权限成功
            Log.e("@@","获取短信权限成功：");
        }

        @Override
        public boolean onFail() {
            //获取短信权限失败
            Toast.makeText(MainActivity.this,"拒绝获取短信权限",Toast.LENGTH_SHORT).show();
            Log.e("@@","获取短信权限失败,返回真则重试获取权限,或者你自己手动获取了之后再返回真也行");
            
            
            return false;
            
        }
    }
    
    
    
    
    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //因为一般只用一次，所以页面销毁就释放。
        AutoVerifyCode.getInstance().release();
    }
 
    
}
