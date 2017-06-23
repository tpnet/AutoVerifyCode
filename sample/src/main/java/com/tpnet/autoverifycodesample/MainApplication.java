package com.tpnet.autoverifycodesample;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tpnet.autoverifycode.BuildConfig;

/**
 * 
 * Created by litp on 2017/6/23.
 */

public class MainApplication extends Application {

    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();
        this.refWatcher = initLeakCancry();
        // Normal app init code...
    }
    
    private RefWatcher initLeakCancry(){
        //在发行模式下关闭LeakCanary
        return BuildConfig.DEBUG? LeakCanary.install(this):RefWatcher.DISABLED;
    }


    public static RefWatcher getRefWatcher(Context context) {
        MainApplication application = (MainApplication) context.getApplicationContext();
        return application.refWatcher;
    }
}
