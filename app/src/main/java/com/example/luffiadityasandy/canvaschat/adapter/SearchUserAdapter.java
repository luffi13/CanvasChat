package com.example.luffiadityasandy.canvaschat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.luffiadityasandy.canvaschat.R;
import com.example.luffiadityasandy.canvaschat.activity.SearchFriendActivity;
import com.example.luffiadityasandy.canvaschat.object.User;
import com.example.luffiadityasandy.canvaschat.view_holder.PreviewHolder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Luffi Aditya Sandy on 16/03/2017.
 */

public class SearchUserAdapter extends ArrayAdapter<User> {
    private List<User> allData;
    private List<User> filteredData;
    User mUser;
    DatabaseReference databaseReference;

    private HashMap<String,User> listFriend;
    private int resource;
    private Context context;


    public SearchUserAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.allData = objects;
        this.filteredData = objects;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser = new User(firebaseUser.getUid(),firebaseUser.getEmail(),firebaseUser.getDisplayName(), FirebaseInstanceId.getInstance().getToken(),firebaseUser.getPhotoUrl().toString());
        databaseReference = FirebaseDatabase.getInstance().getReference();
        listFriend = new HashMap<>();
        getListFriend();
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
                            ||data.getEmail().toLowerCase().contains(constraint)){
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

    private void getListFriend(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("friendship/"+mUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String,User>> type = new GenericTypeIndicator<HashMap<String,User>>() {};
                listFriend = dataSnapshot.getValue(type);
                SearchUserAdapter.this.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initiatePopUpWindow(final User user, View view){
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        final PopupWindow popupWindow;
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.user_preview,(ViewGroup)view.findViewById(R.id.preview_layout));
        int width = view.getWidth()*6/7;
        int height = view.getHeight()*6/7;
        popupWindow = new PopupWindow(layout, width, height,true);
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
        if (listFriend!=null&&listFriend.get(user.getUid())!=null){
            previewHolder.addButton.setVisibility(View.GONE);
        }
        else {
            previewHolder.addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriend(user);
                    popupWindow.dismiss();
                }
            });
        }

    }

    private void addFriend(final User friend){
        friend.setState("friend");
        databaseReference.child("friendship/"+mUser.getUid()+"/"+friend.getUid()).setValue(friend)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(listFriend==null){
                            listFriend = new HashMap<>();
                        }
                        listFriend.put(friend.getUid(),friend);
                        SearchUserAdapter.this.notifyDataSetChanged();
                        Toast.makeText(context, "friend added!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "failed to add friend", Toast.LENGTH_SHORT).show();
                    }
                });
        mUser.setState("request");
        databaseReference.child("friendship/"+friend.getUid()+"/"+mUser.getUid()).setValue(mUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private class UserHolder{
        TextView displayName;
        CircleImageView userPhoto;
        LinearLayout user_ll;

    }


}
