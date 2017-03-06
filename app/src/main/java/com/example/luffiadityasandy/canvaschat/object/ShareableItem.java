package com.example.luffiadityasandy.canvaschat.object;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.firebase.database.Exclude;

/**
 * Created by Luffi Aditya Sandy on 21/02/2017.
 */

public class ShareableItem {
    private String points;
    private String type;
    private String uid;
    private Integer color;
    private Integer strokeWidth;

    public ShareableItem(String points, String type, String uid, int color, int strokeWidth) {
        Log.d("adfa","constructor ny");
        this.points = points;
        this.type = type;
        this.uid = uid;
        this.color = color;
        this.strokeWidth = strokeWidth;
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

    @Exclude
    public ShareablePaint getShareablePaint() {

        ShareablePaint shareablePaint = new ShareablePaint(color,strokeWidth);
        return shareablePaint;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Integer getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(Integer strokeWidth) {
        this.strokeWidth = strokeWidth;
    }



    public class ShareablePaint {
        private int color;
        private int strokeWidth;

        public ShareablePaint(int color, int strokeWidth){
            this.color = color;
            this.strokeWidth =strokeWidth;
        }

        public Paint getPaint(){
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(this.color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(this.strokeWidth);
            return paint;
        }
    }

}
