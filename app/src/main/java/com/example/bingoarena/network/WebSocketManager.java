package com.example.bingoarena.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.bingoarena.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;
    
    private OkHttpClient client;
    private WebSocket webSocket;
    private boolean isConnected = false;
    private String currentToken;
    private List<WebSocketEventListener> listeners = new ArrayList<>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Handler reconnectHandler = new Handler(Looper.getMainLooper());
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY_MS = 3000;

    public interface WebSocketEventListener {
        void onFriendRequest(String requestId, String fromUserId);
        void onMatchInvite(String matchId, String fromUserId);
        void onMatchAccepted(String matchId, String byUserId);
        void onBoardSetupComplete(String matchId, String startingUserId);
        void onOpponentMove(String matchId, int moveNumber, int number, String fromUserId);
        void onYourTurn(String matchId);
        void onMatchFinished(String matchId, String winnerUserId);
        void onConnectionChanged(boolean connected);
    }

    private WebSocketManager() {
        client = new OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void connect(String token) {
        if (isConnected && token.equals(currentToken)) {
            Log.d(TAG, "Already connected with same token");
            return;
        }

        disconnect();
        currentToken = token;
        reconnectAttempts = 0;

        String wsUrl = Constants.WS_URL + "?token=" + token;
        Log.d(TAG, "Connecting to WebSocket: " + wsUrl);

        Request request = new Request.Builder()
            .url(wsUrl)
            .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "WebSocket connected");
                isConnected = true;
                reconnectAttempts = 0;
                notifyConnectionChanged(true);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "WebSocket message: " + text);
                handleMessage(text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closing: " + code + " " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + code + " " + reason);
                isConnected = false;
                notifyConnectionChanged(false);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e(TAG, "WebSocket error: " + t.getMessage(), t);
                isConnected = false;
                notifyConnectionChanged(false);
                attemptReconnect();
            }
        });
    }

    private void attemptReconnect() {
        if (currentToken == null || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.d(TAG, "Max reconnect attempts reached or no token");
            return;
        }

        reconnectAttempts++;
        Log.d(TAG, "Attempting reconnect " + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS);

        reconnectHandler.postDelayed(() -> {
            if (!isConnected && currentToken != null) {
                connect(currentToken);
            }
        }, RECONNECT_DELAY_MS * reconnectAttempts);
    }

    public void disconnect() {
        reconnectHandler.removeCallbacksAndMessages(null);
        if (webSocket != null) {
            webSocket.close(1000, "Disconnect requested");
            webSocket = null;
        }
        isConnected = false;
        currentToken = null;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void addEventListener(WebSocketEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeEventListener(WebSocketEventListener listener) {
        listeners.remove(listener);
    }

    private void handleMessage(String text) {
        try {
            JSONObject json = new JSONObject(text);
            String event = json.optString("event", "");
            JSONObject payload = json.optJSONObject("payload");
            
            if (payload == null) {
                Log.w(TAG, "No payload in message");
                return;
            }

            mainHandler.post(() -> {
                switch (event) {
                    case Constants.WS_FRIEND_REQUEST:
                        String requestId = payload.optString("requestId");
                        String fromUserId = payload.optString("fromUserId");
                        for (WebSocketEventListener listener : listeners) {
                            listener.onFriendRequest(requestId, fromUserId);
                        }
                        break;

                    case Constants.WS_MATCH_INVITE:
                        String matchId = payload.optString("matchId");
                        String inviteFromUserId = payload.optString("fromUserId");
                        for (WebSocketEventListener listener : listeners) {
                            listener.onMatchInvite(matchId, inviteFromUserId);
                        }
                        break;

                    case Constants.WS_MATCH_ACCEPTED:
                        String acceptedMatchId = payload.optString("matchId");
                        String byUserId = payload.optString("byUserId");
                        for (WebSocketEventListener listener : listeners) {
                            listener.onMatchAccepted(acceptedMatchId, byUserId);
                        }
                        break;

                    case Constants.WS_BOARD_SETUP_COMPLETE:
                        String setupMatchId = payload.optString("matchId");
                        String startingUserId = payload.optString("startingUserId");
                        for (WebSocketEventListener listener : listeners) {
                            listener.onBoardSetupComplete(setupMatchId, startingUserId);
                        }
                        break;

                    case Constants.WS_OPPONENT_MOVE:
                        String moveMatchId = payload.optString("matchId");
                        int moveNumber = payload.optInt("move_number");
                        int number = payload.optInt("number");
                        String moveFromUserId = payload.optString("fromUserId");
                        for (WebSocketEventListener listener : listeners) {
                            listener.onOpponentMove(moveMatchId, moveNumber, number, moveFromUserId);
                        }
                        break;

                    case Constants.WS_YOUR_TURN:
                        String turnMatchId = payload.optString("matchId");
                        for (WebSocketEventListener listener : listeners) {
                            listener.onYourTurn(turnMatchId);
                        }
                        break;

                    case Constants.WS_MATCH_FINISHED:
                        String finishedMatchId = payload.optString("matchId");
                        String winnerUserId = payload.optString("winnerUserId");
                        for (WebSocketEventListener listener : listeners) {
                            listener.onMatchFinished(finishedMatchId, winnerUserId);
                        }
                        break;

                    default:
                        Log.w(TAG, "Unknown event: " + event);
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing message: " + e.getMessage());
        }
    }

    private void notifyConnectionChanged(boolean connected) {
        mainHandler.post(() -> {
            for (WebSocketEventListener listener : listeners) {
                listener.onConnectionChanged(connected);
            }
        });
    }
}
