package com.example.luffiadityasandy.canvaschat.view_holder;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.luffiadityasandy.canvaschat.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Luffi Aditya Sandy on 29/03/2017.
 */

public class OfflineCanvasHolder {
    public RelativeLayout root_layout, color_layout;
    public LinearLayout canvas, shape_layout;


    public CircleImageView send_btn,  currentColor_btn;
    public ImageView undo_btn,redo_btn, colorPicker_btn,currentShape_btn, freehand_btn, circle_btn, rectangle_btn, line_btn ;
    public OfflineCanvasHolder(View view) {
        //get button from UI
        canvas = (LinearLayout)view.findViewById(R.id.myCanvas);
        currentColor_btn = (CircleImageView)view.findViewById(R.id.current_color_btn);

        undo_btn = (ImageView) view.findViewById(R.id.undo_btn);
        redo_btn = (ImageView) view.findViewById(R.id.redo_btn);
        send_btn = (CircleImageView)view.findViewById(R.id.send_btn);


        rectangle_btn = (ImageView)view.findViewById(R.id.rectangle_btn);
        circle_btn= (ImageView)view.findViewById(R.id.circle_btn);
        freehand_btn = (ImageView)view.findViewById(R.id.freehand_btn);
        line_btn = (ImageView)view.findViewById(R.id.line_btn);


        colorPicker_btn= (ImageView)view.findViewById(R.id.colorPicker_btn);
        currentShape_btn = (ImageView)view.findViewById(R.id.currentShape_btn);

        shape_layout = (LinearLayout)view.findViewById(R.id.shape_layout);
        color_layout = (RelativeLayout)view.findViewById(R.id.color_layout);

        shape_layout.setVisibility(View.GONE);

        root_layout = (RelativeLayout)view.findViewById(R.id.popup_canvas);
    }
}
