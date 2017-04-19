package com.example.luffiadityasandy.canvaschat.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.object.GCMRequest;
import com.example.luffiadityasandy.canvaschat.service.ServiceMessaging;
import com.example.luffiadityasandy.canvaschat.view_holder.MessageHolder;
import com.example.luffiadityasandy.canvaschat.adapter.ListMessageAdapter;
import com.example.luffiadityasandy.canvaschat.canvas_handler.DrawView;
import com.example.luffiadityasandy.canvaschat.object.Message;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.view_holder.OfflineCanvasHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.realm.RealmObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import yuku.ambilwarna.AmbilWarnaDialog;

public class OfflineCanvasChatActvity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private User receiver;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private LinearLayoutManager linearLayoutManager;
    String channel_id;
    RecyclerView recyclerView;
    ListMessageAdapter listMessageAdapter;
    EditText textMessage_et;
    ImageView sendText_btn;

    DrawView canvasController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_canvas_chat_actvity);

        receiver = (User)getIntent().getSerializableExtra("receiver");
        //Toast.makeText(this, receiver.getName()+"\n"+receiver.getEmail()+"\n"+receiver.getUid()+"\n"+receiver.getToken()+"\n", Toast.LENGTH_SHORT).show();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getChannel();

        ImageView createCanvas_btn = (ImageView)findViewById(R.id.createCanvas_btn);
        recyclerView = (RecyclerView)findViewById(R.id.messageList_rv);
        textMessage_et = (EditText)findViewById(R.id.textMessage_et);
        sendText_btn = (ImageView)findViewById(R.id.sendText_btn) ;

        createCanvas_btn.setOnClickListener(clickHandler);
        sendText_btn.setOnClickListener(clickHandler);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        //try to get last message
        //FloatingActionButton lastMessage_btn = (FloatingActionButton)findViewById(R.id.lastMessage_btn);
        //lastMessage_btn.setOnClickListener(clickHandler);

        //set user state to ever chat
        setIsEverChat();
    }

    private View.OnClickListener clickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.createCanvas_btn:
                    popupCanvas();
                    break;
                case R.id.sendText_btn:
                    sendTextMessage();
                    break;
//                case R.id.lastMessage_btn:
//                    iterateLastMessage();
//                    break;
            }
        }
    };

    private void iterateLastMessage(){
        ArrayList<Message> lastMessage = listMessageAdapter.getLastMessages(2);
        for (int i = 0; i<lastMessage.size();i++){
            Log.d("lastMessage", "iterateLastMessage: "+lastMessage.get(i).getMessage());
        }
    }

    private void setIsEverChat(){
        HashMap <String, Object> data = new HashMap<>();
        data.put("isEverChat",true);
        databaseReference.child("friendship/"+firebaseUser.getUid()+"/"+receiver.getUid()).updateChildren(data);
        databaseReference.child("friendship/"+receiver.getUid()+"/"+firebaseUser.getUid()).updateChildren(data);
    }

    private void popupCanvas(){
        final PopupWindow popupWindow;
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.create_canvas,(ViewGroup)findViewById(R.id.popup_canvas));
        popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(layout, Gravity.CENTER,0,0);

        OfflineCanvasHolder offlineCanvasHolder = new OfflineCanvasHolder(layout);
        canvasController = new DrawView(this);
        canvasController.setBackgroundColor(Color.WHITE);

        offlineCanvasHolder.canvas.addView(canvasController, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        final View mView = offlineCanvasHolder.canvas;

        offlineCanvasHolder.undo_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.redo_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.colorPicker_btn.setOnClickListener(colorHandler);

        offlineCanvasHolder.rectangle_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.circle_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.line_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.freehand_btn.setOnClickListener(buttonHandler);

        offlineCanvasHolder.colorPicker_btn.setOnClickListener( colorHandler);
        offlineCanvasHolder.send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadCanvas(mView,popupWindow);
            }
        });
    }

    View.OnClickListener buttonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.undo_btn:
                    canvasController.onClickUndo();
                    break;
                case R.id.redo_btn:
                    canvasController.onClickRedo();
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
            }
        }
    };

    private View.OnClickListener colorHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.colorPicker_btn:
                    openDialog(false);
                    break;
            }
        }
    };

    void openDialog(boolean supportsAlpha) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(OfflineCanvasChatActvity.this, Color.BLACK, supportsAlpha, new AmbilWarnaDialog.OnAmbilWarnaListener() {
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

    private void showCanvas(){

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.create_canvas,null);
        AlertDialog.Builder canvasDialog = new AlertDialog.Builder(this);
        canvasDialog.setView(view);

        Button undo = (Button)view.findViewById(R.id.undo_btn);
        Button redo = (Button)view.findViewById(R.id.redo_btn);


        LinearLayout canvas_ll = (LinearLayout)view.findViewById(R.id.myCanvas);
        final DrawView canvasController = new DrawView(this);
        canvasController.setBackgroundColor(Color.WHITE);
        canvas_ll.addView(canvasController, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        final View mView = canvas_ll;

        View.OnClickListener buttonHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.undo_btn:
                        canvasController.onClickUndo();
                        break;
                    case R.id.redo_btn:
                        canvasController.onClickRedo();
                        break;
                }
            }
        };

        undo.setOnClickListener(buttonHandler);
        redo.setOnClickListener(buttonHandler);

        canvasDialog.setCancelable(false)
                .setPositiveButton("send",null)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                    }
                });
        final AlertDialog alertDialog = canvasDialog.create();
        alertDialog.show();
