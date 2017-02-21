package com.example.luffiadityasandy.canvaschat.object;

/**
 * Created by Luffi Aditya Sandy on 21/02/2017.
 */

public class ShareableItem {
    private String points;
    private String type;
    private String uid;

    public ShareableItem(String points, String type, String uid) {
        this.points = points;
        this.type = type;
        this.uid = uid;
    }
    public ShareableItem() {
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
