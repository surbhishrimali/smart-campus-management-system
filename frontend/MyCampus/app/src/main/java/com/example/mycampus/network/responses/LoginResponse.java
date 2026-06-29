package com.example.mycampus.network.responses;

import com.example.mycampus.models.User;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("access")
    private String access;
    
    @SerializedName("user")
    private User user;

    public LoginResponse(String access, User user) {
        this.access = access;
        this.user = user;
    }

    public String getAccess() { return access; }
    public User getUser() { return user; }
}
