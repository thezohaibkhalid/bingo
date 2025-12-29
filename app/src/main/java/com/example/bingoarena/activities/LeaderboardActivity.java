package com.example.bingoarena.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.bingoarena.R;
import com.example.bingoarena.adapters.LeaderboardAdapter;
import com.example.bingoarena.models.ApiModels;
import com.example.bingoarena.models.LeaderboardEntry;
import com.example.bingoarena.network.ApiService;
import com.example.bingoarena.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends BaseActivity {
    private static final String TAG = "LeaderboardActivity";

    private TabLayout tabLayout;
    private RecyclerView rvLeaderboard;
    private View emptyState, podiumContainer, skeletonPodium, skeletonList;
    private BottomNavigationView bottomNav;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvRank1Name, tvRank1Wins, tvRank1Initial;
    private TextView tvRank2Name, tvRank2Wins, tvRank2Initial;
    private TextView tvRank3Name, tvRank3Wins, tvRank3Initial;

    private ApiService apiService;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> globalEntries = new ArrayList<>();
    private List<LeaderboardEntry> friendsEntries = new ArrayList<>();
    private List<ObjectAnimator> skeletonAnimators = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        applyWindowInsetsWithBottomNav(findViewById(android.R.id.content), findViewById(R.id.bottomNav));

        apiService = RetrofitClient.getInstance().getApiService();
        initViews();
        setupTabs();
        setupBottomNav();
        setupSwipeRefresh();
        loadLeaderboard(true);
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        emptyState = findViewById(R.id.emptyState);
        bottomNav = findViewById(R.id.bottomNav);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        podiumContainer = findViewById(R.id.podiumContainer);
        skeletonPodium = findViewById(R.id.skeletonPodium);
        skeletonList = findViewById(R.id.skeletonList);

        tvRank1Name = findViewById(R.id.tvRank1Name);
        tvRank1Wins = findViewById(R.id.tvRank1Wins);
        tvRank1Initial = findViewById(R.id.tvRank1Initial);
        tvRank2Name = findViewById(R.id.tvRank2Name);
        tvRank2Wins = findViewById(R.id.tvRank2Wins);
        tvRank2Initial = findViewById(R.id.tvRank2Initial);
        tvRank3Name = findViewById(R.id.tvRank3Name);
        tvRank3Wins = findViewById(R.id.tvRank3Wins);
        tvRank3Initial = findViewById(R.id.tvRank3Initial);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        rvLeaderboard.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.card);
        swipeRefresh.setOnRefreshListener(() -> loadLeaderboard(tabLayout.getSelectedTabPosition() == 0));
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadLeaderboard(tab.getPosition() == 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showSkeletons() {
        skeletonPodium.setVisibility(View.VISIBLE);
        skeletonList.setVisibility(View.VISIBLE);
        podiumContainer.setVisibility(View.GONE);
        rvLeaderboard.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        animateSkeletonViews((ViewGroup) skeletonPodium);
        animateSkeletonViews((ViewGroup) skeletonList);
    }

    private void hideSkeletons() {
        stopSkeletonAnimations();
        skeletonPodium.setVisibility(View.GONE);
        skeletonList.setVisibility(View.GONE);
    }

    private void animateSkeletonViews(ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof ViewGroup) {
                animateSkeletonViews((ViewGroup) child);
            } else {
                ObjectAnimator animator = ObjectAnimator.ofFloat(child, "alpha", 0.3f, 0.7f, 0.3f);
                animator.setDuration(1500);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setStartDelay(i * 100L);
                animator.start();
                skeletonAnimators.add(animator);
            }
        }
    }

    private void stopSkeletonAnimations() {
        for (ObjectAnimator animator : skeletonAnimators) {
            animator.cancel();
        }
        skeletonAnimators.clear();
    }

    private void loadLeaderboard(boolean isGlobal) {
        if (!swipeRefresh.isRefreshing()) {
            showSkeletons();
        }

        Call<ApiModels.ApiResponse<ApiModels.LeaderboardResponse>> call =
                isGlobal ? apiService.getGlobalLeaderboard() : apiService.getFriendsLeaderboard();

        call.enqueue(new Callback<ApiModels.ApiResponse<ApiModels.LeaderboardResponse>>() {
            @Override
            public void onResponse(
                    Call<ApiModels.ApiResponse<ApiModels.LeaderboardResponse>> call,
                    Response<ApiModels.ApiResponse<ApiModels.LeaderboardResponse>> response
            ) {
                swipeRefresh.setRefreshing(false);
                hideSkeletons();

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    List<ApiModels.LeaderboardEntry> apiEntries =
                            response.body().data != null ? response.body().data.entries : null;

                    if (apiEntries != null) {
                        int rank = 1;
                        for (ApiModels.LeaderboardEntry e : apiEntries) {
                            String name = e.displayName != null ? e.displayName : "";
                            String username = e.username != null ? e.username : "";
                            entries.add(new LeaderboardEntry(rank++, name, username, e.wins));
                        }
                    }

                    if (isGlobal) {
                        globalEntries = entries;
                    } else {
                        friendsEntries = entries;
                    }
                    displayLeaderboard(entries);
                    return;
                }

                displayLeaderboard(new ArrayList<>());
            }

            @Override
            public void onFailure(
                    Call<ApiModels.ApiResponse<ApiModels.LeaderboardResponse>> call,
                    Throwable t
            ) {
                swipeRefresh.setRefreshing(false);
                hideSkeletons();
                Log.e(TAG, "Load error: " + t.getMessage());
                displayLeaderboard(new ArrayList<>());
            }
        });
    }

    private void displayLeaderboard(List<LeaderboardEntry> entries) {
        if (entries.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            podiumContainer.setVisibility(View.GONE);
            rvLeaderboard.setVisibility(View.GONE);
            return;
        }
        emptyState.setVisibility(View.GONE);

        if (entries.size() >= 1) {
            tvRank1Name.setText(entries.get(0).getDisplayName());
            tvRank1Wins.setText(entries.get(0).getWins() + " wins");
            tvRank1Initial.setText(entries.get(0).getDisplayName() != null && !entries.get(0).getDisplayName().isEmpty()
                    ? entries.get(0).getDisplayName().substring(0, 1).toUpperCase()
                    : "?");

        }
        if (entries.size() >= 2) {
            tvRank2Name.setText(entries.get(1).getDisplayName());
            tvRank2Wins.setText(entries.get(1).getWins() + " wins");
            tvRank2Initial.setText(entries.get(1).getDisplayName().substring(0, 1).toUpperCase());
        }
        if (entries.size() >= 3) {
            tvRank3Name.setText(entries.get(2).getDisplayName());
            tvRank3Wins.setText(entries.get(2).getWins() + " wins");
            tvRank3Initial.setText(entries.get(2).getDisplayName().substring(0, 1).toUpperCase());
        }

        if (entries.size() >= 3) {
            podiumContainer.setVisibility(View.VISIBLE);
            podiumContainer.setAlpha(0f);
            podiumContainer.animate().alpha(1f).setDuration(400).start();
        } else {
            podiumContainer.setVisibility(View.GONE);
        }

        if (entries.size() > 3) {
            adapter.updateData(entries.subList(3, entries.size()));
            rvLeaderboard.setVisibility(View.VISIBLE);
            rvLeaderboard.setAlpha(0f);
            rvLeaderboard.animate().alpha(1f).setDuration(400).setStartDelay(200).start();
        } else {
            rvLeaderboard.setVisibility(View.GONE);
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_leaderboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivityNoTransition(new Intent(this, DashboardActivity.class));
                finishNoTransition();
                return true;
            } else if (itemId == R.id.nav_friends) {
                startActivityNoTransition(new Intent(this, FriendsActivity.class));
                finishNoTransition();
                return true;
            } else if (itemId == R.id.nav_matches) {
                startActivityNoTransition(new Intent(this, MatchesActivity.class));
                finishNoTransition();
                return true;
            } else if (itemId == R.id.nav_leaderboard) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSkeletonAnimations();
    }
}
