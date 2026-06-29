package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class StudentProfile {
    @SerializedName("id")
    public int id;
    @SerializedName("user")
    public int user;
    @SerializedName("enrollment_number")
    public String enrollmentNumber;
    @SerializedName("department")
    public String department;
    @SerializedName("current_semester")
    public int currentSemester;
    @SerializedName("semester")
    public int semester;
}
