package com.tutump.tutumpdev.Models;

import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.common.data.DataHolder;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by casertillo on 05/07/16.
 */
public class User {

    private String name;
    private String About;
    private Double Age;
    private String dateOfBirth;
    private String work;
    private String education;
    private String profileImage;
    private String gender;
    private String Id;
    private String token;
    private Map<String, Object> pictures;
    private Collection<String> likes;
    //Search Options
    private Boolean searchVisibility;
    private Boolean notificationNewMatch;
    private Boolean notificationsNewMessage;

    public Collection<String> getLikes() {
        return likes;
    }

    public void setLikes(Collection<String> likes) {
        this.likes = likes;
    }
    public Map<String, Object> getPictures() {
        return pictures;
    }

    public void setPictures(Map<String, Object> pictures) {
        this.pictures = pictures;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getSearchVisibility() {
        return searchVisibility;
    }

    public void setSearchVisibility(Boolean searchVisibility) {
        this.searchVisibility = searchVisibility;
    }

    public Boolean getNotificationNewMatch() {
        return notificationNewMatch;
    }

    public void setNotificationNewMatch(Boolean notificationNewMatch) {
        this.notificationNewMatch = notificationNewMatch;
    }

    public Boolean getNotificationsNewMessage() {
        return notificationsNewMessage;
    }

    public void setNotificationsNewMessage(Boolean notificationsNewMessage) {
        this.notificationsNewMessage = notificationsNewMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return About;
    }

    public void setAbout(String about) {
        About = about;
    }

    public Double getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = Double.parseDouble(age);
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public User(){

    }
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public User(Bundle userInfo, String profileImage){

        this.setName(userInfo.getString("first_name"));
        this.setWork(userInfo.getString("work"));
        this.setEducation(userInfo.getString("education"));
        this.setGender(userInfo.getString("gender"));
        this.setDateOfBirth(userInfo.getString("birthday"));
        this.setToken(userInfo.getString("token"));

        //Search Preferences
        this.setSearchVisibility(userInfo.getBoolean("searchVisibility"));
        this.setNotificationNewMatch(userInfo.getBoolean("notificationNewMatch"));
        this.setNotificationsNewMessage(userInfo.getBoolean("notificationsNewMessage"));

        this.profileImage = profileImage;

    }
}
