package com.example.mycampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mycampus.models.*;
import com.example.mycampus.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class facultydsashboardActivity extends AppCompatActivity {

    private TextView tvFacultyId, tvFacultyDept, tvFacultyDesignation;
    private LinearLayout llFacultyResults, llFacultyAttendance;

    private List<Result> results = new ArrayList<>();
    private List<Attendance> attendances = new ArrayList<>();
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_facultydsashboard);

        View mainView = findViewById(android.R.id.content);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        tvFacultyId = findViewById(R.id.tvFacultyId);
        tvFacultyDept = findViewById(R.id.tvFacultyDept);
        tvFacultyDesignation = findViewById(R.id.tvFacultyDesignation);
        llFacultyResults = findViewById(R.id.llFacultyResults);
        llFacultyAttendance = findViewById(R.id.llFacultyAttendance);

        token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");

        findViewById(R.id.btnGenResult).setOnClickListener(v -> {
            startActivity(new Intent(this, ResultManagementActivity.class));
        });

        findViewById(R.id.btnLogAttendance).setOnClickListener(v -> {
            startActivity(new Intent(this, AttendanceManagementActivity.class));
        });

        findViewById(R.id.cvUploadResources).setOnClickListener(v -> {
            startActivity(new Intent(this, UploadResourceActivity.class));
        });
        
        findViewById(R.id.cvManageNotices).setOnClickListener(v -> {
            startActivity(new Intent(this, NoticeActivity.class));
        });

        fetchData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchData();
    }

    private void fetchData() {
        int userId = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getInt("USER_ID", -1);
        if (token.isEmpty()) return;

        if (userId != -1) {
            RetrofitClient.getApiService().getFacultyProfiles(token, userId).enqueue(new Callback<List<FacultyProfile>>() {
                @Override public void onResponse(Call<List<FacultyProfile>> call, Response<List<FacultyProfile>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        FacultyProfile p = response.body().get(0);
                        tvFacultyId.setText(p.facultyId);
                        tvFacultyDept.setText(p.department);
                        tvFacultyDesignation.setText(p.designation);
                    }
                }
                @Override public void onFailure(Call<List<FacultyProfile>> call, Throwable t) {}
            });
        }

        RetrofitClient.getApiService().getResults(token).enqueue(new Callback<List<Result>>() {
            @Override public void onResponse(Call<List<Result>> call, Response<List<Result>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    results.clear();
                    results.addAll(response.body());
                    updateResultsUI();
                }
            }
            @Override public void onFailure(Call<List<Result>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getAttendance(token).enqueue(new Callback<List<Attendance>>() {
            @Override public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    attendances.clear();
                    attendances.addAll(response.body());
                    updateAttendanceUI();
                }
            }
            @Override public void onFailure(Call<List<Attendance>> call, Throwable t) {}
        });
    }

    private void updateResultsUI() {
        llFacultyResults.removeAllViews();
        int count = 0;
        for (Result r : results) {
            if (count >= 5) break;
            View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llFacultyResults, false);
            ((TextView) row.findViewById(android.R.id.text1)).setText("Student ID: " + r.student);
            ((TextView) row.findViewById(android.R.id.text2)).setText("GPA: " + r.gpa + " (Sem " + r.semester + ")");
            llFacultyResults.addView(row);
            count++;
        }
    }

    private void updateAttendanceUI() {
        llFacultyAttendance.removeAllViews();
        int count = 0;
        for (Attendance a : attendances) {
            if (count >= 5) break;
            View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llFacultyAttendance, false);
            ((TextView) row.findViewById(android.R.id.text1)).setText(a.date);
            ((TextView) row.findViewById(android.R.id.text2)).setText("Student ID: " + a.student + (a.isPresent ? " - Present" : " - Absent"));
            llFacultyAttendance.addView(row);
            count++;
        }
    }
}
