package com.example.luffiadityasandy.canvaschat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.fragment.ListChatFragment;
import com.example.luffiadityasandy.canvaschat.view_holder.FriendViewHolder;
import com.example.luffiadityasandy.canvaschat.activity.OfflineCanvasChatActvity;
import com.example.luffiadityasandy.canvaschat.activity.ShareableCanvasActivity;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.view_holder.PreviewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import io.realm.RealmResults;

/**
 * Created by Luffi Aditya Sandy on 16/02/2017.
 */

public class ListFriendAdapter extends FirebaseRecyclerAdapter<User,FriendViewHolder> {
    private Activity activity;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private RealmResults<User> offlineFriendData;
    private boolean isConnected;
    private RecyclerView recyclerView;
    private String fragmentName;

    public ListFriendAdapter(Class<User> modelClass, int modelLayout, Class<FriendViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        isConnected = true;
    }

    public ListFriendAdapter(Class<User> modelClass, int modelLayout, Class<FriendViewHolder> viewHolderClass, Query ref, String fragmentName) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        isConnected = true;
        this.fragmentName = fragmentName;
    }

    @Override
    protected void populateViewHolder(FriendViewHolder viewHolder, final User model, int position) {
        viewHolder.email.setText(model.getEmail());
        viewHolder.displayName.setText(model.getName());
        if (model.getPhotoUrl()==null){
            viewHolder.userPhoto.setImageDrawable(
                    ContextCompat.getDrawable(activity, R.drawable.ic_account_circle)
            );
        }
        else {
            Glide.with(activity).load(model.getPhotoUrl()).into(viewHolder.userPhoto);
        }
        viewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragmentName.equals("chat")){
                    goPrivateChat(model);
                }else{
                    initiatePopUpWindow(model,v);
                }
            }
        });

    }


    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void setOfflineFriendData(RealmResults<User> offlineFriendData) {
        this.offlineFriendData = offlineFriendData;
    }

    private void initiatePopUpWindow(final User user, View view){
        final PopupWindow popupWindow;
        LayoutInflater layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.user_preview,null);
        int width = recyclerView.getWidth()*6/7;
        int height = recyclerView.getHeight()*5/7;
        Log.d("friendAdapter", "initiatePopUpWindow: "+width+" "+height);
        popupWindow = new PopupWindow(layout, width, ViewGroup.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAtLocation(view, Gravity.CENTER,0,0);

        PreviewHolder previewHolder = new PreviewHolder(layout);
        previewHolder.name_tv.setText(user.getName());
        previewHolder.email_tv.setText(user.getEmail());
        previewHolder.chatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goPrivateChat(user);
                popupWindow.dismiss();
            }
        });

        if (user.getPhotoUrl()==null){
            previewHolder.userPhoto.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_account_circle));
        }
        else {
            Glide.with(activity).load(user.getPhotoUrl()+"?sz=300").into(previewHolder.userPhoto);
        }
        if (user.getState().equals("friend")){
            previewHolder.addLayout.setVisibility(View.GONE);
        }
        else {
            previewHolder.addLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriend(user);
                    popupWindow.dismiss();
                }
            });
        }

    }


    private void addFriend(final User friend){
        friend.setState("friend");
        databaseReference.child("friendship/"+ firebaseUser.getUid()+"/"+friend.getUid()).setValue(friend)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void goPrivateChat(User user){
        Intent privateChatIntent = new Intent(activity, OfflineCanvasChatActvity.class);
        User tempUser = new User(user.getUid(),user.getEmail(),user.getName(),user.getToken(),user.getPhotoUrl());
        privateChatIntent.putExtra("receiver",tempUser);
        Log.d("friendadapter", "go private: friend uid"+user.getUid());
        activity.startActivity(privateChatIntent);
    }

    private void goShareableChat(User user){
        Intent privateChatIntent = new Intent(activity, ShareableCanvasActivity.class);
        privateChatIntent.putExtra("receiver",user);
        activity.startActivity(privateChatIntent);
    }

    @Override
    protected User parseSnapshot(DataSnapshot snapshot) {
        User user = super.parseSnapshot(snapshot);
        return user;
    }

    @Override
    public User getItem(int position) {
        if(!isConnected){
            Log.d("check uid from realm", "getItem: "+offlineFriendData.get(position).getUid());
            return offlineFriendData.get(position);
        }
        return super.getItem(position);
    }

    @Override
    public int getItemCount() {
        if(!isConnected){
            return offlineFriendData.size();
        }
        return super.getItemCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }


}
