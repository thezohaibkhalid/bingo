package com.example.bingoarena.models;

public class Match {
    private String id;
    private String opponentId;
    private String opponentName;
    private String status;
    private boolean isMyTurn;
    private String player1Id;
    private String player2Id;
    private String currentTurnUserId;
    private String winnerUserId;

    public Match(String id, String opponentId, String opponentName, String status, boolean isMyTurn) {
        this.id = id;
        this.opponentId = opponentId;
        this.opponentName = opponentName;
        this.status = status;
        this.isMyTurn = isMyTurn;
    }

    public Match(String id, String player1Id, String player2Id, String opponentName, 
                 String status, String currentTurnUserId, String winnerUserId, String myUserId) {
        this.id = id;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.opponentId = player1Id.equals(myUserId) ? player2Id : player1Id;
        this.opponentName = opponentName;
        this.status = status;
        this.currentTurnUserId = currentTurnUserId;
        this.winnerUserId = winnerUserId;
        this.isMyTurn = myUserId.equals(currentTurnUserId);
    }

    public String getId() { return id; }
    public String getOpponentId() { return opponentId; }
    public String getOpponentName() { return opponentName; }
    public String getStatus() { return status; }
    public boolean isMyTurn() { return isMyTurn; }
    public String getPlayer1Id() { return player1Id; }
    public String getPlayer2Id() { return player2Id; }
    public String getCurrentTurnUserId() { return currentTurnUserId; }
    public String getWinnerUserId() { return winnerUserId; }
    
    public void setIsMyTurn(boolean isMyTurn) { this.isMyTurn = isMyTurn; }
    public void setStatus(String status) { this.status = status; }
}