//        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                uploadCanvas(mView, alertDialog);
//            }
//        });

    }

    private void sendTextMessage(){
        HashMap<String, Object> newMessage = new HashMap<>();
        newMessage.put("message",textMessage_et.getText().toString());
        newMessage.put("type","text");
        newMessage.put("time", ServerValue.TIMESTAMP);
        newMessage.put("sender",firebaseUser.getDisplayName());
        databaseReference.child("messages").child(channel_id).push().setValue(newMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sendNotification();
                textMessage_et.setText("");
            }
        });

    }

    private void uploadCanvas(View view, final PopupWindow popupWindow){

        StorageReference imageDir = storageReference.child("image");
        StorageReference imageRef = imageDir.child(firebaseUser.getUid())
                .child(+Calendar.getInstance().get(Calendar.MILLISECOND)+".jpg");
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();

        Bitmap b = Bitmap.createBitmap( view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
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
                Toast.makeText(OfflineCanvasChatActvity.this, "failed upload", Toast.LENGTH_SHORT).show();
                e.printStackTrace();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri uri = taskSnapshot.getDownloadUrl();
                Log.d("uriDownload",uri.toString());
                HashMap<String, Object> message = new HashMap<>();
                message.put("message",uri.toString());
                message.put("type","image");
                message.put("time", ServerValue.TIMESTAMP);
                message.put("sender",firebaseUser.getDisplayName());
                databaseReference.child("messages").child(channel_id).push().setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendNotification();
                    }
                });

                progressDialog.dismiss();
                Toast.makeText(OfflineCanvasChatActvity.this, uri.toString(), Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });


    }

    private void getChannel(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Connecting");

        progressDialog.setMessage("please wait..");
        progressDialog.show();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(OfflineCanvasChatActvity.this, "connection canceled", Toast.LENGTH_SHORT).show();
            }
        });
        databaseReference.child("channels/"+firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiver.getUid())){
//                    databaseReference.child("user_detail").child(firebaseUser.getUid()).setValue(new User(
//                            firebaseUser.getUid(),firebaseUser.getEmail(),firebaseUser.getDisplayName(), FirebaseInstanceId.getInstance().getToken(),firebaseUser.getPhotoUrl().toString()
//                    ));
                    Toast.makeText(OfflineCanvasChatActvity.this, "channel ready"+dataSnapshot.child(receiver.getUid()).getValue(), Toast.LENGTH_SHORT).show();
                    channel_id = dataSnapshot.child(receiver.getUid()).getValue().toString();
                }
                else {
                    HashMap <String , Object> selfChannel = new HashMap<>();
                    selfChannel.put(receiver.getUid(),firebaseUser.getUid()+receiver.getUid());
                    databaseReference.child("channels/").child(firebaseUser.getUid()).updateChildren(selfChannel);

                    HashMap <String , Object> receiverChannel = new HashMap<>();
                    receiverChannel.put(firebaseUser.getUid(),firebaseUser.getUid()+receiver.getUid());
                    databaseReference.child("channels/").child(receiver.getUid()).updateChildren(receiverChannel);
                    Toast.makeText(OfflineCanvasChatActvity.this, "channel created ", Toast.LENGTH_SHORT).show();
                    channel_id = firebaseUser.getUid()+receiver.getUid();
                }
                progressDialog.dismiss();
                setAdapter();
                //Log.d("channel",channel_id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("ConnectionFailed",connectionResult.getErrorMessage());
    }

    private void setAdapter(){
        listMessageAdapter = new ListMessageAdapter(Message.class,
                R.layout.item_message,
                MessageHolder.class,
                databaseReference.child("messages").child(channel_id),
                receiver.getPhotoUrl(),
                this);

        listMessageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                int listFriendCount = listMessageAdapter.getItemCount();
                int lastVisiblePosition  = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if(lastVisiblePosition==1||positionStart>=(listFriendCount-1)){
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(listMessageAdapter);
    }

    private void sendNotification (){

        if(receiver.getToken().equals(""))
            return;
        Log.d("sendnotif", "sendNotification: ");

        final ProgressDialog progressDialog = new ProgressDialog(OfflineCanvasChatActvity.this);
        progressDialog.setTitle("Sending Notification");
        progressDialog.setMessage("please wait....");
        progressDialog.show();

        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://fcm.googleapis.com").addConverterFactory(GsonConverterFactory.create()).build();
        Type type = new TypeToken<GCMRequest>() {}.getType();
        GCMRequest gcmRequest = new GCMRequest(receiver.getToken()
                ,new User(firebaseUser.getUid(),firebaseUser.getEmail(), firebaseUser.getDisplayName(), FirebaseInstanceId.getInstance().getToken()+"",firebaseUser.getPhotoUrl().toString())
                ,"offline_canvas");

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
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("responseNotif","failed");
                t.printStackTrace();
                progressDialog.dismiss();
            }
        });
    }
}
