package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class Attendance {
    @SerializedName("id")
    public int id;
    @SerializedName("student")
    public int student;
    @SerializedName("student_class")
    public int studentClass;
    @SerializedName("date")
    public String date;
    @SerializedName("is_present")
    public boolean isPresent;
}
