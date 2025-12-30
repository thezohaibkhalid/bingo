package com.example.bingoarena.utils;

public class Constants {
    // Base URLs - UPDATE THESE FOR YOUR BACKEND
    public static final String BASE_URL = "https://api.bingo.bitbuilders.tech/api/";
    public static final String WS_URL = "wss://api.bingo.bitbuilders.tech/ws";
    

    public static final String PREF_NAME = "BingoArenaPrefs";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_DISPLAY_NAME = "display_name";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    
    public static final int BOARD_SIZE = 5;
    public static final int TOTAL_NUMBERS = 25;
    public static final int TURN_TIME_LIMIT = 30;
    public static final int LINES_TO_WIN = 5;
    
    public static final String STATUS_INVITED = "INVITED";
    public static final String STATUS_BOARD_SETUP = "BOARD_SETUP";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_FINISHED = "FINISHED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    // WebSocket Events
    public static final String WS_FRIEND_REQUEST = "friend_request";
    public static final String WS_MATCH_INVITE = "match_invite";
    public static final String WS_MATCH_ACCEPTED = "match_accepted";
    public static final String WS_BOARD_SETUP_COMPLETE = "board_setup_complete";
    public static final String WS_OPPONENT_MOVE = "opponent_move";
    public static final String WS_YOUR_TURN = "your_turn";
    public static final String WS_MATCH_FINISHED = "match_finished";
}
