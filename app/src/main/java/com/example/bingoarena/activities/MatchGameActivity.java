package com.example.bingoarena.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bingoarena.R;
import com.example.bingoarena.models.ApiModels.*;
import com.example.bingoarena.network.ApiService;
import com.example.bingoarena.network.RetrofitClient;
import com.example.bingoarena.network.WebSocketManager;
import com.example.bingoarena.utils.Constants;
import com.example.bingoarena.utils.SharedPrefsManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatchGameActivity extends BaseActivity implements WebSocketManager.WebSocketEventListener {
    private static final String TAG = "MatchGameActivity";

    private LinearLayout boardSetupContainer, gameBoardContainer, waitingContainer, resultContainer;
    private GridLayout gridBoardSetup, gridGameBoard;
    private Button btnShuffle, btnConfirmBoard, btnClaimBingo, btnPlayAgain, btnBackToDashboard;
    private TextView tvSetupOpponent, tvOpponentName, tvTurnIndicator, tvTimeLeft, tvMoves;
    private TextView tvWaitingMessage, tvResultTitle, tvResultMessage, tvYourLines, tvOpponentLines;
    private ImageView ivResultIcon;
    private FrameLayout loadingOverlay;

    private String matchId;
    private String myUserId;
    private String opponentName = "Opponent";
    private List<Integer> board = new ArrayList<>();
    private Set<Integer> markedNumbers = new HashSet<>();
    private int selectedCell = -1;
    private boolean isMyTurn = false;
    private CountDownTimer turnTimer;
    
    private ApiService apiService;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_game);
        
        // Apply window insets for edge-to-edge
        applyWindowInsets(findViewById(android.R.id.content));
        
        matchId = getIntent().getStringExtra("matchId");
        apiService = RetrofitClient.getInstance().getApiService();
        prefsManager = new SharedPrefsManager(this);
        myUserId = prefsManager.getUserId();
        
        initViews();
        setupClickListeners();
        WebSocketManager.getInstance().addEventListener(this);
        loadMatchState();
    }

    private void initViews() {
        boardSetupContainer = findViewById(R.id.boardSetupContainer);
        gridBoardSetup = findViewById(R.id.gridBoardSetup);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnConfirmBoard = findViewById(R.id.btnConfirmBoard);
        tvSetupOpponent = findViewById(R.id.tvSetupOpponent);
        gameBoardContainer = findViewById(R.id.gameBoardContainer);
        gridGameBoard = findViewById(R.id.gridGameBoard);
        tvOpponentName = findViewById(R.id.tvOpponentName);
        tvTurnIndicator = findViewById(R.id.tvTurnIndicator);
        tvTimeLeft = findViewById(R.id.tvTimeLeft);
        tvMoves = findViewById(R.id.tvMoves);
        btnClaimBingo = findViewById(R.id.btnClaimBingo);
        waitingContainer = findViewById(R.id.waitingContainer);
        tvWaitingMessage = findViewById(R.id.tvWaitingMessage);
        resultContainer = findViewById(R.id.resultContainer);
        ivResultIcon = findViewById(R.id.ivResultIcon);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        tvYourLines = findViewById(R.id.tvYourLines);
        tvOpponentLines = findViewById(R.id.tvOpponentLines);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupClickListeners() {
        btnShuffle.setOnClickListener(v -> shuffleBoard());
        btnConfirmBoard.setOnClickListener(v -> confirmBoard());
        btnClaimBingo.setOnClickListener(v -> claimBingo());
        btnPlayAgain.setOnClickListener(v -> finish());
        btnBackToDashboard.setOnClickListener(v -> { startActivity(new Intent(this, DashboardActivity.class)); finish(); });
    }

    private void loadMatchState() {
        loadingOverlay.setVisibility(View.VISIBLE);
        
        apiService.getMatchState(matchId).enqueue(new Callback<ApiResponse<MatchState>>() {
            @Override
            public void onResponse(Call<ApiResponse<MatchState>> call, Response<ApiResponse<MatchState>> response) {
                loadingOverlay.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    MatchState state = response.body().data;
                    handleMatchState(state);
                } else {
                    // Match might be in INVITED state - get match details
                    loadMatchDetails();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MatchState>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
                loadMatchDetails();
            }
        });
    }

    private void loadMatchDetails() {
        apiService.getMatch(matchId).enqueue(new Callback<ApiResponse<MatchDetails>>() {
            @Override
            public void onResponse(Call<ApiResponse<MatchDetails>> call, Response<ApiResponse<MatchDetails>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    MatchDetails details = response.body().data;
                    opponentName = details.player1.id.equals(myUserId) ? details.player2.displayName : details.player1.displayName;
                    
                    switch (details.status) {
                        case "INVITED":
                            if (details.player2.id.equals(myUserId)) acceptMatch();
                            else showWaiting();
                            break;
                        case "BOARD_SETUP":
                            showBoardSetup();
                            break;
                        case "IN_PROGRESS":
                            loadMatchState();
                            break;
                        case "FINISHED":
                            showResult(myUserId.equals(details.winnerUserId), 0, 0);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MatchDetails>> call, Throwable t) {
                Toast.makeText(MatchGameActivity.this, "Error loading match", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptMatch() {
        loadingOverlay.setVisibility(View.VISIBLE);
        apiService.acceptMatchInvite(matchId).enqueue(new Callback<ApiResponse<MatchData>>() {
            @Override
            public void onResponse(Call<ApiResponse<MatchData>> call, Response<ApiResponse<MatchData>> response) {
                loadingOverlay.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    showBoardSetup();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MatchData>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
            }
        });
    }

    private void handleMatchState(MatchState state) {
        for (PlayerInfo p : state.players) {
            if (!p.id.equals(myUserId)) opponentName = p.displayName;
        }
        
        markedNumbers.clear();
        for (MoveInfo move : state.moves) {
            markedNumbers.add(move.number);
        }

        if (state.yourBoard != null) {
            board = new ArrayList<>(state.yourBoard.numbers);
        }

        isMyTurn = myUserId.equals(state.match.currentTurnUserId);

        switch (state.match.status) {
            case "BOARD_SETUP":
                if (state.yourBoard == null) showBoardSetup();
                else showWaiting();
                break;
            case "IN_PROGRESS":
                showGameBoard();
                break;
            case "FINISHED":
                showResult(myUserId.equals(state.match.winnerUserId), state.yourLines, state.opponentLines);
                break;
        }
    }

    private void showBoardSetup() {
        hideAllContainers();
        boardSetupContainer.setVisibility(View.VISIBLE);
        tvSetupOpponent.setText(getString(R.string.playing_against, opponentName));
        generateRandomBoard();
        renderSetupBoard();
    }

    private void generateRandomBoard() {
        board.clear();
        for (int i = 1; i <= 25; i++) board.add(i);
        Collections.shuffle(board);
    }

    private void shuffleBoard() {
        Collections.shuffle(board);
        selectedCell = -1;
        renderSetupBoard();
    }

    private void renderSetupBoard() {
        gridBoardSetup.removeAllViews();
        for (int i = 0; i < 25; i++) {
            final int index = i;
            View cell = getLayoutInflater().inflate(R.layout.item_game_cell, gridBoardSetup, false);
            TextView tvNumber = cell.findViewById(R.id.tvNumber);
            tvNumber.setText(String.valueOf(board.get(i)));
            cell.setBackgroundResource(index == selectedCell ? R.drawable.bg_game_cell_selected : R.drawable.bg_game_cell);
            cell.setOnClickListener(v -> {
                if (selectedCell == -1) selectedCell = index;
                else if (selectedCell == index) selectedCell = -1;
                else { int temp = board.get(selectedCell); board.set(selectedCell, board.get(index)); board.set(index, temp); selectedCell = -1; }
                renderSetupBoard();
            });
            gridBoardSetup.addView(cell);
        }
    }

    private void confirmBoard() {
        loadingOverlay.setVisibility(View.VISIBLE);
        SetBoardBody body = new SetBoardBody(board);
        
        apiService.setBoard(matchId, body).enqueue(new Callback<ApiResponse<BoardSetResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BoardSetResponse>> call, Response<ApiResponse<BoardSetResponse>> response) {
                loadingOverlay.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    BoardSetResponse data = response.body().data;
                    if ("IN_PROGRESS".equals(data.match.status)) {
                        isMyTurn = myUserId.equals(data.match.currentTurnUserId);
                        showGameBoard();
                    } else {
                        showWaiting();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BoardSetResponse>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(MatchGameActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showWaiting() {
        hideAllContainers();
        waitingContainer.setVisibility(View.VISIBLE);
        tvWaitingMessage.setText(getString(R.string.your_board_is_set, opponentName));
    }

    private void showGameBoard() {
        hideAllContainers();
        gameBoardContainer.setVisibility(View.VISIBLE);
        tvOpponentName.setText(opponentName);
        updateTurnIndicator();
        renderGameBoard();
        if (isMyTurn) startTurnTimer();
    }

    private void updateTurnIndicator() {
        tvTurnIndicator.setText(isMyTurn ? R.string.your_turn : R.string.their_turn);
        tvTurnIndicator.setTextColor(getColor(isMyTurn ? R.color.primary : R.color.muted_foreground));
        tvMoves.setText(getString(R.string.moves, markedNumbers.size()));
    }

    private void renderGameBoard() {
        gridGameBoard.removeAllViews();
        for (int i = 0; i < 25; i++) {
            final int number = board.get(i);
            View cell = getLayoutInflater().inflate(R.layout.item_game_cell, gridGameBoard, false);
            TextView tvNumber = cell.findViewById(R.id.tvNumber);
            tvNumber.setText(String.valueOf(number));
            boolean marked = markedNumbers.contains(number);
            cell.setBackgroundResource(marked ? R.drawable.bg_game_cell_marked : R.drawable.bg_game_cell);
            tvNumber.setTextColor(getColor(marked ? R.color.primary_foreground : R.color.foreground));
            cell.setOnClickListener(v -> { if (isMyTurn && !markedNumbers.contains(number)) makeMove(number); });
            gridGameBoard.addView(cell);
        }
    }

    private void startTurnTimer() {
        if (turnTimer != null) turnTimer.cancel();
        turnTimer = new CountDownTimer(Constants.TURN_TIME_LIMIT * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimeLeft.setText(getString(R.string.time_left, seconds));
                tvTimeLeft.setTextColor(getColor(seconds <= 5 ? R.color.destructive : R.color.warning));
            }

            @Override
            public void onFinish() {
                List<Integer> available = new ArrayList<>();
                for (int num : board) if (!markedNumbers.contains(num)) available.add(num);
                if (!available.isEmpty()) makeMove(available.get((int) (Math.random() * available.size())));
            }
        }.start();
    }

    private void makeMove(int number) {
        if (turnTimer != null) turnTimer.cancel();
        loadingOverlay.setVisibility(View.VISIBLE);
        
        apiService.makeMove(matchId, new MakeMoveBody(number)).enqueue(new Callback<ApiResponse<MoveResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MoveResponse>> call, Response<ApiResponse<MoveResponse>> response) {
                loadingOverlay.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    markedNumbers.add(number);
                    isMyTurn = myUserId.equals(response.body().data.nextTurnUserId);
                    updateTurnIndicator();
                    renderGameBoard();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MoveResponse>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
            }
        });
    }

    private void claimBingo() {
        loadingOverlay.setVisibility(View.VISIBLE);
        apiService.claimBingo(matchId).enqueue(new Callback<ApiResponse<BingoResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BingoResponse>> call, Response<ApiResponse<BingoResponse>> response) {
                loadingOverlay.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    BingoResponse data = response.body().data;
                    if (data.isWinner) showResult(true, data.lines != null ? data.lines : 5, 0);
                    else Toast.makeText(MatchGameActivity.this, data.reason != null ? data.reason : "Not enough lines", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BingoResponse>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
            }
        });
    }

    private void showResult(boolean isWinner, int yourLines, int oppLines) {
        hideAllContainers();
        resultContainer.setVisibility(View.VISIBLE);
        if (isWinner) {
            ivResultIcon.setImageResource(R.drawable.ic_trophy);
            ivResultIcon.setColorFilter(getColor(R.color.primary));
            resultContainer.setBackgroundResource(R.drawable.bg_victory);
            tvResultTitle.setText(R.string.victory);
            tvResultMessage.setText(R.string.congratulations);
        } else {
            ivResultIcon.setImageResource(R.drawable.ic_x_circle);
            ivResultIcon.setColorFilter(getColor(R.color.destructive));
            resultContainer.setBackgroundResource(R.drawable.bg_defeat);
            tvResultTitle.setText(R.string.defeat);
            tvResultMessage.setText(getString(R.string.opponent_won, opponentName));
        }
        tvYourLines.setText(String.valueOf(yourLines));
        tvOpponentLines.setText(String.valueOf(oppLines));
    }

    private void hideAllContainers() {
        boardSetupContainer.setVisibility(View.GONE);
        gameBoardContainer.setVisibility(View.GONE);
        waitingContainer.setVisibility(View.GONE);
        resultContainer.setVisibility(View.GONE);
    }

    // WebSocket Events
    @Override public void onFriendRequest(String requestId, String fromUserId) {}
    @Override public void onMatchInvite(String matchId, String fromUserId) {}
    @Override public void onMatchAccepted(String matchId, String byUserId) { if (matchId.equals(this.matchId)) runOnUiThread(this::loadMatchState); }
    @Override public void onBoardSetupComplete(String matchId, String startingUserId) { if (matchId.equals(this.matchId)) runOnUiThread(() -> { isMyTurn = myUserId.equals(startingUserId); showGameBoard(); }); }
    @Override public void onOpponentMove(String matchId, int moveNumber, int number, String fromUserId) { if (matchId.equals(this.matchId)) runOnUiThread(() -> { markedNumbers.add(number); isMyTurn = true; updateTurnIndicator(); renderGameBoard(); startTurnTimer(); }); }
    @Override public void onYourTurn(String matchId) { if (matchId.equals(this.matchId)) runOnUiThread(() -> { isMyTurn = true; updateTurnIndicator(); startTurnTimer(); }); }
    @Override public void onMatchFinished(String matchId, String winnerUserId) { if (matchId.equals(this.matchId)) runOnUiThread(() -> showResult(myUserId.equals(winnerUserId), 5, 0)); }
    @Override public void onConnectionChanged(boolean connected) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (turnTimer != null) turnTimer.cancel();
        WebSocketManager.getInstance().removeEventListener(this);
    }
}
