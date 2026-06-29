package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class Result {
    @SerializedName("id")
    public int id;
    @SerializedName("student")
    public int student;
    @SerializedName("semester")
    public int semester;
    @SerializedName("gpa")
    public double gpa;
    @SerializedName("has_backlog")
    public boolean hasBacklog;
    @SerializedName("backlog_subjects")
    public String backlogSubjects;

    public Result() {}

    public Result(int semester, double gpa, boolean hasBacklog, String backlogSubjects) {
        this.semester = semester;
        this.gpa = gpa;
        this.hasBacklog = hasBacklog;
        this.backlogSubjects = backlogSubjects;
    }
}
