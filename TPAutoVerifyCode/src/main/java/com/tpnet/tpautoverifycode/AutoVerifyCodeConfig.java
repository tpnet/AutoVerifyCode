package com.tpnet.tpautoverifycode;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 配置
 * Created by litp on 2017/6/23.
 */
public class AutoVerifyCodeConfig implements Parcelable {

    public final static int CODE_TYPE_NUMBER = 0X111;    //  纯数字
    public final static int CODE_TYPE_LETTER = 0X222;     // 大写字母和小写字母混合
    public final static int CODE_TYPE_BIG_LETTER = 0X333;   //  纯大写字母
    public final static int CODE_TYPE_SMALL_LETTER = 0X444;    //  纯小写字母
    public final static int CODE_TYPE_BIGLETTER_NUMBER = 0X555;  // 大写字母和数字混合
    public final static int CODE_TYPE_SMALLLETTER_NUMBER = 0X666;   // 小写字母和数字混合
    public final static int CODE_TYPE_LETTER_NUMBER = 0X777;   // 字母和数字混合


    //验证码类型
    @IntDef({CODE_TYPE_NUMBER,CODE_TYPE_LETTER,CODE_TYPE_BIG_LETTER,CODE_TYPE_SMALL_LETTER,CODE_TYPE_BIGLETTER_NUMBER,CODE_TYPE_SMALLLETTER_NUMBER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CodeType{}
    
    
    private int mCodeLength;  //短信验证码默认长度0，会自动匹配4-6位数的验证码
    
    private @CodeType int mCodeType;  //验证码类型,默认为数字
    
    private String mSmsBodyStart;  //验证码短信开头文字
    
    private String mSmsBodyContains; //设置验证码短信内容包含文字
    
    private String mSmsSender;          //验证码发送者号码(不建议设置，免得换了短信提供商就要重新发包了)
    
    private String mSmsSenderStart;   //验证码发送者号码的前几位数字(不建议设置，免得换了短信提供商就要重新发包了)
 
    public int getCodeLength() {
        return mCodeLength;
    }

    private void setCodeLength(int codeLength) {
        mCodeLength = codeLength;
    }

    public String getSmsBodyStart() {
        return mSmsBodyStart;
    }

    private void setSmsBodyStart(String smsStart) {
        mSmsBodyStart = smsStart;
    }

    public String getSmsBodyContains() {
        return mSmsBodyContains;
    }

    private void setSmsBodyContains(String smsContains) {
        mSmsBodyContains = smsContains;
    }

    public String getSmsSender() {
        return mSmsSender;
    }

    private void setSmsSender(String smsSender) {
        mSmsSender = smsSender;
    }

    public String getSmsSenderStart() {
        return mSmsSenderStart;
    }

    private void setSmsSenderStart(String smsSenderStart) {
        mSmsSenderStart = smsSenderStart;
    }


    public int getCodeType() {
        return mCodeType;
    }

    public void setCodeType(int mCodeType) {
        this.mCodeType = mCodeType;
    }

 
    public static class Builder {
        
        private int mCodeLength = 0;  
        private @CodeType int mCodeType = CODE_TYPE_NUMBER;  
        private String mSmsBodyStart;  
        private String mSmsBodyContains;
        private String mSmsSender;         
        private String mSmsSenderStart;
      
    
        public Builder smsCodeType(@CodeType int type) {
            mCodeType = type;
            return this;
        }

        public Builder smsSender(String phoneNumber) {
            mSmsSender = phoneNumber;
            return this;
        }

        public Builder smsSenderStart(String numberStart) {
            mSmsSenderStart = numberStart;
            return this;
        }

        public Builder codeLength(int len) {
            mCodeLength = len;
            return this;
        }

        public Builder smsBodyStartWith(String startWith) {
            mSmsBodyStart = startWith;
            return this;
        }

        public Builder smsBodyContains(String contains) {
            mSmsBodyContains = contains;
            return this;
        }

        public AutoVerifyCodeConfig build() {
            AutoVerifyCodeConfig codeConfig = new AutoVerifyCodeConfig();
            codeConfig.setSmsSender(mSmsSender);
            codeConfig.setSmsSenderStart(mSmsSenderStart);
            codeConfig.setCodeLength(mCodeLength);
            codeConfig.setSmsBodyStart(mSmsBodyStart);
            codeConfig.setSmsBodyContains(mSmsBodyContains);
            codeConfig.setCodeType(mCodeType);
            return codeConfig;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCodeLength);
        dest.writeString(this.mSmsBodyStart);
        dest.writeString(this.mSmsBodyContains);
        dest.writeString(this.mSmsSender);
        dest.writeString(this.mSmsSenderStart);
        dest.writeInt(this.mCodeType);
    }

    public AutoVerifyCodeConfig() {
    }

 
    protected AutoVerifyCodeConfig(Parcel in) {
        this.mCodeLength = in.readInt();
        this.mSmsBodyStart = in.readString();
        this.mSmsBodyContains = in.readString();
        this.mSmsSender = in.readString();
        this.mSmsSenderStart = in.readString();
        //这个警告忽略
        this.mCodeType =  in.readInt();
    }

    public static final Creator<AutoVerifyCodeConfig> CREATOR = new Creator<AutoVerifyCodeConfig>() {
        public AutoVerifyCodeConfig createFromParcel(Parcel source) {
            return new AutoVerifyCodeConfig(source);
        }

        public AutoVerifyCodeConfig[] newArray(int size) {
            return new AutoVerifyCodeConfig[size];
        }
    };
}
