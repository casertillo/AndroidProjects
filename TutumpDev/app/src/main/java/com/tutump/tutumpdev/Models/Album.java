package com.tutump.tutumpdev.Models;

import java.io.StringBufferInputStream;

/**
 * Created by casertillo on 25/07/16.
 * Album class functions for the facebook graph response.
 */
public class Album {

    private String id;
    private String name;
    private String imageUrl;
    private String imageCount;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageCount() {
        return imageCount;
    }

    public void setImageCount(String imageCount) {
        this.imageCount = imageCount;
    }

}
