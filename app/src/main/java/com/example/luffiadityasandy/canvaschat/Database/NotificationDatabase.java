package com.example.luffiadityasandy.canvaschat.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Luffi Aditya Sandy on 15/03/2017.
 */

public class NotificationDatabase extends SQLiteOpenHelper  {

    private static final String TAG = "notification database";

    private static final String DB_NAME = "canvas.db";
    private static final String table_name = "notification";
    private static final String column_id = "uid";
    private static final String column_notification = "amount";

    public NotificationDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+table_name+" ( " +
                column_id+" TEXT ,"+
                column_notification + " INT "+
                " )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+table_name);
    }

    public void insertNotification(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newData = new ContentValues();
        newData.put(column_id,uid);
        newData.put(column_notification,0);

        db.insert(table_name,null,newData);
        Log.d(TAG, "insertNotification: "+newData.toString());

    }

    public void updateNotification(String uid){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+table_name+
                " WHERE "+column_id +" = '"+uid+"'",null);

        if (cursor.getCount()==0){
            insertNotification(uid);

        }

        db.execSQL("UPDATE "+table_name +
                " SET "+column_notification + " = "+column_notification+"+1 " +
                "WHERE "+column_id+" = '"+uid+"'"
        );
    }

    public void setNotificationNull(String uid){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+table_name+
                " WHERE "+column_id +" = '"+uid+"'",null);

        if (cursor.getCount()==0){
            insertNotification(uid);

        }

        db.execSQL("UPDATE "+table_name +
                " SET "+column_notification + " = 0 " +
                "WHERE "+column_id+" = '"+uid+"'"
        );
    }

    public HashMap<String,Integer> getAllData(){
        HashMap<String,Integer> allData = new HashMap<>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+table_name,null);

        if(cursor.getCount()>0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                allData.put(
                        cursor.getString(cursor.getColumnIndex(column_id)),
                        cursor.getInt(cursor.getColumnIndex(column_notification))
                );

                Log.d(TAG, "getAllData: "+cursor.getString(cursor.getColumnIndex(column_id))+" "+cursor.getInt(cursor.getColumnIndex(column_notification)));
            }

        }

        return allData;
    }
}
