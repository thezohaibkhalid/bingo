package com.example.bingoarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bingoarena.R;
import com.example.bingoarena.models.Match;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.ViewHolder> {

    private List<Match> matches;
    private OnMatchClickListener listener;
    private String currentUserId;

    public interface OnMatchClickListener {
        void onMatchClick(Match match);
        void onAcceptMatch(Match match);
    }

    public MatchesAdapter(List<Match> matches, OnMatchClickListener listener, String currentUserId) {
        this.matches = matches;
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(matches.get(position));
    }

    @Override
    public int getItemCount() {
        return matches == null ? 0 : matches.size();
    }

    public void updateData(List<Match> newMatches) {
        this.matches = newMatches;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvInitial, tvOpponentName, tvMatchStatus;
        private ImageView ivStatusIcon;
        private Button btnAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvOpponentName = itemView.findViewById(R.id.tvOpponentName);
            tvMatchStatus = itemView.findViewById(R.id.tvMatchStatus);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        void bind(Match match) {
            String opponentName = match.getOpponentName();
            if (opponentName == null || opponentName.isEmpty()) {
                opponentName = "Unknown";
            }

            tvInitial.setText(opponentName.substring(0, 1).toUpperCase());
            tvOpponentName.setText(opponentName);

            String status = match.getStatus();
            boolean isInvitee = currentUserId != null &&
                    currentUserId.equals(match.getPlayer2Id());

            switch (status) {
                case "INVITED":
                    tvMatchStatus.setText(isInvitee ? "Invitation received" : "Invitation sent");
                    ivStatusIcon.setImageResource(R.drawable.ic_clock);
                    break;

                case "BOARD_SETUP":
                    tvMatchStatus.setText("Setting up board");
                    ivStatusIcon.setImageResource(R.drawable.ic_grid);
                    break;

                case "IN_PROGRESS":
                    boolean yourTurn = currentUserId != null &&
                            currentUserId.equals(match.getCurrentTurnUserId());
                    tvMatchStatus.setText(yourTurn ? "Your turn" : "Opponent's turn");
                    ivStatusIcon.setImageResource(R.drawable.ic_swords);
                    break;

                case "FINISHED":
                    boolean winner = currentUserId != null &&
                            currentUserId.equals(match.getWinnerUserId());
                    tvMatchStatus.setText(winner ? "Victory!" : "Defeat");
                    ivStatusIcon.setImageResource(R.drawable.ic_trophy);
                    break;

                default:
                    tvMatchStatus.setText(status);
                    ivStatusIcon.setImageResource(R.drawable.ic_gamepad);
            }

            btnAction.setVisibility(View.VISIBLE);
            btnAction.setEnabled(true);

            switch (status) {
                case "INVITED":
                    if (isInvitee) {
                        btnAction.setText("Accept");
                        btnAction.setOnClickListener(v -> listener.onAcceptMatch(match));
                    } else {
                        btnAction.setText("Waiting...");
                        btnAction.setEnabled(false);
                    }
                    break;

                case "BOARD_SETUP":
                case "IN_PROGRESS":
                case "FINISHED":
                    btnAction.setText("Open");
                    btnAction.setOnClickListener(v -> listener.onMatchClick(match));
                    break;

                default:
                    btnAction.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onMatchClick(match));
        }
    }
}
