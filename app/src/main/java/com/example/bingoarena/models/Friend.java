package com.example.bingoarena.models;

public class Friend {
    private String id;
    private String displayName;
    private String username;
    private boolean isOnline;
    private String avatarUrl;
    private String lastOnlineAt;

    public Friend(String id, String displayName, String username, boolean isOnline) {
        this.id = id;
        this.displayName = displayName;
        this.username = username;
        this.isOnline = isOnline;
    }

    public Friend(String id, String displayName, String username, String avatarUrl, String lastOnlineAt) {
        this.id = id;
        this.displayName = displayName;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.lastOnlineAt = lastOnlineAt;
        // Consider online if last online within 5 minutes
        this.isOnline = lastOnlineAt != null && !lastOnlineAt.isEmpty();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getUsername() { return username; }
    public boolean isOnline() { return isOnline; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getLastOnlineAt() { return lastOnlineAt; }
}
