package com.example.bingo_android;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputEmail, inputPassword;
    private MaterialButton btnLogin, btnRegister;
    private ProgressBar progressBar;

    // TODO: Inject your ApiService (Retrofit) here
    // private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> doLogin());
        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void doLogin() {
        String email = Objects.requireNonNull(inputEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(inputPassword.getText()).toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // TODO: call POST /api/auth/login
        // Example pseudo:
        /*
        apiService.login(new LoginRequest(email, password))
            .enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        String otpId = response.body().data.otpId;
                        Intent i = new Intent(LoginActivity.this, VerifyOtpActivity.class);
                        i.putExtra("otpId", otpId);
                        i.putExtra("email", email);
                        startActivity(i);
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
         */
    }

    private void doRegister() {
        // You can open a RegisterActivity or reuse this screen
        // calling POST /api/auth/register
        Toast.makeText(this, "Implement register flow here", Toast.LENGTH_SHORT).show();
    }
}
