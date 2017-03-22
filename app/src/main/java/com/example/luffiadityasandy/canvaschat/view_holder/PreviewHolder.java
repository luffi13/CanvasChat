package com.example.luffiadityasandy.canvaschat.view_holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luffiadityasandy.canvaschat.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Luffi Aditya Sandy on 22/03/2017.
 */

public class PreviewHolder{
    public TextView name_tv, email_tv;
    public CircleImageView userPhoto;
    public ImageView addButton;

    public PreviewHolder(View view){
        name_tv = (TextView)view.findViewById(R.id.name_tv);
        email_tv = (TextView)view.findViewById(R.id.email_tv);
        userPhoto = (CircleImageView)view.findViewById(R.id.photo);
        addButton = (ImageView)view.findViewById(R.id.add_icon);
    }
}
