package com.example.mycampus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mycampus.models.User;
import com.example.mycampus.network.RetrofitClient;
import com.example.mycampus.network.responses.LoginResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize UI components
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        errorTextView = findViewById(R.id.errorTextView);

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> performLogin());
        }
    }

    private void performLogin() {
        if (usernameEditText == null || passwordEditText == null || errorTextView == null) return;

        String email = "";
        if (usernameEditText.getText() != null) {
            email = usernameEditText.getText().toString().trim();
        }

        String password = "";
        if (passwordEditText.getText() != null) {
            password = passwordEditText.getText().toString().trim();
        }

        errorTextView.setVisibility(View.GONE);

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        // Prepare request body parameters
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        // Make the API request
        Call<LoginResponse> call = RetrofitClient.getApiService().loginUser(credentials);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginData = response.body();
                    String jwtToken = "Bearer " + loginData.getAccess();
                    User user = loginData.getUser();

                    Log.d("API_SUCCESS", "JWT Token received: " + jwtToken);
                    
                    saveUserToPrefs(user, jwtToken);
                    navigateToDashboard(user.role);
                } else {
                    showError(getString(R.string.error_invalid_login));
                    Log.e("API_ERROR", "Login failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
                Log.e("API_FAILURE", "Network error occurred: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

    private void saveUserToPrefs(User user, String token) {
        getSharedPreferences("MY_CAMPUS_PREFS", MODE_PRIVATE)
                .edit()
                .putInt("USER_ID", user.id)
                .putString("USER_ROLE", user.role)
                .putString("USERNAME", user.username)
                .putString("JWT_TOKEN", token)
                .apply();
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("STUDENT".equals(role)) {
            intent = new Intent(MainActivity.this, StudentdashboardActivity.class);
        } else if ("FACULTY".equals(role)) {
            intent = new Intent(MainActivity.this, facultydsashboardActivity.class);
        } else if ("ADMIN".equals(role)) {
            intent = new Intent(MainActivity.this, admindashboardActivity.class);
        } else {
            intent = new Intent(MainActivity.this, second.class);
        }
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
        finish();
    }
}
