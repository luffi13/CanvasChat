package com.example.luffiadityasandy.canvaschat.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.adapter.SearchUserAdapter;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchFriendActivity extends AppCompatActivity {

    User mUser;
    DatabaseReference reference;

    EditText keyword_et;
    ListView listView;

    SearchUserAdapter adapter;
    ArrayList<User>listUser;
    HashMap<String,User> listFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser = new User(currentUser.getUid(),currentUser.getEmail(),currentUser.getDisplayName(), FirebaseInstanceId.getInstance().getToken(),currentUser.getPhotoUrl().toString());

        keyword_et = (EditText)findViewById(R.id.keyword_et);
        listView = (ListView)findViewById(R.id.listUser_lv);

        listUser = new ArrayList<>();
        adapter = new SearchUserAdapter(this,R.layout.item_search_user,listUser);
        listView.setAdapter(adapter);

        getAllUser();
        getListFriend();

    }


    private void getAllUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user_detail");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                Log.d("SearchActivity", "onCancelled: "+databaseError.getMessage());
            }
        });
    }

    private void getListFriend(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("friendship/"+mUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String,User>> type = new GenericTypeIndicator<HashMap<String,User>>() {};
                listFriend = dataSnapshot.getValue(type);
                Log.d("listfriendrespond", "onDataChange: "+listFriend.get("friendkey1").getName());

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("SearchActivity", "onCancelled: "+databaseError.getMessage());
            }
        });
    }


}
