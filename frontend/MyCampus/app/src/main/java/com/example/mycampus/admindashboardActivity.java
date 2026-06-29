package com.example.mycampus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class admindashboardActivity extends AppCompatActivity {

    private MaterialCardView cvAddStudentForm;
    private Button btnToggleForm;
    private TextInputEditText etNewUsername, etNewEmail, etNewEnrollment, etNewDepartment, etNewPassword;
    private Spinner spNewUserRole;
    private TextView tvTotalStudents, tvTotalFaculty, tvTotalResources, tvTotalNotices, tvTotalCerts, tvTotalComplaints;
    private LinearLayout llAdminNotices, llComplaintsTable;

    private List<User> users = new ArrayList<>();
    private List<Notice> notices = new ArrayList<>();
    private List<Complaint> complaints = new ArrayList<>();
    private boolean isFormVisible = false;

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
        cvAddStudentForm = findViewById(R.id.cvAddStudentForm);
        btnToggleForm = findViewById(R.id.btnToggleForm);
        etNewUsername = findViewById(R.id.etNewUsername);
        etNewEmail = findViewById(R.id.etNewEmail);
        etNewEnrollment = findViewById(R.id.etNewEnrollment);
        etNewDepartment = findViewById(R.id.etNewDepartment);
        etNewPassword = findViewById(R.id.etNewPassword);
        spNewUserRole = findViewById(R.id.spNewUserRole);
        
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTotalFaculty = findViewById(R.id.tvTotalFaculty);
        tvTotalResources = findViewById(R.id.tvTotalResources);
        tvTotalNotices = findViewById(R.id.tvTotalNotices);
        tvTotalCerts = findViewById(R.id.tvTotalCerts);
        tvTotalComplaints = findViewById(R.id.tvTotalComplaints);
        
        llAdminNotices = findViewById(R.id.llAdminNotices);
        llComplaintsTable = findViewById(R.id.llComplaintsTable);

        // Setup Role Spinner
        String[] roles = {"STUDENT", "FACULTY"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNewUserRole.setAdapter(adapter);

        btnToggleForm.setOnClickListener(v -> toggleForm());
        findViewById(R.id.btnSubmitStudent).setOnClickListener(v -> submitUser());
        findViewById(R.id.btnPostNotice).setOnClickListener(v -> showPostNoticeDialog());

        fetchData();
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
                    complaints.clear();
                    complaints.addAll(response.body());
                    updateComplaintsUI();
                    tvTotalComplaints.setText(String.valueOf(complaints.size()));
                }
            }
            @Override public void onFailure(Call<List<Complaint>> call, Throwable t) {}
        });

        RetrofitClient.getApiService().getNotices(token).enqueue(new Callback<List<Notice>>() {
            @Override public void onResponse(Call<List<Notice>> call, Response<List<Notice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notices.clear();
                    notices.addAll(response.body());
                    updateNoticesUI();
                    tvTotalNotices.setText(String.valueOf(notices.size()));
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
    }

    private void toggleForm() {
        isFormVisible = !isFormVisible;
        cvAddStudentForm.setVisibility(isFormVisible ? View.VISIBLE : View.GONE);
        btnToggleForm.setText(isFormVisible ? "Cancel" : "New User");
    }

    private void submitUser() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        String username = etNewUsername.getText().toString().trim();
        String email = etNewEmail.getText().toString().trim();
        String password = etNewPassword.getText().toString().trim();
        String enrollment = etNewEnrollment.getText().toString().trim();
        String dept = etNewDepartment.getText().toString().trim();
        String role = spNewUserRole.getSelectedItem().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(username, email, password, role);
        RetrofitClient.getApiService().createUser(token, newUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int newUserId = response.body().id;
                    if ("STUDENT".equals(role)) {
                        createStudentProfile(token, newUserId, enrollment, dept);
                    } else {
                        createFacultyProfile(token, newUserId, enrollment, dept);
                    }
                } else {
                    Toast.makeText(admindashboardActivity.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void createStudentProfile(String token, int userId, String enrollment, String dept) {
        StudentProfile profile = new StudentProfile();
        profile.user = userId;
        profile.enrollmentNumber = enrollment;
        profile.department = dept;
        profile.currentSemester = 1;

        RetrofitClient.getApiService().createStudentProfile(token, profile).enqueue(new Callback<StudentProfile>() {
            @Override
            public void onResponse(Call<StudentProfile> call, Response<StudentProfile> response) {
                Toast.makeText(admindashboardActivity.this, "Student created successfully", Toast.LENGTH_SHORT).show();
                onSuccess();
            }
            @Override public void onFailure(Call<StudentProfile> call, Throwable t) {}
        });
    }

    private void createFacultyProfile(String token, int userId, String facultyId, String dept) {
        FacultyProfile profile = new FacultyProfile();
        profile.user = userId;
        profile.facultyId = facultyId;
        profile.department = dept;
        profile.designation = "Lecturer";

        RetrofitClient.getApiService().createFacultyProfile(token, profile).enqueue(new Callback<FacultyProfile>() {
            @Override
            public void onResponse(Call<FacultyProfile> call, Response<FacultyProfile> response) {
                Toast.makeText(admindashboardActivity.this, "Faculty created successfully", Toast.LENGTH_SHORT).show();
                onSuccess();
            }
            @Override public void onFailure(Call<FacultyProfile> call, Throwable t) {}
        });
    }

    private void onSuccess() {
        clearForm();
        toggleForm();
        fetchData();
    }

    private void clearForm() {
        etNewUsername.setText("");
        etNewEmail.setText("");
        etNewEnrollment.setText("");
        etNewDepartment.setText("");
        etNewPassword.setText("");
    }

    private void showPostNoticeDialog() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Post New Notice");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        final EditText titleInput = new EditText(this); titleInput.setHint("Title"); layout.addView(titleInput);
        final EditText contentInput = new EditText(this); contentInput.setHint("Content"); layout.addView(contentInput);
        builder.setView(layout);
        builder.setPositiveButton("Post", (dialog, which) -> {
            int adminId = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getInt("USER_ID", 1);
            Notice n = new Notice(titleInput.getText().toString(), contentInput.getText().toString(), adminId);
            RetrofitClient.getApiService().postNotice(token, n).enqueue(new Callback<Notice>() {
                @Override public void onResponse(Call<Notice> call, Response<Notice> response) { fetchData(); }
                @Override public void onFailure(Call<Notice> call, Throwable t) {}
            });
        });
        builder.show();
    }

    private void updateComplaintsUI() {
        llComplaintsTable.removeAllViews();
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        
        for (Complaint c : complaints) {
            View row = LayoutInflater.from(this).inflate(R.layout.row_complaint, llComplaintsTable, false);
            TextView tvTitle = row.findViewById(R.id.tvComplaintTitle);
            TextView tvDesc = row.findViewById(R.id.tvComplaintDesc);
            TextView tvStatus = row.findViewById(R.id.tvComplaintStatus);
            Button btnAction = row.findViewById(R.id.btnChangeStatus);
            
            tvTitle.setText(c.title);
            tvDesc.setText(c.description);
            tvStatus.setText(c.status);
            
            if ("resolved".equalsIgnoreCase(c.status)) {
                btnAction.setText("Resolved");
                btnAction.setEnabled(false);
            } else {
                btnAction.setText("Resolve");
                btnAction.setOnClickListener(v -> {
                    java.util.Map<String, String> statusUpdate = new java.util.HashMap<>();
                    statusUpdate.put("status", "resolved");
                    RetrofitClient.getApiService().updateComplaintStatus(token, c.id, statusUpdate).enqueue(new Callback<Complaint>() {
                        @Override public void onResponse(Call<Complaint> call, Response<Complaint> response) { fetchData(); }
                        @Override public void onFailure(Call<Complaint> call, Throwable t) {}
                    });
                });
            }
            
            llComplaintsTable.addView(row);
        }
    }

    private void updateNoticesUI() {
        llAdminNotices.removeAllViews();
        for (Notice n : notices) {
            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llAdminNotices, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);
            text1.setText(n.title);
            text2.setText(n.content);
            itemView.setOnLongClickListener(v -> { deleteNotice(n.id); return true; });
            llAdminNotices.addView(itemView);
        }
    }

    private void deleteNotice(int id) {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        RetrofitClient.getApiService().deleteNotice(token, id).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) { fetchData(); }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}
