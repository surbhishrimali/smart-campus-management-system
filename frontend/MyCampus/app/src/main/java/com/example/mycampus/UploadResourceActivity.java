package com.example.mycampus;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mycampus.models.Note;
import com.example.mycampus.models.Subject;
import com.example.mycampus.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadResourceActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etYoutubeUrl;
    private TextInputLayout tilYoutubeUrl;
    private Spinner spSubject;
    private RadioGroup rgResourceType;
    private View llPdfPicker;
    private TextView tvSelectedFileName;
    private ProgressBar progressBar;
    
    private Uri selectedPdfUri;
    private List<Subject> subjects = new ArrayList<>();
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_resource);

        token = getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", "");

        etTitle = findViewById(R.id.etResourceTitle);
        etDescription = findViewById(R.id.etResourceDescription);
        etYoutubeUrl = findViewById(R.id.etYoutubeUrl);
        tilYoutubeUrl = findViewById(R.id.tilYoutubeUrl);
        spSubject = findViewById(R.id.spSubject);
        rgResourceType = findViewById(R.id.rgResourceType);
        llPdfPicker = findViewById(R.id.llPdfPicker);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        progressBar = findViewById(R.id.progressBar);

        rgResourceType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPdf) {
                llPdfPicker.setVisibility(View.VISIBLE);
                tilYoutubeUrl.setVisibility(View.GONE);
            } else {
                llPdfPicker.setVisibility(View.GONE);
                tilYoutubeUrl.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.btnPickPdf).setOnClickListener(v -> pickPdf());
        findViewById(R.id.btnUpload).setOnClickListener(v -> validateAndUpload());

        fetchSubjects();
    }

    private void fetchSubjects() {
        RetrofitClient.getApiService().getSubjects(token).enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects = response.body();
                    List<String> subjectNames = new ArrayList<>();
                    for (Subject s : subjects) subjectNames.add(s.name);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(UploadResourceActivity.this, android.R.layout.simple_spinner_item, subjectNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spSubject.setAdapter(adapter);
                }
            }
            @Override public void onFailure(Call<List<Subject>> call, Throwable t) {}
        });
    }

    private final ActivityResultLauncher<Intent> pdfPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedPdfUri = result.getData().getData();
                    tvSelectedFileName.setText(getFileName(selectedPdfUri));
                }
            }
    );

    private void pickPdf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        pdfPickerLauncher.launch(intent);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private void validateAndUpload() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("Title required");
            return;
        }

        if (rgResourceType.getCheckedRadioButtonId() == R.id.rbPdf) {
            if (selectedPdfUri == null) {
                Toast.makeText(this, "Please select a PDF", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadPdf(title);
        } else {
            String url = etYoutubeUrl.getText().toString().trim();
            if (url.isEmpty()) {
                etYoutubeUrl.setError("URL required");
                return;
            }
            // In a real app, you'd have a postYoutubeLink endpoint. 
            // Here we'll just toast success as placeholder if endpoint missing or use generic.
            Toast.makeText(this, "YouTube link added successfully (Mock)", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void uploadPdf(String title) {
        progressBar.setVisibility(View.VISIBLE);
        
        try {
            File file = getFileFromUri(selectedPdfUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            RequestBody titlePart = RequestBody.create(MediaType.parse("text/plain"), title);

            RetrofitClient.getApiService().uploadNote(token, titlePart, body).enqueue(new Callback<Note>() {
                @Override
                public void onResponse(Call<Note> call, Response<Note> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(UploadResourceActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(UploadResourceActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Note> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadResourceActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error preparing file", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File file = new File(getCacheDir(), getFileName(uri));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        return file;
    }
}
