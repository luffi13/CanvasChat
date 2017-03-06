package com.example.luffiadityasandy.canvaschat.canvas_handler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.object.ShareableItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Luffi Aditya Sandy on 21/02/2017.
 */

public class ShareableCanvasView extends View implements View.OnTouchListener  {

    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private DatabaseReference databaseReference;
    private float startX, startY, endX, endY;
    String paintTool;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private int color;
    private int strokeWidth;


    private ArrayList<Pair<Path,Paint>> paths = new ArrayList<>();
    private ArrayList<Pair<Path,Paint>> undonePaths = new ArrayList<>();

    Context context;
    String listCoordinate = "";
    String mUid = "";
    String channel_id;

    public ShareableCanvasView(Context context, String channel_id)
    {
        super(context);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.context = context;
        this.channel_id = channel_id;
        this.color = Color.BLACK;
        this.strokeWidth = 6;

        setPaintProperties();

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);

        mCanvas = new Canvas();
        mPath = new Path();
        paintTool = "freehand";

        databaseReference.child("shareable_canvas").child(channel_id).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ShareableItem newItem = dataSnapshot.getValue(ShareableItem.class);
                Log.d("addChild",dataSnapshot.toString());
                Log.d("newItem",newItem.getUid());
                if(!newItem.getUid().equals(mUid)){
                    addPathFromDatabase(newItem);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setPaintColor(int color){
        this.color = color;
        this.mPaint.setColor(color);
    }

    private void setPaintProperties(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //mPath = new Path();
        //canvas.drawPath(mPath, mPaint);
        //Log.d("ondraw",paths.size()+"");
        for (Pair<Path,Paint> p : paths){
            canvas.drawPath(p.first, p.second);
        }
        canvas.drawPath(mPath, mPaint);
    }


    public void setPaintTool(String paintTool){
        this.paintTool = paintTool;
    }

    private void addPathFromDatabase(ShareableItem newItem) {
        //get string that contain points from database and draw it to canvas
        if(newItem.getType().equals("freehand")){
            drawFreeHand(newItem);
        }
        else if(newItem.getType().equals("rectangle")){
            HashMap<String, Float> listPoint = stringToPoint(newItem.getPoints());
            drawRectangle(listPoint.get("left"),listPoint.get("top"),listPoint.get("right"),listPoint.get("bottom"), newItem.getShareablePaint().getPaint());
        }
        else if(newItem.getType().equals("circle")){
            HashMap<String, Float> listPoint = stringToPoint(newItem.getPoints());
            drawCircle(listPoint.get("left"),listPoint.get("top"),listPoint.get("right"),listPoint.get("bottom"), newItem.getShareablePaint().getPaint());
        }
        else if(newItem.getType().equals("line")){
            String[]listData = newItem.getPoints().split("<<");

            drawLine(Float.parseFloat(listData[0]),
                    Float.parseFloat(listData[1]),
                    Float.parseFloat(listData[2]),
                    Float.parseFloat(listData[3]),
                    newItem.getShareablePaint().getPaint());
        }

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

    private void drawFreeHand(ShareableItem newItem){
        String[] listPointString = newItem.getPoints().split("<<");
        Path newPath = new Path();
        Float newX = null, newY = null;
        for(int i = 0 ; i < listPointString.length ; i ++){
            Float x = Float.parseFloat(listPointString[i].split(",")[0]);
            Float y = Float.parseFloat(listPointString[i].split(",")[1]);

            if(i==0){
                newPath.moveTo(x,y);
                newX = x;
                newY = y;
            }
            else if (i==(listPointString.length-1)){
                newPath.lineTo(newX, newY);

                // commit the path to our offscreen
                mCanvas.drawPath(newPath, newItem.getShareablePaint().getPaint());
                paths.add(Pair.create(newPath,newItem.getShareablePaint().getPaint()));
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
            //listPointFloat.add(new Pair<>(x,y));
            invalidate();
        }
    }

    private void drawCircle(float left, float top,  float right, float bottom, Paint paint){
        Path newPath = new Path();
        newPath.addOval(left,top,right,bottom, Path.Direction.CCW);
        mCanvas.drawPath(newPath,paint);
        paths.add(Pair.create(newPath,paint));
        invalidate();
        setPaintProperties();
    }

    private void drawRectangle(float left, float top,  float right, float bottom, Paint paint ){
        Path newPath = new Path();
        newPath.addRect(left,top,right,bottom, Path.Direction.CCW);
        mCanvas.drawPath(newPath,paint);
        paths.add(Pair.create(newPath,paint));
        invalidate();
        setPaintProperties();
    }

    private void drawLine(float startX, float startY, float endX, float endY, Paint paint){
        Path newPath = new Path();
        newPath.moveTo(startX,startY);
        newPath.lineTo(endX,endY);
        mCanvas.drawPath(newPath,paint);
        paths.add(Pair.create(newPath,paint));
        invalidate();
        setPaintProperties();
    }


    private void touch_start(float x, float y) {
        undonePaths.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        listCoordinate =x+","+y+"<<";


    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            listCoordinate =listCoordinate+x+","+y+"<<";
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);

        // kill this so we don't double draw
        paths.add(Pair.create(mPath,mPaint));
        mPath = new Path();
        setPaintProperties();

        //push to database
        ShareableItem item = new ShareableItem(listCoordinate,paintTool, mUid,color,strokeWidth);
        databaseReference.child("shareable_canvas").child(channel_id).child("messages").push().setValue(item);

    }

    public void onClickUndo () {
        Log.d("undoclicked",paths.size()+"");
        if (paths.size()>0)
        {
            undonePaths.add(paths.remove(paths.size()-1));
            invalidate();

        }
    }

    public void onClickRedo (){
        Log.d("redoclicked",paths.size()+"");
        if (undonePaths.size()>0)
        {
            paths.add(undonePaths.remove(undonePaths.size()-1));
            invalidate();
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if(paintTool.equals("freehand")){

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
        }
        else if(paintTool.equals("rectangle")){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    undonePaths.clear();
                    mPath.reset();
                    startX = x;
                    startY = y;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    endX = x;
                    endY = y;
                    HashMap<String,Float> edgePoints = getEdgePoint();
                    drawRectangle(edgePoints.get("left"),edgePoints.get("top"),edgePoints.get("right"),edgePoints.get("bottom"),mPaint);
                    sendPolyData(edgePoints,paintTool);
                    invalidate();
                    break;
            }
        }

        else if(paintTool.equals("circle")){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    undonePaths.clear();
                    mPath.reset();
                    startX = x;
                    startY = y;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    endX = x;
                    endY = y;
                    HashMap<String,Float> edgePoints = getEdgePoint();
                    drawCircle(edgePoints.get("left"),edgePoints.get("top"),edgePoints.get("right"),edgePoints.get("bottom"),mPaint);
                    sendPolyData(edgePoints,paintTool);
                    invalidate();
                    break;
            }
        }
        else if(paintTool.equals("line")){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    undonePaths.clear();
                    mPath.reset();
                    startX = x;
                    startY = y;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    endX = x;
                    endY = y;
                    drawLine(startX,startY,endX,endY,mPaint);
                    sendLineData(startX,startY,endX,endY);
                    break;
            }
        }
        return true;
    }

