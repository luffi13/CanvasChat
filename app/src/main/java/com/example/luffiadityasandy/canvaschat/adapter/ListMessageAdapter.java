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

import java.util.ArrayList;

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
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public Message getItem(int position) {
        return super.getItem(position);
    }

    public ArrayList<Message> getLastMessages(int count){
        int limit = 0;
        if (count<getItemCount()){
            limit = getItemCount()-count;
        }

        ArrayList<Message> listLastMessages = new ArrayList<>();
        for (int i = getItemCount()-1;i>=limit;i-- ){
            listLastMessages.add(getItem(i));
        }
        return listLastMessages;
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
            viewHolder.textMessage_tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
        else {
            if(photo!=null){
                Glide.with(activity).load(photo).into(viewHolder.userPhotoLeft);
            }
            viewHolder.userPhotoRight.setVisibility(View.GONE);
            viewHolder.userPhotoLeft.setVisibility(View.VISIBLE);
            viewHolder.message_ll.setGravity(Gravity.LEFT);
        }
        if(model.getMessage()!=null&&model.getType().equals("image")){
            viewHolder.textMessage_tv.setVisibility(View.GONE);
            viewHolder.imageMessage.setVisibility(View.VISIBLE);
            Glide.with(activity).load(model.getMessage()).into(viewHolder.imageMessage);
        }else if(model.getMessage()!=null&&model.getType().equals("text")){
            viewHolder.textMessage_tv.setVisibility(View.VISIBLE);
            viewHolder.imageMessage.setVisibility(View.GONE);
            viewHolder.textMessage_tv.setText(model.getMessage());

        }

    }
}
