package com.example.luffiadityasandy.canvaschat.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.activity.ListFriendActivity;
import com.example.luffiadityasandy.canvaschat.activity.MainActivity;
import com.example.luffiadityasandy.canvaschat.activity.OfflineCanvasChatActvity;
import com.example.luffiadityasandy.canvaschat.activity.ShareableCanvasActivity;
import com.example.luffiadityasandy.canvaschat.activity.TabLayoutActivity;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import io.realm.RealmObject;

/**
 * Created by Luffi Aditya Sandy on 28/02/2017.
 */

public class MessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        Type type = new TypeToken<User>(){}.getType();
        User sender= gson.fromJson(remoteMessage.getData().get("sender_detail"),type);
        String typeMessage = remoteMessage.getData().get("messageType");
        Log.d("incomingMessage", remoteMessage.getData()+"");
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.send)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setAutoCancel(true);



        if(typeMessage.equals("shareable_canvas")){
            Log.d("typemessage", "shareable");
            Intent resultIntent = new Intent(this, ShareableCanvasActivity.class);
            resultIntent.putExtra("receiver",sender);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(TabLayoutActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }
        else if(typeMessage.equals("offline_canvas")) {
            Log.d("typemessage", "offline");
            Intent firstIntent = new Intent(getApplicationContext(), TabLayoutActivity.class);
            Intent secondIntent = new Intent(getApplicationContext(), OfflineCanvasChatActvity.class);
            firstIntent.putExtra("receiver",sender);
            secondIntent.putExtra("receiver",sender);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(TabLayoutActivity.class);
            stackBuilder.addNextIntent(firstIntent);
            stackBuilder.addNextIntent(secondIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT );
            mBuilder.setContentIntent(resultPendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(123);
        notificationManager.notify(123,mBuilder.build());
    }

}
