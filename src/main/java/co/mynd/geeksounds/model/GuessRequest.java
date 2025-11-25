package co.mynd.geeksounds.model;

public class GuessRequest {
    private String playerName;

    public GuessRequest() {
    }

    public GuessRequest(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
