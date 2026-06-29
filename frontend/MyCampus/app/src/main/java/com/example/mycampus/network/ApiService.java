package com.example.mycampus.network;

import com.example.mycampus.models.*;
import com.example.mycampus.network.responses.LoginResponse;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    // 1. Login Endpoint
    @POST("api/auth/login/")
    Call<LoginResponse> loginUser(@Body Map<String, String> credentials);

    // Existing generic endpoints (Updates with Token Header)
    @GET("api/users/")
    Call<List<User>> getUsers(@Header("Authorization") String token);

    @POST("api/users/")
    Call<User> createUser(@Header("Authorization") String token, @Body User user);

    @DELETE("api/users/{id}/")
    Call<Void> deleteUser(@Header("Authorization") String token, @Path("id") int id);

    @PUT("api/users/{id}/")
    Call<User> updateUser(@Header("Authorization") String token, @Path("id") int id, @Body User user);

    @POST("api/student-profiles/")
    Call<StudentProfile> createStudentProfile(@Header("Authorization") String token, @Body StudentProfile profile);

    @POST("api/faculty-profiles/")
    Call<FacultyProfile> createFacultyProfile(@Header("Authorization") String token, @Body FacultyProfile profile);

    @GET("api/student-profiles/")
    Call<List<StudentProfile>> getStudentProfiles(@Header("Authorization") String token, @Query("user_id") Integer userId);

    @GET("api/faculty-profiles/")
    Call<List<FacultyProfile>> getFacultyProfiles(@Header("Authorization") String token, @Query("user_id") Integer userId);

    @GET("api/results/")
    Call<List<Result>> getResults(@Header("Authorization") String token);

    @POST("api/results/")
    Call<Result> postResult(@Header("Authorization") String token, @Body Result result);

    @DELETE("api/results/{id}/")
    Call<Void> deleteResult(@Header("Authorization") String token, @Path("id") int id);

    @GET("api/projects/")
    Call<List<Project>> getProjects(@Header("Authorization") String token);

    @POST("api/projects/")
    Call<Project> postProject(@Header("Authorization") String token, @Body Project project);

    @DELETE("api/projects/{id}/")
    Call<Void> deleteProject(@Header("Authorization") String token, @Path("id") int id);

    @GET("api/certificates/")
    Call<List<Certificate>> getCertificates(@Header("Authorization") String token);

    @POST("api/certificates/")
    Call<Certificate> postCertificate(@Header("Authorization") String token, @Body Certificate certificate);

    @DELETE("api/certificates/{id}/")
    Call<Void> deleteCertificate(@Header("Authorization") String token, @Path("id") int id);

    @GET("api/notices/")
    Call<List<Notice>> getNotices(@Header("Authorization") String token);

    @POST("api/notices/")
    Call<Notice> postNotice(@Header("Authorization") String token, @Body Notice notice);

    @DELETE("api/notices/{id}/")
    Call<Void> deleteNotice(@Header("Authorization") String token, @Path("id") int id);

    @PUT("api/notices/{id}/")
    Call<Notice> updateNotice(@Header("Authorization") String token, @Path("id") int id, @Body Notice notice);

    @GET("api/attendance/")
    Call<List<Attendance>> getAttendance(@Header("Authorization") String token);

    @POST("api/attendance/")
    Call<Attendance> postAttendance(@Header("Authorization") String token, @Body Attendance attendance);

    @DELETE("api/attendance/{id}/")
    Call<Void> deleteAttendance(@Header("Authorization") String token, @Path("id") int id);

    @GET("api/notes/")
    Call<List<Note>> getNotes(@Header("Authorization") String token);

    @Multipart
    @POST("api/notes/")
    Call<Note> uploadNote(@Header("Authorization") String token,
                          @Part("title") okhttp3.RequestBody title,
                          @Part okhttp3.MultipartBody.Part file);

    @GET("api/pyqs/")
    Call<List<Pyq>> getPyqs(@Header("Authorization") String token);

    @GET("api/youtube-recommendations/")
    Call<List<YoutubeRecommendation>> getYoutubeRecommendations(@Header("Authorization") String token);

    @GET("api/courses/")
    Call<List<Course>> getCourses(@Header("Authorization") String token);

    @GET("api/subjects/")
    Call<List<Subject>> getSubjects(@Header("Authorization") String token);

    @GET("api/complaints/")
    Call<List<Complaint>> getComplaints(@Header("Authorization") String token);

    @POST("api/complaints/")
    Call<Complaint> postComplaint(@Header("Authorization") String token, @Body Complaint complaint);

    @PATCH("api/complaints/{id}/")
    Call<Complaint> updateComplaintStatus(@Header("Authorization") String token, @Path("id") int id, @Body Map<String, String> status);
}
