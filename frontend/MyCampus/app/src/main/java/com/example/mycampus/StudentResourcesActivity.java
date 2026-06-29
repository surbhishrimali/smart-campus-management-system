package com.example.mycampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.example.mycampus.models.*;
import com.example.mycampus.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentResourcesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etResourceSearch;
    private MaterialButton btnFilterResAll, btnFilterResNotes, btnFilterResPyq, btnFilterResVideos;
    private RecyclerView rvResources;
    private TextView tvEmptyResources;

    private List<UnifiedResource> allResources = new ArrayList<>();
    private List<UnifiedResource> filteredResources = new ArrayList<>();
    private ResourceAdapter adapter;
    private String currentTypeFilter = "ALL";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_resources);

        // Bind Views
        btnBack = findViewById(R.id.btnBack);
        etResourceSearch = findViewById(R.id.etResourceSearch);
        btnFilterResAll = findViewById(R.id.btnFilterResAll);
        btnFilterResNotes = findViewById(R.id.btnFilterResNotes);
        btnFilterResPyq = findViewById(R.id.btnFilterResPyq);
        btnFilterResVideos = findViewById(R.id.btnFilterResVideos);
        rvResources = findViewById(R.id.rvResources);
        tvEmptyResources = findViewById(R.id.tvEmptyResources);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Setup Recycler
        rvResources.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResourceAdapter(filteredResources);
        rvResources.setAdapter(adapter);

        // Filter Click Listeners
        btnFilterResAll.setOnClickListener(v -> setFilter("ALL"));
        btnFilterResNotes.setOnClickListener(v -> setFilter("NOTES"));
        btnFilterResPyq.setOnClickListener(v -> setFilter("PYQS"));
        btnFilterResVideos.setOnClickListener(v -> setFilter("VIDEOS"));

        // Search watcher
        etResourceSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                applyFilterAndSearch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Init UI states
        updateFilterStyles();
        loadAllResources();
    }

    private void setFilter(String filter) {
        currentTypeFilter = filter;
        updateFilterStyles();
        applyFilterAndSearch();
    }

    private void updateFilterStyles() {
        btnFilterResAll.setStrokeWidth(currentTypeFilter.equals("ALL") ? 3 : 1);
        btnFilterResNotes.setStrokeWidth(currentTypeFilter.equals("NOTES") ? 3 : 1);
        btnFilterResPyq.setStrokeWidth(currentTypeFilter.equals("PYQS") ? 3 : 1);
        btnFilterResVideos.setStrokeWidth(currentTypeFilter.equals("VIDEOS") ? 3 : 1);
    }

    private void loadAllResources() {
        String token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");
        if (token.isEmpty()) return;

        allResources.clear();

        // 1. Fetch Notes
        RetrofitClient.getApiService().getNotes(token).enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Note note : response.body()) {
                        allResources.add(new UnifiedResource(note.id, note.title, "Subject ID: " + note.subject, "NOTES", note.fileUrl));
                    }
                    applyFilterAndSearch();
                }
            }
            @Override public void onFailure(Call<List<Note>> call, Throwable t) {}
        });

        // 2. Fetch PYQs
        RetrofitClient.getApiService().getPyqs(token).enqueue(new Callback<List<Pyq>>() {
            @Override
            public void onResponse(Call<List<Pyq>> call, Response<List<Pyq>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Pyq pyq : response.body()) {
                        allResources.add(new UnifiedResource(pyq.id, "Question Paper " + pyq.year, "Subject ID: " + pyq.subject, "PYQS", pyq.fileUrl));
                    }
                    applyFilterAndSearch();
                }
            }
            @Override public void onFailure(Call<List<Pyq>> call, Throwable t) {}
        });
    }

    private void applyFilterAndSearch() {
        filteredResources.clear();
        for (UnifiedResource r : allResources) {
            // Type Filter
            if (!currentTypeFilter.equals("ALL") && !r.type.equalsIgnoreCase(currentTypeFilter)) {
                continue;
            }
            // Search Query Filter
            if (!searchQuery.isEmpty()) {
                String title = r.title != null ? r.title.toLowerCase() : "";
                String subject = r.subject != null ? r.subject.toLowerCase() : "";
                if (!title.contains(searchQuery) && !subject.contains(searchQuery)) {
                    continue;
                }
            }
            filteredResources.add(r);
        }
        adapter.notifyDataSetChanged();

        if (filteredResources.isEmpty()) {
            tvEmptyResources.setVisibility(View.VISIBLE);
            rvResources.setVisibility(View.GONE);
        } else {
            tvEmptyResources.setVisibility(View.GONE);
            rvResources.setVisibility(View.VISIBLE);
        }
    }

    // LOCAL MODEL FOR REPRESENTING MULTIPLE RESOURCES TYPES
    private static class UnifiedResource {
        int id;
        String title;
        String subject;
        String type; // NOTES, PYQS, VIDEOS
        String url;

        UnifiedResource(int id, String title, String subject, String type, String url) {
            this.id = id;
            this.title = title;
            this.subject = subject;
            this.type = type;
            this.url = url;
        }
    }

    // ADAPTER FOR RECYCLER VIEW
    private class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ViewHolder> {
        private List<UnifiedResource> list;

        ResourceAdapter(List<UnifiedResource> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resource_student, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UnifiedResource res = list.get(position);
            holder.tvResTitle.setText(res.title);
            holder.tvResSubject.setText(res.subject);
            holder.tvResType.setText(res.type);

            if ("VIDEOS".equals(res.type)) {
                holder.tvResIcon.setText("🎥");
                holder.tvResType.setBackgroundColor(0xFFFEF3C7); // Light Yellow
                holder.tvResType.setTextColor(0xFFD97706);
                holder.btnOpenResource.setText("Watch Video");
            } else {
                holder.tvResIcon.setText("📄");
                holder.tvResType.setBackgroundColor(0xFFEFF6FF); // Light Blue
                holder.tvResType.setTextColor(0xFF3B82F6);
                holder.btnOpenResource.setText("Open Document");
            }

            holder.btnOpenResource.setOnClickListener(v -> {
                if (res.url == null || res.url.isEmpty()) {
                    Toast.makeText(StudentResourcesActivity.this, "URL not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(res.url));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(StudentResourcesActivity.this, "Invalid link or viewer not found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvResIcon, tvResTitle, tvResSubject, tvResType;
            MaterialButton btnOpenResource;

            ViewHolder(View itemView) {
                super(itemView);
                tvResIcon = itemView.findViewById(R.id.tvResIcon);
                tvResTitle = itemView.findViewById(R.id.tvResTitle);
                tvResSubject = itemView.findViewById(R.id.tvResSubject);
                tvResType = itemView.findViewById(R.id.tvResType);
                btnOpenResource = itemView.findViewById(R.id.btnOpenResource);
            }
        }
    }
}
