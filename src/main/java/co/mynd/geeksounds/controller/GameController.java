package co.mynd.geeksounds.controller;

import co.mynd.geeksounds.model.GameState;
import co.mynd.geeksounds.model.GuessRequest;
import co.mynd.geeksounds.model.Player;
import co.mynd.geeksounds.service.GameService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;
    private final ResourceLoader resourceLoader;

    @Value("${game.company.name}")
    private String companyName;

    @Value("${game.company.subtitle}")
    private String companySubtitle;

    @Value("${game.library.win.path}")
    private String winJinglesPath;

    @Value("${game.library.lose.path}")
    private String loseJinglesPath;

    @Value("${game.images.path}")
    private String imagesPath;

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

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("companyName", companyName);
        config.put("companySubtitle", companySubtitle);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/player-image/{playerName}")
    public ResponseEntity<Resource> getPlayerImage(@PathVariable String playerName) {
        try {
            // Try different image extensions
            String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

            for (String ext : extensions) {
                String filename = playerName + ext;
                Resource resource = resourceLoader.getResource("classpath:" + imagesPath + "/" + filename);

                if (resource.exists()) {
                    String contentType = "image/jpeg";
                    if (ext.equals(".png")) {
                        contentType = "image/png";
                    } else if (ext.equals(".gif")) {
                        contentType = "image/gif";
                    } else if (ext.equals(".webp")) {
                        contentType = "image/webp";
                    }

                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                            .body(resource);
                }
            }

            // If no image found, return 404
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
            String path = type.equals("win") ? winJinglesPath : loseJinglesPath;
            String filename = getRandomJingleFile(path);

            if (filename == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = resourceLoader.getResource("classpath:" + path + "/" + filename);

            if (resource.exists()) {
                String contentType = getContentType(filename);
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

    private String getRandomJingleFile(String path) {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + path);
            if (!resource.exists()) {
                return null;
            }

            Path jingleDir = Paths.get(resource.getURI());
            try (Stream<Path> paths = Files.walk(jingleDir, 1)) {
                List<String> jingles = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> {
                            String name = p.getFileName().toString().toLowerCase();
                            return name.endsWith(".mp3") || name.endsWith(".wav") ||
                                   name.endsWith(".ogg") || name.endsWith(".m4a");
                        })
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());

                if (jingles.isEmpty()) {
                    return null;
                }

                Random random = new Random();
                return jingles.get(random.nextInt(jingles.size()));
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String getContentType(String filename) {
        if (filename.endsWith(".wav")) {
            return "audio/wav";
        } else if (filename.endsWith(".ogg")) {
            return "audio/ogg";
        } else if (filename.endsWith(".m4a")) {
            return "audio/mp4";
        }
        return "audio/mpeg";
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
