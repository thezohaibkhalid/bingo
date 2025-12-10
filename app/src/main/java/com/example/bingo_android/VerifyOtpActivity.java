package com.example.bingo_android;

public class VerifyOtpActivity extends AppCompatActivity {

    private TextInputEditText inputOtp;
    private MaterialButton btnVerifyOtp;
    private ProgressBar progressOtp;

    private String otpId;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        inputOtp = findViewById(R.id.inputOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        progressOtp = findViewById(R.id.progressOtp);

        otpId = getIntent().getStringExtra("otpId");
        email = getIntent().getStringExtra("email");

        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
    }

    private void showLoading(boolean show) {
        progressOtp.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void verifyOtp() {
        String code = Objects.requireNonNull(inputOtp.getText()).toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // TODO: call POST /api/auth/verify-otp
        /*
        apiService.verifyOtp(new VerifyOtpRequest(otpId, code))
            .enqueue(new Callback<VerifyOtpResponse>() { ... })
         */

        // On success:
        // - Save token to SharedPreferences
        // - Navigate to MainActivity
    }
}
