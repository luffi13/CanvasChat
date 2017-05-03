package com.example.luffiadityasandy.canvaschat.adapter;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.view_holder.MessageHolder;
import com.example.luffiadityasandy.canvaschat.object.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Luffi Aditya Sandy on 17/02/2017.
 */

public class ListMessageAdapter extends FirebaseRecyclerAdapter<Message, MessageHolder> {



    private String userPhoto;
    private Activity activity;
    private String myPhoto;
    private String mUserName;
    private RealmList<Message> offlineMessageData;
    private boolean isConnected;


    public ListMessageAdapter(Class<Message> modelClass, int modelLayout, Class<MessageHolder> viewHolderClass, DatabaseReference ref, String userPhoto, Activity activity) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.userPhoto = userPhoto;
        this.activity = activity;
        myPhoto = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
        mUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString();
        isConnected = true;
    }



    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public void setOfflineMessageData(RealmList<Message> offlineMessageData) {
        this.offlineMessageData = offlineMessageData;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    @Override
    public int getItemCount() {
        if (!isConnected){
            return offlineMessageData.size();
        }
        return super.getItemCount();
    }

    @Override
    public Message getItem(int position) {
        if (!isConnected){
            return offlineMessageData.get(offlineMessageData.size()-position-1);
        }
        return super.getItem(position);
    }

    public RealmList<Message> getLastMessages(int count){
        int limit = 0;
        if (count<getItemCount()){
            limit = getItemCount()-count;
        }

        RealmList<Message> listLastMessages = new RealmList<>();
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
            viewHolder.message_layout.setBackgroundResource(R.drawable.in_message_bg);
            viewHolder.textMessage_tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
        else {
            if(photo!=null){
                Glide.with(activity).load(photo).into(viewHolder.userPhotoLeft);
            }
            viewHolder.userPhotoRight.setVisibility(View.GONE);
            viewHolder.userPhotoLeft.setVisibility(View.VISIBLE);
            viewHolder.message_ll.setGravity(Gravity.LEFT);
            viewHolder.message_layout.setBackgroundResource(R.drawable.out_message_bg);
            viewHolder.textMessage_tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
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
