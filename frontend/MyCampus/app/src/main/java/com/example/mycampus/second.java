package com.example.mycampus;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class second extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple UI for the dashboard placeholder
        TextView textView = new TextView(this);
        String role = getIntent().getStringExtra("USER_ROLE");
        textView.setText("Welcome to the Dashboard!\nRole: " + (role != null ? role : "Unknown"));
        textView.setTextSize(24);
        textView.setPadding(50, 50, 50, 50);
        
        setContentView(textView);
    }
}
