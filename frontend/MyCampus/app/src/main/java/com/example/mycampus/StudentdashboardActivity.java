package com.example.mycampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mycampus.models.*;
import com.example.mycampus.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentdashboardActivity extends AppCompatActivity {

    private TextView tvStudentName, tvEnrollmentNumber, tvDepartment;
    private TextView tvAttendancePercent;
    private ProgressBar pbAttendance;
    private TextView tvOverallGpa;
    private LinearLayout llGpaChart;
    private LinearLayout llNotices;
    private LinearLayout llResults;
    private LinearLayout llProjects;
    private LinearLayout llCertificates;

    private List<Result> results = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private List<Certificate> certificates = new ArrayList<>();
    private List<Notice> notices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_studentdashboard);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize Views
        tvStudentName = findViewById(R.id.tvStudentName);
        tvEnrollmentNumber = findViewById(R.id.tvEnrollmentNumber);
        tvDepartment = findViewById(R.id.tvDepartment);
        
        tvAttendancePercent = findViewById(R.id.tvAttendancePercent);
        pbAttendance = findViewById(R.id.pbAttendance);
        tvOverallGpa = findViewById(R.id.tvOverallGpa);
        llGpaChart = findViewById(R.id.llGpaChart);
        llNotices = findViewById(R.id.llNotices);
        llResults = findViewById(R.id.llResults);
        llProjects = findViewById(R.id.llProjects);
        llCertificates = findViewById(R.id.llCertificates);

        findViewById(R.id.tvAttendancePercent).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentAttendanceActivity.class));
        });

        findViewById(R.id.btnViewResults).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentResultsActivity.class));
        });

        findViewById(R.id.btnTranscript).setOnClickListener(v -> requestTranscript());
        findViewById(R.id.btnLeave).setOnClickListener(v -> applyForLeave());
        findViewById(R.id.btnResources).setOnClickListener(v -> {
            startActivity(new Intent(this, ResourcesActivity.class));
        });
        findViewById(R.id.btnNotices).setOnClickListener(v -> {
            startActivity(new Intent(this, NoticeActivity.class));
        });

        fetchData();
    }

    private void fetchData() {
        SharedPreferences prefs = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", "");
        int userId = prefs.getInt("USER_ID", -1);
        String username = prefs.getString("USERNAME", "Student");
        
        tvStudentName.setText(username);

        if (token.isEmpty() || userId == -1) return;

        RetrofitClient.getApiService().getStudentProfiles(token, userId).enqueue(new Callback<List<StudentProfile>>() {
            @Override
            public void onResponse(Call<List<StudentProfile>> call, Response<List<StudentProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    StudentProfile profile = response.body().get(0);
                    tvEnrollmentNumber.setText("Enrollment: " + profile.enrollmentNumber);
                    tvDepartment.setText("Department: " + profile.department);
                }
            }
            @Override public void onFailure(Call<List<StudentProfile>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getResults(token).enqueue(new Callback<List<Result>>() {
            @Override
            public void onResponse(Call<List<Result>> call, Response<List<Result>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    results.clear();
                    for (Result r : response.body()) if (r.student == userId) results.add(r);
                    updateResultsUI();
                    calculateOverallGpa();
                    updateGpaTrendUI();
                }
            }
            @Override public void onFailure(Call<List<Result>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getNotices(token).enqueue(new Callback<List<Notice>>() {
            @Override
            public void onResponse(Call<List<Notice>> call, Response<List<Notice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notices.clear();
                    notices.addAll(response.body());
                    updateNoticesUI();
                }
            }
            @Override public void onFailure(Call<List<Notice>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getProjects(token).enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    projects.clear();
                    for (Project p : response.body()) if (p.student == userId) projects.add(p);
                    updateProjectsUI();
                }
            }
            @Override public void onFailure(Call<List<Project>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getCertificates(token).enqueue(new Callback<List<Certificate>>() {
            @Override
            public void onResponse(Call<List<Certificate>> call, Response<List<Certificate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    certificates.clear();
                    for (Certificate c : response.body()) if (c.student == userId) certificates.add(c);
                    updateCertificatesUI();
                }
            }
            @Override public void onFailure(Call<List<Certificate>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getAttendance(token).enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int presentCount = 0;
                    int myTotal = 0;
                    for (Attendance a : response.body()) {
                        if (a.student == userId) {
                            myTotal++;
                            if (a.isPresent) presentCount++;
                        }
                    }
                    if (myTotal > 0) {
                        int percent = (presentCount * 100) / myTotal;
                        updateAttendance(percent);
                    } else {
                        updateAttendance(0);
                    }
                }
            }
            @Override public void onFailure(Call<List<Attendance>> call, Throwable t) {}
        });
    }

    private void updateAttendance(int percent) {
        tvAttendancePercent.setText(percent + "%");
        pbAttendance.setProgress(percent);
    }

    private void calculateOverallGpa() {
        if (results.isEmpty()) {
            tvOverallGpa.setText("Overall: 0.0");
            return;
        }
        double totalGpa = 0;
        for (Result r : results) totalGpa += r.gpa;
        double avg = totalGpa / results.size();
        tvOverallGpa.setText(String.format(Locale.getDefault(), "Overall: %.2f", avg));
    }

    private void updateResultsUI() {
        llResults.removeAllViews();
        for (Result res : results) {
            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llResults, false);
            TextView t1 = itemView.findViewById(android.R.id.text1);
            TextView t2 = itemView.findViewById(android.R.id.text2);
            t1.setText("Semester " + res.semester + " - GPA: " + res.gpa);
            t2.setText(res.hasBacklog ? "Backlogs: " + res.backlogSubjects : "No Backlogs");
            llResults.addView(itemView);
        }
    }

    private void updateNoticesUI() {
        llNotices.removeAllViews();
        int count = 0;
        for (Notice n : notices) {
            if (count >= 3) break; // Only show 3 alerts on dashboard
            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llNotices, false);
            TextView t1 = itemView.findViewById(android.R.id.text1);
            TextView t2 = itemView.findViewById(android.R.id.text2);
            t1.setText(n.title);
            t2.setText(n.content);
            llNotices.addView(itemView);
            count++;
        }
    }

    private void updateGpaTrendUI() {
        llGpaChart.removeAllViews();
        for (Result res : results) {
            View bar = new View(this);
            int height = (int) (res.gpa * 10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, height);
            params.setMargins(4, 0, 4, 0);
            bar.setLayoutParams(params);
            bar.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            llGpaChart.addView(bar);
        }
    }

    private void updateProjectsUI() {
        llProjects.removeAllViews();
        for (Project proj : projects) {
            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llProjects, false);
            TextView t1 = itemView.findViewById(android.R.id.text1);
            TextView t2 = itemView.findViewById(android.R.id.text2);
            t1.setText(proj.title);
            t2.setText(proj.description);
            llProjects.addView(itemView);
        }
    }

    private void updateCertificatesUI() {
        llCertificates.removeAllViews();
        for (Certificate cert : certificates) {
            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llCertificates, false);
            TextView t1 = itemView.findViewById(android.R.id.text1);
            TextView t2 = itemView.findViewById(android.R.id.text2);
            t1.setText(cert.title);
            t2.setText(cert.issuedBy + " - " + cert.issueDate);
            llCertificates.addView(itemView);
        }
    }

    private void requestTranscript() {
        Toast.makeText(this, "Transcript request filed.", Toast.LENGTH_SHORT).show();
    }

    private void applyForLeave() {
        Toast.makeText(this, "Leave submitted.", Toast.LENGTH_SHORT).show();
    }
}
