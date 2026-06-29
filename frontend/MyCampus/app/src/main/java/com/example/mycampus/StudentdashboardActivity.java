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
    private LinearLayout llComplaints;
    private LinearLayout llResults;
    private LinearLayout llProjects;
    private LinearLayout llCertificates;

    private List<Result> results = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private List<Certificate> certificates = new ArrayList<>();
    private List<Complaint> complaints = new ArrayList<>();

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
        llComplaints = findViewById(R.id.llComplaints);
        llResults = findViewById(R.id.llResults);
        llProjects = findViewById(R.id.llProjects);
        llCertificates = findViewById(R.id.llCertificates);

        findViewById(R.id.tvAttendancePercent).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentAttendanceActivity.class));
        });

        findViewById(R.id.btnViewResults).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentResultsActivity.class));
        });

        findViewById(R.id.btnNewComplaint).setOnClickListener(v -> showFileComplaintDialog());

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

        RetrofitClient.getApiService().getComplaints(token).enqueue(new Callback<List<Complaint>>() {
            @Override
            public void onResponse(Call<List<Complaint>> call, Response<List<Complaint>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    complaints.clear();
                    complaints.addAll(response.body());
                    updateComplaintsUI();
                }
            }
            @Override public void onFailure(Call<List<Complaint>> call, Throwable t) {}
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

    private void updateComplaintsUI() {
        llComplaints.removeAllViews();
        int count = 0;
        for (Complaint c : complaints) {
            if (count >= 5) break; // Only show 5 complaints on dashboard
            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llComplaints, false);
            TextView t1 = itemView.findViewById(android.R.id.text1);
            TextView t2 = itemView.findViewById(android.R.id.text2);
            t1.setText(c.title + " (" + c.status + ")");
            t2.setText(c.description);
            llComplaints.addView(itemView);
            count++;
        }
    }

    private void showFileComplaintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File New Complaint");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final android.widget.EditText etTitleInput = new android.widget.EditText(this);
        etTitleInput.setHint("Complaint Title");
        layout.addView(etTitleInput);

        final android.widget.EditText etDescInput = new android.widget.EditText(this);
        etDescInput.setHint("Description");
        etDescInput.setMinLines(3);
        layout.addView(etDescInput);

        final android.widget.Spinner spPriority = new android.widget.Spinner(this);
        String[] priorities = {"LOW", "MEDIUM", "HIGH"};
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(spinnerAdapter);
        spPriority.setSelection(1); // MEDIUM default
        layout.addView(spPriority);

        builder.setView(layout);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String title = etTitleInput.getText().toString().trim();
            String desc = etDescInput.getText().toString().trim();
            String priority = spPriority.getSelectedItem().toString();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            submitComplaint(title, desc, priority);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void submitComplaint(String title, String description, String priority) {
        SharedPreferences prefs = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", "");
        int userId = prefs.getInt("USER_ID", -1);
        if (token.isEmpty() || userId == -1) return;

        Complaint complaint = new Complaint();
        complaint.student = userId;
        complaint.title = title;
        complaint.description = description;
        complaint.status = "PENDING";

        RetrofitClient.getApiService().postComplaint(token, complaint).enqueue(new Callback<Complaint>() {
            @Override
            public void onResponse(Call<Complaint> call, Response<Complaint> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudentdashboardActivity.this, "Complaint filed successfully!", Toast.LENGTH_SHORT).show();
                    fetchData(); // Reload data
                } else {
                    Toast.makeText(StudentdashboardActivity.this, "Failed to file complaint", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Complaint> call, Throwable t) {
                Toast.makeText(StudentdashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
