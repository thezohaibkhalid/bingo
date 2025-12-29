package com.example.bingoarena.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.example.bingoarena.R;
import com.example.bingoarena.adapters.FriendsPagerAdapter;
import com.example.bingoarena.adapters.SearchResultsAdapter;
import com.example.bingoarena.models.ApiModels.*;
import com.example.bingoarena.models.Friend;
import com.example.bingoarena.network.ApiService;
import com.example.bingoarena.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends BaseActivity {
    private static final String TAG = "FriendsActivity";

    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private Button btnAddFriend;
    private SwipeRefreshLayout swipeRefresh;
    
    private ApiService apiService;
    private SearchResultsAdapter searchAdapter;
    private FriendsPagerAdapter pagerAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        
        applyWindowInsetsWithBottomNav(findViewById(android.R.id.content), findViewById(R.id.bottomNav));
        
        apiService = RetrofitClient.getInstance().getApiService();
        
        initViews();
        setupViewPager();
        setupSearch();
        setupBottomNav();
        setupSwipeRefresh();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNav);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchResultsAdapter(new ArrayList<>(), this::onAddFriend);
        rvSearchResults.setAdapter(searchAdapter);
        
        btnAddFriend.setOnClickListener(v -> {
            startActivityWithTransition(new Intent(this, UserSearchActivity.class));
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.card);
        swipeRefresh.setOnRefreshListener(() -> {
            // Refresh the current fragment
            if (pagerAdapter != null) {
                pagerAdapter.notifyDataSetChanged();
            }
            swipeRefresh.setRefreshing(false);
        });
    }

    private void setupViewPager() {
        pagerAdapter = new FriendsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? getString(R.string.my_friends) : getString(R.string.requests));
        }).attach();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                
                if (s.length() >= 2) {
                    searchRunnable = () -> searchUsers(s.toString());
                    handler.postDelayed(searchRunnable, 300);
                } else {
                    rvSearchResults.setVisibility(View.GONE);
                    swipeRefresh.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUsers(String query) {
        apiService.searchUsers(query).enqueue(new Callback<ApiResponse<List<UserSearchResult>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserSearchResult>>> call, Response<ApiResponse<List<UserSearchResult>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<Friend> results = new ArrayList<>();
                    for (UserSearchResult u : response.body().data) {
                        results.add(new Friend(u.id, u.displayName, u.username, false));
                    }
                    searchAdapter.updateData(results);
                    rvSearchResults.setVisibility(View.VISIBLE);
                    rvSearchResults.setAlpha(0f);
                    rvSearchResults.animate().alpha(1f).setDuration(300).start();
                    swipeRefresh.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserSearchResult>>> call, Throwable t) {
                Log.e(TAG, "Search error: " + t.getMessage());
            }
        });
    }

    private void onAddFriend(Friend user) {
        swipeRefresh.setRefreshing(true);
        SendFriendRequestBody body = new SendFriendRequestBody(user.getId());
        
        apiService.sendFriendRequest(body).enqueue(new Callback<ApiResponse<FriendRequestResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FriendRequestResponse>> call, Response<ApiResponse<FriendRequestResponse>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(FriendsActivity.this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                    etSearch.setText("");
                } else {
                    String msg = response.body() != null ? response.body().message : "Failed";
                    Toast.makeText(FriendsActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FriendRequestResponse>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(FriendsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_friends);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivityNoTransition(new Intent(this, DashboardActivity.class));
                finishNoTransition();
                return true;
            } else if (itemId == R.id.nav_friends) {
                return true;
            } else if (itemId == R.id.nav_matches) {
                startActivityNoTransition(new Intent(this, MatchesActivity.class));
                finishNoTransition();
                return true;
            } else if (itemId == R.id.nav_leaderboard) {
                startActivityNoTransition(new Intent(this, LeaderboardActivity.class));
                finishNoTransition();
                return true;
            }
            return false;
        });
    }
}