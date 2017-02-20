package com.example.luffiadityasandy.canvaschat.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luffiadityasandy.canvaschat.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Luffi Aditya Sandy on 16/02/2017.
 */

public class FriendViewHolder extends RecyclerView.ViewHolder {
    public TextView email, displayName;
    public CircleImageView userPhoto;

    public FriendViewHolder(View itemView) {
        super(itemView);
        email = (TextView)itemView.findViewById(R.id.email);
        displayName = (TextView)itemView.findViewById(R.id.displayName);
        userPhoto = (CircleImageView)itemView.findViewById(R.id.photo);
    }
}
