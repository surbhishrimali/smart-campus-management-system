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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycampus.models.Complaint;
import com.example.mycampus.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageComplaintsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvSummaryPending, tvSummaryResolved, tvSummaryHighPriority;
    private EditText etComplaintSearch;
    private MaterialButton btnCompFilterAll, btnCompFilterPending, btnCompFilterResolved, btnCompFilterHigh;
    private RecyclerView rvComplaints;
    private TextView tvEmptyComplaints;

    private List<Complaint> allComplaints = new ArrayList<>();
    private List<Complaint> filteredComplaints = new ArrayList<>();
    private ComplaintAdapter adapter;
    private String currentFilter = "ALL";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_complaints);

        // Bind Views
        btnBack = findViewById(R.id.btnBack);
        tvSummaryPending = findViewById(R.id.tvSummaryPending);
        tvSummaryResolved = findViewById(R.id.tvSummaryResolved);
        tvSummaryHighPriority = findViewById(R.id.tvSummaryHighPriority);
        etComplaintSearch = findViewById(R.id.etComplaintSearch);
        btnCompFilterAll = findViewById(R.id.btnCompFilterAll);
        btnCompFilterPending = findViewById(R.id.btnCompFilterPending);
        btnCompFilterResolved = findViewById(R.id.btnCompFilterResolved);
        btnCompFilterHigh = findViewById(R.id.btnCompFilterHigh);
        rvComplaints = findViewById(R.id.rvComplaints);
        tvEmptyComplaints = findViewById(R.id.tvEmptyComplaints);

        // Setup Recycler
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(filteredComplaints);
        rvComplaints.setAdapter(adapter);

        // Back Button
        btnBack.setOnClickListener(v -> finish());

        // Filters
        btnCompFilterAll.setOnClickListener(v -> setFilter("ALL"));
        btnCompFilterPending.setOnClickListener(v -> setFilter("pending"));
        btnCompFilterResolved.setOnClickListener(v -> setFilter("resolved"));
        btnCompFilterHigh.setOnClickListener(v -> setFilter("HIGH"));

        // Search Input Watcher
        etComplaintSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                applyFilterAndSearch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Initialize Filter styles
        updateFilterButtonStyles();

        // Load Complaints
        loadComplaints();
    }

    private void loadComplaints() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().getComplaints(token).enqueue(new Callback<List<Complaint>>() {
            @Override
            public void onResponse(Call<List<Complaint>> call, Response<List<Complaint>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allComplaints.clear();
                    allComplaints.addAll(response.body());
                    updateSummaryCounts();
                    applyFilterAndSearch();
                } else {
                    Toast.makeText(ManageComplaintsActivity.this, "Failed to load complaints", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Complaint>> call, Throwable t) {
                Toast.makeText(ManageComplaintsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummaryCounts() {
        int pending = 0;
        int resolved = 0;
        int high = 0;
        for (Complaint c : allComplaints) {
            if ("resolved".equalsIgnoreCase(c.status)) resolved++;
            else pending++;

            // We filter high priority from priority field or check if it matches 'HIGH'
            // In our system priority choices are: 'LOW', 'MEDIUM', 'HIGH'
            if (c.description != null && c.description.toLowerCase().contains("urgent")) {
                high++;
            }
        }
        tvSummaryPending.setText(String.valueOf(pending));
        tvSummaryResolved.setText(String.valueOf(resolved));
        tvSummaryHighPriority.setText(String.valueOf(high));
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterButtonStyles();
        applyFilterAndSearch();
    }

    private void updateFilterButtonStyles() {
        btnCompFilterAll.setStrokeWidth(currentFilter.equals("ALL") ? 3 : 1);
        btnCompFilterPending.setStrokeWidth(currentFilter.equals("pending") ? 3 : 1);
        btnCompFilterResolved.setStrokeWidth(currentFilter.equals("resolved") ? 3 : 1);
        btnCompFilterHigh.setStrokeWidth(currentFilter.equals("HIGH") ? 3 : 1);
    }

    private void applyFilterAndSearch() {
        filteredComplaints.clear();
        for (Complaint c : allComplaints) {
            // Filter by Status
            if (currentFilter.equals("pending") && !"pending".equalsIgnoreCase(c.status)) {
                continue;
            }
            if (currentFilter.equals("resolved") && !"resolved".equalsIgnoreCase(c.status)) {
                continue;
            }
            // High Priority check
            if (currentFilter.equals("HIGH")) {
                boolean isHigh = c.description != null && c.description.toLowerCase().contains("urgent");
                if (!isHigh) continue;
            }

            // Search Filter
            if (!searchQuery.isEmpty()) {
                String title = c.title != null ? c.title.toLowerCase() : "";
                String desc = c.description != null ? c.description.toLowerCase() : "";
                if (!title.contains(searchQuery) && !desc.contains(searchQuery)) {
                    continue;
                }
            }

            filteredComplaints.add(c);
        }
        adapter.notifyDataSetChanged();

        if (filteredComplaints.isEmpty()) {
            tvEmptyComplaints.setVisibility(View.VISIBLE);
            rvComplaints.setVisibility(View.GONE);
        } else {
            tvEmptyComplaints.setVisibility(View.GONE);
            rvComplaints.setVisibility(View.VISIBLE);
        }
    }

    // RESOLVE ACTION
    private void confirmResolveComplaint(Complaint complaint) {
        new AlertDialog.Builder(this)
                .setTitle("Resolve Complaint")
                .setMessage("Are you sure you want to mark this complaint as resolved?")
                .setPositiveButton("Resolve", (dialog, which) -> resolveComplaint(complaint))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resolveComplaint(Complaint complaint) {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "resolved");

        RetrofitClient.getApiService().updateComplaintStatus(token, complaint.id, statusUpdate).enqueue(new Callback<Complaint>() {
            @Override
            public void onResponse(Call<Complaint> call, Response<Complaint> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageComplaintsActivity.this, "Complaint resolved successfully!", Toast.LENGTH_SHORT).show();
                    loadComplaints();
                } else {
                    Toast.makeText(ManageComplaintsActivity.this, "Failed to resolve complaint", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Complaint> call, Throwable t) {
                Toast.makeText(ManageComplaintsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // DETAIL VIEW
    private void showComplaintDetails(Complaint complaint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Complaint Details");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_complaint_details, null);
        builder.setView(view);

        TextView tvDetTitle = view.findViewById(R.id.tvDetTitle);
        TextView tvDetDesc = view.findViewById(R.id.tvDetDesc);
        TextView tvDetUser = view.findViewById(R.id.tvDetUser);
        TextView tvDetStatus = view.findViewById(R.id.tvDetStatus);

        tvDetTitle.setText("Title: " + complaint.title);
        tvDetDesc.setText("Description: " + complaint.description);
        tvDetUser.setText("Submitter User ID: " + complaint.student);
        tvDetStatus.setText("Status: " + complaint.status);

        builder.setPositiveButton("Close", null);
        builder.show();
    }

    // COMPLAINT ADAPTER
    private class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

        private List<Complaint> list;

        ComplaintAdapter(List<Complaint> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint_admin, parent, false);
            return new ComplaintViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
            Complaint c = list.get(position);
            holder.tvCompTitle.setText(c.title);
            holder.tvCompDesc.setText(c.description);
            holder.tvCompStatus.setText(c.status);
            holder.tvCompUser.setText("Submitter ID: " + c.student);

            boolean isResolved = "resolved".equalsIgnoreCase(c.status);
            if (isResolved) {
                holder.tvCompStatus.setBackgroundColor(0xFFDCFCE7); // Light Green
                holder.tvCompStatus.setTextColor(0xFF166534);
                holder.btnResolve.setEnabled(false);
                holder.btnResolve.setText("Resolved");
                holder.btnResolve.setTextColor(0xFF9CA3AF);
            } else {
                holder.tvCompStatus.setBackgroundColor(0xFFFEF3C7); // Light Yellow
                holder.tvCompStatus.setTextColor(0xFFD97706);
                holder.btnResolve.setEnabled(true);
                holder.btnResolve.setText("Resolve");
                holder.btnResolve.setTextColor(0xFF22C55E);
                holder.btnResolve.setOnClickListener(v -> confirmResolveComplaint(c));
            }

            // High Priority UI change
            boolean isHigh = c.description != null && c.description.toLowerCase().contains("urgent");
            if (isHigh) {
                holder.tvCompPriority.setText("Priority: HIGH");
                holder.tvCompPriority.setTextColor(0xFFEF4444);
            } else {
                holder.tvCompPriority.setText("Priority: MEDIUM");
                holder.tvCompPriority.setTextColor(0xFFF59E0B);
            }

            holder.btnViewDetails.setOnClickListener(v -> showComplaintDetails(c));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ComplaintViewHolder extends RecyclerView.ViewHolder {
            TextView tvCompTitle, tvCompDesc, tvCompUser, tvCompPriority, tvCompDate, tvCompStatus;
            Button btnViewDetails, btnResolve;

            ComplaintViewHolder(View itemView) {
                super(itemView);
                tvCompTitle = itemView.findViewById(R.id.tvCompTitle);
                tvCompDesc = itemView.findViewById(R.id.tvCompDesc);
                tvCompUser = itemView.findViewById(R.id.tvCompUser);
                tvCompPriority = itemView.findViewById(R.id.tvCompPriority);
                tvCompDate = itemView.findViewById(R.id.tvCompDate);
                tvCompStatus = itemView.findViewById(R.id.tvCompStatus);
                btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
                btnResolve = itemView.findViewById(R.id.btnResolve);
            }
        }
    }
}
