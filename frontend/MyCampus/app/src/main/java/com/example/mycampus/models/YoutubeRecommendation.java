package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class YoutubeRecommendation {
    @SerializedName("id")
    public int id;
    @SerializedName("subject")
    public int subject;
    @SerializedName("title")
    public String title;
    @SerializedName("video_url")
    public String videoUrl;
}
