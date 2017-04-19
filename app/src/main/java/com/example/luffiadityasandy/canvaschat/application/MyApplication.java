package com.example.luffiadityasandy.canvaschat.application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Luffi Aditya Sandy on 10/04/2017.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration configuration = new RealmConfiguration.Builder(getApplicationContext()).build();
        Realm.setDefaultConfiguration(configuration);
    }
}
