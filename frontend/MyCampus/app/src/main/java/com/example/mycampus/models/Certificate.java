package com.example.mycampus.models;

import com.google.gson.annotations.SerializedName;

public class Certificate {
    @SerializedName("id")
    public int id;
    @SerializedName("student")
    public int student;
    @SerializedName("title")
    public String title;
    @SerializedName("issued_by")
    public String issuedBy;
    @SerializedName("issue_date")
    public String issueDate;
    @SerializedName("certificate_url")
    public String certificateUrl;

    public Certificate() {}

    public Certificate(int id, String title, String issuedBy, String issueDate) {
        this.id = id;
        this.title = title;
        this.issuedBy = issuedBy;
        this.issueDate = issueDate;
    }
}
