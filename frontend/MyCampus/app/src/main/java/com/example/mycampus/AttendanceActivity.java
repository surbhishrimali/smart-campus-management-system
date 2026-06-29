package com.example.mycampus;

import android.content.res.ColorStateList;
import android.graphics.Color;
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

import com.example.mycampus.models.Attendance;
import com.example.mycampus.models.Subject;
import com.example.mycampus.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity extends AppCompatActivity {

    private LinearLayout llAttendanceRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendance);

        View mainView = findViewById(android.R.id.content);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        llAttendanceRecords = findViewById(R.id.llAttendanceRecords);

        fetchAttendanceData();
    }

    private void fetchAttendanceData() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        RetrofitClient.getApiService().getSubjects(token).enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final Map<Integer, String> subjectMap = new HashMap<>();
                    for (Subject s : response.body()) subjectMap.put(s.id, s.name);
                    
                    RetrofitClient.getApiService().getAttendance(token).enqueue(new Callback<List<Attendance>>() {
                        @Override
                        public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                processAttendance(response.body(), subjectMap);
                            }
                        }
                        @Override public void onFailure(Call<List<Attendance>> call, Throwable t) {}
                    });
                }
            }
            @Override public void onFailure(Call<List<Subject>> call, Throwable t) {}
        });
    }

    private void processAttendance(List<Attendance> attendances, Map<Integer, String> subjectMap) {
        Map<Integer, List<Attendance>> grouped = new HashMap<>();
        for (Attendance a : attendances) {
            if (!grouped.containsKey(a.studentClass)) grouped.put(a.studentClass, new ArrayList<>());
            grouped.get(a.studentClass).add(a);
        }

        llAttendanceRecords.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Map.Entry<Integer, List<Attendance>> entry : grouped.entrySet()) {
            String courseName = subjectMap.getOrDefault(entry.getKey(), "Unknown Course");
            int total = entry.getValue().size();
            int present = 0;
            for (Attendance a : entry.getValue()) if (a.isPresent) present++;
            double percent = total > 0 ? (present * 100.0) / total : 0;

            View itemView = inflater.inflate(R.layout.item_attendance_record, llAttendanceRecords, false);
            TextView tvCourse = itemView.findViewById(R.id.tvCourseName);
            TextView tvPerc = itemView.findViewById(R.id.tvPercentage);
            ProgressBar pb = itemView.findViewById(R.id.pbAttendance);
            TextView tvTotal = itemView.findViewById(R.id.tvTotalClasses);
            TextView tvAtt = itemView.findViewById(R.id.tvAttendedClasses);
            TextView tvWarn = itemView.findViewById(R.id.tvWarning);

            tvCourse.setText(courseName);
            tvPerc.setText(String.format(Locale.getDefault(), "%.1f%%", percent));
            pb.setProgress((int) percent);
            tvTotal.setText(getString(R.string.total_classes, total));
            tvAtt.setText(getString(R.string.attended_classes, present));

            int color = percent < 75 ? Color.parseColor("#F59E0B") : Color.parseColor("#34D399");
            tvPerc.setTextColor(color);
            pb.setProgressTintList(ColorStateList.valueOf(color));
            tvWarn.setVisibility(percent < 75 ? View.VISIBLE : View.GONE);

            llAttendanceRecords.addView(itemView);
        }
    }
}
