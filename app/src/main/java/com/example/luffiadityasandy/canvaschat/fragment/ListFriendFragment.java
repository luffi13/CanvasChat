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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFriendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFriendFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "ListFriendFragment";

    DatabaseReference databaseReference;
    ListFriendAdapter adapterFriend;
    ListFriendAdapter requestAdapter;
    RecyclerView friend_rv, request_rv;
    LinearLayoutManager friendManager, requestManager;

    private ArrayList<User> listFriend;
    String mUid="";

    public ListFriendFragment() {
        // Required empty public constructor
    }

    public static ListFriendFragment newInstance(String uid) {
        ListFriendFragment fragment = new ListFriendFragment();
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
        adapterFriend = new ListFriendAdapter(User.class, R.layout.item_friend, FriendViewHolder.class,databaseReference.orderByChild("state").equalTo("friend"));
        adapterFriend.setActivity(getActivity());
        requestAdapter = new ListFriendAdapter(User.class, R.layout.item_friend, FriendViewHolder.class,databaseReference.orderByChild("state").equalTo("request"));
        requestAdapter.setActivity(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_friend, container, false);
        friend_rv = (RecyclerView)rootView.findViewById(R.id.rv_listFriend);
        request_rv = (RecyclerView)rootView.findViewById(R.id.rv_listRequest);

        friendManager = new LinearLayoutManager(getActivity());
        requestManager = new LinearLayoutManager(getActivity());
        friend_rv.setAdapter(adapterFriend);
        friend_rv.setLayoutManager(friendManager);

        request_rv.setAdapter(requestAdapter);
        request_rv.setLayoutManager(requestManager);

        return rootView;
    }

    private void getListFriend(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<User>> listType = new GenericTypeIndicator<ArrayList<User>>() {};
//                listFriend = dataSnapshot.getValue(listType);
                Log.d(TAG, "onDataChange: "+dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
