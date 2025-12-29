package com.example.bingoarena.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.bingoarena.R;
import com.example.bingoarena.models.ApiModels;
import com.example.bingoarena.network.RetrofitClient;
import com.example.bingoarena.utils.SharedPrefsManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends BaseActivity {

    private EditText[] otpFields;
    private Button btnVerify;
    private ProgressBar progressBar;
    private TextView tvEmail;

    private SharedPrefsManager prefsManager;
    private String otpId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        applyWindowInsets(findViewById(android.R.id.content));

        prefsManager = SharedPrefsManager.getInstance(this);

        otpId = getIntent().getStringExtra("otp_id");
        if (otpId == null || otpId.isEmpty()) {
            Toast.makeText(this, "Invalid OTP session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvEmail = findViewById(R.id.tvEmail);
        btnVerify = findViewById(R.id.btnVerify);
        progressBar = findViewById(R.id.progressBar);

        String email = getIntent().getStringExtra("email");
        if (tvEmail != null) {
            if (email != null && !email.trim().isEmpty()) {
                tvEmail.setText("Code sent to " + email.trim());
            }
        }

        otpFields = new EditText[]{
                findViewById(R.id.etOtp1),
                findViewById(R.id.etOtp2),
                findViewById(R.id.etOtp3),
                findViewById(R.id.etOtp4),
                findViewById(R.id.etOtp5),
                findViewById(R.id.etOtp6)
        };

        View back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> finish());

        setupOtpInputs();

        btnVerify.setOnClickListener(v -> {
            String code = getOtpCode();
            if (code.length() != 6) {
                Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOtp(code);
        });

        if (otpFields[0] != null) otpFields[0].requestFocus();
        updateVerifyState();
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            EditText field = otpFields[i];
            if (field == null) continue;

            field.addTextChangedListener(new TextWatcher() {
                boolean handlingPaste = false;

                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (handlingPaste) return;

                    String val = s == null ? "" : s.toString();

                    if (index == 0 && val.length() > 1) {
                        String digits = val.replaceAll("[^0-9]", "");
                        if (digits.length() > 6) digits = digits.substring(0, 6);

                        handlingPaste = true;
                        for (int j = 0; j < otpFields.length; j++) {
                            if (otpFields[j] == null) continue;
                            if (j < digits.length()) {
                                otpFields[j].setText(String.valueOf(digits.charAt(j)));
                            } else {
                                otpFields[j].setText("");
                            }
                        }
                        handlingPaste = false;

                        int focusIndex = Math.min(digits.length(), 5);
                        if (otpFields[focusIndex] != null) otpFields[focusIndex].requestFocus();
                        updateVerifyState();
                        return;
                    }

                    if (val.length() == 1) {
                        if (index < otpFields.length - 1 && otpFields[index + 1] != null) {
                            otpFields[index + 1].requestFocus();
                        }
                    }

                    updateVerifyState();
                }
            });

            field.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    String val = field.getText() == null ? "" : field.getText().toString();
                    if (val.isEmpty() && index > 0 && otpFields[index - 1] != null) {
                        otpFields[index - 1].requestFocus();
                        otpFields[index - 1].setSelection(otpFields[index - 1].getText() != null ? otpFields[index - 1].getText().length() : 0);
                        updateVerifyState();
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void updateVerifyState() {
        boolean ready = true;
        for (EditText f : otpFields) {
            String v = (f == null || f.getText() == null) ? "" : f.getText().toString().trim();
            if (v.length() != 1) {
                ready = false;
                break;
            }
        }
        btnVerify.setEnabled(ready);
        btnVerify.setAlpha(ready ? 1f : 0.5f);
    }

    private String getOtpCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText f : otpFields) {
            String v = (f == null || f.getText() == null) ? "" : f.getText().toString().trim();
            sb.append(v);
        }
        return sb.toString();
    }

    private void verifyOtp(String code) {
        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);
        btnVerify.setAlpha(0.5f);

        ApiModels.VerifyOtpRequest body = new ApiModels.VerifyOtpRequest(otpId, code);

        RetrofitClient.getInstance().getApiService().verifyOtp(body)
                .enqueue(new Callback<ApiModels.ApiResponse<ApiModels.VerifyOtpResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiModels.ApiResponse<ApiModels.VerifyOtpResponse>> call,
                            Response<ApiModels.ApiResponse<ApiModels.VerifyOtpResponse>> response
                    ) {
                        progressBar.setVisibility(View.GONE);
                        updateVerifyState();

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(OtpVerificationActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ApiModels.ApiResponse<ApiModels.VerifyOtpResponse> res = response.body();
                        if (!res.success) {
                            String msg = (res.message != null && !res.message.isEmpty()) ? res.message : "Invalid OTP";
                            Toast.makeText(OtpVerificationActivity.this, msg, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ApiModels.VerifyOtpResponse data = res.data;
                        if (data == null || data.token == null || data.token.isEmpty() || data.user == null) {
                            Toast.makeText(OtpVerificationActivity.this, "Invalid server response", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        prefsManager.saveLoginData(
                                data.token,
                                data.user.id,
                                data.user.displayName,
                                data.user.username,
                                data.user.email
                        );

                        Intent intent = new Intent(OtpVerificationActivity.this, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(
                            Call<ApiModels.ApiResponse<ApiModels.VerifyOtpResponse>> call,
                            Throwable t
                    ) {
                        progressBar.setVisibility(View.GONE);
                        updateVerifyState();
                        Toast.makeText(OtpVerificationActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
