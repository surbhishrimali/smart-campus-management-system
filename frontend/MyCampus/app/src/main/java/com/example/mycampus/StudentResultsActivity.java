package com.example.mycampus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mycampus.models.Result;
import com.example.mycampus.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentResultsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout llResults;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_results);

        btnBack = findViewById(R.id.btnBack);
        llResults = findViewById(R.id.llResults);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        btnBack.setOnClickListener(v -> finish());

        fetchResults();
    }

    private void fetchResults() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        int userId = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getInt("USER_ID", -1);
        if (token.isEmpty() || userId == -1) return;

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getResults(token).enqueue(new Callback<List<Result>>() {
            @Override
            public void onResponse(Call<List<Result>> call, Response<List<Result>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayResults(response.body(), userId);
                }
            }

            @Override
            public void onFailure(Call<List<Result>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void displayResults(List<Result> list, int userId) {
        llResults.removeAllViews();
        boolean found = false;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Result r : list) {
            if (r.student == userId) {
                found = true;
                View v = inflater.inflate(R.layout.item_result_row, llResults, false);
                ((TextView) v.findViewById(R.id.tvSubject)).setText("Semester Result Summary");
                ((TextView) v.findViewById(R.id.tvGrade)).setText("GPA: " + r.gpa);
                ((TextView) v.findViewById(R.id.tvSemester)).setText("Semester " + r.semester);
                ((TextView) v.findViewById(R.id.tvMarks)).setText("Subjects List: " + (r.hasBacklog ? r.backlogSubjects : "Cleared"));
                ((TextView) v.findViewById(R.id.tvRemarks)).setText(r.hasBacklog ? "Status: Fail / Backlogs Exist" : "Status: Pass / Cleared");

                llResults.addView(v);
            }
        }

        tvEmptyState.setVisibility(found ? View.GONE : View.VISIBLE);
    }
}
