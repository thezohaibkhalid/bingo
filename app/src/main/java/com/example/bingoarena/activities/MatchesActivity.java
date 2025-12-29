package com.example.bingoarena.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.bingoarena.R;
import com.example.bingoarena.adapters.MatchesAdapter;
import com.example.bingoarena.models.ApiModels.*;
import com.example.bingoarena.models.Match;
import com.example.bingoarena.network.ApiService;
import com.example.bingoarena.network.RetrofitClient;
import com.example.bingoarena.utils.SharedPrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchesActivity extends BaseActivity implements MatchesAdapter.OnMatchClickListener {
    private static final String TAG = "MatchesActivity";

    private TabLayout tabLayout;
    private RecyclerView rvMatches;
    private View emptyState, skeletonContainer;
    private BottomNavigationView bottomNav;
    private SwipeRefreshLayout swipeRefresh;

    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private MatchesAdapter adapter;
    private List<Match> allMatches = new ArrayList<>();
    private List<ObjectAnimator> skeletonAnimators = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        applyWindowInsetsWithBottomNav(findViewById(android.R.id.content), findViewById(R.id.bottomNav));

        apiService = RetrofitClient.getInstance().getApiService();
        prefsManager = SharedPrefsManager.getInstance(this);

        initViews();
        setupTabs();
        setupBottomNav();
        setupSwipeRefresh();
        loadMatches();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        rvMatches = findViewById(R.id.rvMatches);
        emptyState = findViewById(R.id.emptyState);
        bottomNav = findViewById(R.id.bottomNav);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        skeletonContainer = findViewById(R.id.skeletonContainer);

        rvMatches.setLayoutManager(new LinearLayoutManager(this));

        String myUserId = prefsManager.getUserId();
        adapter = new MatchesAdapter(new ArrayList<Match>(), this, myUserId);

        rvMatches.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.card);
        swipeRefresh.setOnRefreshListener(this::loadMatches);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterMatches(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showSkeletons() {
        skeletonContainer.setVisibility(View.VISIBLE);
        rvMatches.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        animateSkeletonViews((ViewGroup) skeletonContainer);
    }

    private void hideSkeletons() {
        stopSkeletonAnimations();
        skeletonContainer.setVisibility(View.GONE);
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

    private void loadMatches() {
        if (!swipeRefresh.isRefreshing()) {
            showSkeletons();
        }

        apiService.getMatches().enqueue(new Callback<ApiResponse<List<MatchData>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MatchData>>> call, Response<ApiResponse<List<MatchData>>> response) {
                swipeRefresh.setRefreshing(false);
                hideSkeletons();

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    allMatches.clear();
                    String myUserId = prefsManager.getUserId();

                    for (MatchData data : response.body().data) {
                        Match match = new Match(
                                data.id,
                                data.player1Id,
                                data.player2Id,
                                "Opponent",
                                data.status,
                                data.currentTurnUserId,
                                data.winnerUserId,
                                myUserId
                        );
                        allMatches.add(match);
                    }

                    filterMatches(tabLayout.getSelectedTabPosition());
                } else {
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MatchData>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                hideSkeletons();
                Log.e(TAG, "Load error: " + t.getMessage());
                showEmpty();
            }
        });
    }

    private void showEmpty() {
        emptyState.setVisibility(View.VISIBLE);
        rvMatches.setVisibility(View.GONE);
    }

    private void filterMatches(int tabPosition) {
        List<Match> filtered;
        switch (tabPosition) {
            case 0:
                filtered = allMatches.stream()
                        .filter(m -> "IN_PROGRESS".equals(m.getStatus()) || "BOARD_SETUP".equals(m.getStatus()))
                        .collect(Collectors.toList());
                break;
            case 1:
                filtered = allMatches.stream()
                        .filter(m -> "INVITED".equals(m.getStatus()))
                        .collect(Collectors.toList());
                break;
            case 2:
                filtered = allMatches.stream()
                        .filter(m -> "FINISHED".equals(m.getStatus()) || "CANCELLED".equals(m.getStatus()))
                        .collect(Collectors.toList());
                break;
            default:
                filtered = allMatches;
        }

        adapter.updateData(filtered);

        if (filtered.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvMatches.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvMatches.setVisibility(View.VISIBLE);
            rvMatches.setAlpha(0f);
            rvMatches.animate().alpha(1f).setDuration(300).start();
        }
    }

    @Override
    public void onMatchClick(Match match) {
        Intent intent = new Intent(this, MatchGameActivity.class);
        intent.putExtra("matchId", match.getId());
        startActivityWithTransition(intent);
    }

    @Override
    public void onAcceptMatch(Match match) {
        Intent intent = new Intent(this, MatchGameActivity.class);
        intent.putExtra("matchId", match.getId());
        startActivityWithTransition(intent);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_matches);
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
                return true;
            } else if (itemId == R.id.nav_leaderboard) {
                startActivityNoTransition(new Intent(this, LeaderboardActivity.class));
                finishNoTransition();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMatches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSkeletonAnimations();
    }
}
