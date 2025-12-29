package com.example.bingoarena.network;

import com.example.bingoarena.models.ApiModels.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface ApiService {

    // ==================== AUTH ====================
    
    @POST("auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest body);

    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest body);

    @POST("auth/verify-otp")
    Call<ApiResponse<VerifyOtpResponse>> verifyOtp(@Body VerifyOtpRequest body);

    @GET("auth/me")
    Call<ApiResponse<User>> getMe();

    // ==================== USERS ====================
    
    @GET("users/search")
    Call<ApiResponse<List<UserSearchResult>>> searchUsers(@Query("query") String query);

    @GET("users/me")
    Call<ApiResponse<User>> getMyProfile();

    @GET("users/username-available")
    Call<ApiResponse<UsernameAvailability>> checkUsername(@Query("username") String username);

    // ==================== FRIENDS ====================
    
    @POST("friends/requests")
    Call<ApiResponse<FriendRequestResponse>> sendFriendRequest(@Body SendFriendRequestBody body);

    @GET("friends/requests")
    Call<ApiResponse<FriendRequestsResponse>> getFriendRequests();

    @POST("friends/requests/{id}/accept")
    Call<ApiResponse<SuccessResponse>> acceptFriendRequest(@Path("id") String requestId);

    @POST("friends/requests/{id}/reject")
    Call<ApiResponse<SuccessResponse>> rejectFriendRequest(@Path("id") String requestId);

    @GET("friends")
    Call<ApiResponse<List<FriendData>>> getFriends();

    // ==================== MATCHES ====================
    
    @POST("matches/invite")
    Call<ApiResponse<MatchData>> inviteToMatch(@Body InviteMatchBody body);

    @GET("matches")
    Call<ApiResponse<List<MatchData>>> getMatches();

    @GET("matches/active-with/{friendId}")
    Call<ApiResponse<ActiveMatchResponse>> getActiveMatchWith(@Path("friendId") String friendId);

    @GET("matches/{matchId}")
    Call<ApiResponse<MatchDetails>> getMatch(@Path("matchId") String matchId);

    @POST("matches/{matchId}/accept")
    Call<ApiResponse<MatchData>> acceptMatchInvite(@Path("matchId") String matchId);

    @POST("matches/{matchId}/board")
    Call<ApiResponse<BoardSetResponse>> setBoard(@Path("matchId") String matchId, @Body SetBoardBody body);

    @GET("matches/{matchId}/state")
    Call<ApiResponse<MatchState>> getMatchState(@Path("matchId") String matchId);

    @POST("matches/{matchId}/move")
    Call<ApiResponse<MoveResponse>> makeMove(@Path("matchId") String matchId, @Body MakeMoveBody body);

    @POST("matches/{matchId}/bingo")
    Call<ApiResponse<BingoResponse>> claimBingo(@Path("matchId") String matchId);

    // ==================== LEADERBOARD ====================
    @GET("leaderboard/global")
    Call<ApiResponse<LeaderboardResponse>> getGlobalLeaderboard();

    @GET("leaderboard/friends")
    Call<ApiResponse<LeaderboardResponse>> getFriendsLeaderboard();

}
