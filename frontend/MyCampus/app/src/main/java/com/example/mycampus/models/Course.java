package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class Course {
    @SerializedName("id")
    public int id;
    @SerializedName("name")
    public String name;
    @SerializedName("code")
    public String code;
}
