package com.example.mycampus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycampus.models.Notice;
import com.example.mycampus.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CampusNoticesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etNoticeTitle, etNoticeDesc;
    private Spinner spNoticeAudience, spNoticeType;
    private Button btnPublishNotice;
    private RecyclerView rvNotices;
    private TextView tvEmptyNotices;

    private List<Notice> noticeList = new ArrayList<>();
    private NoticeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_notices);

        // Bind Views
        btnBack = findViewById(R.id.btnBack);
        etNoticeTitle = findViewById(R.id.etNoticeTitle);
        etNoticeDesc = findViewById(R.id.etNoticeDesc);
        spNoticeAudience = findViewById(R.id.spNoticeAudience);
        spNoticeType = findViewById(R.id.spNoticeType);
        btnPublishNotice = findViewById(R.id.btnPublishNotice);
        rvNotices = findViewById(R.id.rvNotices);
        tvEmptyNotices = findViewById(R.id.tvEmptyNotices);

        // Setup Back Button
        btnBack.setOnClickListener(v -> finish());

        // Setup Recycler
        rvNotices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter(noticeList);
        rvNotices.setAdapter(adapter);

        // Setup Spinners
        String[] audiences = {"ALL", "STUDENT", "FACULTY"};
        ArrayAdapter<String> audAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, audiences);
        audAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNoticeAudience.setAdapter(audAdapter);

        String[] types = {"ANNOUNCEMENT", "ALERT", "EVENT"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNoticeType.setAdapter(typeAdapter);

        // Setup Publish Click
        btnPublishNotice.setOnClickListener(v -> publishNotice());

        // Load notices
        loadNotices();
    }

    private void loadNotices() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().getNotices(token).enqueue(new Callback<List<Notice>>() {
            @Override
            public void onResponse(Call<List<Notice>> call, Response<List<Notice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    noticeList.clear();
                    noticeList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (noticeList.isEmpty()) {
                        tvEmptyNotices.setVisibility(View.VISIBLE);
                        rvNotices.setVisibility(View.GONE);
                    } else {
                        tvEmptyNotices.setVisibility(View.GONE);
                        rvNotices.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override public void onFailure(Call<List<Notice>> call, Throwable t) {}
        });
    }

    private void publishNotice() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        int userId = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getInt("USER_ID", 1);
        if (token.isEmpty()) return;

        String title = etNoticeTitle.getText().toString().trim();
        String desc = etNoticeDesc.getText().toString().trim();
        String audience = spNoticeAudience.getSelectedItem().toString();
        String type = spNoticeType.getSelectedItem().toString();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Title and Description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Notice notice = new Notice(title, desc, userId);
        notice.targetRole = audience;
        notice.notificationType = type;

        RetrofitClient.getApiService().postNotice(token, notice).enqueue(new Callback<Notice>() {
            @Override
            public void onResponse(Call<Notice> call, Response<Notice> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CampusNoticesActivity.this, "Notice published successfully!", Toast.LENGTH_SHORT).show();
                    etNoticeTitle.setText("");
                    etNoticeDesc.setText("");
                    loadNotices();
                } else {
                    Toast.makeText(CampusNoticesActivity.this, "Failed to publish notice", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Notice> call, Throwable t) {
                Toast.makeText(CampusNoticesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // EDIT NOTICE
    private void showEditNoticeDialog(Notice notice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Notice");

        View form = LayoutInflater.from(this).inflate(R.layout.dialog_edit_notice, null);
        builder.setView(form);

        EditText etTitle = form.findViewById(R.id.etEditNoticeTitle);
        EditText etDesc = form.findViewById(R.id.etEditNoticeDesc);
        Spinner spAud = form.findViewById(R.id.spEditNoticeAudience);
        Spinner spTyp = form.findViewById(R.id.spEditNoticeType);

        etTitle.setText(notice.title);
        etDesc.setText(notice.content);

        // Spinners setup
        String[] audiences = {"ALL", "STUDENT", "FACULTY"};
        ArrayAdapter<String> audAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, audiences);
        audAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAud.setAdapter(audAdapter);
        if (notice.targetRole != null) {
            for (int i = 0; i < audiences.length; i++) {
                if (audiences[i].equalsIgnoreCase(notice.targetRole)) {
                    spAud.setSelection(i);
                    break;
                }
            }
        }

        String[] types = {"ANNOUNCEMENT", "ALERT", "EVENT"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTyp.setAdapter(typeAdapter);
        if (notice.notificationType != null) {
            for (int i = 0; i < types.length; i++) {
                if (types[i].equalsIgnoreCase(notice.notificationType)) {
                    spTyp.setSelection(i);
                    break;
                }
            }
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String aud = spAud.getSelectedItem().toString();
            String typ = spTyp.getSelectedItem().toString();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            notice.title = title;
            notice.content = desc;
            notice.targetRole = aud;
            notice.notificationType = typ;

            saveNoticeEdits(notice);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveNoticeEdits(Notice notice) {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().updateNotice(token, notice.id, notice).enqueue(new Callback<Notice>() {
            @Override
            public void onResponse(Call<Notice> call, Response<Notice> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CampusNoticesActivity.this, "Notice updated successfully!", Toast.LENGTH_SHORT).show();
                    loadNotices();
                } else {
                    Toast.makeText(CampusNoticesActivity.this, "Failed to update notice", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<Notice> call, Throwable t) {}
        });
    }

    // DELETE NOTICE
    private void confirmDeleteNotice(Notice notice) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Notice")
                .setMessage("Are you sure you want to delete this notice?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNotice(notice))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNotice(Notice notice) {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().deleteNotice(token, notice.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CampusNoticesActivity.this, "Notice deleted successfully!", Toast.LENGTH_SHORT).show();
                    loadNotices();
                } else {
                    Toast.makeText(CampusNoticesActivity.this, "Failed to delete notice", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // NOTICE RECYCLER ADAPTER
    private class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

        private List<Notice> list;

        NoticeAdapter(List<Notice> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice_admin, parent, false);
            return new NoticeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
            Notice n = list.get(position);
            holder.tvNoticeTitle.setText(n.title);
            holder.tvNoticeDesc.setText(n.content);
            holder.tvNoticeAudience.setText(n.targetRole != null ? n.targetRole : "ALL");
            holder.tvNoticeType.setText("Type: " + (n.notificationType != null ? n.notificationType : "ANNOUNCEMENT"));

            if (n.createdAt != null && n.createdAt.length() >= 10) {
                holder.tvNoticeDate.setText(n.createdAt.substring(0, 10));
            } else {
                holder.tvNoticeDate.setText("");
            }

            holder.btnEditNotice.setOnClickListener(v -> showEditNoticeDialog(n));
            holder.btnDeleteNotice.setOnClickListener(v -> confirmDeleteNotice(n));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class NoticeViewHolder extends RecyclerView.ViewHolder {
            TextView tvNoticeTitle, tvNoticeDesc, tvNoticeAudience, tvNoticeType, tvNoticeDate;
            Button btnEditNotice, btnDeleteNotice;

            NoticeViewHolder(View itemView) {
                super(itemView);
                tvNoticeTitle = itemView.findViewById(R.id.tvNoticeTitle);
                tvNoticeDesc = itemView.findViewById(R.id.tvNoticeDesc);
                tvNoticeAudience = itemView.findViewById(R.id.tvNoticeAudience);
                tvNoticeType = itemView.findViewById(R.id.tvNoticeType);
                tvNoticeDate = itemView.findViewById(R.id.tvNoticeDate);
                btnEditNotice = itemView.findViewById(R.id.btnEditNotice);
                btnDeleteNotice = itemView.findViewById(R.id.btnDeleteNotice);
            }
        }
    }
}
