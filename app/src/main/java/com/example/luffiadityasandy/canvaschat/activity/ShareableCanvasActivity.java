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
import com.example.luffiadityasandy.canvaschat.object.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ShareableCanvasActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    User receiver;
    String channel_id = "";

    LinearLayout canvas;
    View mView;
    ShareableCanvasView canvasController;
    Button undo, redo, save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shareable_canvas);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        receiver = (User)getIntent().getSerializableExtra("receiver");
        undo = (Button)findViewById(R.id.undo);
        redo = (Button)findViewById(R.id.redo);
        save = (Button)findViewById(R.id.saveCanvas) ;
        canvas = (LinearLayout)findViewById(R.id.myCanvas);

        undo.setOnClickListener(clickHandler);
        redo.setOnClickListener(clickHandler);
        save.setOnClickListener(clickHandler);

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
            }
        }
    };

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

    public void prepareCanvasView(){
        canvasController = new ShareableCanvasView(this,channel_id);
        canvasController.setBackgroundColor(Color.WHITE);
        canvas.addView(canvasController, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mView = canvas;
    }
}
