package com.example.luffiadityasandy.canvaschat.object;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Luffi Aditya Sandy on 16/02/2017.
 */

@IgnoreExtraProperties
public class User extends RealmObject implements Serializable {

    @PrimaryKey
    private String uid;

    private String email;
    private String name;
    private String token;
    private String state;
    private Boolean isEverChat;
    private String photoUrl;

    public User(String uid, String email, String name, String token,String photoUrl) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.token = token;
        this.photoUrl = photoUrl;
    }

    public User(){

    }

    public String getPhotoUrl() {
        return photoUrl;
    }


    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getIsEverChat() {
        return isEverChat;
    }

    public void setIsEverChat(Boolean everChat) {
        isEverChat = everChat;
    }

}
