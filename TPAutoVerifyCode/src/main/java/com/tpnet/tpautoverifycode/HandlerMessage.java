package com.tpnet.tpautoverifycode;

import android.os.Message;
import android.text.TextUtils;

import com.tpnet.tpautoverifycode.callback.GetMessageListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tpnet.tpautoverifycode.AutoVerifyCodeConfig.CODE_TYPE_BIGLETTER_NUMBER;
import static com.tpnet.tpautoverifycode.AutoVerifyCodeConfig.CODE_TYPE_BIG_LETTER;
import static com.tpnet.tpautoverifycode.AutoVerifyCodeConfig.CODE_TYPE_LETTER;
import static com.tpnet.tpautoverifycode.AutoVerifyCodeConfig.CODE_TYPE_LETTER_NUMBER;
import static com.tpnet.tpautoverifycode.AutoVerifyCodeConfig.CODE_TYPE_NUMBER;
import static com.tpnet.tpautoverifycode.AutoVerifyCodeConfig.CODE_TYPE_SMALLLETTER_NUMBER;
import static com.tpnet.tpautoverifycode.AutoVerifyCodeConfig.CODE_TYPE_SMALL_LETTER;
import static com.tpnet.tpautoverifycode.ReadSmsService.RECEIVER_SENDER_MSG;
import static com.tpnet.tpautoverifycode.ReadSmsService.RECEIVER_SMS_CODE_MSG;
import static com.tpnet.tpautoverifycode.ReadSmsService.RECEIVER_SMS_MSG;

/**
 * 处理消息
 * Created by litp on 2017/6/23.
 */

public class HandlerMessage implements GetMessageListener{
    
    
    private AutoVerifyCodeConfig mConfig;

    public HandlerMessage(AutoVerifyCodeConfig mConfig) {
        this.mConfig = mConfig;
    }

    @Override
    public boolean onGetMessageInfo(String sender, String message) {
        if (checkSmsSender(sender) && checkSmsBody(message)) {
            sendMsg(RECEIVER_SMS_MSG, message);
            String smsCode = parseSmsBody(message);
            sendMsg(RECEIVER_SMS_CODE_MSG, smsCode);
            return true;
        }else{
            return false;
        }
    }


    /**
     * 检查短信发送者
     *
     * @param smsSender
     * @return
     */
    private boolean checkSmsSender(String smsSender) {

        sendMsg(RECEIVER_SENDER_MSG,smsSender);

        if(!TextUtils.isEmpty(smsSender)){

            if (!TextUtils.isEmpty(mConfig.getSmsSender())) {
                return smsSender.equals(mConfig.getSmsSender());
            }
            if(!TextUtils.isEmpty(mConfig.getSmsSenderStart())){
                return smsSender.startsWith(mConfig.getSmsSenderStart());
            }
        }

        return true;
    }


    /**
     * 检查短信内容
     *
     * @param smsBody
     * @return
     */
    private boolean checkSmsBody(String smsBody) {
        if(!TextUtils.isEmpty(smsBody)){
            if (!TextUtils.isEmpty(mConfig.getSmsBodyStart()) && !TextUtils.isEmpty(mConfig.getSmsBodyContains())) {
                return smsBody.startsWith(mConfig.getSmsBodyStart()) && smsBody.contains(mConfig.getSmsBodyContains());
            } else if (!TextUtils.isEmpty(mConfig.getSmsBodyStart())) {
                return smsBody.startsWith(mConfig.getSmsBodyStart());
            } else if (!TextUtils.isEmpty(mConfig.getSmsBodyContains())) {
                return smsBody.contains(mConfig.getSmsBodyContains());
            }else {
                return true;
            }
        }

        return false;

    }

    /**
     * 解析短信得到验证码
     *
     * @param smsBody 短信内容
     * @return 验证码
     */
    private String parseSmsBody(String smsBody) {


        int length = mConfig.getCodeLength();
        String textLength;
        if (length == 0) {
            //默认解析4-6位数
            textLength = "4,6";
        } else {
            textLength = String.valueOf(length);
        }

        String regex = "(\\d{" + textLength + "})";


        @AutoVerifyCodeConfig.CodeType
        int type = mConfig.getCodeType();
        switch (type) {
            case CODE_TYPE_NUMBER:
                //数字
                //regex = "(\\d{" + textLength + "})";
                break;
            case CODE_TYPE_LETTER:
                //大小写字母
                regex = "([a-zA-Z]{" + textLength + "})";

                break;
            case CODE_TYPE_BIG_LETTER:
                //大写字母
                regex = "([A-Z]{" + textLength + "})";

                break;
            case CODE_TYPE_SMALL_LETTER:
                //小写字母
                regex = "([a-z]{" + textLength + "})";

                break;
            case CODE_TYPE_BIGLETTER_NUMBER:
                // 大写字母和数字
                regex = "([A-Z\\d]{" + textLength + "})";

                break;
            case CODE_TYPE_SMALLLETTER_NUMBER:
                //小写字母和数字
                regex = "([a-z\\d]{" + textLength + "})";

                break;
            case CODE_TYPE_LETTER_NUMBER:
                //字母和数字
                regex = "([a-zA-Z\\d]{" + textLength + "})";

                break;
            default:


        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(smsBody);

        String smsCode = "";
        if (matcher.find()) {
            smsCode = matcher.group(0);
        }
        return smsCode;
    }

    /**
     * 回调
     *
     * @param msgWhat
     * @param msgObj
     */
    private void sendMsg(int msgWhat, String msgObj) {

        Message msg = Message.obtain();
        msg.what = msgWhat;
        msg.obj = msgObj;
        AutoVerifyCode.getInstance().getHandler().sendMessage(msg);

    }
}
