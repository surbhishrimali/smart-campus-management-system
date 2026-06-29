package com.example.mycampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class admindashboardActivity extends AppCompatActivity {

    private TextView tvTotalStudents, tvTotalFaculty, tvTotalResources, tvTotalNotices, tvTotalCerts, tvTotalComplaints;
    private TextView tvAdminName;

    private List<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admindashboard);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize Views
        tvAdminName = findViewById(R.id.tvAdminName);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTotalFaculty = findViewById(R.id.tvTotalFaculty);
        tvTotalResources = findViewById(R.id.tvTotalResources);
        tvTotalNotices = findViewById(R.id.tvTotalNotices);
        tvTotalCerts = findViewById(R.id.tvTotalCerts);
        tvTotalComplaints = findViewById(R.id.tvTotalComplaints);

        // Welcome Username Set
        String username = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("USERNAME", "Admin");
        tvAdminName.setText("Welcome, " + username);

        // Bind Navigation Clicks
        findViewById(R.id.cvManageUsers).setOnClickListener(v -> {
            startActivity(new Intent(this, ManageUsersActivity.class));
        });

        findViewById(R.id.cvManageComplaints).setOnClickListener(v -> {
            startActivity(new Intent(this, ManageComplaintsActivity.class));
        });

        findViewById(R.id.cvCampusNotices).setOnClickListener(v -> {
            startActivity(new Intent(this, CampusNoticesActivity.class));
        });

        fetchData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchData(); // Reload totals on resume
    }

    private void fetchData() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().getUsers(token).enqueue(new Callback<List<User>>() {
            @Override public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    users.clear();
                    users.addAll(response.body());
                    calculateTotals();
                }
            }
            @Override public void onFailure(Call<List<User>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getComplaints(token).enqueue(new Callback<List<Complaint>>() {
            @Override public void onResponse(Call<List<Complaint>> call, Response<List<Complaint>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvTotalComplaints.setText(String.valueOf(response.body().size()));
                }
            }
            @Override public void onFailure(Call<List<Complaint>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getNotices(token).enqueue(new Callback<List<Notice>>() {
            @Override public void onResponse(Call<List<Notice>> call, Response<List<Notice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvTotalNotices.setText(String.valueOf(response.body().size()));
                }
            }
            @Override public void onFailure(Call<List<Notice>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getCertificates(token).enqueue(new Callback<List<Certificate>>() {
            @Override public void onResponse(Call<List<Certificate>> call, Response<List<Certificate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvTotalCerts.setText(String.valueOf(response.body().size()));
                }
            }
            @Override public void onFailure(Call<List<Certificate>> call, Throwable t) {}
        });
    }

    private void calculateTotals() {
        int studentCount = 0;
        int facultyCount = 0;
        for (User u : users) {
            if ("STUDENT".equals(u.role)) studentCount++;
            else if ("FACULTY".equals(u.role)) facultyCount++;
        }
        tvTotalStudents.setText(String.valueOf(studentCount));
        tvTotalFaculty.setText(String.valueOf(facultyCount));
        // Simple mock of resources count for representation
        tvTotalResources.setText("15"); 
    }
}
