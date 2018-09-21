package com.yasir.ecom;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by AliAh on 29/03/2018.
 */

public class ClassifiedAdsApplication extends Application {
    private static ClassifiedAdsApplication instance;


    public static ClassifiedAdsApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
