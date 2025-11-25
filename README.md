# Sound Trivia Game

A fun, interactive sound trivia game with a clean, professional interface built with Spring Boot and vanilla JavaScript.

**Free to use and play!** Perfect for team events, parties, or office entertainment.

## Features

- 🎯 **Company Branding**: Customizable company/team name and subtitle
- 👥 **Multiple Players**: Configure player names and add profile photos
- 🖼️ **Player Avatars**: Display player profile pictures on the live scoreboard
- 🎵 **Sound Trivia**: Play random sounds from your collection
- 🔁 **Replay Button**: Instantly replay sounds that are too short to catch
- ✅ **Answer Verification**: Confirm correct/incorrect guesses with popup
- 📊 **Live Scoreboard**: Real-time leaderboard with rankings
- 🎉 **Randomized Jingles**: Multiple win/lose sounds for variety
- 🏆 **Bonus Round**: Automatic tiebreaker with harder sounds
- 🎨 **Professional Design**: Clean, corporate blue/white color scheme
- 🔄 **Sound Looping**: Sounds repeat with 2-second pauses until answered
- ⚡ **No Database Required**: All state managed in-memory
- 📱 **Responsive**: Works on desktop, tablet, and mobile devices

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

### Game Controls

1. **Start Game**: Click "START GAME" to begin
2. **Play Sound**: Click "PLAY" to hear a random sound
3. **Play Again**: Click "PLAY AGAIN" to replay the current sound (useful for short sounds)
4. **Someone Got It**: When a player shouts out the answer, click "STOP - SOMEONE GOT IT!"
5. **Select Player**: Click the name of the player who thinks they know it
6. **Verify Answer**: A popup shows the actual sound name with two options:
   - Click **✓ CORRECT** if they got it right → They get a point + win jingle plays
   - Click **✗ INCORRECT** if they got it wrong → No point + lose jingle plays
7. **Wrong Guess Button**: If you selected the wrong player by mistake, click "WRONG GUESS - GO BACK"
8. **Skip**: If nobody knows, click "SKIP" → Lose jingle plays, move to next sound

### Game Flow

1. **Setup**: Game starts with all players at 0 points
2. **Playing**: Sounds loop with 2-second pauses until someone guesses or you skip
3. **Scoring**: First correct guess gets a point
4. **Progression**: Continue through all sounds in your collection
5. **Tiebreaker**: If multiple players tie for first place → Bonus round starts automatically
6. **Winner**: Celebrate the champion with a trophy animation!

### Scoreboard

- **Live Updates**: Watch scores change in real-time on the right side
- **Player Photos**: Shows player avatars (or initials if no photo)
- **Rankings**:
  - 🥇 First place highlighted in red
  - 🥈 Second place in blue
  - 🥉 Third place in green

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
- Gradle 8.x (included via wrapper)
- Modern web browser with HTML5 audio support
- Sound files (MP3, WAV, OGG, or M4A format)

## Tips for Best Experience

### Sound Selection
- Use iconic, recognizable sounds (startup sounds, notification tones, movie quotes)
- Mix difficulty levels - some easy wins, some challenging
- Keep sounds between 2-10 seconds for best gameplay
- Test volume levels to ensure consistency

### Player Photos
- Square images work best (200x200px or larger)
- Name files exactly as player names (case-sensitive)
- Supported: JPG, PNG, GIF, WebP

### Jingles
- **Win sounds**: Applause, cheers, success fanfares (2-5 seconds)
- **Lose sounds**: "Wah wah", buzzer, comedic fails (2-5 seconds)
- Having 3-5 variations keeps the game fresh

### Hosting
- Run on a laptop connected to speakers for best audio
- Display on a large screen or projector for visibility
- One person acts as game master to control the interface

## Troubleshooting

**Sounds not playing?**
- Check file formats (MP3, WAV, OGG, M4A)
- Verify files are in the correct directories
- Check browser console for errors

**Images not showing?**
- Ensure filenames match player names exactly
- Check file extensions (.jpg, .png, etc.)
- Verify images are in `src/main/resources/images/`

**Port already in use?**
- Change port in `application.properties`
- Default is 8080, try 8081 or 9000

## License

**Free to use and play!**

This project is free to use, modify, and deploy for personal, educational, and commercial purposes. Perfect for:
- Office team building events
- Virtual happy hours
- Birthday parties
- Educational settings
- Corporate events
- Friend gatherings

Enjoy the game! 🎵🎮🎉
