package com.example.bingoarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bingoarena.R;
import com.example.bingoarena.models.LeaderboardEntry;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardEntry> entries;

    public LeaderboardAdapter(List<LeaderboardEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);
        holder.bind(entry, position + 1);
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    public void updateData(List<LeaderboardEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRank, tvInitial, tvDisplayName, tvUsername, tvWins;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvWins = itemView.findViewById(R.id.tvWins);
        }

        void bind(LeaderboardEntry entry, int rank) {
            if (tvRank != null) {
                tvRank.setText(String.valueOf(rank));
            }
            
            if (tvInitial != null) {
                String initial = entry.getDisplayName() != null && !entry.getDisplayName().isEmpty()
                    ? entry.getDisplayName().substring(0, 1).toUpperCase()
                    : "?";
                tvInitial.setText(initial);
            }
            
            if (tvDisplayName != null) {
                tvDisplayName.setText(entry.getDisplayName());
            }
            
            if (tvUsername != null) {
                tvUsername.setText("@" + entry.getUsername());
            }
            
            if (tvWins != null) {
                tvWins.setText(entry.getWins() + " wins");
            }
        }
    }
}
