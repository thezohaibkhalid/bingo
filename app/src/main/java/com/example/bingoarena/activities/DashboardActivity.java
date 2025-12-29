package com.example.bingoarena.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bingoarena.R;
import com.example.bingoarena.adapters.FriendsAdapter;
import com.example.bingoarena.adapters.MatchesAdapter;
import com.example.bingoarena.models.Friend;
import com.example.bingoarena.models.Match;
import com.example.bingoarena.models.ApiModels.ActiveMatchResponse;
import com.example.bingoarena.models.ApiModels.ApiResponse;
import com.example.bingoarena.models.ApiModels.FriendData;
import com.example.bingoarena.models.ApiModels.InviteMatchBody;
import com.example.bingoarena.models.ApiModels.MatchData;
import com.example.bingoarena.network.ApiService;
import com.example.bingoarena.network.RetrofitClient;
import com.example.bingoarena.network.WebSocketManager;
import com.example.bingoarena.utils.SharedPrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends BaseActivity implements
        WebSocketManager.WebSocketEventListener,
        MatchesAdapter.OnMatchClickListener,
        FriendsAdapter.OnFriendClickListener {

    private static final String TAG = "DashboardActivity";

    private TextView tvWelcome, tvActiveMatches, tvTotalWins, tvFriendsCount, tvPendingInvites;
    private TextView tvViewAllMatches, tvViewAllFriends;
    private RecyclerView rvActiveMatches, rvOnlineFriends;
    private View cardNoMatches, skeletonStats, skeletonMatches, statsContainer;
    private Button btnFindFriends;
    private BottomNavigationView bottomNav;
    private SwipeRefreshLayout swipeRefresh;

    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private MatchesAdapter matchesAdapter;
    private FriendsAdapter friendsAdapter;
    private final List<Match> allMatches = new ArrayList<>();
    private final List<Friend> allFriends = new ArrayList<>();
    private final List<ObjectAnimator> skeletonAnimators = new ArrayList<>();
    private boolean matchesLoaded = false;
    private boolean friendsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        View rootView = findViewById(android.R.id.content);
        applyWindowInsetsWithBottomNav(rootView, findViewById(R.id.bottomNav));

        apiService = RetrofitClient.getInstance().getApiService();
        prefsManager = SharedPrefsManager.getInstance(this);

        if (!prefsManager.isLoggedIn()) {
            startActivityWithTransition(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupRecyclerViews();
        setupClickListeners();
        setupBottomNav();
        setupSwipeRefresh();

        WebSocketManager.getInstance().connect(prefsManager.getToken());
        WebSocketManager.getInstance().addEventListener(this);

        loadData();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvActiveMatches = findViewById(R.id.tvActiveMatches);
        tvTotalWins = findViewById(R.id.tvTotalWins);
        tvFriendsCount = findViewById(R.id.tvFriendsCount);
        tvPendingInvites = findViewById(R.id.tvPendingInvites);
        tvViewAllMatches = findViewById(R.id.tvViewAllMatches);
        tvViewAllFriends = findViewById(R.id.tvViewAllFriends);
        rvActiveMatches = findViewById(R.id.rvActiveMatches);
        rvOnlineFriends = findViewById(R.id.rvOnlineFriends);
        cardNoMatches = findViewById(R.id.cardNoMatches);
        btnFindFriends = findViewById(R.id.btnFindFriends);
        bottomNav = findViewById(R.id.bottomNav);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        skeletonStats = findViewById(R.id.skeletonStats);
        skeletonMatches = findViewById(R.id.skeletonMatches);
        statsContainer = findViewById(R.id.statsContainer);

        String displayName = prefsManager.getDisplayName();
        String firstName = displayName == null || displayName.trim().isEmpty()
                ? "Player"
                : displayName.trim().split(" ")[0];

        tvWelcome.setText(getString(R.string.welcome_user, firstName));
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.card);
        swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void setupRecyclerViews() {
        rvActiveMatches.setLayoutManager(new LinearLayoutManager(this));
        matchesAdapter = new MatchesAdapter(new ArrayList<Match>(), this, prefsManager.getUserId());
        rvActiveMatches.setAdapter(matchesAdapter);

        rvOnlineFriends.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        friendsAdapter = new FriendsAdapter(new ArrayList<Friend>(), this, FriendsAdapter.TYPE_HORIZONTAL);
        rvOnlineFriends.setAdapter(friendsAdapter);
    }

    private void setupClickListeners() {
        tvViewAllMatches.setOnClickListener(v -> startActivityWithTransition(new Intent(this, MatchesActivity.class)));
        tvViewAllFriends.setOnClickListener(v -> startActivityWithTransition(new Intent(this, FriendsActivity.class)));
        btnFindFriends.setOnClickListener(v -> startActivityWithTransition(new Intent(this, FriendsActivity.class)));
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_friends) {
                startActivityNoTransition(new Intent(this, FriendsActivity.class));
                return true;
            }
            if (itemId == R.id.nav_matches) {
                startActivityNoTransition(new Intent(this, MatchesActivity.class));
                return true;
            }
            if (itemId == R.id.nav_leaderboard) {
                startActivityNoTransition(new Intent(this, LeaderboardActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showSkeletons() {
        skeletonStats.setVisibility(View.VISIBLE);
        skeletonMatches.setVisibility(View.VISIBLE);
        statsContainer.setVisibility(View.GONE);
        rvActiveMatches.setVisibility(View.GONE);
        cardNoMatches.setVisibility(View.GONE);

        animateSkeletonViews((ViewGroup) skeletonStats);
        animateSkeletonViews((ViewGroup) skeletonMatches);
    }

    private void hideSkeletons() {
        stopSkeletonAnimations();
        skeletonStats.setVisibility(View.GONE);
        skeletonMatches.setVisibility(View.GONE);
        statsContainer.setVisibility(View.VISIBLE);
        statsContainer.setAlpha(0f);
        statsContainer.animate().alpha(1f).setDuration(300).start();
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
        for (ObjectAnimator animator : skeletonAnimators) animator.cancel();
        skeletonAnimators.clear();
    }

    private void loadData() {
        matchesLoaded = false;
        friendsLoaded = false;

        if (!swipeRefresh.isRefreshing()) showSkeletons();

        loadMatches();
        loadFriends();
    }

    private void loadMatches() {
        apiService.getMatches().enqueue(new Callback<ApiResponse<List<MatchData>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MatchData>>> call, Response<ApiResponse<List<MatchData>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<MatchData> matchDataList = response.body().data;
                    allMatches.clear();

                    String myUserId = prefsManager.getUserId();
                    int activeCount = 0;
                    int winCount = 0;
                    int pendingCount = 0;

                    if (matchDataList != null) {
                        for (MatchData data : matchDataList) {
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

                            if ("IN_PROGRESS".equals(data.status) || "BOARD_SETUP".equals(data.status)) activeCount++;
                            if ("INVITED".equals(data.status) && myUserId != null && myUserId.equals(data.player2Id)) pendingCount++;
                            if ("FINISHED".equals(data.status) && myUserId != null && myUserId.equals(data.winnerUserId)) winCount++;
                        }
                    }

                    tvActiveMatches.setText(String.valueOf(activeCount));
                    tvTotalWins.setText(String.valueOf(winCount));
                    tvPendingInvites.setText(String.valueOf(pendingCount));

                    List<Match> activeMatches = new ArrayList<>();
                    for (Match m : allMatches) {
                        if ("IN_PROGRESS".equals(m.getStatus()) || "BOARD_SETUP".equals(m.getStatus())) activeMatches.add(m);
                    }

                    matchesAdapter.updateData(activeMatches.size() > 3 ? activeMatches.subList(0, 3) : activeMatches);

                    if (activeMatches.isEmpty()) {
                        cardNoMatches.setVisibility(View.VISIBLE);
                        rvActiveMatches.setVisibility(View.GONE);
                    } else {
                        cardNoMatches.setVisibility(View.GONE);
                        rvActiveMatches.setVisibility(View.VISIBLE);
                        rvActiveMatches.setAlpha(0f);
                        rvActiveMatches.animate().alpha(1f).setDuration(300).start();
                    }
                }

                matchesLoaded = true;
                checkLoadingComplete();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MatchData>>> call, Throwable t) {
                Log.e(TAG, "Load matches error: " + (t != null ? t.getMessage() : "unknown"));
                matchesLoaded = true;
                checkLoadingComplete();
            }
        });
    }

    private void loadFriends() {
        apiService.getFriends().enqueue(new Callback<ApiResponse<List<FriendData>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FriendData>>> call, Response<ApiResponse<List<FriendData>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<FriendData> friendDataList = response.body().data;
                    allFriends.clear();

                    if (friendDataList != null) {
                        for (FriendData data : friendDataList) {
                            Friend friend = new Friend(
                                    data.id,
                                    data.displayName,
                                    "",
                                    data.avatarUrl,
                                    data.lastOnlineAt
                            );
                            allFriends.add(friend);
                        }
                    }

                    tvFriendsCount.setText(String.valueOf(allFriends.size()));
                    friendsAdapter.updateData(allFriends.size() > 5 ? allFriends.subList(0, 5) : allFriends);
                }

                friendsLoaded = true;
                checkLoadingComplete();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FriendData>>> call, Throwable t) {
                Log.e(TAG, "Load friends error: " + (t != null ? t.getMessage() : "unknown"));
                friendsLoaded = true;
                checkLoadingComplete();
            }
        });
    }

    private void checkLoadingComplete() {
        if (matchesLoaded && friendsLoaded) {
            swipeRefresh.setRefreshing(false);
            hideSkeletons();
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
        swipeRefresh.setRefreshing(true);

        apiService.acceptMatchInvite(match.getId()).enqueue(new Callback<ApiResponse<MatchData>>() {
            @Override
            public void onResponse(Call<ApiResponse<MatchData>> call, Response<ApiResponse<MatchData>> response) {
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    MatchData data = response.body().data;
                    String matchId = data != null ? data.id : match.getId();

                    Toast.makeText(DashboardActivity.this, "Match accepted!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(DashboardActivity.this, MatchGameActivity.class);
                    intent.putExtra("matchId", matchId);
                    startActivityWithTransition(intent);
                } else {
                    String msg = "Failed to accept match";
                    if (response.body() != null && response.body().message != null) msg = response.body().message;
                    Toast.makeText(DashboardActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MatchData>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(DashboardActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFriendClick(Friend friend) {
        apiService.getActiveMatchWith(friend.getId()).enqueue(new Callback<ApiResponse<ActiveMatchResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ActiveMatchResponse>> call, Response<ApiResponse<ActiveMatchResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    ActiveMatchResponse data = response.body().data;
                    if (data != null && data.hasActiveMatch) {
                        Intent intent = new Intent(DashboardActivity.this, MatchGameActivity.class);
                        intent.putExtra("matchId", data.matchId);
                        startActivityWithTransition(intent);
                    } else {
                        showChallengeDialog(friend);
                    }
                } else {
                    showChallengeDialog(friend);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ActiveMatchResponse>> call, Throwable t) {
                showChallengeDialog(friend);
            }
        });
    }

    @Override
    public void onChallengeClick(Friend friend) {
        showChallengeDialog(friend);
    }

    private void showChallengeDialog(Friend friend) {
        new AlertDialog.Builder(this)
                .setTitle("Challenge " + friend.getDisplayName() + "?")
                .setMessage("Send a match invitation to " + friend.getDisplayName() + "?")
                .setPositiveButton("Challenge", (dialog, which) -> inviteFriend(friend))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void inviteFriend(Friend friend) {
        swipeRefresh.setRefreshing(true);

        InviteMatchBody body = new InviteMatchBody(friend.getId());
        apiService.inviteToMatch(body).enqueue(new Callback<ApiResponse<MatchData>>() {
            @Override
            public void onResponse(Call<ApiResponse<MatchData>> call, Response<ApiResponse<MatchData>> response) {
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    MatchData match = response.body().data;
                    Toast.makeText(DashboardActivity.this, "Invitation sent!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(DashboardActivity.this, MatchGameActivity.class);
                    intent.putExtra("matchId", match != null ? match.id : "");
                    startActivityWithTransition(intent);
                } else {
                    String errorMsg = "Failed to send invitation";
                    if (response.body() != null && response.body().message != null) errorMsg = response.body().message;
                    Toast.makeText(DashboardActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MatchData>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(DashboardActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFriendRequest(String requestId, String fromUserId) {
        runOnUiThread(() -> Toast.makeText(this, "New friend request!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMatchInvite(String matchId, String fromUserId) {
        runOnUiThread(() -> {
            Toast.makeText(this, "New match invitation!", Toast.LENGTH_SHORT).show();
            loadData();
        });
    }

    @Override
    public void onMatchAccepted(String matchId, String byUserId) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Match accepted! Setting up...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MatchGameActivity.class);
            intent.putExtra("matchId", matchId);
            startActivityWithTransition(intent);
        });
    }

    @Override
    public void onBoardSetupComplete(String matchId, String startingUserId) {}

    @Override
    public void onOpponentMove(String matchId, int moveNumber, int number, String fromUserId) {}

    @Override
    public void onYourTurn(String matchId) {
        runOnUiThread(() -> Toast.makeText(this, "It's your turn!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMatchFinished(String matchId, String winnerUserId) {
        runOnUiThread(this::loadData);
    }

    @Override
    public void onConnectionChanged(boolean connected) {
        Log.d(TAG, "WebSocket connected: " + connected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSkeletonAnimations();
        WebSocketManager.getInstance().removeEventListener(this);
    }
}
