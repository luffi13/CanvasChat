package com.example.luffiadityasandy.canvaschat.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Luffi Aditya Sandy on 01/03/2017.
 */

public interface ServiceMessaging {

    @POST("fcm/send")
    Call<JsonElement> sendNotification(@Header("Authorization") String key, @Header("Content-Type") String contentType,
                                       @Body JsonObject data);


}
