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

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private List<Friend> results;
    private OnAddFriendListener listener;

    public interface OnAddFriendListener {
        void onAddFriend(Friend user);
    }

    public SearchResultsAdapter(List<Friend> results, OnAddFriendListener listener) {
        this.results = results;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend user = results.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    public void updateData(List<Friend> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvInitial, tvDisplayName, tvUsername;
        private Button btnAddFriend;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }

        void bind(Friend user) {
            if (tvInitial != null) {
                String initial = user.getDisplayName() != null && !user.getDisplayName().isEmpty()
                    ? user.getDisplayName().substring(0, 1).toUpperCase()
                    : "?";
                tvInitial.setText(initial);
            }
            
            if (tvDisplayName != null) {
                tvDisplayName.setText(user.getDisplayName());
            }
            
            if (tvUsername != null) {
                tvUsername.setText("@" + user.getUsername());
            }
            
            if (btnAddFriend != null) {
                btnAddFriend.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddFriend(user);
                    }
                });
            }
        }
    }
}
