package com.example.luffiadityasandy.canvaschat.activity;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.ViewHolder.FriendViewHolder;
import com.example.luffiadityasandy.canvaschat.adapter.ListFriendAdapter;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListFriendActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private ListFriendAdapter listFriendAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        verifyUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        recyclerView = (RecyclerView)findViewById(R.id.rv_listFriend);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        listFriendAdapter = new ListFriendAdapter(
                User.class,
                R.layout.item_friend,
                FriendViewHolder.class,
                databaseReference.child("user_detail")
                );

        listFriendAdapter.setActivity(this);

        listFriendAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int listFriendCount = listFriendAdapter.getItemCount();
                int lastVisiblePosition  = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if(lastVisiblePosition==1||positionStart>=(listFriendCount-1)){
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(listFriendAdapter);
    }

    private void verifyUser(){
        if(firebaseUser == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "connection problem...", Toast.LENGTH_SHORT).show();
    }
}
