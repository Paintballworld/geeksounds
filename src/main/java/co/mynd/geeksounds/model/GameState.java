package co.mynd.geeksounds.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public enum State {
        WAITING,      // Waiting for game to start
        PLAYING,      // Sound is playing
        GUESSING,     // Waiting for player selection
        BONUS_ROUND,  // Bonus round active
        FINISHED      // Game finished
    }

    private State state;
    private List<Player> players;
    private String currentSound;
    private List<String> playedSounds;
    private List<String> availableSounds;
    private boolean isBonusRound;

    public GameState() {
        this.state = State.WAITING;
        this.players = new ArrayList<>();
        this.playedSounds = new ArrayList<>();
        this.availableSounds = new ArrayList<>();
        this.isBonusRound = false;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getCurrentSound() {
        return currentSound;
    }

    public void setCurrentSound(String currentSound) {
        this.currentSound = currentSound;
    }

    public List<String> getPlayedSounds() {
        return playedSounds;
    }

    public void setPlayedSounds(List<String> playedSounds) {
        this.playedSounds = playedSounds;
    }

    public List<String> getAvailableSounds() {
        return availableSounds;
    }

    public void setAvailableSounds(List<String> availableSounds) {
        this.availableSounds = availableSounds;
    }

    public boolean isBonusRound() {
        return isBonusRound;
    }

    public void setBonusRound(boolean bonusRound) {
        isBonusRound = bonusRound;
    }
}
