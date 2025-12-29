package com.example.bingoarena.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.bingoarena.R;
import com.example.bingoarena.utils.SharedPrefsManager;

public class MainActivity extends BaseActivity {

    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefsManager = new SharedPrefsManager(this);
        
        // Check if already logged in
        if (prefsManager.isLoggedIn()) {
            navigateToDashboard();
            return;
        }
        
        setContentView(R.layout.activity_main);
        
        // Apply window insets for edge-to-edge
        applyTopWindowInset(findViewById(android.R.id.content));
        
        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnStartPlaying = findViewById(R.id.btnStartPlaying);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        
        btnGetStarted.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnStartPlaying.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }
    
    private void navigateToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}
