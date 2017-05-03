package com.example.luffiadityasandy.canvaschat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.adapter.ListFriendAdapter;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.view_holder.FriendViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.realm.Realm;

public class ListChatFragment extends Fragment {

    DatabaseReference databaseReference, connectedRef;
    ListFriendAdapter adapter;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    String mUid = "";

    boolean isEverConnected;
    Realm realm;
    private ArrayList<User> listFriend;

    public ListChatFragment() {
        // Required empty public constructor
    }

    public static ListChatFragment newInstance(String uid) {
        ListChatFragment fragment = new ListChatFragment();
        Bundle args = new Bundle();
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUid = getArguments().getString("uid");
        databaseReference = FirebaseDatabase.getInstance().getReference("/friendship/"+mUid);
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        adapter = new ListFriendAdapter(User.class, R.layout.item_friend, FriendViewHolder.class,databaseReference.orderByChild("isEverChat").equalTo(true),"chat");
        adapter.setActivity(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_list_chat, container, false);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.listChat_rv);

        realm = Realm.getInstance(getActivity());
        connectionDetector();

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }
    private void connectionDetector(){
        isEverConnected = false;
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    System.out.println("connected to firebase");
                    isEverConnected = true;
                    adapter.setConnected(true);
                    adapter.notifyDataSetChanged();
                    saveListFriend();
                    connectedRef.removeEventListener(this);

                } else if(!isEverConnected) {
                    System.out.println("never connected to firebase");

                    //refer adapter to offline adapter
                    adapter.setOfflineFriendData(realm.where(User.class).equalTo("state","friend").equalTo("isEverChat",true).findAll());
                    adapter.setConnected(false);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //save list friend to local
    private void saveListFriend(){
        databaseReference.orderByChild("state").equalTo("friend")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot children : dataSnapshot.getChildren()
                                ) {
                            User friend = children.getValue(User.class);
                            Log.d("listchatfragment", "onDataChange save list friend: "+friend.getUid());
                            realm.beginTransaction();
                            realm.copyToRealmOrUpdate(friend);
                            realm.commitTransaction();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

}
