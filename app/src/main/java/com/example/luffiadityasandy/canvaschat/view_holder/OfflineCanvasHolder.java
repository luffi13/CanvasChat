package com.example.luffiadityasandy.canvaschat.view_holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.luffiadityasandy.canvaschat.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Luffi Aditya Sandy on 29/03/2017.
 */

public class OfflineCanvasHolder {
    public RelativeLayout root_layout, colorPickerLayout,shapePickerLayout;
    public LinearLayout canvas;

    public CircleImageView undo_btn,redo_btn, send_btn,colorPicker_btn, currentColor_btn, currentShape_btn, freehand_btn, circle_btn, rectangle_btn, line_btn, invite_btn;

    public OfflineCanvasHolder(View view) {
        //get button from UI
        undo_btn = (CircleImageView) view.findViewById(R.id.undo_btn);
        redo_btn = (CircleImageView) view.findViewById(R.id.redo_btn);
        send_btn = (CircleImageView)view.findViewById(R.id.send_btn);

        //save = (Button)findViewById(R.id.saveCanvas) ;
        rectangle_btn = (CircleImageView)view.findViewById(R.id.rectangle_btn);
        circle_btn= (CircleImageView)view.findViewById(R.id.circle_btn);
        freehand_btn = (CircleImageView)view.findViewById(R.id.freehand_btn);
        line_btn = (CircleImageView)view.findViewById(R.id.line_btn);
        //invite_btn = (Button) findViewById(R.id.invite_btn);
        canvas = (LinearLayout)view.findViewById(R.id.myCanvas);

        colorPicker_btn= (CircleImageView)view.findViewById(R.id.colorPicker_btn);

        currentShape_btn = (CircleImageView)view.findViewById(R.id.currentShape_btn);
        currentColor_btn = (CircleImageView)view.findViewById(R.id.currentColor_btn);

        colorPickerLayout = (RelativeLayout)view.findViewById(R.id.colorPicker_layout);
        shapePickerLayout = (RelativeLayout)view.findViewById(R.id.shapePicker_layout);
        root_layout = (RelativeLayout)view.findViewById(R.id.popup_canvas);
    }
}
