package com.example.luffiadityasandy.canvaschat.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.view_holder.FriendViewHolder;
import com.example.luffiadityasandy.canvaschat.activity.OfflineCanvasChatActvity;
import com.example.luffiadityasandy.canvaschat.activity.ShareableCanvasActivity;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by Luffi Aditya Sandy on 16/02/2017.
 */

public class ListFriendAdapter extends FirebaseRecyclerAdapter<User,FriendViewHolder> {
    private Activity activity;

    public ListFriendAdapter(Class modelClass, int modelLayout, Class viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
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

        viewHolder.displayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPrivateChat(model);
            }
        });
        viewHolder.email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goShareableChat(model);
            }
        });




    }

    private void goPrivateChat(User user){
        Intent privateChatIntent = new Intent(activity, OfflineCanvasChatActvity.class);
        privateChatIntent.putExtra("receiver",user);
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

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
