package com.example.luffiadityasandy.canvaschat.object;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

/**
 * Created by Luffi Aditya Sandy on 15/02/2017.
 */

@IgnoreExtraProperties
public class Message {
    private String sender;
    private String message;
    private Long time;
    private String type;

    public Message() {
    }

    public Message(String message, String sender, Long time, String type) {
        this.sender = sender;
        this.time = time;
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String canvasUri) {
        this.message = canvasUri;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
