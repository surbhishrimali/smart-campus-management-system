package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class Note {
    @SerializedName("id")
    public int id;
    @SerializedName("subject")
    public int subject;
    @SerializedName("title")
    public String title;
    @SerializedName("file_url")
    public String fileUrl;
}
