package com.example.luffiadityasandy.canvaschat.activity;

import android.app.ProgressDialog;
import android.app.Service;
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
import com.example.luffiadityasandy.canvaschat.object.GCMRequest;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.object.Message;
import com.example.luffiadityasandy.canvaschat.service.ServiceMessaging;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class WriteDatabaseActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    EditText sender, receiver, message;
    GoogleApiClient googleApiClient;
    String username;
    Button insert_btn;


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
        insert_btn = (Button)findViewById(R.id.insert_btn);

        sender.setText(firebaseUser.getDisplayName());


        insert_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //insertNewFriend();
                Log.d("masuk ", "masuk");

                final ProgressDialog progressDialog = new ProgressDialog(WriteDatabaseActivity.this);
                progressDialog.setTitle("aa");
                progressDialog.setMessage("aaadss");
                progressDialog.show();
                Retrofit retrofit = new Retrofit.Builder().baseUrl("https://fcm.googleapis.com").addConverterFactory(GsonConverterFactory.create()).build();

//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("to","e-8P7hnQpC8:APA91bEJC0WgDJCP3_PR8TeIwY-b-JhH_bJcMtd1BEb02g6XYTuDrZkAy7XC_OtwSE0hfl42rs5TotiV4C3FpG85UlBydjKM2YgfB3EChG9LcRyO8CLC2RhnDWX8NY5qfn2ooFt6-oLZ");
//                JsonObject notification = new JsonObject();
//                notification.addProperty("title","title");
//                notification.addProperty("body","body");
//                jsonObject.add("notification",notification);
//                Log.d("jsonobject",jsonObject.toString());


                Gson gson = new Gson();
                Type type = new TypeToken<GCMRequest>() {}.getType();
                GCMRequest gcmRequest = new GCMRequest("e-8P7hnQpC8:APA91bEJC0WgDJCP3_PR8TeIwY-b-JhH_bJcMtd1BEb02g6XYTuDrZkAy7XC_OtwSE0hfl42rs5TotiV4C3FpG85UlBydjKM2YgfB3EChG9LcRyO8CLC2RhnDWX8NY5qfn2ooFt6-oLZ"
                        ,new User(firebaseUser.getUid(),firebaseUser.getEmail(), firebaseUser.getDisplayName(),FirebaseInstanceId.getInstance().getToken()+"",firebaseUser.getPhotoUrl().toString())
                        ,"shareable");

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

                //insertNewMessage(new Message(sender.getText().toString(),receiver.getText().toString(),message.getText().toString()));
            }
        });

    }



    private class Data{
        String type;
        User sender;

        public Data() {
        }

        public Data(String type, User sender) {
            this.type = type;
            this.sender = sender;
        }
    }

    private class NotificationMessage{
        String title, body ;

        public NotificationMessage() {
        }

        public NotificationMessage(String title , String body) {
            this.body = body;
            this.title = title;
        }
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
