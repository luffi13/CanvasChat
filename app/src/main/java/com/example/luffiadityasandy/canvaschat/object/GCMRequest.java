package com.example.luffiadityasandy.canvaschat.object;

import android.app.Notification;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Luffi Aditya Sandy on 01/03/2017.
 */

public class GCMRequest {
    public String to;
    public Map<String, Object> data;
    public Map<String, Object> notification;

    public GCMRequest(String to, User sender, String messageType) {
        this.to = to;
        data = new HashMap<>();
        data.put("sender_detail", sender);
        data.put("messageType", messageType);

        notification = new HashMap<>();
        notification.put("title",sender.getName());
        notification.put("body","send you a message");
        notification.put("icon","myicon");
    }

}
