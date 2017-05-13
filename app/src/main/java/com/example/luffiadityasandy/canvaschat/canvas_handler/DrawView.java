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
import android.util.Pair;
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
    private static final float TOUCH_TOLERANCE = 4;

    private float mX, mY;
    private float startX, startY, endX, endY;
    private int color;
    String paintTool;
    private int strokeWidth;

    private ArrayList<Pair<Path,Paint>> paths = new ArrayList<>();
    private ArrayList<Pair<Path,Paint>> undonePaths = new ArrayList<>();
    HashMap<String,Void> listFunction;


    Context context;
    String listCoordinate = "";

    public DrawView(Context context)
    {
        super(context);
        this.context = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        this.color = Color.BLACK;
        this.strokeWidth = 6;

        setPaintProperties();

        mCanvas = new Canvas();
        mPath = new Path();
        paintTool = "freehand";
        listFunction = new HashMap<>();
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

    public void setPaintColor(int color){
        this.color = color;
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
        Paint newPaint = new Paint();
        newPaint = mPaint;
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        paths.add(Pair.create(mPath,newPaint));
        setPaintProperties();
        mPath = new Path();
    }

    private void drawRectangle(){

        Path newPath = new Path();
        Paint newPaint = new Paint();
        newPaint = mPaint;
        HashMap<String,Float> edgePoints = getEdgePoint();
        newPath.addRect(edgePoints.get("left"),edgePoints.get("top"),
                edgePoints.get("right"),edgePoints.get("bottom"),
                Path.Direction.CCW);
        mCanvas.drawPath(newPath,mPaint);
        paths.add(Pair.create(newPath,newPaint));
        invalidate();
        setPaintProperties();
    }

    private void drawCircle(){
        Path newPath = new Path();
        Paint newPaint = new Paint();
        newPaint = mPaint;
        HashMap<String,Float> edgePoints = getEdgePoint();
        newPath.addOval(edgePoints.get("left"),edgePoints.get("top"),
                edgePoints.get("right"),edgePoints.get("bottom"),
                Path.Direction.CCW);
        mCanvas.drawPath(newPath,mPaint);
        paths.add(Pair.create(newPath,newPaint));
        invalidate();
        setPaintProperties();
    }

    private void drawLine(){
        Path newPath = new Path();
        Paint newPaint = new Paint();
        newPaint = mPaint;
        newPath.moveTo(startX,startY);
        newPath.lineTo(endX,endY);
        mCanvas.drawPath(newPath,mPaint);
        paths.add(Pair.create(newPath,newPaint));
        invalidate();
        setPaintProperties();
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
                    drawRectangle();
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
                    drawCircle();
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
                    drawLine();
                    break;
            }
        }
        return true;
    }

}
