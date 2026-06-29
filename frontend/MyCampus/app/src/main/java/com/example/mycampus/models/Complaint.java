package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class Complaint {
    @SerializedName("id")
    public int id;
    @SerializedName("student")
    public int student;
    @SerializedName("title")
    public String title;
    @SerializedName("description")
    public String description;
    @SerializedName("status")
    public String status;
}
