package com.example.luffiadityasandy.canvaschat.canvas_handler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.example.luffiadityasandy.canvaschat.object.ShareableItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Luffi Aditya Sandy on 21/02/2017.
 */

public class ShareableCanvasView extends View implements View.OnTouchListener  {

    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private DatabaseReference databaseReference;

    ArrayList<String> listInFloat = new ArrayList<>();

    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();

    Context context;
    String listCoordinate = "";
    String mUid = "";

    public ShareableCanvasView(Context context)
    {
        super(context);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.context = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(6);
        mCanvas = new Canvas();
        mPath = new Path();

        databaseReference.child("shareable_canvas").addChildEventListener(new ChildEventListener() {
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

    private void addPathFromDatabase(ShareableItem newItem) {
        //extract point to list point
        String[] listPointString = newItem.getPoints().split("<<");
        Float newX = null, newY = null;
        Path newPath = new Path();

        ArrayList<Pair<Float,Float>> listPointFloat= new ArrayList<>();
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
                mCanvas.drawPath(newPath, mPaint);
                paths.add(newPath);
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


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //mPath = new Path();
        //canvas.drawPath(mPath, mPaint);
        //Log.d("ondraw",paths.size()+"");
        for (Path p : paths){
            canvas.drawPath(p, mPaint);
        }
        canvas.drawPath(mPath, mPaint);
    }


    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

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
        Log.d("listcoordinate",listInFloat.toString());
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        paths.add(mPath);
        mPath = new Path();

        //push to database

        ShareableItem item = new ShareableItem(listCoordinate,"free_hand", FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.child("shareable_canvas").push().setValue(item);

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
        return true;
    }
}
