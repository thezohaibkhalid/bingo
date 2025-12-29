package com.example.bingoarena.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bingoarena.R;
import com.example.bingoarena.adapters.SearchResultsAdapter;
import com.example.bingoarena.models.ApiModels;
import com.example.bingoarena.models.Friend;
import com.example.bingoarena.network.RetrofitClient;
import com.example.bingoarena.utils.SharedPrefsManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSearchActivity extends BaseActivity implements SearchResultsAdapter.OnAddFriendListener {

    private EditText etSearch;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvHint;

    private SearchResultsAdapter adapter;
    private List<Friend> searchResults = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        applyWindowInsets(findViewById(android.R.id.content));

        prefsManager = SharedPrefsManager.getInstance(this);

        initViews();
        setupRecyclerView();
        setupSearch();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvResults = findViewById(R.id.rvResults);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvHint = findViewById(R.id.tvHint);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            etSearch.setText("");
            searchResults.clear();
            adapter.updateData(searchResults);
            updateVisibility(false, false);
        });
    }

    private void setupRecyclerView() {
        adapter = new SearchResultsAdapter(searchResults, this);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                String query = s.toString().trim();

                if (query.length() < 2) {
                    searchResults.clear();
                    adapter.updateData(searchResults);
                    updateVisibility(false, query.isEmpty());
                    return;
                }

                searchRunnable = () -> searchUsers(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }
        });

        etSearch.requestFocus();
    }

    private void searchUsers(String query) {
        String token = prefsManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);

        RetrofitClient.getInstance().getApiService().searchUsers(query)
                .enqueue(new Callback<ApiModels.ApiResponse<List<ApiModels.UserSearchResult>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiModels.ApiResponse<List<ApiModels.UserSearchResult>>> call,
                            Response<ApiModels.ApiResponse<List<ApiModels.UserSearchResult>>> response
                    ) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            List<ApiModels.UserSearchResult> data = response.body().data;
                            searchResults.clear();

                            if (data != null) {
                                for (ApiModels.UserSearchResult usr : data) {
                                    Friend friend = new Friend(
                                            usr.id,
                                            usr.displayName,
                                            usr.username,
                                            false
                                    );
                                    searchResults.add(friend);
                                }
                            }

                            adapter.updateData(searchResults);
                            updateVisibility(true, false);
                        } else {
                            Toast.makeText(UserSearchActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiModels.ApiResponse<List<ApiModels.UserSearchResult>>> call,
                            Throwable t
                    ) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(UserSearchActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateVisibility(boolean hasSearched, boolean showHint) {
        if (showHint) {
            tvHint.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            rvResults.setVisibility(View.GONE);
        } else if (hasSearched) {
            tvHint.setVisibility(View.GONE);
            tvEmpty.setVisibility(searchResults.isEmpty() ? View.VISIBLE : View.GONE);
            rvResults.setVisibility(searchResults.isEmpty() ? View.GONE : View.VISIBLE);
        } else {
            tvHint.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            rvResults.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAddFriend(Friend user) {
        String token = prefsManager.getToken();
        if (token == null || token.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);

        ApiModels.SendFriendRequestBody body = new ApiModels.SendFriendRequestBody(user.getId());

        RetrofitClient.getInstance().getApiService().sendFriendRequest(body)
                .enqueue(new Callback<ApiModels.ApiResponse<ApiModels.FriendRequestResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiModels.ApiResponse<ApiModels.FriendRequestResponse>> call,
                            Response<ApiModels.ApiResponse<ApiModels.FriendRequestResponse>> response
                    ) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            Toast.makeText(
                                    UserSearchActivity.this,
                                    "Friend request sent to " + user.getDisplayName() + "!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            searchResults.remove(user);
                            adapter.updateData(searchResults);
                            updateVisibility(true, false);
                        } else {
                            String errorMsg = "Failed to send request";
                            if (response.body() != null && response.body().message != null && !response.body().message.isEmpty()) {
                                errorMsg = response.body().message;
                            }
                            Toast.makeText(UserSearchActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiModels.ApiResponse<ApiModels.FriendRequestResponse>> call,
                            Throwable t
                    ) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(UserSearchActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}
