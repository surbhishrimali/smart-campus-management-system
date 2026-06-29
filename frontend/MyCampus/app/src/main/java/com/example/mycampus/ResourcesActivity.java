package com.example.mycampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mycampus.models.*;
import com.example.mycampus.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResourcesActivity extends AppCompatActivity {

    private LinearLayout llResourcesList;
    private EditText etSearch;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private List<ResourceItem> displayList = new ArrayList<>();
    private String userRole;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resources);

        SharedPreferences prefs = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE);
        userRole = prefs.getString("USER_ROLE", "STUDENT");
        token = prefs.getString("JWT_TOKEN", "");

        View mainView = findViewById(android.R.id.content);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        llResourcesList = findViewById(R.id.llResourcesList);
        etSearch = findViewById(R.id.etSearch);
        // I need to add progressBar and tvEmptyState to activity_resources.xml or handle them here.
        // Let's assume they might be missing and I'll add them to the XML next.
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        findViewById(R.id.btnSearch).setOnClickListener(v -> searchResources());
        
        View btnAdd = findViewById(R.id.btnAddResource);
        if (!"FACULTY".equals(userRole) && !"ADMIN".equals(userRole)) {
            btnAdd.setVisibility(View.GONE);
        } else {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(this, UploadResourceActivity.class);
                startActivity(intent);
            });
        }

        fetchResources();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchResources();
    }

    private void fetchResources() {
        if (token.isEmpty()) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        displayList.clear();
        
        RetrofitClient.getApiService().getNotes(token).enqueue(new Callback<List<Note>>() {
            @Override public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Note n : response.body()) 
                        displayList.add(new ResourceItem(n.id, n.title, "Note", n.fileUrl, "Faculty"));
                    updateUI();
                }
            }
            @Override public void onFailure(Call<List<Note>> call, Throwable t) { updateUI(); }
        });
    }

    private void updateUI() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        llResourcesList.removeAllViews();
        
        if (displayList.isEmpty()) {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (ResourceItem item : displayList) {
            View v = inflater.inflate(R.layout.item_resource, llResourcesList, false);
            ((TextView) v.findViewById(R.id.tvResourceTitle)).setText(item.title);
            ((TextView) v.findViewById(R.id.tvResourceType)).setText(item.type);
            ((TextView) v.findViewById(R.id.tvFacultyName)).setText("Uploaded by: " + item.faculty);
            
            TextView icon = v.findViewById(R.id.tvResourceIcon);
            TextView btnOpen = v.findViewById(R.id.btnOpen);

            icon.setText("📄");
            btnOpen.setText("View / Download PDF");

            btnOpen.setOnClickListener(view -> {
                if (item.url != null && !item.url.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.url)));
                } else {
                    Toast.makeText(this, "Link not available", Toast.LENGTH_SHORT).show();
                }
            });
            
            View btnRemove = v.findViewById(R.id.btnRemoveResource);
            if (!"FACULTY".equals(userRole) && !"ADMIN".equals(userRole)) {
                btnRemove.setVisibility(View.GONE);
            } else {
                btnRemove.setOnClickListener(view -> deleteResource(item));
            }
            
            llResourcesList.addView(v);
        }
    }

    private void searchResources() {
        String q = etSearch.getText().toString().toLowerCase().trim();
        if (q.isEmpty()) {
            fetchResources();
            return;
        }
        List<ResourceItem> filtered = new ArrayList<>();
        for (ResourceItem i : displayList) if (i.title.toLowerCase().contains(q)) filtered.add(i);
        displayList.clear();
        displayList.addAll(filtered);
        updateUI();
    }

    private void deleteResource(ResourceItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Resource")
            .setMessage("Are you sure you want to delete this?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Mock delete
                displayList.remove(item);
                updateUI();
                Toast.makeText(this, "Resource deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    static class ResourceItem {
        int id; String title, type, url, faculty;
        ResourceItem(int id, String title, String type, String url, String faculty) {
            this.id = id; this.title = title; this.type = type; this.url = url; this.faculty = faculty;
        }
    }
}
