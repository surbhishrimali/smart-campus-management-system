package com.example.mycampus;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycampus.models.Result;
import com.example.mycampus.models.Subject;
import com.example.mycampus.models.User;
import com.example.mycampus.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultManagementActivity extends AppCompatActivity {

    private Spinner spSubject;
    private RecyclerView rvStudents;
    private ProgressBar progressBar;
    
    private List<User> students = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result_management);

        token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");

        spSubject = findViewById(R.id.spSubject);
        rvStudents = findViewById(R.id.rvStudents);
        progressBar = findViewById(R.id.progressBar);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        fetchData();
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getApiService().getSubjects(token).enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects = response.body();
                    List<String> names = new ArrayList<>();
                    for (Subject s : subjects) names.add(s.name);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ResultManagementActivity.this, android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spSubject.setAdapter(adapter);
                }
            }
            @Override public void onFailure(Call<List<Subject>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getUsers(token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    students.clear();
                    for (User u : response.body()) {
                        if ("STUDENT".equals(u.role)) students.add(u);
                    }
                    rvStudents.setAdapter(new ResultInputAdapter(students, (student, marks, grade, remarks) -> {
                        uploadResult(student, marks, grade, remarks);
                    }));
                }
            }
            @Override public void onFailure(Call<List<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void uploadResult(User student, double marks, String grade, String remarks) {
        int subjectIdx = spSubject.getSelectedItemPosition();
        if (subjectIdx == -1) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        // Mocking the result object. The Api expects semester, gpa, etc.
        // We'll adapt it to the existing postResult endpoint.
        Result r = new Result();
        r.student = student.id;
        r.semester = 1; // Default
        r.gpa = marks; // Using marks as gpa for mock
        r.hasBacklog = false;
        r.backlogSubjects = remarks;

        RetrofitClient.getApiService().postResult(token, r).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ResultManagementActivity.this, "Result uploaded for " + student.username, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
