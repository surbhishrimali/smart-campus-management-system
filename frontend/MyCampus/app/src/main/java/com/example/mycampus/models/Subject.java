package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class Subject {
    @SerializedName("id")
    public int id;
    @SerializedName("name")
    public String name;
    @SerializedName("semester")
    public int semester;
    @SerializedName("course")
    public int course;
}
