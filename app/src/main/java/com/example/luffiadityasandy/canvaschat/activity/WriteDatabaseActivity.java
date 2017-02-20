package com.example.luffiadityasandy.canvaschat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.object.Message;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class WriteDatabaseActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    EditText sender, receiver, message;
    GoogleApiClient googleApiClient;
    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        database = FirebaseDatabase.getInstance();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser==null){
            startActivity(new Intent(this,LoginActivity.class));
        }
        else {
            username = firebaseUser.getDisplayName();
        }

        Log.d("userDetail",username+"jj"+firebaseUser.getEmail());

        reference = database.getReference();

        sender = (EditText)findViewById(R.id.sender);
        receiver = (EditText)findViewById(R.id.receiver);
        message = (EditText)findViewById(R.id.message);
        Button insert = (Button)findViewById(R.id.insert_btn);

        sender.setText(firebaseUser.getDisplayName());
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNewFriend();
                //insertNewMessage(new Message(sender.getText().toString(),receiver.getText().toString(),message.getText().toString()));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                signOut();
            case R.id.profile_menu:
                Toast.makeText(this, firebaseUser.getDisplayName()+" "+firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void insertNewMessage(Message message){
        reference.child("user").child(firebaseUser.getUid()).push().setValue(message);
    }

    private void insertNewFriend(){
        reference.child("friendship/"+firebaseUser.getUid()).push().child("friend_id").setValue("ujuDQg8zH7TFEggxsxHJ6vL0Fhi1");
    }

    public void signOut(){
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }


}
