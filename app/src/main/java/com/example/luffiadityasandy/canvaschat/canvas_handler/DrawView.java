package com.example.luffiadityasandy.canvaschat.canvas_handler;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.object.ShareableItem;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Luffi on 03/01/2017.
 */

public class DrawView  extends View implements View.OnTouchListener {
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private float startX, startY, endX, endY;
    private static final float TOUCH_TOLERANCE = 4;

    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();
    HashMap<String,Void> listFunction;

    String paintTool;

    Context context;
    String listCoordinate = "";

    public DrawView(Context context)
    {
        super(context);
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
        paintTool = "freehand";
        listFunction = new HashMap<>();
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

    public void setPaintColor(int color){
        mPaint.setColor(color);
    }

    public void setPaintTool(String tool){
        this.paintTool = tool;
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
        Log.d("coordinate",x +" "+y);
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
        paths.add(mPath);
        mPath = new Path();
    }

    private void drawRectangle(){

        float top, bottom, left, right;

        if(startX<endX){
            left = startX;
            right = endX;
        }
        else {
            left = endX;
            right = startX;
        }

        if(startY<endY){
            top = startY;
            bottom = endY;
        }
        else {
            top = endY;
            bottom = startY;
        }

        mPath.addRect(new RectF(left,top,right,bottom), Path.Direction.CCW);
        mCanvas.drawPath(mPath,mPaint);
        paths.add(mPath);
        mPath = new Path();
    }

    private void drawCircle(){
        float top, bottom, left, right;

        if(startX<endX){
            left = startX;
            right = endX;
        }
        else {
            left = endX;
            right = startX;
        }

        if(startY<endY){
            top = startY;
            bottom = endY;
        }
        else {
            top = endY;
            bottom = startY;
        }
        mPath.addOval(left,top,right,bottom, Path.Direction.CCW);
        mCanvas.drawPath(mPath,mPaint);
        paths.add(mPath);
        mPath = new Path();
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

        if(paintTool == "freehand"){
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

        else if(paintTool == "rectangle"){
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
                    drawRectangle();
                    invalidate();
                    break;
            }
        }

        else if(paintTool == "circle"){
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
                    drawCircle();
                    invalidate();
                    break;
            }
        }

        return true;
    }

}
