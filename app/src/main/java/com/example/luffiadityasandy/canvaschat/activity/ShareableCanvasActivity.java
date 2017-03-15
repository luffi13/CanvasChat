package com.example.luffiadityasandy.canvaschat.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.canvas_handler.DrawView;
import com.example.luffiadityasandy.canvaschat.canvas_handler.ShareableCanvasView;
import com.example.luffiadityasandy.canvaschat.object.GCMRequest;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.service.InstanceIdService;
import com.example.luffiadityasandy.canvaschat.service.ServiceMessaging;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import yuku.ambilwarna.AmbilWarnaDialog;

public class ShareableCanvasActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    User receiver;
    String channel_id = "";

    LinearLayout canvas;
    View mView;
    ShareableCanvasView canvasController;
    Button undo, redo, save ,
            freehand_btn, circle_btn, rectangle_btn, line_btn, invite_btn,
            red_btn, blue_btn;

    AmbilWarnaDialog colorPickerDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shareable_canvas);

        //Firebase get user and database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        receiver = (User)getIntent().getSerializableExtra("receiver");

        //get button from UI
        undo = (Button)findViewById(R.id.undo);
        redo = (Button)findViewById(R.id.redo);
        save = (Button)findViewById(R.id.saveCanvas) ;
        rectangle_btn = (Button)findViewById(R.id.rectangle_btn);
        circle_btn= (Button)findViewById(R.id.circle_btn);
        freehand_btn = (Button)findViewById(R.id.freehand_btn);
        line_btn = (Button)findViewById(R.id.line_btn);
        invite_btn = (Button) findViewById(R.id.invite_btn);
        canvas = (LinearLayout)findViewById(R.id.myCanvas);

        //color
        red_btn  = (Button)findViewById(R.id.red_btn);
        blue_btn = (Button)findViewById(R.id.blue_btn);



        //set listener for button click
        undo.setOnClickListener(clickHandler);
        redo.setOnClickListener(clickHandler);
        save.setOnClickListener(clickHandler);
        rectangle_btn.setOnClickListener(clickHandler);
        circle_btn.setOnClickListener(clickHandler);
        line_btn.setOnClickListener(clickHandler);
        freehand_btn.setOnClickListener(clickHandler);
        invite_btn.setOnClickListener(clickHandler);

        //set color click
        red_btn.setOnClickListener(colorHandler);
        blue_btn.setOnClickListener( colorHandler);

        //
        getChannel();
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
                    //uploadDataByte(mView);
                    break;
                case R.id.rectangle_btn:
                    canvasController.setPaintTool("rectangle");
                    break;
                case R.id.circle_btn:
                    canvasController.setPaintTool("circle");
                    break;
                case R.id.freehand_btn:
                    canvasController.setPaintTool("freehand");
                    break;
                case R.id.line_btn:
                    canvasController.setPaintTool("line");
                    break;
                case R.id.invite_btn:
                    sendNotification();
                    break;

            }
        }
    };

    private View.OnClickListener colorHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.red_btn:
                    changeColor(Color.RED);
                    break;
                case R.id.blue_btn:
                    openDialog(false);
                    break;
            }
        }
    };

    void openDialog(boolean supportsAlpha) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(ShareableCanvasActivity.this, Color.BLACK, supportsAlpha, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
                canvasController.setPaintColor(color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void changeColor(int color){
        canvasController.setPaintColor(color);
    }

    private void deleteDatabase(){
        databaseReference.child("coba_delete").orderByChild("v1").equalTo("1.1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data:dataSnapshot.getChildren()) {
                    Log.d("data",data.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //get channel to push message in firebase database
    private void getChannel(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Connecting");
        progressDialog.setMessage("please wait..");
        progressDialog.show();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(ShareableCanvasActivity.this, "connection canceled", Toast.LENGTH_SHORT).show();
            }
        });
        databaseReference.child("channels/"+firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiver.getUid())){
//                    databaseReference.child("user_detail").child(firebaseUser.getUid()).setValue(new User(
//                            firebaseUser.getUid(),firebaseUser.getEmail(),firebaseUser.getDisplayName(), FirebaseInstanceId.getInstance().getToken(),firebaseUser.getPhotoUrl().toString()
//                    ));
                    Toast.makeText(ShareableCanvasActivity.this, "channel ready"+dataSnapshot.child(receiver.getUid()).getValue(), Toast.LENGTH_SHORT).show();
                    channel_id = dataSnapshot.child(receiver.getUid()).getValue().toString()+"_sc";
                }
                else {
                    HashMap<String , Object> selfChannel = new HashMap<>();
                    selfChannel.put(receiver.getUid(),firebaseUser.getUid()+receiver.getUid());
                    databaseReference.child("channels/").child(firebaseUser.getUid()).updateChildren(selfChannel);

                    HashMap <String , Object> receiverChannel = new HashMap<>();
                    receiverChannel.put(firebaseUser.getUid(),firebaseUser.getUid()+receiver.getUid());
                    databaseReference.child("channels/").child(receiver.getUid()).updateChildren(receiverChannel);
                    Toast.makeText(ShareableCanvasActivity.this, "channel created ", Toast.LENGTH_SHORT).show();
                    channel_id = firebaseUser.getUid()+receiver.getUid()+"_sc";
                }
                prepareCanvasView();
                progressDialog.dismiss();
                //Log.d("channel",channel_id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    //prepare canvas by set background color and set size of canvas
    public void prepareCanvasView(){
        canvasController = new ShareableCanvasView(this,channel_id);
        canvasController.setBackgroundColor(Color.WHITE);
        canvas.addView(canvasController,canvas.getWidth(),canvas.getHeight());
        Log.d("size canvas", canvas.getWidth()+" "+canvas.getHeight());
        mView = canvas;
        setMyCanvasSize(canvas.getWidth(),canvas.getHeight());
//        getReceiverCanvas();
    }


    //save our screen size in database for scaling
    private void setMyCanvasSize(Integer width, Integer height){
        HashMap<String,Integer> myScreenSize = new HashMap<>();
        myScreenSize.put("width",width);
        myScreenSize.put("height",height);
        canvasController.setMyScrenSize(myScreenSize);
        databaseReference.child("shareable_canvas/"+channel_id+"/screensize/"+firebaseUser.getUid()).setValue(myScreenSize);
    }

    //get receiver screen size
    private void getReceiverCanvas(){
        databaseReference.child("shareable_canvas/"+channel_id+"/screensize/"+firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()==null){
                            canvasController.setReceiverScreenSize(canvasController.getMyScrenSize());
                            return;
                        }
                        HashMap<String,Integer> receiverScreenSize = new HashMap<>();
                        receiverScreenSize.put("width",Integer.parseInt(dataSnapshot.child("width").getValue()+""));
                        receiverScreenSize.put("height",Integer.parseInt(dataSnapshot.child("height").getValue()+""));
                        canvasController.setReceiverScreenSize(receiverScreenSize);
                        Log.d("receiverScreen", receiverScreenSize.get("width")+" "+receiverScreenSize.get("height"));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    //invite friend to chat with us
    private void sendNotification (){

        if(receiver.getToken().equals(""))
            return;

        final ProgressDialog progressDialog = new ProgressDialog(ShareableCanvasActivity.this);
        progressDialog.setTitle("Sending Notification");
        progressDialog.setMessage("please wait....");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://fcm.googleapis.com").addConverterFactory(GsonConverterFactory.create()).build();
        Gson gson = new Gson();
        Type type = new TypeToken<GCMRequest>() {}.getType();
        GCMRequest gcmRequest = new GCMRequest(receiver.getToken()
                ,new User(firebaseUser.getUid(),firebaseUser.getEmail(), firebaseUser.getDisplayName(), FirebaseInstanceId.getInstance().getToken()+"",firebaseUser.getPhotoUrl().toString())
                ,"shareable_canvas");

        Type jsonObjectType = new TypeToken<JsonObject>(){}.getType();
        JsonObject jsonData = gson.fromJson(gson.toJson(gcmRequest,type),jsonObjectType);
        Log.d("jsonData",jsonData.toString());

        ServiceMessaging serviceMessaging = retrofit.create(ServiceMessaging.class);
        Call<JsonElement> sendNotification = serviceMessaging.sendNotification("key=AAAAMcvxv1U:APA91bHeOlyavQ32g0sFldoXUmKI_xD0EbA5q5y-3nebcpQCFk8vVM_W0BCSeueL2_FHf4ya_K7kksAfn10qSGiKmH0bRnGBEwOJU2YREbS2st0ybU37SHsnqhCIXV_-TSpQY62pWMF1",
                "application/json",jsonData);


        sendNotification.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                Log.d("responseNotif",response.body().toString());
                Integer isSuccess = response.body().getAsJsonObject().get("success").getAsInt();
                Log.d("isSuccess", isSuccess+"");
                if(isSuccess==1){
                    Toast.makeText(ShareableCanvasActivity.this, "invitation sent", Toast.LENGTH_SHORT).show();
                    invite_btn.setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("responseNotif","failed");
                Toast.makeText(ShareableCanvasActivity.this, "failed invite", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                progressDialog.dismiss();
            }
        });
    }


}
