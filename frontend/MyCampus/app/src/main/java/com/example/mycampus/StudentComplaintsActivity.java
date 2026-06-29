package com.example.mycampus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycampus.models.Complaint;
import com.example.mycampus.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentComplaintsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private View btnNewComplaint;
    private RecyclerView rvComplaints;
    private TextView tvEmptyComplaints;

    private List<Complaint> complaintList = new ArrayList<>();
    private ComplaintAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_complaints);

        btnBack = findViewById(R.id.btnBack);
        btnNewComplaint = findViewById(R.id.btnNewComplaint);
        rvComplaints = findViewById(R.id.rvComplaints);
        tvEmptyComplaints = findViewById(R.id.tvEmptyComplaints);

        btnBack.setOnClickListener(v -> finish());

        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(complaintList);
        rvComplaints.setAdapter(adapter);

        btnNewComplaint.setOnClickListener(v -> showNewComplaintDialog());

        loadComplaints();
    }

    private void loadComplaints() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().getComplaints(token).enqueue(new Callback<List<Complaint>>() {
            @Override
            public void onResponse(Call<List<Complaint>> call, Response<List<Complaint>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    complaintList.clear();
                    complaintList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (complaintList.isEmpty()) {
                        tvEmptyComplaints.setVisibility(View.VISIBLE);
                        rvComplaints.setVisibility(View.GONE);
                    } else {
                        tvEmptyComplaints.setVisibility(View.GONE);
                        rvComplaints.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override public void onFailure(Call<List<Complaint>> call, Throwable t) {}
        });
    }

    private void showNewComplaintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File New Complaint");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        builder.setView(view);

        EditText etTitle = view.findViewById(R.id.etName);
        EditText etDesc = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etExtra = view.findViewById(R.id.etExtraField);
        EditText etDept = view.findViewById(R.id.etDept);
        EditText etSemester = view.findViewById(R.id.etSemester);

        // Customize the generic fields for complaint input
        etTitle.setHint("Complaint Title");
        etDesc.setHint("Detailed Description");
        etPassword.setVisibility(View.GONE);
        etExtra.setVisibility(View.GONE);
        etDept.setVisibility(View.GONE);
        etSemester.setVisibility(View.GONE);

        // Add a spinner dynamically or overlay it
        LinearLayout container = (LinearLayout) etTitle.getParent();
        Spinner spPriority = new Spinner(this);
        String[] priorities = {"LOW", "MEDIUM", "HIGH"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(spinnerAdapter);
        spPriority.setSelection(1); // MEDIUM default
        container.addView(spPriority);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String priority = spPriority.getSelectedItem().toString();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Title and Description are required", Toast.LENGTH_SHORT).show();
                return;
            }

            submitComplaint(title, desc, priority);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void submitComplaint(String title, String desc, String priority) {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        int userId = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getInt("USER_ID", -1);
        if (token.isEmpty() || userId == -1) return;

        Complaint complaint = new Complaint();
        complaint.student = userId;
        complaint.title = title;
        complaint.description = desc + " [" + priority + " PRIORITY]";
        complaint.status = "pending";

        RetrofitClient.getApiService().postComplaint(token, complaint).enqueue(new Callback<Complaint>() {
            @Override
            public void onResponse(Call<Complaint> call, Response<Complaint> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudentComplaintsActivity.this, "Complaint filed successfully!", Toast.LENGTH_SHORT).show();
                    loadComplaints();
                } else {
                    Toast.makeText(StudentComplaintsActivity.this, "Failed to submit complaint", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<Complaint> call, Throwable t) {}
        });
    }

    private class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {
        private List<Complaint> list;

        ComplaintAdapter(List<Complaint> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint_student, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Complaint c = list.get(position);
            holder.tvCompTitle.setText(c.title);
            holder.tvCompDesc.setText(c.description);
            holder.tvCompStatus.setText(c.status);

            boolean isResolved = "resolved".equalsIgnoreCase(c.status);
            if (isResolved) {
                holder.tvCompStatus.setBackgroundColor(0xFFDCFCE7); // Light Green
                holder.tvCompStatus.setTextColor(0xFF166534);
            } else {
                holder.tvCompStatus.setBackgroundColor(0xFFFEF3C7); // Light Yellow
                holder.tvCompStatus.setTextColor(0xFFD97706);
            }

            // Extract priority from text if bracketed
            if (c.description != null && c.description.contains("HIGH PRIORITY")) {
                holder.tvCompPriority.setText("Priority: HIGH");
                holder.tvCompPriority.setTextColor(0xFFEF4444);
            } else if (c.description != null && c.description.contains("LOW PRIORITY")) {
                holder.tvCompPriority.setText("Priority: LOW");
                holder.tvCompPriority.setTextColor(0xFF3B82F6);
            } else {
                holder.tvCompPriority.setText("Priority: MEDIUM");
                holder.tvCompPriority.setTextColor(0xFFF59E0B);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCompTitle, tvCompDesc, tvCompPriority, tvCompStatus, tvCompDate;

            ViewHolder(View itemView) {
                super(itemView);
                tvCompTitle = itemView.findViewById(R.id.tvCompTitle);
                tvCompDesc = itemView.findViewById(R.id.tvCompDesc);
                tvCompPriority = itemView.findViewById(R.id.tvCompPriority);
                tvCompStatus = itemView.findViewById(R.id.tvCompStatus);
                tvCompDate = itemView.findViewById(R.id.tvCompDate);
            }
        }
    }
}
