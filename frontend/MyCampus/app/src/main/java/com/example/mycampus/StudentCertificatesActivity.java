package com.example.mycampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycampus.models.Certificate;
import com.example.mycampus.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentCertificatesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private View btnRequestCert;
    private RecyclerView rvCertificates;
    private TextView tvEmptyCertificates;

    private List<Certificate> certificateList = new ArrayList<>();
    private CertificateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_certificates);

        btnBack = findViewById(R.id.btnBack);
        btnRequestCert = findViewById(R.id.btnRequestCert);
        rvCertificates = findViewById(R.id.rvCertificates);
        tvEmptyCertificates = findViewById(R.id.tvEmptyCertificates);

        btnBack.setOnClickListener(v -> finish());

        rvCertificates.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CertificateAdapter(certificateList);
        rvCertificates.setAdapter(adapter);

        btnRequestCert.setOnClickListener(v -> showRequestCertDialog());

        loadCertificates();
    }

    private void loadCertificates() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        int userId = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getInt("USER_ID", -1);
        if (token.isEmpty() || userId == -1) return;

        RetrofitClient.getApiService().getCertificates(token).enqueue(new Callback<List<Certificate>>() {
            @Override
            public void onResponse(Call<List<Certificate>> call, Response<List<Certificate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    certificateList.clear();
                    for (Certificate c : response.body()) {
                        if (c.student == userId) {
                            certificateList.add(c);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (certificateList.isEmpty()) {
                        tvEmptyCertificates.setVisibility(View.VISIBLE);
                        rvCertificates.setVisibility(View.GONE);
                    } else {
                        tvEmptyCertificates.setVisibility(View.GONE);
                        rvCertificates.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override public void onFailure(Call<List<Certificate>> call, Throwable t) {}
        });
    }

    private void showRequestCertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request Bonafide Certificate");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        builder.setView(view);

        EditText etTitle = view.findViewById(R.id.etName);
        EditText etDesc = view.findViewById(R.id.etEmail);
        view.findViewById(R.id.etPassword).setVisibility(View.GONE);
        view.findViewById(R.id.etExtraField).setVisibility(View.GONE);
        view.findViewById(R.id.etDept).setVisibility(View.GONE);
        view.findViewById(R.id.etSemester).setVisibility(View.GONE);

        etTitle.setHint("Certificate Type (e.g. Bonafide, Internship NOC)");
        etDesc.setHint("Purpose / Reason");

        builder.setPositiveButton("Request", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Certificate type is required", Toast.LENGTH_SHORT).show();
                return;
            }

            requestNewCertificate(title, desc);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void requestNewCertificate(String title, String purpose) {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        int userId = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getInt("USER_ID", -1);
        if (token.isEmpty() || userId == -1) return;

        Certificate c = new Certificate();
        c.student = userId;
        c.title = title;
        c.issuedBy = "Administration";
        c.status = "PENDING";
        c.certificateUrl = "http://example.com/mock.pdf";

        RetrofitClient.getApiService().postCertificate(token, c).enqueue(new Callback<Certificate>() {
            @Override
            public void onResponse(Call<Certificate> call, Response<Certificate> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudentCertificatesActivity.this, "Certificate requested successfully!", Toast.LENGTH_SHORT).show();
                    loadCertificates();
                } else {
                    Toast.makeText(StudentCertificatesActivity.this, "Failed to submit request", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<Certificate> call, Throwable t) {}
        });
    }

    private class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {
        private List<Certificate> list;

        CertificateAdapter(List<Certificate> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate_student, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Certificate c = list.get(position);
            holder.tvCertTitle.setText(c.title);
            holder.tvCertIssuer.setText("Issued By: " + (c.issuedBy != null ? c.issuedBy : "Admin"));
            holder.tvCertStatus.setText(c.status != null ? c.status : "PENDING");

            if (c.issueDate != null && c.issueDate.length() >= 10) {
                holder.tvCertDate.setText("Date: " + c.issueDate.substring(0, 10));
            } else {
                holder.tvCertDate.setText("Status: Pending Approval");
            }

            boolean isApproved = "APPROVED".equalsIgnoreCase(c.status);
            if (isApproved) {
                holder.tvCertStatus.setBackgroundColor(0xFFDCFCE7); // Light Green
                holder.tvCertStatus.setTextColor(0xFF166534);
                holder.btnOpenCert.setVisibility(View.VISIBLE);
                holder.btnOpenCert.setOnClickListener(v -> {
                    if (c.certificateUrl != null && !c.certificateUrl.isEmpty()) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(c.certificateUrl));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(StudentCertificatesActivity.this, "Cannot open link", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                holder.tvCertStatus.setBackgroundColor(0xFFFEF3C7); // Light Yellow
                holder.tvCertStatus.setTextColor(0xFFD97706);
                holder.btnOpenCert.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCertIcon, tvCertTitle, tvCertIssuer, tvCertStatus, tvCertDate;
            MaterialButton btnOpenCert;

            ViewHolder(View itemView) {
                super(itemView);
                tvCertIcon = itemView.findViewById(R.id.tvCertIcon);
                tvCertTitle = itemView.findViewById(R.id.tvCertTitle);
                tvCertIssuer = itemView.findViewById(R.id.tvCertIssuer);
                tvCertStatus = itemView.findViewById(R.id.tvCertStatus);
                tvCertDate = itemView.findViewById(R.id.tvCertDate);
                btnOpenCert = itemView.findViewById(R.id.btnOpenCert);
            }
        }
    }
}
