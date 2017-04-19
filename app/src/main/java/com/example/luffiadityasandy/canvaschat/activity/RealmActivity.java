package com.example.luffiadityasandy.canvaschat.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.object.FriendMessage;
import com.example.luffiadityasandy.canvaschat.object.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RealmActivity extends AppCompatActivity {
    ArrayList<Message> messages =  new ArrayList<>();
    Realm realm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm);

        realm = Realm.getInstance(this);

        getListMessage();
//        addData();
//        getData();

//        addFriendMessage();
//        getFriendMessages();
    }

    public void addData(){
        realm.beginTransaction();
        Message message = realm.createObject(Message.class);
        message.setMessage("message");
        message.setSender("luffi");
        message.setTime((long)213414);
        message.setType("text");
        realm.copyToRealm(message);
        realm.commitTransaction();
    }

    public void getData(){
        RealmResults<Message> results = realm.where(Message.class).findAll();
        for (int i = 0; i < results.size(); i ++){
            Log.d("realmresult", "getData: "+"size "+results.size()+" index 0 : "+results.get(i).getMessage());
        }
    }

    public void addFriendMessage(){
        realm.beginTransaction();
        FriendMessage friendMessage = new FriendMessage();
        friendMessage.setUid("luffi");

        RealmList<Message> messages = new RealmList<>();
        messages.add(new Message("message1","adit",(long)12131241,"text"));
        messages.add(new Message("message2","adit",(long)12131241,"text"));

        friendMessage.setRealmList(messages);

        realm.copyToRealm(friendMessage);

        realm.commitTransaction();


    }

    public void getFriendMessages(){
        FriendMessage results = realm.where(FriendMessage.class).equalTo("uid","luffi").findFirst();
        Log.d("list", "getFriendMessages: "+results.getRealmList().get(0).getMessage());
    }

    public void getListMessage(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("messages/UDdzwBtHldgsoXrwrAx19AcVT073ujuDQg8zH7TFEggxsxHJ6vL0Fhi1");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d("realm", "onDataChange: "+dataSnapshot.getValue());
                messages.add(dataSnapshot.getValue(Message.class));
                Log.d("datasnapshot", "onDataChange: "+dataSnapshot.getChildren());
                for (int i = 0 ; i < messages.size();i++){
                    Log.d("messagesFromFirebase", "onDataChange: "+messages.get(i).getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
