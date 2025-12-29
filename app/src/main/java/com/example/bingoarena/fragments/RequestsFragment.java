package com.example.bingoarena.fragments;

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
import com.example.bingoarena.adapters.RequestsAdapter;
import com.example.bingoarena.models.ApiModels;
import com.example.bingoarena.models.FriendRequest;
import com.example.bingoarena.network.RetrofitClient;
import com.example.bingoarena.utils.SharedPrefsManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsFragment extends Fragment implements RequestsAdapter.OnRequestActionListener {

    private RecyclerView rvIncoming, rvOutgoing;
    private LinearLayout incomingSection, outgoingSection, emptyState;

    private RequestsAdapter incomingAdapter, outgoingAdapter;
    private List<FriendRequest> incomingRequests = new ArrayList<>();
    private List<FriendRequest> outgoingRequests = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        initViews(view);
        setupRecyclerViews();
        loadRequests();
    }

    private void initViews(View view) {
        rvIncoming = view.findViewById(R.id.rvIncoming);
        rvOutgoing = view.findViewById(R.id.rvOutgoing);
        incomingSection = view.findViewById(R.id.incomingSection);
        outgoingSection = view.findViewById(R.id.outgoingSection);
        emptyState = view.findViewById(R.id.emptyState);
    }

    private void setupRecyclerViews() {
        incomingAdapter = new RequestsAdapter(incomingRequests, true, this);
        if (rvIncoming != null) {
            rvIncoming.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvIncoming.setAdapter(incomingAdapter);
        }

        outgoingAdapter = new RequestsAdapter(outgoingRequests, false, this);
        if (rvOutgoing != null) {
            rvOutgoing.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvOutgoing.setAdapter(outgoingAdapter);
        }
    }

    private void loadRequests() {
        String token = prefsManager.getToken();
        if (token == null || token.isEmpty()) return;

        RetrofitClient.getInstance().getApiService().getFriendRequests()
                .enqueue(new Callback<ApiModels.ApiResponse<ApiModels.FriendRequestsResponse>>() {
                    @Override
                    public void onResponse(Call<ApiModels.ApiResponse<ApiModels.FriendRequestsResponse>> call,
                                           Response<ApiModels.ApiResponse<ApiModels.FriendRequestsResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            ApiModels.FriendRequestsResponse data = response.body().getData();

                            incomingRequests.clear();
                            outgoingRequests.clear();

                            if (data != null) {
                                if (data.getIncoming() != null) {
                                    for (ApiModels.IncomingRequest ir : data.getIncoming()) {
                                        FriendRequest req = new FriendRequest(
                                                ir.getId(),
                                                ir.getRequesterId(),
                                                ir.getRequesterName(),
                                                ir.getRequesterUsername(),
                                                true
                                        );
                                        req.setCreatedAt(ir.getCreatedAt());
                                        incomingRequests.add(req);
                                    }
                                }

                                if (data.getOutgoing() != null) {
                                    for (ApiModels.OutgoingRequest or : data.getOutgoing()) {
                                        FriendRequest req = new FriendRequest(
                                                or.getId(),
                                                or.getAddresseeId(),
                                                or.getAddresseeName(),
                                                or.getAddresseeUsername(),
                                                false
                                        );
                                        req.setCreatedAt(or.getCreatedAt());
                                        outgoingRequests.add(req);
                                    }
                                }
                            }

                            incomingAdapter.updateData(incomingRequests);
                            outgoingAdapter.updateData(outgoingRequests);

                            updateVisibility();
                        } else {
                            Toast.makeText(requireContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiModels.ApiResponse<ApiModels.FriendRequestsResponse>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateVisibility() {
        boolean hasIncoming = !incomingRequests.isEmpty();
        boolean hasOutgoing = !outgoingRequests.isEmpty();

        if (incomingSection != null) {
            incomingSection.setVisibility(hasIncoming ? View.VISIBLE : View.GONE);
        }

        if (outgoingSection != null) {
            outgoingSection.setVisibility(hasOutgoing ? View.VISIBLE : View.GONE);
        }

        if (emptyState != null) {
            emptyState.setVisibility(!hasIncoming && !hasOutgoing ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onAccept(FriendRequest request) {
        String token = prefsManager.getToken();
        if (token == null || token.isEmpty()) return;

        RetrofitClient.getInstance().getApiService().acceptFriendRequest(request.getId())
                .enqueue(new Callback<ApiModels.ApiResponse<ApiModels.SuccessResponse>>() {
                    @Override
                    public void onResponse(Call<ApiModels.ApiResponse<ApiModels.SuccessResponse>> call,
                                           Response<ApiModels.ApiResponse<ApiModels.SuccessResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), "Friend request accepted!", Toast.LENGTH_SHORT).show();
                            loadRequests();
                        } else {
                            Toast.makeText(requireContext(), "Failed to accept request", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiModels.ApiResponse<ApiModels.SuccessResponse>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Failed to accept request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onReject(FriendRequest request) {
        String token = prefsManager.getToken();
        if (token == null || token.isEmpty()) return;

        RetrofitClient.getInstance().getApiService().rejectFriendRequest(request.getId())
                .enqueue(new Callback<ApiModels.ApiResponse<ApiModels.SuccessResponse>>() {
                    @Override
                    public void onResponse(Call<ApiModels.ApiResponse<ApiModels.SuccessResponse>> call,
                                           Response<ApiModels.ApiResponse<ApiModels.SuccessResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), "Friend request rejected", Toast.LENGTH_SHORT).show();
                            loadRequests();
                        } else {
                            Toast.makeText(requireContext(), "Failed to reject request", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiModels.ApiResponse<ApiModels.SuccessResponse>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Failed to reject request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRequests();
    }
}
