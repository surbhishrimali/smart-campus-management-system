package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class FacultyProfile {
    @SerializedName("id")
    public int id;
    @SerializedName("user")
    public int user;
    @SerializedName("faculty_id")
    public String facultyId;
    @SerializedName("department")
    public String department;
    @SerializedName("designation")
    public String designation;
}
