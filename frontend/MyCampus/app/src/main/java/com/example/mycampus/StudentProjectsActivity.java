package com.example.mycampus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycampus.models.Project;
import com.example.mycampus.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentProjectsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvProjects;
    private TextView tvEmptyProjects;

    private List<Project> projectList = new ArrayList<>();
    private ProjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_projects);

        btnBack = findViewById(R.id.btnBack);
        rvProjects = findViewById(R.id.rvProjects);
        tvEmptyProjects = findViewById(R.id.tvEmptyProjects);

        btnBack.setOnClickListener(v -> finish());

        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter(projectList);
        rvProjects.setAdapter(adapter);

        loadProjects();
    }

    private void loadProjects() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        int userId = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getInt("USER_ID", -1);
        if (token.isEmpty() || userId == -1) return;

        RetrofitClient.getApiService().getProjects(token).enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    projectList.clear();
                    for (Project p : response.body()) {
                        if (p.student == userId) {
                            projectList.add(p);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (projectList.isEmpty()) {
                        tvEmptyProjects.setVisibility(View.VISIBLE);
                        rvProjects.setVisibility(View.GONE);
                    } else {
                        tvEmptyProjects.setVisibility(View.GONE);
                        rvProjects.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override public void onFailure(Call<List<Project>> call, Throwable t) {}
        });
    }

    private class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
        private List<Project> list;

        ProjectAdapter(List<Project> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_student, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Project p = list.get(position);
            holder.tvProjTitle.setText(p.title);
            holder.tvProjDesc.setText(p.description);
            holder.tvProjGuide.setText("Supervisor / Guide: Faculty User " + p.faculty);
            holder.tvProjStatus.setText(p.status != null ? p.status : "ASSIGNED");

            if ("COMPLETED".equalsIgnoreCase(p.status)) {
                holder.tvProjStatus.setBackgroundColor(0xFFDCFCE7); // Light Green
                holder.tvProjStatus.setTextColor(0xFF166534);
            } else {
                holder.tvProjStatus.setBackgroundColor(0xFFEFF6FF); // Light Blue
                holder.tvProjStatus.setTextColor(0xFF3B82F6);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProjTitle, tvProjDesc, tvProjGuide, tvProjStatus, tvProjDate;

            ViewHolder(View itemView) {
                super(itemView);
                tvProjTitle = itemView.findViewById(R.id.tvProjTitle);
                tvProjDesc = itemView.findViewById(R.id.tvProjDesc);
                tvProjGuide = itemView.findViewById(R.id.tvProjGuide);
                tvProjStatus = itemView.findViewById(R.id.tvProjStatus);
                tvProjDate = itemView.findViewById(R.id.tvProjDate);
            }
        }
    }
}
