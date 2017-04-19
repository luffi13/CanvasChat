package com.example.luffiadityasandy.canvaschat.object;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Luffi Aditya Sandy on 10/04/2017.
 */

public class FriendMessage extends RealmObject{

    @PrimaryKey
    private String uid;

    private RealmList<Message> realmList;

    public FriendMessage() {
        realmList = new RealmList<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public RealmList<Message> getRealmList() {
        return realmList;
    }

    public void setRealmList(RealmList<Message> realmList) {
        this.realmList = realmList;
    }
}
