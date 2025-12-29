package com.example.bingoarena.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bingoarena.R;
import com.example.bingoarena.activities.MatchGameActivity;
import com.example.bingoarena.adapters.FriendsAdapter;
import com.example.bingoarena.models.ApiModels;
import com.example.bingoarena.models.Friend;
import com.example.bingoarena.network.RetrofitClient;
import com.example.bingoarena.utils.SharedPrefsManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsListFragment extends Fragment implements FriendsAdapter.OnFriendClickListener {

    private RecyclerView rvFriends;
    private LinearLayout emptyState;

    private FriendsAdapter friendsAdapter;
    private List<Friend> friendsList = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        initViews(view);
        setupRecyclerView();
        loadFriends();
    }

    private void initViews(View view) {
        rvFriends = view.findViewById(R.id.rvFriends);
        emptyState = view.findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        friendsAdapter = new FriendsAdapter(friendsList, this);
        rvFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFriends.setAdapter(friendsAdapter);
    }

    private void loadFriends() {
        String token = prefsManager.getToken();
        if (token == null || token.isEmpty()) return;

        RetrofitClient.getInstance().getApiService().getFriends()
                .enqueue(new Callback<ApiModels.ApiResponse<List<ApiModels.FriendData>>>() {
                    @Override
                    public void onResponse(Call<ApiModels.ApiResponse<List<ApiModels.FriendData>>> call,
                                           Response<ApiModels.ApiResponse<List<ApiModels.FriendData>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<ApiModels.FriendData> data = response.body().getData();
                            friendsList.clear();

                            if (data != null) {
                                for (ApiModels.FriendData fd : data) {
                                    Friend friend = new Friend(
                                            fd.id,
                                            fd.displayName,
                                            fd.avatarUrl,
                                            false
                                    );
                                    friendsList.add(friend);
                                }
                            }

                            friendsAdapter.updateData(friendsList);
                            updateEmptyState();
                        } else {
                            Toast.makeText(requireContext(), "Failed to load friends", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiModels.ApiResponse<List<ApiModels.FriendData>>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Failed to load friends", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(friendsList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (rvFriends != null) {
            rvFriends.setVisibility(friendsList.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onFriendClick(Friend friend) {
    }

    @Override
    public void onChallengeClick(Friend friend) {
        Intent intent = new Intent(requireContext(), MatchGameActivity.class);
        intent.putExtra("friend_id", friend.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFriends();
    }
}
