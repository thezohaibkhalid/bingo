package com.example.bingoarena.models;

public class FriendRequest {
    private String id;
    private String userId;
    private String displayName;
    private String username;
    private boolean isIncoming;
    private String createdAt;

    public FriendRequest(String id, String userId, String displayName, String username, boolean isIncoming) {
        this.id = id;
        this.userId = userId;
        this.displayName = displayName;
        this.username = username;
        this.isIncoming = isIncoming;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getUsername() { return username; }
    public boolean isIncoming() { return isIncoming; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getInitial() {
        if (displayName != null && !displayName.isEmpty()) return displayName.substring(0, 1).toUpperCase();
        if (username != null && !username.isEmpty()) return username.substring(0, 1).toUpperCase();
        return "?";
    }
}
