package co.mynd.geeksounds.controller;

import co.mynd.geeksounds.model.GameState;
import co.mynd.geeksounds.model.GuessRequest;
import co.mynd.geeksounds.model.Player;
import co.mynd.geeksounds.service.GameService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;
    private final ResourceLoader resourceLoader;

    public GameController(GameService gameService, ResourceLoader resourceLoader) {
        this.gameService = gameService;
        this.resourceLoader = resourceLoader;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame() {
        gameService.startGame();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Game started!");
        response.put("state", gameService.getGameState());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/play")
    public ResponseEntity<Map<String, Object>> playSound() {
        String sound = gameService.playRandomSound();
        Map<String, Object> response = new HashMap<>();

        if (sound != null) {
            response.put("sound", sound);
            response.put("soundUrl", "/api/game/sound/" + sound);
            response.put("state", gameService.getGameState().getState());
        } else {
            response.put("message", "No more sounds available");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopSound() {
        gameService.stopSound();
        Map<String, Object> response = new HashMap<>();
        response.put("state", gameService.getGameState().getState());
        response.put("message", "Waiting for player selection");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/skip")
    public ResponseEntity<Map<String, Object>> skipSound() {
        gameService.skipSound();
        Map<String, Object> response = new HashMap<>();
        response.put("state", gameService.getGameState().getState());
        response.put("message", "Sound skipped");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/guess")
    public ResponseEntity<Map<String, Object>> playerGuessed(@RequestBody GuessRequest request) {
        String currentSound = gameService.getGameState().getCurrentSound();
        gameService.playerGuessed(request.getPlayerName());

        Map<String, Object> response = new HashMap<>();
        response.put("message", request.getPlayerName() + " scored!");
        response.put("soundName", formatSoundName(currentSound));
        response.put("state", gameService.getGameState().getState());
        response.put("leaderboard", gameService.getLeaderboard());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/state")
    public ResponseEntity<GameState> getGameState() {
        return ResponseEntity.ok(gameService.getGameState());
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Player>> getLeaderboard() {
        return ResponseEntity.ok(gameService.getLeaderboard());
    }

    @GetMapping("/sound/{filename}")
    public ResponseEntity<Resource> getSound(@PathVariable String filename) {
        try {
            GameState state = gameService.getGameState();
            String path = state.isBonusRound() ? "sounds/bonus/" : "sounds/";
            Resource resource = resourceLoader.getResource("classpath:" + path + filename);

            if (resource.exists()) {
                String contentType = "audio/mpeg";
                if (filename.endsWith(".wav")) {
                    contentType = "audio/wav";
                } else if (filename.endsWith(".ogg")) {
                    contentType = "audio/ogg";
                } else if (filename.endsWith(".m4a")) {
                    contentType = "audio/mp4";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/jingle/{type}")
    public ResponseEntity<Resource> getJingle(@PathVariable String type) {
        try {
            String filename = type.equals("hooray") ? "hooray.mp3" : "sad.mp3";
            Resource resource = resourceLoader.getResource("classpath:library/" + filename);

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String formatSoundName(String filename) {
        if (filename == null) {
            return "";
        }

        String nameWithoutExtension = filename.replaceAll("\\.(mp3|wav|ogg|m4a)$", "");

        String formatted = nameWithoutExtension
                .replaceAll("_", " ")
                .replaceAll("-", " ");

        String[] words = formatted.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }

        return result.toString().trim();
    }
}
