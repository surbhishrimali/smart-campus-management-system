package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class Project {
    @SerializedName("id")
    public int id;
    @SerializedName("student")
    public int student;
    @SerializedName("title")
    public String title;
    @SerializedName("description")
    public String description;
    @SerializedName("link")
    public String link;

    public Project() {}

    public Project(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }
}
