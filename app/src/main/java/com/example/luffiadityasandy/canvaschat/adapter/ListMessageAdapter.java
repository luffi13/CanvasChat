package com.example.luffiadityasandy.canvaschat.adapter;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.luffiadityasandy.canvaschat.view_holder.MessageHolder;
import com.example.luffiadityasandy.canvaschat.object.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by Luffi Aditya Sandy on 17/02/2017.
 */

public class ListMessageAdapter extends FirebaseRecyclerAdapter<Message, MessageHolder> {

    private String userPhoto;
    private Activity activity;
    private String myPhoto;
    private String mUserName;

    public ListMessageAdapter(Class<Message> modelClass, int modelLayout, Class<MessageHolder> viewHolderClass, DatabaseReference ref, String userPhoto, Activity activity) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.userPhoto = userPhoto;
        this.activity = activity;
        myPhoto = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
        mUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString();
    }


    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }


    @Override
    protected void populateViewHolder(MessageHolder viewHolder, Message model, int position) {
        String photo = userPhoto;
        if(model.getSender().equals(mUserName)){
            photo = myPhoto;
            if(photo!=null){
                Glide.with(activity).load(photo).into(viewHolder.userPhotoRight);
            }
            viewHolder.userPhotoLeft.setVisibility(View.GONE);
            viewHolder.userPhotoRight.setVisibility(View.VISIBLE);
            viewHolder.message_ll.setGravity(Gravity.RIGHT);
        }
        else {
            if(photo!=null){
                Glide.with(activity).load(photo).into(viewHolder.userPhotoLeft);
            }
            viewHolder.userPhotoRight.setVisibility(View.GONE);
            viewHolder.userPhotoLeft.setVisibility(View.VISIBLE);
            viewHolder.message_ll.setGravity(Gravity.LEFT);
        }
        if(model.getCanvasUri()!=null){
            Glide.with(activity).load(model.getCanvasUri()).into(viewHolder.imageMessage);
        }else {

        }

    }
}
