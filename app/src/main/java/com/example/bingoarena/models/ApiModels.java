package com.example.bingoarena.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// ==================== GENERIC RESPONSE ====================

public class ApiModels {

    public static class ApiResponse<T> {
        public boolean success;
        public String message;
        public T data;

        @SerializedName("statusCode")
        public int statusCode;

        public boolean isSuccess() {
            return success;
        }

        public T getData() {
            return data;
        }
    }


    // ==================== AUTH MODELS ====================

    public static class RegisterRequest {
        public String email;
        public String password;
        public String username;
        public String displayName;

        public RegisterRequest(String email, String password, String username, String displayName) {
            this.email = email;
            this.password = password;
            this.username = username;
            this.displayName = displayName;
        }
    }

    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class LoginResponse {
        public String otpId;
    }

    public static class VerifyOtpRequest {
        public String otpId;
        public String code;

        public VerifyOtpRequest(String otpId, String code) {
            this.otpId = otpId;
            this.code = code;
        }
    }

    public static class VerifyOtpResponse {
        public String token;
        public User user;
    }

    // ==================== USER MODELS ====================

    public static class User {
        public String id;
        public String email;
        public String username;
        @SerializedName("display_name")
        public String displayName;
        @SerializedName("avatar_url")
        public String avatarUrl;
        @SerializedName("created_at")
        public String createdAt;
    }

    public static class UserSearchResult {
        public String id;
        public String username;
        @SerializedName("display_name")
        public String displayName;
        @SerializedName("avatar_url")
        public String avatarUrl;
    }

    public static class UsernameAvailability {
        public String username;
        public boolean available;
    }

    // ==================== FRIEND MODELS ====================

    public static class SendFriendRequestBody {
        @SerializedName("addressee_id")
        public String addresseeId;

        public SendFriendRequestBody(String addresseeId) {
            this.addresseeId = addresseeId;
        }
    }

    public static class FriendRequestResponse {
        public String id;
        public String requesterId;
        public String addresseeId;
        public String status;
        public String createdAt;
    }

    public static class FriendRequestsResponse {
        public List<IncomingRequest> incoming;
        public List<OutgoingRequest> outgoing;

        public List<IncomingRequest> getIncoming() {
            return incoming;
        }

        public List<OutgoingRequest> getOutgoing() {
            return outgoing;
        }
    }

    public static class IncomingRequest {
        public String id;

        @SerializedName("requester_id")
        public String requesterId;

        @SerializedName("requester_name")
        public String requesterName;

        @SerializedName("requester_username")
        public String requesterUsername;

        @SerializedName("created_at")
        public String createdAt;

        public String getId() {
            return id;
        }

        public String getRequesterId() {
            return requesterId;
        }

        public String getRequesterName() {
            return requesterName;
        }

        public String getRequesterUsername() {
            return requesterUsername;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public static class OutgoingRequest {
        public String id;

        @SerializedName("addressee_id")
        public String addresseeId;

        @SerializedName("addressee_name")
        public String addresseeName;

        @SerializedName("addressee_username")
        public String addresseeUsername;

        @SerializedName("created_at")
        public String createdAt;

        public String getId() {
            return id;
        }

        public String getAddresseeId() {
            return addresseeId;
        }

        public String getAddresseeName() {
            return addresseeName;
        }

        public String getAddresseeUsername() {
            return addresseeUsername;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }


    public static class FriendData {
        public String id;
        @SerializedName("display_name")
        public String displayName;
        @SerializedName("avatar_url")
        public String avatarUrl;
        @SerializedName("last_online_at")
        public String lastOnlineAt;
    }

    // ==================== MATCH MODELS ====================

    public static class InviteMatchBody {
        @SerializedName("friend_id")
        public String friendId;

        public InviteMatchBody(String friendId) {
            this.friendId = friendId;
        }
    }

    public static class MatchData {
        public String id;
        public String player1Id;
        public String player2Id;
        public String status;
        public String currentTurnUserId;
        public String winnerUserId;
        public String createdAt;
        public String startedAt;
        public String endedAt;
    }

    public static class ActiveMatchResponse {
        public boolean hasActiveMatch;
        public String matchId;
    }

    public static class MatchDetails {
        public String id;
        public String status;
        @SerializedName("current_turn_user_id")
        public String currentTurnUserId;
        @SerializedName("winner_user_id")
        public String winnerUserId;
        public PlayerInfo player1;
        public PlayerInfo player2;
        @SerializedName("created_at")
        public String createdAt;
        @SerializedName("started_at")
        public String startedAt;
        @SerializedName("ended_at")
        public String endedAt;
    }

    public static class PlayerInfo {
        public String id;
        @SerializedName("display_name")
        public String displayName;
    }

    public static class SetBoardBody {
        public String mode;
        public List<Integer> numbers;

        public SetBoardBody(List<Integer> numbers) {
            this.numbers = numbers;
        }

        public SetBoardBody(String mode) {
            this.mode = mode;
        }
    }

    public static class BoardSetResponse {
        public BoardInfo board;
        public MatchInfo match;
    }

    public static class BoardInfo {
        public String id;
        public String matchId;
        public String userId;
        public List<Integer> numbers;
        public String createdAt;
    }

    public static class MatchInfo {
        public String id;
        public String status;
        public String currentTurnUserId;
        public String startedAt;
    }

    public static class MatchState {
        public MatchStateInfo match;
        public List<PlayerInfo> players;
        @SerializedName("your_board")
        public BoardData yourBoard;
        public List<MoveInfo> moves;
        @SerializedName("your_lines")
        public int yourLines;
        @SerializedName("opponent_lines")
        public int opponentLines;
    }

    public static class MatchStateInfo {
        public String id;
        public String status;
        @SerializedName("current_turn_user_id")
        public String currentTurnUserId;
        @SerializedName("winner_user_id")
        public String winnerUserId;
    }

    public static class BoardData {
        public List<Integer> numbers;
        public int size;
    }

    public static class MoveInfo {
        @SerializedName("move_number")
        public int moveNumber;
        public int number;
        @SerializedName("chosen_by_user_id")
        public String chosenByUserId;
    }

    public static class MakeMoveBody {
        public int number;

        public MakeMoveBody(int number) {
            this.number = number;
        }
    }

    public static class MoveResponse {
        public boolean success;
        public MoveInfo move;
        @SerializedName("your_lines")
        public int yourLines;
        @SerializedName("opponent_lines")
        public int opponentLines;
        @SerializedName("next_turn_user_id")
        public String nextTurnUserId;
    }

    public static class BingoResponse {
        public boolean success;
        public Integer lines;
        @SerializedName("is_winner")
        public boolean isWinner;
        public String reason;
    }

    // ==================== LEADERBOARD MODELS ====================

    public static class LeaderboardEntry {
        public int rank;
        @SerializedName("display_name")
        public String displayName;
        public String username;
        public int wins;
    }

    public static class LeaderboardResponse {
        public List<LeaderboardEntry> entries;
    }

    // ==================== GENERIC SUCCESS ====================

    public static class SuccessResponse {
        public boolean success;
    }
}
