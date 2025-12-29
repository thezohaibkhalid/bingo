package com.example.bingoarena.models;

public class LeaderboardEntry {
    private int rank;
    private String displayName;
    private String username;
    private int wins;

    public LeaderboardEntry(int rank, String displayName, String username, int wins) {
        this.rank = rank;
        this.displayName = displayName;
        this.username = username;
        this.wins = wins;
    }

    public int getRank() { return rank; }
    public String getDisplayName() { return displayName; }
    public String getUsername() { return username; }
    public int getWins() { return wins; }
}
