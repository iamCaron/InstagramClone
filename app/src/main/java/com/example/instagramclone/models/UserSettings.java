package com.example.instagramclone.models;

public class UserSettings {
    private User user;
    private UserAccountSettings settings;

    public UserSettings(User user,UserAccountSettings userAccountSettings){
        this.user=user;
        this.settings=userAccountSettings;
    }

    public UserSettings(){}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserAccountSettings getSettings() {
        return settings;
    }

    public void setSettings(UserAccountSettings settings) {
        this.settings = settings;
    }


    @Override
    public String toString() {
        return "UserSettings{" +
                "user=" + user +
                ", settings=" + settings +
                '}';
    }
}
