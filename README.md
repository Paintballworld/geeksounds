# Sound Trivia Game Show

A fun, interactive sound trivia game with a game-show style interface built with Spring Boot and vanilla JavaScript.

## Features

- **Company Branding**: Customizable company/team name and subtitle
- **Multiple Players**: Configure player names in the properties file
- **Player Photos**: Display player profile pictures on the scoreboard
- **Sound Trivia**: Play random sounds and have players guess them
- **Live Scoreboard**: Real-time leaderboard sorted by score with player avatars
- **Randomized Jingles**: Multiple celebration and fail sounds randomly selected
- **Bonus Round**: Automatic tiebreaker when multiple players have the same score
- **Corporate Design**: Professional blue/red color scheme with animations
- **Looping Sounds**: Sounds loop with 2-second pauses until guessed or skipped

## Setup

### 1. Configure Company & Players

Edit `src/main/resources/application.properties`:

```properties
# Company/Team branding
game.company.name=Your Company
game.company.subtitle=Sound Trivia Game

# Player names (comma-separated)
game.players=Alice,Bob,Charlie,Diana,Eve
```

### 2. Add Player Images (Optional)

Add player profile photos to `src/main/resources/images/`:
- Name images exactly as player names: `Alice.jpg`, `Bob.png`, etc.
- Supported formats: JPG, PNG, GIF, WebP
- Recommended: 200x200px square images
- If no image found, displays initial placeholder

### 3. Add Sound Files

Add your sound files to these directories:

- **Main sounds**: `src/main/resources/sounds/`
  - Place your trivia sound files here (mp3, wav, ogg, m4a)
  - Example: `windows_95_startup.mp3`, `mario_coin.wav`

- **Bonus round sounds**: `src/main/resources/sounds/bonus/`
  - Place harder sounds for tiebreaker rounds

- **Win jingles**: `src/main/resources/library/win/`
  - Add 3-5 celebration/victory sounds (randomly selected when someone guesses correctly)

- **Lose jingles**: `src/main/resources/library/lose/`
  - Add 3-5 fail/wrong sounds (randomly selected when sound is skipped)

### 4. Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## How to Play

1. **Start Game**: Click the big "START GAME" button
2. **Play Sound**: Click "PLAY" to play a random sound
3. **Stop**: When someone knows the answer, click "STOP - SOMEONE GOT IT!"
4. **Select Player**: Click the name of the player who guessed correctly
5. **Skip**: If nobody knows, click "SKIP" to move to the next sound
6. **Scoreboard**: Watch the live scoreboard on the right update automatically
7. **Winner**: After all sounds are played, the winner is announced
8. **Bonus Round**: If there's a tie, a bonus round automatically starts

## Game Flow

1. Game starts with all players at 0 points
2. Random sounds play from the collection
3. Players compete to guess the sound
4. First to guess correctly gets a point
5. Sound name is displayed for 3 seconds after a correct guess
6. Continue until all sounds are played
7. If tie at the end → Bonus round with harder sounds
8. Winner announced with celebratory animation

## Technical Details

### Backend (Spring Boot)
- **GameService**: Core game logic and state management
- **GameController**: REST API endpoints
- **Models**: Player, GameState, GuessRequest

### Frontend
- Pure HTML/CSS/JavaScript (no frameworks)
- Responsive game-show themed design
- Animated UI elements
- Audio playback support

### API Endpoints

- `POST /api/game/start` - Start new game
- `POST /api/game/play` - Play random sound
- `POST /api/game/stop` - Stop current sound (someone guessed)
- `POST /api/game/skip` - Skip current sound
- `POST /api/game/guess` - Record player guess
- `GET /api/game/state` - Get current game state
- `GET /api/game/leaderboard` - Get current leaderboard
- `GET /api/game/sound/{filename}` - Stream sound file
- `GET /api/game/jingle/{type}` - Stream jingle file

## Customization

### Change Port
Edit `src/main/resources/application.properties`:
```properties
server.port=8080
```

### Change Company Name
Edit `src/main/resources/application.properties`:
```properties
game.company.name=YourCompany
```

### Modify Player Names
Edit `src/main/resources/application.properties`:
```properties
game.players=Player1,Player2,Player3
```

### Customize Colors & Branding
Edit `src/main/resources/static/styles.css` to change the color scheme:
- Primary Blue: `#00539F`
- Red: `#EE1C25`
- Modify gradients and accent colors to match your brand

Edit `src/main/resources/application.properties` to customize text:
```properties
game.company.name=Your Company Name
game.company.subtitle=Your Custom Subtitle
```

## Requirements

- Java 17 or higher
- Gradle 8.x
- Modern web browser with audio support

## License

Free to use and modify for personal and commercial projects.
