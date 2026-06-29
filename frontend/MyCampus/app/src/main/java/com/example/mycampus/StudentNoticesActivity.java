package com.example.mycampus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class StudentNoticesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etNoticeSearch;
    private RecyclerView rvNotices;
    private TextView tvEmptyNotices;

    private List<Notice> allNotices = new ArrayList<>();
    private List<Notice> filteredNotices = new ArrayList<>();
    private NoticeAdapter adapter;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_notices);

        btnBack = findViewById(R.id.btnBack);
        etNoticeSearch = findViewById(R.id.etNoticeSearch);
        rvNotices = findViewById(R.id.rvNotices);
        tvEmptyNotices = findViewById(R.id.tvEmptyNotices);

        btnBack.setOnClickListener(v -> finish());

        rvNotices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter(filteredNotices);
        rvNotices.setAdapter(adapter);

        etNoticeSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                applySearch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadNotices();
    }

    private void loadNotices() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().getNotices(token).enqueue(new Callback<List<Notice>>() {
            @Override
            public void onResponse(Call<List<Notice>> call, Response<List<Notice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allNotices.clear();
                    allNotices.addAll(response.body());
                    applySearch();
                } else {
                    Toast.makeText(StudentNoticesActivity.this, "Failed to load notices", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<List<Notice>> call, Throwable t) {}
        });
    }

    private void applySearch() {
        filteredNotices.clear();
        for (Notice n : allNotices) {
            if (!searchQuery.isEmpty()) {
                String title = n.title != null ? n.title.toLowerCase() : "";
                String desc = n.content != null ? n.content.toLowerCase() : "";
                if (!title.contains(searchQuery) && !desc.contains(searchQuery)) {
                    continue;
                }
            }
            filteredNotices.add(n);
        }
        adapter.notifyDataSetChanged();

        if (filteredNotices.isEmpty()) {
            tvEmptyNotices.setVisibility(View.VISIBLE);
            rvNotices.setVisibility(View.GONE);
        } else {
            tvEmptyNotices.setVisibility(View.GONE);
            rvNotices.setVisibility(View.VISIBLE);
        }
    }

    private class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {
        private List<Notice> list;

        NoticeAdapter(List<Notice> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice_admin, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

            // Hide action controls since this is the student view
            holder.btnEditNotice.setVisibility(View.GONE);
            holder.btnDeleteNotice.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNoticeTitle, tvNoticeDesc, tvNoticeAudience, tvNoticeType, tvNoticeDate;
            View btnEditNotice, btnDeleteNotice;

            ViewHolder(View itemView) {
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
