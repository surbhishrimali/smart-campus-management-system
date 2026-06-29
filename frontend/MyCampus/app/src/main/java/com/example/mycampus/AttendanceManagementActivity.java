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

import com.example.mycampus.models.Attendance;
import com.example.mycampus.models.Subject;
import com.example.mycampus.models.User;
import com.example.mycampus.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceManagementActivity extends AppCompatActivity {

    private Spinner spSubject;
    private RecyclerView rvStudents;
    private ProgressBar progressBar;
    private AttendanceAdapter adapter;
    
    private List<User> students = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendance_management);

        token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");

        spSubject = findViewById(R.id.spSubject);
        rvStudents = findViewById(R.id.rvStudents);
        progressBar = findViewById(R.id.progressBar);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnSaveAttendance).setOnClickListener(v -> saveAttendance());

        fetchData();
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Fetch Subjects
        RetrofitClient.getApiService().getSubjects(token).enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects = response.body();
                    List<String> names = new ArrayList<>();
                    for (Subject s : subjects) names.add(s.name);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AttendanceManagementActivity.this, android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spSubject.setAdapter(adapter);
                }
            }
            @Override public void onFailure(Call<List<Subject>> call, Throwable t) {}
        });

        // Fetch Students
        RetrofitClient.getApiService().getUsers(token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    students.clear();
                    for (User u : response.body()) {
                        if ("STUDENT".equals(u.role)) students.add(u);
                    }
                    adapter = new AttendanceAdapter(students);
                    rvStudents.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void saveAttendance() {
        if (adapter == null) return;
        Map<Integer, String> attendanceData = adapter.getAttendanceMap();
        if (attendanceData.isEmpty()) {
            Toast.makeText(this, "Mark at least one student", Toast.LENGTH_SHORT).show();
            return;
        }

        int subjectIdx = spSubject.getSelectedItemPosition();
        if (subjectIdx == -1) return;
        int subjectId = subjects.get(subjectIdx).id;
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        progressBar.setVisibility(View.VISIBLE);
        
        // In a real app, you might have a bulk upload. 
        // Here we'll simulate it by looping or just a success toast.
        // The prompt says "Add Save Attendance button" and "Save to API".
        
        int totalToSave = attendanceData.size();
        final int[] savedCount = {0};

        for (Map.Entry<Integer, String> entry : attendanceData.entrySet()) {
            Attendance a = new Attendance();
            a.student = entry.getKey();
            a.studentClass = subjectId;
            a.date = date;
            a.isPresent = entry.getValue().equals("P");

            RetrofitClient.getApiService().postAttendance(token, a).enqueue(new Callback<Attendance>() {
                @Override
                public void onResponse(Call<Attendance> call, Response<Attendance> response) {
                    savedCount[0]++;
                    if (savedCount[0] == totalToSave) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AttendanceManagementActivity.this, "Attendance saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<Attendance> call, Throwable t) {
                    savedCount[0]++;
                    if (savedCount[0] == totalToSave) progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}
