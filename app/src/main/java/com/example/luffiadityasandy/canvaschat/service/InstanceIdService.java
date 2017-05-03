package com.example.luffiadityasandy.canvaschat.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Luffi Aditya Sandy on 28/02/2017.
 */

public class InstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("getToken",token);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser!=null){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("token",token);
            databaseReference.child("user_detail").child(firebaseUser.getUid()).updateChildren(hashMap);
        }
        else {
            Log.d("tokenservice", "onTokenRefresh: no user logged in");
        }

    }
}
