package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class Notice {
    @SerializedName("id")
    public int id;
    @SerializedName("title")
    public String title;
    @SerializedName("content")
    public String content;
    @SerializedName("author")
    public int author;
    @SerializedName("created_at")
    public String createdAt;

    public Notice() {}

    public Notice(String title, String content, int author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }
}
