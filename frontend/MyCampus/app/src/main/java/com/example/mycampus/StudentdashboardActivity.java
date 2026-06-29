package com.example.mycampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    private TextView tvAttendancePercent, tvOverallGpa, tvTotalResources, tvPendingCerts;
    private TextView tvStudentAvatar;

    private List<Result> results = new ArrayList<>();
    private int totalNotesCount = 0;
    private int totalPyqCount = 0;

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
        tvStudentAvatar = findViewById(R.id.tvStudentAvatar);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvEnrollmentNumber = findViewById(R.id.tvEnrollmentNumber);
        tvDepartment = findViewById(R.id.tvDepartment);

        tvAttendancePercent = findViewById(R.id.tvAttendancePercent);
        tvOverallGpa = findViewById(R.id.tvOverallGpa);
        tvTotalResources = findViewById(R.id.tvTotalResources);
        tvPendingCerts = findViewById(R.id.tvPendingCerts);

        // Toolbar Back Button handling
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Apply Leave & Transcript Actions
        findViewById(R.id.btnLeave).setOnClickListener(v -> applyForLeave());
        findViewById(R.id.btnTranscript).setOnClickListener(v -> requestTranscript());

        // Bind Navigation Clicks
        findViewById(R.id.cvStudentNotices).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentNoticesActivity.class));
        });

        findViewById(R.id.cvStudentResources).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentResourcesActivity.class));
        });

        findViewById(R.id.cvStudentComplaints).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentComplaintsActivity.class));
        });

        findViewById(R.id.cvStudentResults).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentResultsActivity.class));
        });

        findViewById(R.id.cvStudentProjects).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentProjectsActivity.class));
        });

        findViewById(R.id.cvStudentCertificates).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentCertificatesActivity.class));
        });

        fetchData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchData();
    }

    private void fetchData() {
        SharedPreferences prefs = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", "");
        int userId = prefs.getInt("USER_ID", -1);
        String username = prefs.getString("USERNAME", "Student");

        tvStudentName.setText(username);
        if (username.length() > 0) {
            tvStudentAvatar.setText(username.substring(0, 1).toUpperCase());
        }

        if (token.isEmpty() || userId == -1) return;

        // 1. Fetch Student Profile for Enrollment & Dept
        RetrofitClient.getApiService().getStudentProfiles(token, userId).enqueue(new Callback<List<StudentProfile>>() {
            @Override
            public void onResponse(Call<List<StudentProfile>> call, Response<List<StudentProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    StudentProfile profile = response.body().get(0);
                    tvEnrollmentNumber.setText("Enrollment: " + (profile.enrollmentNumber != null ? profile.enrollmentNumber : "N/A"));
                    tvDepartment.setText("Department: " + (profile.department != null ? profile.department : "N/A"));
                }
            }
            @Override public void onFailure(Call<List<StudentProfile>> call, Throwable t) {}
        });

        // 2. Fetch Results for GPA
        RetrofitClient.getApiService().getResults(token).enqueue(new Callback<List<Result>>() {
            @Override
            public void onResponse(Call<List<Result>> call, Response<List<Result>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    results.clear();
                    for (Result r : response.body()) {
                        if (r.student == userId) results.add(r);
                    }
                    calculateOverallGpa();
                }
            }
            @Override public void onFailure(Call<List<Result>> call, Throwable t) {}
        });

        // 3. Fetch Certificates for Pending status count
        RetrofitClient.getApiService().getCertificates(token).enqueue(new Callback<List<Certificate>>() {
            @Override
            public void onResponse(Call<List<Certificate>> call, Response<List<Certificate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int pendingCount = 0;
                    for (Certificate c : response.body()) {
                        if (c.student == userId && "PENDING".equalsIgnoreCase(c.status)) {
                            pendingCount++;
                        }
                    }
                    tvPendingCerts.setText(String.valueOf(pendingCount));
                }
            }
            @Override public void onFailure(Call<List<Certificate>> call, Throwable t) {}
        });

        // 4. Fetch Attendance percent
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
                        tvAttendancePercent.setText(percent + "%");
                    } else {
                        tvAttendancePercent.setText("0%");
                    }
                }
            }
            @Override public void onFailure(Call<List<Attendance>> call, Throwable t) {}
        });

        // 5. Fetch Resources Counts
        totalNotesCount = 0;
        totalPyqCount = 0;

        RetrofitClient.getApiService().getNotes(token).enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    totalNotesCount = response.body().size();
                    updateResourcesTotal();
                }
            }
            @Override public void onFailure(Call<List<Note>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getPyqs(token).enqueue(new Callback<List<Pyq>>() {
            @Override
            public void onResponse(Call<List<Pyq>> call, Response<List<Pyq>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    totalPyqCount = response.body().size();
                    updateResourcesTotal();
                }
            }
            @Override public void onFailure(Call<List<Pyq>> call, Throwable t) {}
        });
    }

    private void updateResourcesTotal() {
        int sum = totalNotesCount + totalPyqCount;
        tvTotalResources.setText(String.valueOf(sum));
    }

    private void calculateOverallGpa() {
        if (results.isEmpty()) {
            tvOverallGpa.setText("0.0");
            return;
        }
        double totalGpa = 0;
        for (Result r : results) totalGpa += r.gpa;
        double avg = totalGpa / results.size();
        tvOverallGpa.setText(String.format(Locale.getDefault(), "%.2f", avg));
    }

    private void requestTranscript() {
        Toast.makeText(this, "Transcript request filed.", Toast.LENGTH_SHORT).show();
    }

    private void applyForLeave() {
        Toast.makeText(this, "Leave submitted.", Toast.LENGTH_SHORT).show();
    }
}
