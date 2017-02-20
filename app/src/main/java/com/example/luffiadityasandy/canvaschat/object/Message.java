package com.example.luffiadityasandy.canvaschat.object;

import com.google.firebase.database.ServerValue;

/**
 * Created by Luffi Aditya Sandy on 15/02/2017.
 */

public class Message {
    public String sender;
    public Long time;

    public String getCanvasUri() {
        return canvasUri;
    }

    public void setCanvasUri(String canvasUri) {
        this.canvasUri = canvasUri;
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

    public String canvasUri;

    public Message() {
    }

    public Message(String canvasUri, String sender, Long time) {
        this.sender = sender;
        this.time = time;
        this.canvasUri = canvasUri;
    }
}
