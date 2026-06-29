package com.example.mycampus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mycampus.models.Attendance;
import com.example.mycampus.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAttendanceActivity extends AppCompatActivity {

    private TextView tvPercent, tvTotal, tvPresent, tvAbsent;
    private LinearLayout llHistory;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_attendance);

        tvPercent = findViewById(R.id.tvAttendancePercent);
        tvTotal = findViewById(R.id.tvTotalClasses);
        tvPresent = findViewById(R.id.tvPresentClasses);
        tvAbsent = findViewById(R.id.tvAbsentClasses);
        llHistory = findViewById(R.id.llHistory);
        progressBar = findViewById(R.id.progressBar);

        fetchAttendance();
    }

    private void fetchAttendance() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getAttendance(token).enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    processAttendance(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Attendance>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void processAttendance(List<Attendance> list) {
        int total = list.size();
        int present = 0;
        for (Attendance a : list) if (a.isPresent) present++;
        int absent = total - present;
        int percent = total > 0 ? (present * 100) / total : 0;

        tvPercent.setText(percent + "%");
        tvTotal.setText(String.valueOf(total));
        tvPresent.setText(String.valueOf(present));
        tvAbsent.setText(String.valueOf(absent));

        llHistory.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Attendance a : list) {
            View v = inflater.inflate(R.layout.item_attendance_history, llHistory, false);
            ((TextView) v.findViewById(R.id.tvSubjectName)).setText("Subject ID: " + a.studentClass);
            ((TextView) v.findViewById(R.id.tvDate)).setText(a.date);
            
            TextView tvStatus = v.findViewById(R.id.tvStatus);
            if (a.isPresent) {
                tvStatus.setText("PRESENT");
                tvStatus.setTextColor(0xFF4CAF50);
                tvStatus.setBackgroundColor(0xFFE8F5E9);
            } else {
                tvStatus.setText("ABSENT");
                tvStatus.setTextColor(0xFFF44336);
                tvStatus.setBackgroundColor(0xFFFFEBEE);
            }
            llHistory.addView(v);
        }
    }
}
