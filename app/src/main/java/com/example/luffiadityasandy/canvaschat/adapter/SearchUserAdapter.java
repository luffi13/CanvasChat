package com.example.luffiadityasandy.canvaschat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.object.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Luffi Aditya Sandy on 16/03/2017.
 */

public class SearchUserAdapter extends ArrayAdapter<User> {
    private List<User> allData;
    private List<User> filteredData;

    private int resource;
    private Context context;


    public SearchUserAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.allData = objects;
        this.filteredData = objects;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final User user = getItem(position);
        UserHolder userHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(resource,parent,false);
            userHolder = new UserHolder();
//            userHolder.email = (TextView)convertView.findViewById(R.id.email);
            userHolder.displayName = (TextView)convertView.findViewById(R.id.displayName);
            userHolder.userPhoto = (CircleImageView)convertView.findViewById(R.id.photo);
            userHolder.user_ll = (LinearLayout)convertView.findViewById(R.id.friend_ll);
            convertView.setTag(userHolder);
        }
        else {
            userHolder = (UserHolder)convertView.getTag();
        }
//        userHolder.email.setText(user.getEmail());
        userHolder.displayName.setText(user.getName());
        if (user.getPhotoUrl()==null){
            userHolder.userPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_account_circle));
        }
        else {
            Glide.with(context).load(user.getPhotoUrl()).into(userHolder.userPhoto);
        }

        userHolder.user_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopUpWindow(user,parent);
            }
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Nullable
    @Override
    public User getItem(int position) {
        return filteredData.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if(constraint==null||constraint.length()==0){
                    results.values = allData;
                    results.count = allData.size();
                }
                else {
                    ArrayList<User> resultFilter = new ArrayList<>();
                    for (User data : allData){
                        if(data.getName().toLowerCase().contains(constraint)
                            ||data.email.toLowerCase().contains(constraint)){
                            resultFilter.add(data);
                        }
                    }
                    results.values = resultFilter;
                    results.count = resultFilter.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData  = (ArrayList<User>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    private void initiatePopUpWindow(User user, View view){
        PopupWindow popupWindow;
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.user_preview,null);
        popupWindow = new PopupWindow(layout,500,500,true);
        popupWindow.showAtLocation(view, Gravity.CENTER,0,0);

        PreviewHolder previewHolder = new PreviewHolder(layout);
        previewHolder.name_tv.setText(user.getName());
        previewHolder.email_tv.setText(user.getEmail());
        if (user.getPhotoUrl()==null){
            previewHolder.userPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_account_circle));
        }
        else {
            Glide.with(context).load(user.getPhotoUrl()+"?sz=300").into(previewHolder.userPhoto);
        }
        previewHolder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "add button clicked", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private class UserHolder{
        public TextView displayName;
        public CircleImageView userPhoto;
        public LinearLayout user_ll;

    }

    private class PreviewHolder{
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
}
