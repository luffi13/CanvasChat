package com.example.luffiadityasandy.canvaschat.activity;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.adapter.SearchUserAdapter;
import com.example.luffiadityasandy.canvaschat.object.GCMRequest;
import com.example.luffiadityasandy.canvaschat.object.ShareableItem;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.object.Message;
import com.example.luffiadityasandy.canvaschat.service.ServiceMessaging;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
    EditText editText;
    GoogleApiClient googleApiClient;
    String username;
    Button process_btn;
    ListView listView;

    ArrayList<User> listUser ;
    SearchUserAdapter adapter;

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

        editText = (EditText)findViewById(R.id.editText);
        process_btn = (Button)findViewById(R.id.insert_btn);
        listView = (ListView)findViewById(R.id.listView);

        editText.addTextChangedListener(textWatcher);
        listUser = new ArrayList<>();
        adapter = new SearchUserAdapter(this,R.layout.item_friend,listUser);
        listView.setAdapter(adapter);


        process_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //insertNewFriend();
                Log.d("masuk ", "masuk");
                getAlluser();
                //insertNewMessage(new Message(sender.getText().toString(),receiver.getText().toString(),message.getText().toString()));
            }
        });

    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapter.getFilter().filter(s.toString().toLowerCase());
                }catch (Exception e){
                    e.printStackTrace();
                }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void getDataWithPaint(){
        reference.child("shareable_canvas").child("test_with_paint").child("key1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ShareableItem shareableItem = dataSnapshot.getValue(ShareableItem.class);
                Log.d("writeDatabaseActivty",shareableItem.getUid()+" color :"+shareableItem.getShareablePaint().getPaint().getColor());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("writeDatabaseActivty",databaseError.getMessage());

            }
        });
    }

    private void getAlluser(){
        reference.child("user_detail").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    listUser.add(child.getValue(User.class));
                    adapter.notifyDataSetChanged();
                    System.out.println(child.getValue(User.class).getName());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(){
        final ProgressDialog progressDialog = new ProgressDialog(WriteDatabaseActivity.this);
        progressDialog.setTitle("aa");
        progressDialog.setMessage("aaadss");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://fcm.googleapis.com").addConverterFactory(GsonConverterFactory.create()).build();
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
