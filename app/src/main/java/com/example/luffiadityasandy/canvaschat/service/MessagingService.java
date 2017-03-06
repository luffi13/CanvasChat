package com.example.luffiadityasandy.canvaschat.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.activity.ListFriendActivity;
import com.example.luffiadityasandy.canvaschat.activity.MainActivity;
import com.example.luffiadityasandy.canvaschat.activity.OfflineCanvasChatActvity;
import com.example.luffiadityasandy.canvaschat.activity.ShareableCanvasActivity;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Luffi Aditya Sandy on 28/02/2017.
 */

public class MessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("incomingMessage", remoteMessage.getData()+"");
        Gson gson = new Gson();
        Type type = new TypeToken<User>(){}.getType();
        User sender= gson.fromJson(remoteMessage.getData().get("sender_detail"),type);
        String typeMessage = remoteMessage.getData().get("messageType");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody());


        if(typeMessage.equals("shareable_canvas")){
            Intent resultIntent = new Intent(this, ShareableCanvasActivity.class);
            resultIntent.putExtra("receiver",sender);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(ShareableCanvasActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }
        else if(typeMessage.equals("offline_canvas")) {
            Intent resultIntent = new Intent(this, OfflineCanvasChatActvity.class);
            resultIntent.putExtra("receiver",sender);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(OfflineCanvasChatActvity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,mBuilder.build());
    }

}
