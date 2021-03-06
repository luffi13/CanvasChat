package com.example.luffiadityasandy.canvaschat.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.object.FriendMessage;
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
import com.google.firebase.storage.OnPausedListener;
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

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import yuku.ambilwarna.AmbilWarnaDialog;

public class OfflineCanvasChatActvity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "OfflineCanvasActivity";
    private User receiver;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private LinearLayoutManager linearLayoutManager;
    private boolean isEverConnected;
    private boolean connected;
    private Realm realm;
    private DatabaseReference connectedRef;
    private CircleImageView lastColorButton;

    //uploadtask
    UploadTask uploadTask;

    //input panel
    private RelativeLayout keyboardPanel;
    private LinearLayout canvasPanel;

    //menu panel
    private RelativeLayout keyboardMenu, canvasMenu, shareCanvasMenu;


    String channel_id;
    RecyclerView recyclerView;
    ListMessageAdapter listMessageAdapter;
    EditText textMessage_et;
    ImageView sendText_btn;

    DrawView canvasController;

    int lastColor = Color.BLACK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_canvas_chat_actvity);

        receiver = (User)getIntent().getSerializableExtra("receiver");
        realm = Realm.getInstance(this);
        channel_id = "tempChannel";
        setTitle(receiver.getName());

        Log.d(TAG, "onCreate: userid on offline canvas "+receiver.getUid());
        //Toast.makeText(this, receiver.getName()+"\n"+receiver.getEmail()+"\n"+receiver.getUid()+"\n"+receiver.getToken()+"\n", Toast.LENGTH_SHORT).show();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ImageView hideKeyboard_btn = (ImageView)findViewById(R.id.hide_keyboard_btn);
        recyclerView = (RecyclerView)findViewById(R.id.messageList_rv);
        textMessage_et = (EditText)findViewById(R.id.textMessage_et);
        sendText_btn = (ImageView)findViewById(R.id.sendText_btn) ;
        keyboardPanel = (RelativeLayout)findViewById(R.id.keyboard_panel_layout);
        canvasPanel = (LinearLayout)findViewById(R.id.canvas_panel_layout);
        keyboardMenu = (RelativeLayout)findViewById(R.id.keyboard_menu);
        canvasMenu= (RelativeLayout)findViewById(R.id.canvas_menu);
        shareCanvasMenu= (RelativeLayout)findViewById(R.id.share_canvas_menu);

        hideKeyboard_btn.setOnClickListener(clickHandler);
        sendText_btn.setOnClickListener(clickHandler);
        keyboardMenu.setOnClickListener(clickHandler);
        canvasMenu.setOnClickListener(clickHandler);
        shareCanvasMenu.setOnClickListener(clickHandler);

        keyboardPanel.setVisibility(View.INVISIBLE);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //set user state to ever chat by connection detector
        setAdapter();
        isEverConnected = false;
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(firstConnectionListener);
        connectedRef.addValueEventListener(connectionListener);


    }


    private ValueEventListener firstConnectionListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            boolean connected = snapshot.getValue(Boolean.class);
            if (connected) {
                System.out.println("connected to firebase");
                isEverConnected = true;
                setIsEverChat();
                databaseReference.child("channels/"+firebaseUser.getUid()).addListenerForSingleValueEvent(channelListener);
                listMessageAdapter.setConnected(true);
                connectedRef.removeEventListener(this);
                databaseReference.child("user_detail/"+receiver.getUid()).addValueEventListener(tokenListener);

            } else if(!isEverConnected) {
                System.out.println("never connected to firebase");
                //refer adapter to offline adapter
                if (getMyLastMessage()!=null){
                    listMessageAdapter.setOfflineMessageData(getMyLastMessage());
                }
                listMessageAdapter.setConnected(false);
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            System.err.println("Listener was cancelled");
        }
    };

    private ValueEventListener connectionListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            boolean flag = snapshot.getValue(Boolean.class);
            if (flag) {
                connected= true;
            } else {
                connected = false;
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            System.err.println("Listener was cancelled");
        }
    };

    private ValueEventListener tokenListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange tokenlistener: "+dataSnapshot.getValue().toString());
            User newUpdatedReceiverData = dataSnapshot.getValue(User.class);
            if (newUpdatedReceiverData!=null){
                receiver.setToken(newUpdatedReceiverData.getToken());
            }
            //Log.d(TAG, "onDataChange receiver token change: "+receiver.getToken());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void setLastColorButton(int buttonId){
        lastColorButton.setBorderWidth(0);
        lastColorButton = (CircleImageView)offlineCanvasHolder.rootLayout.findViewById(buttonId);
        lastColorButton.setBorderWidth(6);

        lastColorButton.setBorderColor(Color.parseColor("#ff0000"));
    }

    private View.OnClickListener clickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.hide_keyboard_btn:
                    showCanvasPanel();
                    break;
                case R.id.keyboard_menu:
                    showKeyboardPanel();
                    break;
                case R.id.sendText_btn:
                    sendTextMessage();
                    break;
                case R.id.canvas_menu:
                    popupCanvas();
                    break;
                case R.id.share_canvas_menu:
                    openShareableCanvas();
                    break;
