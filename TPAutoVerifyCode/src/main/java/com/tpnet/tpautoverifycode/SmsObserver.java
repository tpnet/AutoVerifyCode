package com.tpnet.tpautoverifycode;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import static com.tpnet.tpautoverifycode.ReadSmsService.PROJECTION;

/**
 * 短信数据库观察者
 * Created by litp on 2017/6/23.
 */

public class SmsObserver  extends ContentObserver {

    private long lastTimeofCall = 0L;    //最后一次数据库回调的时间

    private Context mContext;
    private HandlerMessage mHandlerMessage;

    //API level>=19,可直接使用Telephony.Sms.Inbox.CONTENT_URI
    private static final String SMS_INBOX_URI = "content://sms/inbox";


    public SmsObserver(Context context,HandlerMessage handlerMessage){
        super(AutoVerifyCode.getInstance().getHandler());
        this.mContext = context;
        this.mHandlerMessage = handlerMessage;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        if(lastTimeofCall != 0){
            if(System.currentTimeMillis() -  lastTimeofCall < 1000){
                //1s之内的回调，过滤，防止多次回调
                return;
            }
        }
        lastTimeofCall = System.currentTimeMillis();

        Cursor cursor = mContext.getContentResolver().query(
                Uri.parse(SMS_INBOX_URI), PROJECTION
                , Telephony.Sms.READ + "=?"
                , new String[]{"0"}
                , Telephony.Sms.Inbox.DEFAULT_SORT_ORDER);

        getSmsCodeFromObserver(cursor);
    }

    
    /**
     * 从数据库得到短信验证码
     *
     * @param cursor
     */
    void getSmsCodeFromObserver(Cursor cursor) {
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            String sender = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
            String smsBody = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
            
            if(mHandlerMessage.onGetMessageInfo(sender,smsBody)){
                break;
            }
          
        }

      
        if (!cursor.isClosed()) {
            cursor.close();
        }
    }
}
