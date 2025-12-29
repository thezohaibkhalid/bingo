package com.example.bingoarena.models;

public class User {
    private String id;
    private String email;
    private String username;
    private String displayName;
    private String avatarUrl;

    public User(String id, String email, String username, String displayName, String avatarUrl) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
}