//                case R.id.lastMessage_btn:
//                    iterateLastMessage();
//                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        if(isEverConnected){
            addFriendMessage();
        }
        super.onStop();
    }

    private void showCanvasPanel(){
        canvasPanel.setVisibility(View.VISIBLE);
        keyboardPanel.setVisibility(View.INVISIBLE);
    }

    private void showKeyboardPanel(){
        keyboardPanel.setVisibility(View.VISIBLE);
        canvasPanel.setVisibility(View.GONE);
    }

    private void openShareableCanvas(){
        if (!connected){
            Toast.makeText(this, "please check your connection", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ShareableCanvasActivity.class);
        intent.putExtra("receiver",receiver);
        startActivity(intent);
    }


    public void addFriendMessage(){
        try {
            realm.beginTransaction();
            FriendMessage myLastMessage = new FriendMessage();
            myLastMessage.setUid(receiver.getUid());

            RealmList<Message> messages = listMessageAdapter.getLastMessages(10);
            if (messages == null)
                return;
            myLastMessage.setRealmList(messages);

            realm.copyToRealmOrUpdate(myLastMessage);

        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            realm.commitTransaction();
        }

    }

    public RealmList<Message> getMyLastMessage(){
        FriendMessage results = realm.where(FriendMessage.class).equalTo("uid",receiver.getUid()).findFirst();
        if (results!=null )
            return results.getRealmList();
        else return null;
    }
    private void setIsEverChat(){
        HashMap <String, Object> data = new HashMap<>();
        data.put("isEverChat",true);
        databaseReference.child("friendship/"+firebaseUser.getUid()+"/"+receiver.getUid()).updateChildren(data);
        databaseReference.child("friendship/"+receiver.getUid()+"/"+firebaseUser.getUid()).updateChildren(data);
    }

    OfflineCanvasHolder offlineCanvasHolder;
    private void popupCanvas(){
        final PopupWindow popupWindow;
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.create_canvas,(ViewGroup)findViewById(R.id.popup_canvas));
        popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(layout, Gravity.CENTER,0,0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        offlineCanvasHolder = new OfflineCanvasHolder(layout);
        lastColorButton = (CircleImageView)layout.findViewById(R.id.black_button);
        offlineCanvasHolder.currentShape_btn.setImageResource(R.mipmap.freeshape);
        canvasController = new DrawView(this);

        offlineCanvasHolder.canvas.addView(canvasController, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        final View mView = offlineCanvasHolder.canvas;

        offlineCanvasHolder.undo_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.redo_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.colorPicker_btn.setOnClickListener(colorHandler);

        offlineCanvasHolder.rectangle_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.circle_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.line_btn.setOnClickListener(buttonHandler);
        offlineCanvasHolder.freehand_btn.setOnClickListener(buttonHandler);

        offlineCanvasHolder.currentShape_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (offlineCanvasHolder.shape_layout.getVisibility()==View.GONE){
                    offlineCanvasHolder.shape_layout.setVisibility(View.VISIBLE);
                    offlineCanvasHolder.color_layout.setVisibility(View.GONE);
                }
                else {
                    offlineCanvasHolder.shape_layout.setVisibility(View.GONE);
                    offlineCanvasHolder.color_layout.setVisibility(View.VISIBLE);
                }
            }
        });

        offlineCanvasHolder.colorPicker_btn.setOnClickListener( colorHandler);
        offlineCanvasHolder.send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadCanvas(mView,popupWindow);
            }
        });

        //holder
