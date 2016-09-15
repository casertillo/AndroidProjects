package com.tutump.tutumpdev.Models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by casertillo on 05/08/16.
 */
public class Match {
    private String id;
    private String name;
    private String photoUrl;
    private String chatId;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Match(){

    }
    public Match(String id, String name, String photoUrl, String token){
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.token = token;
    }
    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("photoUrl", photoUrl);
        result.put("chatId", chatId);
        result.put("token", token);
        return result;
    }
}
