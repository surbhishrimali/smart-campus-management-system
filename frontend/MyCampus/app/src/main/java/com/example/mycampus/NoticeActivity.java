package com.example.mycampus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mycampus.models.Notice;
import com.example.mycampus.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeActivity extends AppCompatActivity {

    private LinearLayout llNoticesContainer;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private List<Notice> notices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notice);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        llNoticesContainer = findViewById(R.id.llNoticesContainer);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        fetchNotices();
    }

    private void fetchNotices() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) {
            showDummyNotices();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getNotices(token).enqueue(new Callback<List<Notice>>() {
            @Override
            public void onResponse(Call<List<Notice>> call, Response<List<Notice>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    notices.clear();
                    notices.addAll(response.body());
                    updateUI();
                } else {
                    showDummyNotices();
                }
            }

            @Override
            public void onFailure(Call<List<Notice>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showDummyNotices();
            }
        });
    }

    private void showDummyNotices() {
        notices.clear();
        notices.add(new Notice("Mid-Term Exam Schedule", "The mid-term exams for all semesters will begin on June 15th, 2024.", 1));
        notices.add(new Notice("Cultural Fest 2024", "Registrations for the annual cultural fest are now open. Join us for a week of fun!", 1));
        notices.add(new Notice("Holiday Notice", "The campus will remain closed on Friday for the public holiday.", 1));
        updateUI();
    }

    private void updateUI() {
        llNoticesContainer.removeAllViews();
        if (notices.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }
        tvEmptyState.setVisibility(View.GONE);
        
        for (Notice notice : notices) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_notice, llNoticesContainer, false);
            ((TextView) view.findViewById(R.id.tvNoticeTitle)).setText(notice.title);
            ((TextView) view.findViewById(R.id.tvNoticeContent)).setText(notice.content);
            ((TextView) view.findViewById(R.id.tvNoticeDate)).setText(notice.createdAt != null ? notice.createdAt.substring(0, 10) : "22 May 2024");
            
            // Randomly set a type for dummy feel
            TextView tvType = view.findViewById(R.id.tvNoticeType);
            if (notice.title.contains("Exam")) {
                tvType.setText("EXAM");
                tvType.setBackgroundColor(0xFFFF5252);
            } else if (notice.title.contains("Fest")) {
                tvType.setText("EVENT");
                tvType.setBackgroundColor(0xFF448AFF);
            } else {
                tvType.setText("GENERAL");
                tvType.setBackgroundColor(0xFF4CAF50);
            }

            llNoticesContainer.addView(view);
        }
    }
}
