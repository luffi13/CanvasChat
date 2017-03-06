package com.example.luffiadityasandy.canvaschat.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.object.GCMRequest;
import com.example.luffiadityasandy.canvaschat.service.ServiceMessaging;
import com.example.luffiadityasandy.canvaschat.view_holder.MessageHolder;
import com.example.luffiadityasandy.canvaschat.adapter.ListMessageAdapter;
import com.example.luffiadityasandy.canvaschat.canvas_handler.DrawView;
import com.example.luffiadityasandy.canvaschat.object.Message;
import com.example.luffiadityasandy.canvaschat.object.User;
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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
        Button createCanvas_btn = (Button)findViewById(R.id.createCanvas_btn);
        createCanvas_btn.setOnClickListener(clickHandler);

        recyclerView = (RecyclerView)findViewById(R.id.messageList_rv);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);



    }

    private View.OnClickListener clickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.createCanvas_btn:
                    showCanvas();
                    break;
            }
        }
    };

    private void showCanvas(){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.create_canvas,null);
        AlertDialog.Builder canvasDialog = new AlertDialog.Builder(this);
        canvasDialog.setView(view);

        Button undo = (Button)view.findViewById(R.id.undo);
        Button redo = (Button)view.findViewById(R.id.redo);
        LinearLayout canvas_ll = (LinearLayout)view.findViewById(R.id.myCanvas);
        final DrawView canvasController = new DrawView(this);
        canvasController.setBackgroundColor(Color.WHITE);
        canvas_ll.addView(canvasController, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        final View mView = canvas_ll;

        View.OnClickListener buttonHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.undo:
                        canvasController.onClickUndo();
                        break;
                    case R.id.redo:
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
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadCanvas(mView, alertDialog);
            }
        });

    }

    private void uploadCanvas(View view, final AlertDialog alertDialog){

        StorageReference imageDir = storageReference.child("image");
        StorageReference imageRef = imageDir.child(firebaseUser.getUid())
                .child(+Calendar.getInstance().get(Calendar.MILLISECOND)+".jpg");
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
                Toast.makeText(OfflineCanvasChatActvity.this, "failed upload", Toast.LENGTH_SHORT).show();
                e.printStackTrace();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri uri = taskSnapshot.getDownloadUrl();
                Log.d("uriDownload",uri.toString());
                HashMap<String, Object> message = new HashMap<>();
                message.put("canvasUri",uri.toString());
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
                alertDialog.dismiss();
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

        final ProgressDialog progressDialog = new ProgressDialog(OfflineCanvasChatActvity.this);
        progressDialog.setTitle("Sending Notification");
        progressDialog.setMessage("please wait....");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://fcm.googleapis.com").addConverterFactory(GsonConverterFactory.create()).build();
        Gson gson = new Gson();
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
