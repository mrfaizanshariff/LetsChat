package com.aelinstudios.sjcetalks.Models;

public class User {
    private String uid,name,phoneNumber, profileImage;
    //its a good practice to have an empty constructor when working with firebase


    public User() {
    }

    public User(String uid, String name, String phoneNumber, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
       this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