    private void sendPolyData(HashMap<String,Float> edgePoints, String type){
        String edges = edgePoints.get("left")+"<<"+edgePoints.get("top")+"<<"+edgePoints.get("right")+"<<"+edgePoints.get("bottom");
        ShareableItem item = new ShareableItem(edges,type, mUid,color,strokeWidth);
        databaseReference.child("shareable_canvas").child(channel_id).child("messages").push().setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "failed to send data by type", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendLineData(float startX, float startY, float endX, float endY){
        String points = startX+"<<"+startY+"<<"+endX+"<<"+endY;
        ShareableItem item = new ShareableItem(points,"line",mUid,color,strokeWidth);
        databaseReference.child("shareable_canvas").child(channel_id).child("messages").push().setValue(item)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "failed to send data by type", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private HashMap<String,Float> getEdgePoint() {
        HashMap<String, Float> matchingFloat = new HashMap<>();

        if(startX<endX){
            matchingFloat.put("left",startX);
            matchingFloat.put("right",endX);
        }
        else {
            matchingFloat.put("left",endX);
            matchingFloat.put("right",startX);
        }

        if(startY<endY){
            matchingFloat.put("top",startY);
            matchingFloat.put("bottom",endY);
        }
        else {
            matchingFloat.put("top",endY);
            matchingFloat.put("bottom",startY);
        }
        return matchingFloat;
    }
}
