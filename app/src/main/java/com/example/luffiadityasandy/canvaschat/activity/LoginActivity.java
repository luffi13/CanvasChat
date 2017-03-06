package com.example.luffiadityasandy.canvaschat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;

    private static final int RC_SIGN_IN = 9001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();

        if(firebaseUser!=null){
            //go to main activity
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        SignInButton signInButton = (SignInButton)findViewById(R.id.signInButton);
        signInButton.setOnClickListener(clickHandler);


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("conenctionFailed",connectionResult.toString());
        Toast.makeText(this, "Google Play Service Error", Toast.LENGTH_SHORT).show();
    }


    private View.OnClickListener clickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.signInButton:
                    signIn();
                    break;
            }
        }
    };


    private void signIn(){
        Intent loginIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(loginIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogleAccount(account);
            }
            else {
                Log.d("login failed","Google sign in failed");
            }
        }
    }

    private void firebaseAuthWithGoogleAccount(GoogleSignInAccount account) {
        Log.d("login success", "goole with id"+account.getId()+account.getDisplayName());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("credential ", "credential complete"+task.isSuccessful());
                if(!task.isSuccessful()){
                    Log.d("credential failed",task.getException()+"");
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
                else {
                    insertNewUser();
                    Toast.makeText(LoginActivity.this, "succes login", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();
                }
            }
        });
    }

    private void insertNewUser(){
        databaseReference.child("user_detail").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebaseUser =mFirebaseAuth.getCurrentUser();
                databaseReference.child("user_detail").child(firebaseUser.getUid()).setValue(new User(
                        firebaseUser.getUid(),firebaseUser.getEmail(),firebaseUser.getDisplayName(), FirebaseInstanceId.getInstance().getToken(),firebaseUser.getPhotoUrl().toString()
                ));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
