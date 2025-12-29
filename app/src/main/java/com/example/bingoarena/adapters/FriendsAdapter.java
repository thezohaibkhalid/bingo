package com.example.bingoarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bingoarena.R;
import com.example.bingoarena.models.Friend;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    
    public static final int TYPE_VERTICAL = 0;
    public static final int TYPE_HORIZONTAL = 1;
    
    private List<Friend> friends;
    private OnFriendClickListener listener;
    private int viewType;

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
        void onChallengeClick(Friend friend);
    }

    public FriendsAdapter(List<Friend> friends, OnFriendClickListener listener, int viewType) {
        this.friends = friends;
        this.listener = listener;
        this.viewType = viewType;
    }

    public FriendsAdapter(List<Friend> friends, OnFriendClickListener listener) {
        this(friends, listener, TYPE_VERTICAL);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = this.viewType == TYPE_HORIZONTAL ? R.layout.item_friend_horizontal : R.layout.item_friend;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend friend = friends.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friends != null ? friends.size() : 0;
    }

    public void updateData(List<Friend> newFriends) {
        this.friends = newFriends;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvInitial, tvDisplayName, tvUsername;
        private Button btnAction;
        private View onlineIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnAction = itemView.findViewById(R.id.btnAction);
            onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        }

        void bind(Friend friend) {
            if (tvInitial != null) {
                String initial = friend.getDisplayName() != null && !friend.getDisplayName().isEmpty() 
                    ? friend.getDisplayName().substring(0, 1).toUpperCase() 
                    : "?";
                tvInitial.setText(initial);
            }
            
            if (tvDisplayName != null) {
                tvDisplayName.setText(friend.getDisplayName());
            }
            
            if (tvUsername != null) {
                tvUsername.setText("@" + friend.getUsername());
            }
            
            if (onlineIndicator != null) {
                onlineIndicator.setVisibility(friend.isOnline() ? View.VISIBLE : View.GONE);
            }
            
            if (btnAction != null) {
                btnAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onChallengeClick(friend);
                    }
                });
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFriendClick(friend);
                }
            });
        }
    }
}