//        offlineCanvasHolder.bottomToolbar.setBackgroundResource(R.drawable.oval_bg);
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
                    offlineCanvasHolder.currentShape_btn.setImageResource(R.mipmap.rectangle);
                    break;
                case R.id.circle_btn:
                    canvasController.setPaintTool("circle");
                    offlineCanvasHolder.currentShape_btn.setImageResource(R.mipmap.circle);
                    break;
                case R.id.freehand_btn:
                    canvasController.setPaintTool("freehand");
                    offlineCanvasHolder.currentShape_btn.setImageResource(R.mipmap.freeshape);
                    break;
                case R.id.line_btn:
                    canvasController.setPaintTool("line");
                    offlineCanvasHolder.currentShape_btn.setImageResource(R.mipmap.line);
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

    public void changeColor(View view){
        switch (view.getId()){
            case R.id.black_button:
                canvasController.setPaintColor(Color.BLACK);
                break;
            case R.id.red_button:
                canvasController.setPaintColor(Color.parseColor("#ed5757"));
                break;
            case R.id.yellow_button:
                canvasController.setPaintColor(Color.parseColor("#f7f740"));
                break;
            case R.id.green_button:
                canvasController.setPaintColor(Color.parseColor("#76ef6b"));
                break;
            case R.id.city_button:
                canvasController.setPaintColor(Color.parseColor("#49f4f4"));
                break;
            case R.id.blue_button:
                canvasController.setPaintColor(Color.parseColor("#4753ff"));
                break;
            case R.id.purple_btn:
                canvasController.setPaintColor(Color.parseColor("#b733e8"));
                break;
            case R.id.current_color_btn:
                canvasController.setPaintColor(lastColor);
                break;
        }
        setLastColorButton(view.getId());
    }

    void openDialog(boolean supportsAlpha) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(OfflineCanvasChatActvity.this, Color.BLACK, supportsAlpha, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
                canvasController.setPaintColor(color);
                lastColor = color;
                offlineCanvasHolder.currentColor_btn.setColorFilter(color);
                setLastColorButton(R.id.current_color_btn);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }



    private void sendTextMessage(){
        if (!connected){
            Toast.makeText(this, "please check your connection", Toast.LENGTH_SHORT).show();
            return;
        }
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
        if (!connected){
            Toast.makeText(this, "please check your connection", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference imageDir = storageReference.child("image");
        StorageReference imageRef = imageDir.child(firebaseUser.getUid())
                .child(+Calendar.getInstance().getTimeInMillis()+".jpg");
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

        uploadTask = imageRef.putBytes(data);
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


    private ValueEventListener channelListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "channellistener: "+receiver.getUid());
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
            setAdapter();
            //Log.d("channel",channel_id);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: "+databaseError.getMessage());
        }
    };

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

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.scrollToPosition(positionStart);
            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(listMessageAdapter);
    }

    private void sendNotification (){

        if(receiver.getToken()==null)
            return;
        Log.d("sendnotif", "sendNotification: "+receiver.getToken());

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
                Log.d("responseNotification",response.body().toString());
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("responseNotif","failed");
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        connectedRef.removeEventListener(connectionListener);
        connectedRef.removeEventListener(firstConnectionListener);
        databaseReference.removeEventListener(channelListener);
        super.onDestroy();
    }
}
