package com.example.luffiadityasandy.canvaschat.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    public void clickHandler(View v){
        switch (v.getId()){
            case R.id.listFriend_btn:
                startActivity(new Intent(this,ListFriendActivity.class));
                break;
            case R.id.canvas_btn:
                startActivity(new Intent(this,SignatureActivity.class));
                break;
            case R.id.writeDatabase_btn:
                startActivity(new Intent(this, WriteDatabaseActivity.class));
                break;
            case R.id.signOut_btn:
                signOut();
                break;
        }
    }

    public void signOut(){
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "connection Problem", Toast.LENGTH_SHORT).show();
    }
}
