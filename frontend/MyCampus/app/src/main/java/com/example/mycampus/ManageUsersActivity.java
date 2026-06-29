package com.example.mycampus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycampus.models.*;
import com.example.mycampus.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageUsersActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvSummaryStudents, tvSummaryFaculty, tvSummaryAdmins;
    private MaterialButton btnAddStudent, btnAddFaculty, btnAddAdmin;
    private EditText etUserSearch;
    private MaterialButton btnFilterAll, btnFilterStudents, btnFilterFaculty, btnFilterAdmins;
    private RecyclerView rvUsers;
    private TextView tvEmptyUsers;

    private List<User> allUsersList = new ArrayList<>();
    private List<User> filteredUsersList = new ArrayList<>();
    private UserAdapter userAdapter;
    private String currentRoleFilter = "ALL";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        // Bind Views
        btnBack = findViewById(R.id.btnBack);
        tvSummaryStudents = findViewById(R.id.tvSummaryStudents);
        tvSummaryFaculty = findViewById(R.id.tvSummaryFaculty);
        tvSummaryAdmins = findViewById(R.id.tvSummaryAdmins);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnAddFaculty = findViewById(R.id.btnAddFaculty);
        btnAddAdmin = findViewById(R.id.btnAddAdmin);
        etUserSearch = findViewById(R.id.etUserSearch);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterStudents = findViewById(R.id.btnFilterStudents);
        btnFilterFaculty = findViewById(R.id.btnFilterFaculty);
        btnFilterAdmins = findViewById(R.id.btnFilterAdmins);
        rvUsers = findViewById(R.id.rvUsers);
        tvEmptyUsers = findViewById(R.id.tvEmptyUsers);

        // Setup Recycler
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(filteredUsersList);
        rvUsers.setAdapter(userAdapter);

        // Back Button
        btnBack.setOnClickListener(v -> finish());

        // Quick Actions Listeners
        btnAddStudent.setOnClickListener(v -> showAddUserDialog("STUDENT"));
        btnAddFaculty.setOnClickListener(v -> showAddUserDialog("FACULTY"));
        btnAddAdmin.setOnClickListener(v -> showAddUserDialog("ADMIN"));

        // Role Filters Listeners
        btnFilterAll.setOnClickListener(v -> setRoleFilter("ALL"));
        btnFilterStudents.setOnClickListener(v -> setRoleFilter("STUDENT"));
        btnFilterFaculty.setOnClickListener(v -> setRoleFilter("FACULTY"));
        btnFilterAdmins.setOnClickListener(v -> setRoleFilter("ADMIN"));

        // Search Input Watcher
        etUserSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                applyFilterAndSearch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Initialize Filter Button Styles
        updateFilterButtonStyles();

        // Fetch Users list
        loadUsers();
    }

    private void loadUsers() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().getUsers(token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsersList.clear();
                    allUsersList.addAll(response.body());
                    updateSummaryCounts();
                    applyFilterAndSearch();
                } else {
                    Toast.makeText(ManageUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ManageUsersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummaryCounts() {
        int studentCount = 0;
        int facultyCount = 0;
        int adminCount = 0;
        for (User u : allUsersList) {
            if ("STUDENT".equalsIgnoreCase(u.role)) studentCount++;
            else if ("FACULTY".equalsIgnoreCase(u.role)) facultyCount++;
            else if ("ADMIN".equalsIgnoreCase(u.role)) adminCount++;
        }
        tvSummaryStudents.setText(String.valueOf(studentCount));
        tvSummaryFaculty.setText(String.valueOf(facultyCount));
        tvSummaryAdmins.setText(String.valueOf(adminCount));
    }

    private void setRoleFilter(String role) {
        currentRoleFilter = role;
        updateFilterButtonStyles();
        applyFilterAndSearch();
    }

    private void updateFilterButtonStyles() {
        btnFilterAll.setStrokeWidth(currentRoleFilter.equals("ALL") ? 3 : 1);
        btnFilterStudents.setStrokeWidth(currentRoleFilter.equals("STUDENT") ? 3 : 1);
        btnFilterFaculty.setStrokeWidth(currentRoleFilter.equals("FACULTY") ? 3 : 1);
        btnFilterAdmins.setStrokeWidth(currentRoleFilter.equals("ADMIN") ? 3 : 1);
    }

    private void applyFilterAndSearch() {
        filteredUsersList.clear();
        for (User user : allUsersList) {
            // Check Role Filter
            if (!currentRoleFilter.equals("ALL") && !currentRoleFilter.equalsIgnoreCase(user.role)) {
                continue;
            }
            // Check Search Query
            if (!currentSearchQuery.isEmpty()) {
                String name = user.fullName != null ? user.fullName.toLowerCase() : "";
                String email = user.email != null ? user.email.toLowerCase() : "";
                String username = user.username != null ? user.username.toLowerCase() : "";
                if (!name.contains(currentSearchQuery) && !email.contains(currentSearchQuery) && !username.contains(currentSearchQuery)) {
                    continue;
                }
            }
            filteredUsersList.add(user);
        }
        userAdapter.notifyDataSetChanged();

        if (filteredUsersList.isEmpty()) {
            tvEmptyUsers.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        } else {
            tvEmptyUsers.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
        }
    }

    // CREATE USER DIALOG
    private void showAddUserDialog(String role) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New " + role);

        View form = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        builder.setView(form);

        EditText etName = form.findViewById(R.id.etName);
        EditText etEmail = form.findViewById(R.id.etEmail);
        EditText etPassword = form.findViewById(R.id.etPassword);
        EditText etExtraField = form.findViewById(R.id.etExtraField); // Enrollment/Faculty ID
        EditText etDept = form.findViewById(R.id.etDept);
        EditText etSemester = form.findViewById(R.id.etSemester);

        // Customize fields based on role
        if ("ADMIN".equals(role)) {
            etExtraField.setVisibility(View.GONE);
            etDept.setVisibility(View.GONE);
            etSemester.setVisibility(View.GONE);
        } else if ("FACULTY".equals(role)) {
            etExtraField.setHint("Faculty ID");
            etSemester.setVisibility(View.GONE);
        } else {
            etExtraField.setHint("Enrollment Number");
        }

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String extra = etExtraField.getText().toString().trim();
            String dept = etDept.getText().toString().trim();
            String sem = etSemester.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Name, email and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            createNewUser(role, name, email, password, extra, dept, sem);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createNewUser(String role, String name, String email, String password, String extra, String dept, String sem) {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        User user = new User(name, email, password, role);
        user.fullName = name;
        user.department = dept;

        RetrofitClient.getApiService().createUser(token, user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int id = response.body().id;
                    if ("STUDENT".equals(role)) {
                        createStudentProfile(token, id, extra, dept, sem);
                    } else if ("FACULTY".equals(role)) {
                        createFacultyProfile(token, id, extra, dept);
                    } else {
                        Toast.makeText(ManageUsersActivity.this, "Admin created successfully", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    }
                } else {
                    Toast.makeText(ManageUsersActivity.this, "Failed to create user account", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void createStudentProfile(String token, int userId, String enrollment, String dept, String sem) {
        StudentProfile p = new StudentProfile();
        p.user = userId;
        p.enrollmentNumber = enrollment;
        p.department = dept;
        try {
            p.currentSemester = Integer.parseInt(sem);
        } catch (Exception e) {
            p.currentSemester = 1;
        }

        RetrofitClient.getApiService().createStudentProfile(token, p).enqueue(new Callback<StudentProfile>() {
            @Override
            public void onResponse(Call<StudentProfile> call, Response<StudentProfile> response) {
                Toast.makeText(ManageUsersActivity.this, "Student registered successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
            @Override public void onFailure(Call<StudentProfile> call, Throwable t) {}
        });
    }

    private void createFacultyProfile(String token, int userId, String facultyId, String dept) {
        FacultyProfile p = new FacultyProfile();
        p.user = userId;
        p.facultyId = facultyId;
        p.department = dept;
        p.designation = "Professor";

        RetrofitClient.getApiService().createFacultyProfile(token, p).enqueue(new Callback<FacultyProfile>() {
            @Override
            public void onResponse(Call<FacultyProfile> call, Response<FacultyProfile> response) {
                Toast.makeText(ManageUsersActivity.this, "Faculty registered successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
            @Override public void onFailure(Call<FacultyProfile> call, Throwable t) {}
        });
    }

    // EDIT USER DIALOG
    private void showEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit User Account");

        View form = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        builder.setView(form);

        EditText etName = form.findViewById(R.id.etName);
        EditText etEmail = form.findViewById(R.id.etEmail);
        EditText etPassword = form.findViewById(R.id.etPassword); // Can leave blank to keep current
        EditText etExtraField = form.findViewById(R.id.etExtraField);
        EditText etDept = form.findViewById(R.id.etDept);
        EditText etSemester = form.findViewById(R.id.etSemester);

        // Pre-fill fields
        etName.setText(user.fullName != null ? user.fullName : user.username);
        etEmail.setText(user.email);
        etDept.setText(user.department);
        etPassword.setHint("Leave blank to keep same");

        // Hide profile specific fields during generic user edit to avoid overriding profile models
        etExtraField.setVisibility(View.GONE);
        etSemester.setVisibility(View.GONE);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String dept = etDept.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            user.fullName = name;
            user.username = name;
            user.email = email;
            user.department = dept;
            if (!password.isEmpty()) {
                user.password = password;
            }

            saveUserEdits(user);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveUserEdits(User user) {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().updateUser(token, user.id, user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageUsersActivity.this, "User details updated successfully", Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(ManageUsersActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    // VIEW PROFILE DIALOG
    private void showUserProfile(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Profile Details");

        View profileView = LayoutInflater.from(this).inflate(R.layout.dialog_view_profile, null);
        builder.setView(profileView);

        TextView tvProfName = profileView.findViewById(R.id.tvProfName);
        TextView tvProfEmail = profileView.findViewById(R.id.tvProfEmail);
        TextView tvProfRole = profileView.findViewById(R.id.tvProfRole);
        TextView tvProfDept = profileView.findViewById(R.id.tvProfDept);

        tvProfName.setText("Name: " + (user.fullName != null ? user.fullName : user.username));
        tvProfEmail.setText("Email: " + user.email);
        tvProfRole.setText("Role: " + user.role);
        tvProfDept.setText("Department: " + (user.department != null && !user.department.isEmpty() ? user.department : "N/A"));

        builder.setPositiveButton("Close", null);
        builder.show();
    }

    // DELETE USER
    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User Account")
                .setMessage("Are you sure you want to permanently delete the account for " + (user.fullName != null ? user.fullName : user.username) + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().deleteUser(token, user.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageUsersActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(ManageUsersActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // USER RECYCLER ADAPTER
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        private List<User> list;

        UserAdapter(List<User> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = list.get(position);
            String name = user.fullName != null ? user.fullName : user.username;
            holder.tvUserName.setText(name);
            holder.tvUserEmail.setText(user.email);
            holder.tvUserRole.setText(user.role);
            holder.tvUserDept.setText("Dept: " + (user.department != null && !user.department.isEmpty() ? user.department : "N/A"));
            holder.tvUserAvatar.setText(name != null && name.length() > 0 ? name.substring(0, 1).toUpperCase() : "U");

            // Setup buttons
            holder.btnViewProfile.setOnClickListener(v -> showUserProfile(user));
            holder.btnEditUser.setOnClickListener(v -> showEditUserDialog(user));
            holder.btnDeleteUser.setOnClickListener(v -> confirmDeleteUser(user));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserAvatar, tvUserName, tvUserEmail, tvUserRole, tvUserDept;
            Button btnViewProfile, btnEditUser, btnDeleteUser;

            UserViewHolder(View itemView) {
                super(itemView);
                tvUserAvatar = itemView.findViewById(R.id.tvUserAvatar);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
                tvUserRole = itemView.findViewById(R.id.tvUserRole);
                tvUserDept = itemView.findViewById(R.id.tvUserDept);
                btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
                btnEditUser = itemView.findViewById(R.id.btnEditUser);
                btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
            }
        }
    }
}
