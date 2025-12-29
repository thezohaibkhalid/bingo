package com.example.bingoarena.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bingoarena.R;
import com.example.bingoarena.models.ApiModels.*;
import com.example.bingoarena.network.ApiService;
import com.example.bingoarena.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {
    private static final String TAG = "RegisterActivity";

    private EditText etEmail, etUsername, etDisplayName, etPassword;
    private Button btnRegister;
    private ProgressBar progressBar, pbUsername;
    private ImageView ivUsernameStatus;
    private TextView tvLogin;
    
    private ApiService apiService;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable usernameCheckRunnable;
    private boolean isUsernameAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Apply window insets for edge-to-edge
        applyWindowInsets(findViewById(android.R.id.content));
        
        apiService = RetrofitClient.getInstance().getApiService();
        
        initViews();
        setupClickListeners();
        setupUsernameWatcher();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etDisplayName = findViewById(R.id.etDisplayName);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        pbUsername = findViewById(R.id.pbUsername);
        ivUsernameStatus = findViewById(R.id.ivUsernameStatus);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void setupUsernameWatcher() {
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (usernameCheckRunnable != null) {
                    handler.removeCallbacks(usernameCheckRunnable);
                }
                ivUsernameStatus.setVisibility(View.GONE);
                isUsernameAvailable = false;
                
                String username = s.toString().toLowerCase().trim();
                if (username.length() >= 3) {
                    pbUsername.setVisibility(View.VISIBLE);
                    usernameCheckRunnable = () -> checkUsernameAvailability(username);
                    handler.postDelayed(usernameCheckRunnable, 500);
                } else {
                    pbUsername.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkUsernameAvailability(String username) {
        apiService.checkUsername(username).enqueue(new Callback<ApiResponse<UsernameAvailability>>() {
            @Override
            public void onResponse(Call<ApiResponse<UsernameAvailability>> call, Response<ApiResponse<UsernameAvailability>> response) {
                pbUsername.setVisibility(View.GONE);
                ivUsernameStatus.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    isUsernameAvailable = response.body().data.available;
                    
                    if (isUsernameAvailable) {
                        ivUsernameStatus.setImageResource(R.drawable.ic_check);
                        ivUsernameStatus.setColorFilter(getColor(R.color.success));
                    } else {
                        ivUsernameStatus.setImageResource(R.drawable.ic_x);
                        ivUsernameStatus.setColorFilter(getColor(R.color.destructive));
                    }
                } else {
                    ivUsernameStatus.setImageResource(R.drawable.ic_x);
                    ivUsernameStatus.setColorFilter(getColor(R.color.destructive));
                    isUsernameAvailable = false;
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UsernameAvailability>> call, Throwable t) {
                pbUsername.setVisibility(View.GONE);
                ivUsernameStatus.setVisibility(View.VISIBLE);
                ivUsernameStatus.setImageResource(R.drawable.ic_x);
                ivUsernameStatus.setColorFilter(getColor(R.color.destructive));
                isUsernameAvailable = false;
                Log.e(TAG, "Username check error: " + t.getMessage());
            }
        });
    }

    private void handleRegister() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().toLowerCase().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3 || username.length() > 30) {
            Toast.makeText(this, "Username must be 3-30 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!username.matches("^[a-z0-9._]+$")) {
            Toast.makeText(this, "Username can only contain lowercase letters, numbers, dots, and underscores", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isUsernameAvailable) {
            Toast.makeText(this, "Username is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        
        RegisterRequest request = new RegisterRequest(
            email, 
            password, 
            username, 
            displayName.isEmpty() ? username : displayName
        );
        
        apiService.register(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(RegisterActivity.this, "Account created! Please login.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    String errorMsg = "Registration failed";
                    if (response.body() != null && response.body().message != null) {
                        errorMsg = response.body().message;
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Registration error: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etUsername.setEnabled(!isLoading);
        etDisplayName.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }
}
