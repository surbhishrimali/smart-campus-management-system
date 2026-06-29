package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    public int id;
    @SerializedName("username")
    public String username;
    @SerializedName("email")
    public String email;
    @SerializedName("role")
    public String role;
    @SerializedName("phone_number")
    public String phoneNumber;
    @SerializedName("password")
    public String password;

    public User() {}

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
