package co.mynd.geeksounds.service;

import co.mynd.geeksounds.model.GameState;
import co.mynd.geeksounds.model.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GameService {

    private final GameState gameState;
    private final ResourceLoader resourceLoader;

    @Value("${game.players}")
    private String playerNames;

    @Value("${game.sounds.path}")
    private String soundsPath;

    @Value("${game.sounds.bonus.path}")
    private String bonusSoundsPath;

    public GameService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.gameState = new GameState();
    }

    public void initializeGame() {
        List<Player> players = Arrays.stream(playerNames.split(","))
                .map(String::trim)
                .map(Player::new)
                .collect(Collectors.toList());

        gameState.setPlayers(players);
        gameState.setState(GameState.State.WAITING);
        gameState.setBonusRound(false);

        loadSounds(false);

        for (Player player : gameState.getPlayers()) {
            player.resetScore();
        }
    }

    public void startGame() {
        initializeGame();
        gameState.setState(GameState.State.WAITING);
    }

    public String playRandomSound() {
        if (gameState.getAvailableSounds().isEmpty()) {
            return null;
        }

        Random random = new Random();
        int index = random.nextInt(gameState.getAvailableSounds().size());
        String sound = gameState.getAvailableSounds().remove(index);

        gameState.setCurrentSound(sound);
        gameState.getPlayedSounds().add(sound);
        gameState.setState(GameState.State.PLAYING);

        return sound;
    }

    public void stopSound() {
        gameState.setState(GameState.State.GUESSING);
    }

    public void skipSound() {
        gameState.setCurrentSound(null);

        if (gameState.getAvailableSounds().isEmpty()) {
            checkGameEnd();
        } else {
            gameState.setState(GameState.State.WAITING);
        }
    }

    public void playerGuessed(String playerName) {
        Optional<Player> playerOpt = gameState.getPlayers().stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst();

        if (playerOpt.isPresent()) {
            playerOpt.get().incrementScore();
        }

        gameState.setCurrentSound(null);

        if (gameState.getAvailableSounds().isEmpty()) {
            checkGameEnd();
        } else {
            gameState.setState(GameState.State.WAITING);
        }
    }

    private void checkGameEnd() {
        List<Player> sortedPlayers = gameState.getPlayers().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()))
                .collect(Collectors.toList());

        if (sortedPlayers.isEmpty()) {
            gameState.setState(GameState.State.FINISHED);
            return;
        }

        int maxScore = sortedPlayers.get(0).getScore();
        long winnersCount = sortedPlayers.stream()
                .filter(p -> p.getScore() == maxScore)
                .count();

        if (winnersCount > 1 && !gameState.isBonusRound()) {
            startBonusRound();
        } else {
            gameState.setState(GameState.State.FINISHED);
        }
    }

    private void startBonusRound() {
        gameState.setBonusRound(true);
        loadSounds(true);
        gameState.setState(GameState.State.WAITING);
    }

    private void loadSounds(boolean isBonusRound) {
        gameState.getAvailableSounds().clear();
        gameState.getPlayedSounds().clear();

        try {
            String pathToLoad = isBonusRound ? bonusSoundsPath : soundsPath;
            Resource resource = resourceLoader.getResource("classpath:" + pathToLoad);

            if (resource.exists()) {
                Path soundDir = Paths.get(resource.getURI());
                try (Stream<Path> paths = Files.walk(soundDir, 1)) {
                    List<String> sounds = paths
                            .filter(Files::isRegularFile)
                            .filter(p -> {
                                String name = p.getFileName().toString().toLowerCase();
                                return name.endsWith(".mp3") || name.endsWith(".wav") ||
                                       name.endsWith(".ogg") || name.endsWith(".m4a");
                            })
                            .map(p -> p.getFileName().toString())
                            .collect(Collectors.toList());

                    gameState.setAvailableSounds(sounds);
                }
            }
        } catch (IOException e) {
            gameState.setAvailableSounds(new ArrayList<>());
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public List<Player> getLeaderboard() {
        return gameState.getPlayers().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()))
                .collect(Collectors.toList());
    }
}
