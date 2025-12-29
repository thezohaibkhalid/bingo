package com.example.bingoarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bingoarena.R;
import com.example.bingoarena.models.FriendRequest;
import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private List<FriendRequest> requests;
    private boolean isIncoming;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(FriendRequest request);
        void onReject(FriendRequest request);
    }

    public RequestsAdapter(List<FriendRequest> requests, boolean isIncoming, OnRequestActionListener listener) {
        this.requests = requests;
        this.isIncoming = isIncoming;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests != null ? requests.size() : 0;
    }

    public void updateData(List<FriendRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvInitial, tvDisplayName, tvUsername, tvPending;
        private LinearLayout actionsContainer;
        private Button btnAccept, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPending = itemView.findViewById(R.id.tvPending);
            actionsContainer = itemView.findViewById(R.id.actionsContainer);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        void bind(FriendRequest request) {
            String displayName = request.getDisplayName();
            String username = request.getUsername();
            
            if (tvInitial != null) {
                String initial = displayName != null && !displayName.isEmpty()
                    ? displayName.substring(0, 1).toUpperCase()
                    : "?";
                tvInitial.setText(initial);
            }
            
            if (tvDisplayName != null) {
                tvDisplayName.setText(displayName != null ? displayName : "Unknown");
            }
            
            if (tvUsername != null) {
                tvUsername.setText(username != null ? "@" + username : "");
            }
            
            if (isIncoming) {
                // Show accept/reject buttons for incoming requests
                if (actionsContainer != null) {
                    actionsContainer.setVisibility(View.VISIBLE);
                }
                if (tvPending != null) {
                    tvPending.setVisibility(View.GONE);
                }
                
                if (btnAccept != null) {
                    btnAccept.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onAccept(request);
                        }
                    });
                }
                
                if (btnReject != null) {
                    btnReject.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onReject(request);
                        }
                    });
                }
            } else {
                // Show pending badge for outgoing requests
                if (actionsContainer != null) {
                    actionsContainer.setVisibility(View.GONE);
                }
                if (tvPending != null) {
                    tvPending.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
