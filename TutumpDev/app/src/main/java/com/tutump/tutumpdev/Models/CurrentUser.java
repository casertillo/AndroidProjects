package com.tutump.tutumpdev.Models;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by casertillo on 18/07/16.
 */
public class CurrentUser {
    private static CurrentUser mInstance = null;
    private String name;
    private String age = "";
    private String job = "";
    private String education = "";
    private ArrayList<String> pictures = null;
    private Map<String, Object> dashboardPictures=  null;
    private String about = "";
    private Uri profilePicture;
    private Double Lat;
    private Double Lng;
    private Collection<String> viewedProfiles = null;

    public void setViewedProfiles(Collection<String> viewed){
        this.viewedProfiles = viewed;
    }
    public Collection<String> getViewedProfiles(){
        return this.viewedProfiles;
    }
    public void addViewedProfiles(String profileId)
    {
        Set id = new HashSet();
        id.add(profileId);
        if(this.viewedProfiles == null) {
            this.viewedProfiles = id;
        } else {
            List list = new ArrayList(this.viewedProfiles);
            list.add(profileId);
            this.viewedProfiles = new HashSet<String>(list);
        }
    }

    public Double getLat() {
        return Lat;
    }

    public void setLat(Double lat) {
        Lat = lat;
    }

    public Double getLng() {
        return Lng;
    }

    public void setLng(Double lng) {
        Lng = lng;
    }

    public Map<String, Object> getdashboardPicturess() {
        return dashboardPictures;
    }

    public void setdashboardPictures(Map<String, Object> profilePictures) {
        this.dashboardPictures = profilePictures;
    }

    public void adddashboardPictures(String key, String image){
        dashboardPictures.put(key, image);
    }
    public void removeDashboardPictures(String key){
        dashboardPictures.remove(key);
    }
    public Uri getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Uri profilePicture) {
        this.profilePicture = profilePicture;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String Age) {
        age = Age;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String Job) {
        job = Job;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void addPicture(String image)
    {
        pictures.add(image);
    }
    private CurrentUser()
    {

    }
    public static CurrentUser getCurrentUser() {
        if(mInstance == null)
        {
            mInstance = new CurrentUser();
        }
        return mInstance;
    }
}
