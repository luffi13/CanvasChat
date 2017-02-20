package com.example.luffiadityasandy.canvaschat.canvas_handler;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


/**
 * Created by Luffi on 03/01/2017.
 */

public class DrawView  extends View implements View.OnTouchListener {
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    HashMap<Path,CoordinateHolder> listCoordinate;

    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();
    private ArrayList<Float> listxAxis;
    private ArrayList<Float> listyAxis;

    public static String tempDir;

    Context context;
    File savePath;
    String path_image;


    private Bitmap mBitmap;
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

        //instansiasi list coordinate
        listCoordinate = new HashMap<>();

        //inistalisasi axisholder
        listxAxis = new ArrayList<>();
        listyAxis = new ArrayList<>();

        mBitmap= BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        //prepare destination path
//        this.tempDir = "storage/emulated/0/DCIM/canvas" ;
//        Log.d("tempir",tempDir.toString());
//        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
//        File directory = cw.getDir("canvas", Context.MODE_PRIVATE);

//        prepareDirectory();
//        String uniqueId = Calendar.getInstance().getTime().getDate() + "_" + Calendar.getInstance().getTime().getHours() + "_" + Math.random();
//        String current = uniqueId + ".png";
//        savePath = new File(directory,current);



    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //mPath = new Path();
        //canvas.drawPath(mPath, mPaint);
        Log.d("ondraw",paths.size()+"");
        for (Path p : paths){
            canvas.drawPath(p, mPaint);
        }
        canvas.drawPath(mPath, mPaint);
    }

    public void saveCanvas(View v)
    {
        Log.d("log_tag", "Width: " + v.getWidth());
        Log.d("log_tag", "Height: " + v.getHeight());
        if(mBitmap == null)
        {
            mBitmap =  Bitmap.createBitmap (this.getWidth(), this.getHeight(), Bitmap.Config.RGB_565);;
        }
        Canvas canvas = new Canvas(mBitmap);
        try
        {
            ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
            File directory = cw.getDir("canvas", Context.MODE_PRIVATE);
            FileOutputStream mFileOutStream = new FileOutputStream(new File(directory,"result.jpg"));

            v.draw(canvas);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, mFileOutStream);

            mFileOutStream.flush();
            mFileOutStream.close();

            path_image = MediaStore.Images.Media.insertImage(context.getContentResolver(), mBitmap, "title", null);
            Log.v("log_tag","url: " + path_image);
            //In case you want to delete the file
            //boolean deleted = mypath.delete();
            //Log.v("log_tag","deleted: " + mypath.toString() + deleted);
            //If you want to convert the image to string use base64 converter

        }
        catch(Exception e)
        {
            Log.v("log_tag", e.toString());
        }
    }

    private boolean prepareDirectory()
    {
        try
        {
            if (makedirs())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(context, "Could not initiate File System.. Is Sdcard mounted properly?", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean makedirs()
    {
        File tempdir = new File(this.tempDir);
        if (!tempdir.exists())
            tempdir.mkdirs();

        if (tempdir.isDirectory())
        {
            File[] files = tempdir.listFiles();
            for (File file : files)
            {
                if (!file.delete())
                {
                    System.out.println("Failed to delete " + file);
                }
            }
        }
        return (tempdir.isDirectory());
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        undonePaths.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        listxAxis.clear();
        listyAxis.clear();
        listxAxis.add(x);
        listyAxis.add(y);


    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            listxAxis.add(x);
            listyAxis.add(y);
        }
    }
    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        paths.add(mPath);
        listCoordinate.put(mPath,new CoordinateHolder(listxAxis,listyAxis));
        mPath = new Path();
        listxAxis = new ArrayList<>();
        listyAxis = new ArrayList<>();


    }

    public void onClickUndo () {
        Log.d("undoclicked",paths.size()+"");
        if (paths.size()>0)
        {
            undonePaths.add(paths.remove(paths.size()-1));
            invalidate();

        }
        else
        {

        }
        //toast the user
    }

    public void onClickRedo (){
        Log.d("redoclicked",paths.size()+"");
        if (undonePaths.size()>0)
        {
            paths.add(undonePaths.remove(undonePaths.size()-1));
            invalidate();
        }
        else
        {

        }
        //toast the user
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

    private class CoordinateHolder{
        ArrayList<Float> xAxis, yAxis;

        public CoordinateHolder(ArrayList<Float> xAxis, ArrayList<Float> yAxis) {
            this.xAxis = new ArrayList<>();
            this.yAxis = new ArrayList<>();
            this.xAxis = xAxis;
            this.yAxis = yAxis;

        }
    }
}
