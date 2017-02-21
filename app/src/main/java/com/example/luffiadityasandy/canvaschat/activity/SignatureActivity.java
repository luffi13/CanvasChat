package com.example.luffiadityasandy.canvaschat.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.canvas_handler.DrawView;
import com.example.luffiadityasandy.canvaschat.canvas_handler.ShareableCanvasView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SignatureActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseStorage storage;

    FirebaseAuth.AuthStateListener mAuthListener;

    ShareableCanvasView canvasController;
    LinearLayout canvas;
    View mView;
    Button undo, redo, save;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Draw SignatureActivity");

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user !=null){

                }else {
                    mAuth.createUserWithEmailAndPassword("luffi.as@gmail.com","bismillah3kali")
                            .addOnCompleteListener(SignatureActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        Toast.makeText(SignatureActivity.this, "auth failed", Toast.LENGTH_SHORT).show();
                                        Log.d("failedauth",task.getException().toString());
                                    }
                                    else
                                        Toast.makeText(SignatureActivity.this, "success auth", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        };

        undo = (Button)findViewById(R.id.undo);
        redo = (Button)findViewById(R.id.redo);
        save = (Button)findViewById(R.id.saveCanvas) ;

        verifyLocationPermissions(this);

        canvas = (LinearLayout)findViewById(R.id.myCanvas);
        canvasController = new ShareableCanvasView(this);
        canvasController.setBackgroundColor(Color.WHITE);
        canvas.addView(canvasController, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mView = canvas;
        undo.setOnClickListener(clickHandler);
        redo.setOnClickListener(clickHandler);
        save.setOnClickListener(clickHandler);

        //storage to upload bitmap
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://canvaschat-e41e4.appspot.com");

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private View.OnClickListener clickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.undo:
                    canvasController.onClickUndo();
                    break;
                case R.id.redo:
                    canvasController.onClickRedo();
                    break;
                case R.id.saveCanvas:
                    //Toast.makeText(SignatureActivity.this, "saved", Toast.LENGTH_SHORT).show();
                    uploadDataByte(mView);
                    break;
            }
        }
    };

    private void saveCanvas(View v) {
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);

        // path to /Android/data
        File directory = getOutputMediaFile();
        Log.d("imagedir",directory.toString());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(directory);
            // Use the compress method on the BitMap object to write image to the OutputStream
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void uploadDataByte(View view){

        StorageReference imageDir = storageReference.child("image");
        StorageReference imageRef = imageDir.child("canvas.jpg");
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();

        Bitmap b = Bitmap.createBitmap( view.getLayoutParams().width, view.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[]data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("please wait....");
        progressDialog.show();
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SignatureActivity.this, "failed upload", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri uri = taskSnapshot.getDownloadUrl();
                Log.d("uriDownload",uri.toString());
                progressDialog.dismiss();
                Toast.makeText(SignatureActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    private static void verifyLocationPermissions(Activity activity) {
        int permission;
        String[] PERMISSIONS_LOCATION = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        // Check if we have write permission
        permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_LOCATION,
                    1
            );
        }
    }


}
