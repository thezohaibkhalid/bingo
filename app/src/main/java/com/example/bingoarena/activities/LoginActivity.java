package com.example.bingoarena.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bingoarena.R;
import com.example.bingoarena.models.ApiModels.*;
import com.example.bingoarena.network.ApiService;
import com.example.bingoarena.network.RetrofitClient;
import com.example.bingoarena.network.WebSocketManager;
import com.example.bingoarena.utils.SharedPrefsManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword, etOtp;
    private Button btnLogin, btnVerify, btnResendOtp;
    private LinearLayout credentialsForm, otpForm;
    private ProgressBar progressBar;
    private TextView tvSignUp, tvTitle, tvSubtitle;
    
    private String otpId = "";
    private String currentEmail = "";
    private ApiService apiService;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Apply window insets for edge-to-edge
        applyWindowInsets(findViewById(android.R.id.content));
        
        apiService = RetrofitClient.getInstance().getApiService();
        prefsManager = new SharedPrefsManager(this);
        
        // Redirect if already logged in
        if (prefsManager.isLoggedIn()) {
            navigateToDashboard();
            return;
        }
        
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etOtp = findViewById(R.id.etOtp);
        btnLogin = findViewById(R.id.btnLogin);
        btnVerify = findViewById(R.id.btnVerify);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        credentialsForm = findViewById(R.id.credentialsForm);
        otpForm = findViewById(R.id.otpForm);
        progressBar = findViewById(R.id.progressBar);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        btnVerify.setOnClickListener(v -> handleVerifyOtp());
        btnResendOtp.setOnClickListener(v -> handleLogin());
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        currentEmail = email;
        setLoading(true);
        
        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    String receivedOtpId = response.body().data.otpId;
                    
                    // Navigate to dedicated OTP verification activity
                    Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                    intent.putExtra("otp_id", receivedOtpId);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    
                    Toast.makeText(LoginActivity.this, "OTP sent to your email!", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Login failed";
                    if (response.body() != null && response.body().message != null) {
                        errorMsg = response.body().message;
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Login error: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleVerifyOtp() {
        String otp = etOtp.getText().toString().trim();

        if (otp.isEmpty() || otp.length() != 6) {
            Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        
        VerifyOtpRequest request = new VerifyOtpRequest(otpId, otp);
        apiService.verifyOtp(request).enqueue(new Callback<ApiResponse<VerifyOtpResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<VerifyOtpResponse>> call, Response<ApiResponse<VerifyOtpResponse>> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    VerifyOtpResponse data = response.body().data;
                    User user = data.user;
                    
                    // Save login data
                    prefsManager.saveLoginData(
                        data.token,
                        user.id,
                        user.displayName != null ? user.displayName : user.username,
                        user.username,
                        user.email
                    );
                    
                    // Connect WebSocket
                    WebSocketManager.getInstance().connect(data.token);
                    
                    Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    String errorMsg = "Verification failed";
                    if (response.body() != null && response.body().message != null) {
                        errorMsg = response.body().message;
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<VerifyOtpResponse>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "OTP verification error: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOtpForm() {
        credentialsForm.setVisibility(View.GONE);
        otpForm.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.verify_otp);
        tvSubtitle.setText(R.string.otp_sent);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnVerify.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etOtp.setEnabled(!isLoading);
    }

    private void navigateToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}
