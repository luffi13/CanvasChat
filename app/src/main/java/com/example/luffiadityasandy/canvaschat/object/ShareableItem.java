package com.example.luffiadityasandy.canvaschat.object;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

/**
 * Created by Luffi Aditya Sandy on 21/02/2017.
 */


public class ShareableItem {
    private String points;
    private String type;
    private String uid;
    private Integer color;
    private Integer strokeWidth;
    private String key;
    private Integer screenWidth;
    private Integer screenHeight;

    @Exclude
    private Integer canvasWidth;

    @Exclude
    private Integer canvasHeight;

    @Exclude
    private Path path;

    public ShareableItem(String points, String type, String uid, int color, int strokeWidth, String key, Integer width, Integer height) {
        Log.d("adfa","constructor ny");
        this.points = points;
        this.type = type;
        this.uid = uid;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.key = key;
        this.screenWidth= width;
        this.screenHeight = height;
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

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public ShareablePaint getShareablePaint() {

        ShareablePaint shareablePaint = new ShareablePaint(color,strokeWidth);
        return shareablePaint;
    }

    @Exclude
    public void setPath(){
        if(this.getType().equals("freehand")){
            path= getFreeHand();
        }
        else if(this.getType().equals("rectangle")){
            HashMap<String, Float> listPoint = stringToPoint(this.getPoints());
            path= getRectangle(listPoint.get("left"),listPoint.get("top"),
                    listPoint.get("right"),listPoint.get("bottom"));
        }
        else if(this.getType().equals("circle")){
            HashMap<String, Float> listPoint = stringToPoint(this.getPoints());
            path= getCircle(listPoint.get("left"),listPoint.get("top"),
                    listPoint.get("right"),listPoint.get("bottom"));
        }
        else if(this.getType().equals("line")){
            String[]listData = this.getPoints().split("<<");

            path= getLine(Float.parseFloat(listData[0]),
                    Float.parseFloat(listData[1]),
                    Float.parseFloat(listData[2]),
                    Float.parseFloat(listData[3]));
        }
    }

    @Exclude
    public Path getPath(){
        return this.path;
    }


    @Exclude
    private Path getFreeHand(){
        final float TOUCH_TOLERANCE = 4;
        String[] listPointString = this.getPoints().split("<<");
        Path newPath = new Path();
        Float newX = null, newY = null;
        for(int i = 0 ; i < listPointString.length ; i ++){
            Float x = adjustXPoint(Float.parseFloat(listPointString[i].split(",")[0]));
            Float y = adjustYPoint(Float.parseFloat(listPointString[i].split(",")[1]));

            if(i==0){
                newPath.moveTo(x,y);
                newX = x;
                newY = y;
            }
            else if (i==(listPointString.length-1)){
                newPath.lineTo(newX, newY);
            }
            else {
                float dx = Math.abs(x - newX);
                float dy = Math.abs(y - newY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    newPath.quadTo(newX, newY, (x + newX)/2, (y + newY)/2);
                    newX = x;
                    newY = y;
                }
            }
        }

        return newPath;
    }

    @Exclude
    private Path getCircle(float left, float top,  float right, float bottom){
        Path newPath = new Path();
        newPath.addOval(
                adjustXPoint(left),
                adjustYPoint(top),
                adjustXPoint(right),
                adjustYPoint(bottom),
                Path.Direction.CCW);
        return newPath;
    }

    @Exclude
    private Path getRectangle(float left, float top,  float right, float bottom){
        Path newPath = new Path();
        newPath.addRect(
                adjustXPoint(left),
                adjustYPoint(top),
                adjustXPoint(right),
                adjustYPoint(bottom)
                , Path.Direction.CCW);
        return newPath;
    }

    @Exclude
    private Path getLine(float startX, float startY, float endX, float endY){
        Path newPath = new Path();
        newPath.moveTo(
                adjustXPoint(startX),
                adjustYPoint(startY));
        newPath.lineTo(
                adjustXPoint(endX),
                adjustYPoint(endY));
        return newPath;
    }

    @Exclude
    public void setCanvasSize(int width, int height){
        this.canvasWidth = width;
        this.canvasHeight = height;
    }


    private Float adjustXPoint(Float point){
        Float param = (float)canvasWidth/screenWidth;
        Float index = (float)canvasWidth/screenWidth;
        Float pointAwal = param*point;

        Log.d("adjustX", index+" "+pointAwal);
        return param*point;
    }

    private Float adjustYPoint(Float point){
        Float param = (float)canvasHeight/screenHeight;
        return param*point;
    }

    private HashMap<String,Float> stringToPoint(String data){
        String[]listData = data.split("<<");
        HashMap<String, Float> floatPoints = new HashMap<>();
        floatPoints.put("left",Float.parseFloat(listData[0]));
        floatPoints.put("top",Float.parseFloat(listData[1]));
        floatPoints.put("right",Float.parseFloat(listData[2]));
        floatPoints.put("bottom",Float.parseFloat(listData[3]));

        return floatPoints;
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
