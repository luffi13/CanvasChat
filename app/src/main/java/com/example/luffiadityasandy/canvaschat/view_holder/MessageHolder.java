package com.example.luffiadityasandy.canvaschat.view_holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.luffiadityasandy.canvaschat.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Luffi Aditya Sandy on 17/02/2017.
 */

public class MessageHolder extends RecyclerView.ViewHolder {
    public CircleImageView userPhotoRight,userPhotoLeft;
    public ImageView imageMessage;
    public LinearLayout message_ll;

    public MessageHolder(View itemView) {
        super(itemView);
        userPhotoLeft = (CircleImageView)itemView.findViewById(R.id.userPhotoLeft);
        imageMessage = (ImageView)itemView.findViewById(R.id.imageMessage);
        message_ll = (LinearLayout)itemView.findViewById(R.id.message_ll);
        userPhotoRight =(CircleImageView)itemView.findViewById(R.id.userPhotoRight);
    }


}
