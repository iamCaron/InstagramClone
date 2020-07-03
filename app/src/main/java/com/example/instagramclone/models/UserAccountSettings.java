package com.example.instagramclone.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserAccountSettings implements Parcelable {
    private String description,display_name,profile_photo,username,website,userId;
    private long followers,following,posts;

    public UserAccountSettings(String description, String display_name, long followers, long following, long posts, String profile_photo, String username, String website,String userId) {
        this.description = description;
        this.display_name = display_name;
        this.profile_photo = profile_photo;
        this.username = username;
        this.website = website;
        this.followers = followers;
        this.following = following;
        this.posts = posts;
        this.userId=userId;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserAccountSettings(){}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay_Name() {
        return display_name;
    }

    public void setDisplayName(String display_name) {
        this.display_name = display_name;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public long getFollowers() {
        return followers;
    }

    public void setFollowers(long followers) {
        this.followers = followers;
    }

    public long getFollowing() {
        return following;
    }

    public void setFollowing(long following) {
        this.following = following;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
