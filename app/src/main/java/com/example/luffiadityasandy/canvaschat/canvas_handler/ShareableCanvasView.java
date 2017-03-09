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
import com.example.luffiadityasandy.canvaschat.object.ShareablePath;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    String tempKey = "";


    private ArrayList<ShareableItem> paths = new ArrayList<>();
    private ArrayList<ShareableItem> myPath = new ArrayList<>();
    private ArrayList<ShareableItem> undonePaths = new ArrayList<>();
    private HashMap<String,ShareableItem> referencePath = new HashMap<>();

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

        generatePushKey();
        setPaintProperties();

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);

        mCanvas = new Canvas();
        mPath = new Path();
        paintTool = "freehand";


        initCanvas();

        databaseReference.child("shareable_canvas").child(channel_id).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ShareableItem newItem = dataSnapshot.getValue(ShareableItem.class);
                Log.d("addChild",dataSnapshot.getValue().toString());
                if(!newItem.getUid().equals(mUid)){
                    addPathFromDatabase(newItem);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("onChildChanged",dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                ShareableItem removedItem = dataSnapshot.getValue(ShareableItem.class);
                Log.d("removeDetected",removedItem.getKey());
                ShareableItem removedItemInLocal = referencePath.get(removedItem.getKey());
                if(removedItemInLocal==null){
                    return;
                }
                Log.d("removediteminlocal",removedItemInLocal.getKey());
                //paths.remove(removedItemInLocal);
                if(paths.contains(removedItemInLocal)){

                    paths.remove(removedItemInLocal);
                    Log.d("containremoved", "true");
                }
                referencePath.remove(removedItem.getKey());
                invalidate();


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    void initCanvas(){
        databaseReference.child("shareable_canvas").child(channel_id).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d("init data",dataSnapshot.getValue().toString());

                for (DataSnapshot child : dataSnapshot.getChildren()){
                    ShareableItem shareableItem = child.getValue(ShareableItem.class);
                    if(shareableItem.getUid().equals(mUid)){
                        addPathFromDatabase(shareableItem);
                        myPath.add(shareableItem);
                    }
                }
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
        for (ShareableItem p : paths){
            canvas.drawPath(p.getPath(), p.getShareablePaint().getPaint());
        }
        canvas.drawPath(mPath, mPaint);
    }


    public void setPaintTool(String paintTool){
        this.paintTool = paintTool;
    }

    private void addPathFromDatabase(ShareableItem newItem) {
        //get string that contain points from database and draw it to canvas
        mCanvas.drawPath(newItem.getPath(), newItem.getShareablePaint().getPaint());
        paths.add(newItem);
        referencePath.put(newItem.getKey(),newItem);
        invalidate();

    }

    private void generatePushKey(){
        this.tempKey = databaseReference.child("shareable_canvas").child(channel_id).child("messages").push().getKey();

    }





    private void freehandStart(float x, float y) {
        undonePaths.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        listCoordinate =x+","+y+"<<";
    }
    private void freehandMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            listCoordinate =listCoordinate+x+","+y+"<<";
        }
    }

    private  ShareableItem freehandFinish() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);

        ShareableItem item = new ShareableItem(listCoordinate,paintTool, mUid,color,strokeWidth,tempKey);
        paths.add(item);
        mPath = new Path();
        setPaintProperties();

        //add to listKeyPath\
        myPath.add(item);
        generatePushKey();
        return item;
    }

    public void onClickUndo () {
        Log.d("undoclicked",paths.size()+"");
        if (myPath.size()>0)
        {
            ShareableItem deletedPath = myPath.remove(myPath.size()-1);
            paths.remove(deletedPath);
            undonePaths.add(deletedPath);
            referencePath.remove(deletedPath);
            Log.d("deletedKey",deletedPath.getKey());
            deleteFirebase(deletedPath.getKey());
            invalidate();
        }
    }

    public void deleteFirebase(String key){
        Log.d("deletedKey",key);
        FirebaseDatabase.getInstance().getReference("shareable_canvas/"+channel_id+"/messages/"+key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("deleteFirebase",dataSnapshot.getValue().toString());
                dataSnapshot.getRef().setValue(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onClickRedo (){
        Log.d("redoclicked",paths.size()+"");
        if (undonePaths.size()>0)
        {
            ShareableItem redoPath = undonePaths.remove(undonePaths.size()-1);
            paths.add(redoPath);
            myPath.add(redoPath);
            sendNewData(redoPath);
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
                    freehandStart(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    freehandMove(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    sendNewData(freehandFinish());
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
                    sendNewData(drawRectangle());
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
                    sendNewData(drawCircle());
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
                    sendNewData(drawLine());
                    break;
            }
        }
        return true;
    }

    private ShareableItem drawCircle(){
        Path newPath = new Path();
        HashMap<String,Float> edgePoints = getEdgePoint();
        newPath.addOval(edgePoints.get("left"),edgePoints.get("top"),
                edgePoints.get("right"),edgePoints.get("bottom"),
                Path.Direction.CCW);
        mCanvas.drawPath(newPath,mPaint);
        String pointsInString = generatePointToString(edgePoints);
        ShareableItem item = new ShareableItem(pointsInString,"circle",mUid,color,strokeWidth,tempKey);
        paths.add(item);
        invalidate();
        setPaintProperties();
        generatePushKey();
        return item;
    }

    private ShareableItem drawRectangle(){
        Path newPath = new Path();
        HashMap<String,Float> edgePoints = getEdgePoint();
        newPath.addRect(edgePoints.get("left"),edgePoints.get("top"),
                edgePoints.get("right"),edgePoints.get("bottom"),
                Path.Direction.CCW);
        mCanvas.drawPath(newPath,mPaint);
        String pointsInString = generatePointToString(edgePoints);
        ShareableItem item = new ShareableItem(pointsInString,"rectangle",mUid,color,strokeWidth,tempKey);
        paths.add(item);
        invalidate();
        setPaintProperties();
        generatePushKey();
        return item;
    }

    private ShareableItem drawLine(){
        Path newPath = new Path();
        newPath.moveTo(startX,startY);
        newPath.lineTo(endX,endY);
        mCanvas.drawPath(newPath,mPaint);

        String pointsInString = startX+"<<"+startY+"<<"+endX+"<<"+endY;
        ShareableItem item = new ShareableItem(pointsInString,"line",mUid,color,strokeWidth,tempKey);
        paths.add(item);
        invalidate();
        setPaintProperties();
        generatePushKey();
        return item;
    }

    private String generatePointToString(HashMap<String,Float> edgePoints){
        return edgePoints.get("left")+"<<"+edgePoints.get("top")+"<<"+edgePoints.get("right")+"<<"+edgePoints.get("bottom");
    }


    private void sendNewData(ShareableItem item){
        databaseReference.child("shareable_canvas").child(channel_id).child("messages").child(item.getKey()).setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
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
