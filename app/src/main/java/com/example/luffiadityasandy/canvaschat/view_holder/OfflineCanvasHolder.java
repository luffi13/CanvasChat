package com.example.luffiadityasandy.canvaschat.view_holder;

import android.media.Image;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.luffiadityasandy.canvaschat.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Luffi Aditya Sandy on 29/03/2017.
 */

public class OfflineCanvasHolder {
    public RelativeLayout  color_layout;
    public LinearLayout canvas, shape_layout;


    public CircleImageView   currentColor_btn;
    public FloatingActionButton send_btn;
    public ImageView  colorPicker_btn,currentShape_btn, freehand_btn, circle_btn, rectangle_btn, line_btn ;
    public ImageButton undo_btn,redo_btn;
    public View rootLayout;
    public OfflineCanvasHolder(View view) {
        //get button from UI
        rootLayout = view;
        canvas = (LinearLayout)view.findViewById(R.id.myCanvas);
        currentColor_btn = (CircleImageView)view.findViewById(R.id.current_color_btn);

        undo_btn = (ImageButton) view.findViewById(R.id.undo_btn);
        redo_btn = (ImageButton) view.findViewById(R.id.redo_btn);
        send_btn = (FloatingActionButton) view.findViewById(R.id.send_btn);


        rectangle_btn = (ImageView)view.findViewById(R.id.rectangle_btn);
        circle_btn= (ImageView)view.findViewById(R.id.circle_btn);
        freehand_btn = (ImageView)view.findViewById(R.id.freehand_btn);
        line_btn = (ImageView)view.findViewById(R.id.line_btn);


        colorPicker_btn= (ImageView)view.findViewById(R.id.colorPicker_btn);
        currentShape_btn = (ImageView)view.findViewById(R.id.currentShape_btn);

        shape_layout = (LinearLayout)view.findViewById(R.id.shape_layout);
        color_layout = (RelativeLayout)view.findViewById(R.id.color_layout);

        shape_layout.setVisibility(View.GONE);

    }
}
