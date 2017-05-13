package com.example.luffiadityasandy.canvaschat.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

    private User mUser;
    private EditText keyword_et;
    private ListView listView;

    private SearchUserAdapter adapter;
    private ArrayList<User>listUser;
    private boolean isConnected;
    private DatabaseReference connectionReference;

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

        keyword_et.addTextChangedListener(textWatcher);

        getAllUser();
        isConnected = false;

        connectionReference = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectionReference.addValueEventListener(connectionListener);

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

    private ValueEventListener connectionListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            boolean flag = snapshot.getValue(Boolean.class);
            if (flag) {
                isConnected= true;
                adapter.setConnected(true);
            } else {
                isConnected = false;
                adapter.setConnected(false);
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            System.err.println("Listener was cancelled");
        }
    };




}
