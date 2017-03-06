package com.example.luffiadityasandy.canvaschat.session;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Luffi Aditya Sandy on 02/03/2017.
 */

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME =  "CanvasPreferences";
    public SessionManager (Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }
}
