package com.tpnet.tpautoverifycode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;

import static android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION;

/**
 * 短信广播接收者
 * Created by litp on 2017/6/23.
 */

public class SmsReceiver extends BroadcastReceiver {

    private HandlerMessage mHandlerMessage;

    public SmsReceiver(HandlerMessage mHandlerMessage) {
        this.mHandlerMessage = mHandlerMessage;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
            getSmsCodeFromReceiver(intent);
        }
    }

    /**
     * 从广播中得到短信验证码
     *
     * @param intent
     */
    void getSmsCodeFromReceiver(Intent intent) {
        SmsMessage[] messages = null;
        if (Build.VERSION.SDK_INT >= 19) {
            messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        } else {
            messages = getSmsUnder19(intent);
        }


        if (messages == null) return;


        if (messages.length > 0) {
            for (SmsMessage sms : messages) {
                String smsSender = sms.getOriginatingAddress();
                String smsBody = sms.getMessageBody();
                
                if(mHandlerMessage.onGetMessageInfo(smsSender,smsBody)){
                    break;
                }
              
            }
        }
    }

    @Nullable
    private SmsMessage[] getSmsUnder19(Intent intent) {
        SmsMessage[] messages;
        Bundle bundle = intent.getExtras();
        // https://developer.android.com/reference/android/provider/Telephony.Sms.Intents.html#SMS_DELIVER_ACTION
        Object[] pdus = (Object[]) bundle.get("pdus");

        if ((pdus == null) || (pdus.length == 0)) {
            return null;
        }

        messages = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }
        return messages;
    }
}