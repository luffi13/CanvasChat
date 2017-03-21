package com.example.luffiadityasandy.canvaschat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.adapter.ListFriendAdapter;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.view_holder.FriendViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListChatFragment extends Fragment {

    DatabaseReference databaseReference;
    ListFriendAdapter adapter;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    String mUid = "";

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
        adapter = new ListFriendAdapter(User.class, R.layout.item_friend, FriendViewHolder.class,databaseReference.orderByChild("isEverChat").equalTo(true));
        adapter.setActivity(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_list_chat, container, false);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.listChat_rv);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

}
