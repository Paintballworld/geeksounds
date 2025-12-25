const API_BASE = '/api/game';

let gameState = null;
let currentAudio = null;
let jingleAudio = null;
let currentGuess = { playerName: null, soundName: null };

const screens = {
    welcome: document.getElementById('welcome-screen'),
    playing: document.getElementById('playing-screen'),
    playerSelection: document.getElementById('player-selection-screen'),
    winner: document.getElementById('winner-screen'),
    bonus: document.getElementById('bonus-screen')
};

const buttons = {
    startGame: document.getElementById('start-game-btn'),
    play: document.getElementById('play-btn'),
    replay: document.getElementById('replay-btn'),
    stop: document.getElementById('stop-btn'),
    skip: document.getElementById('skip-btn'),
    playAgain: document.getElementById('play-again-btn'),
    bonusStart: document.getElementById('bonus-start-btn')
};

const elements = {
    soundStatus: document.getElementById('sound-status-text'),
    scoreboardList: document.getElementById('scoreboard-list'),
    gameStatus: document.getElementById('game-status'),
    playerButtons: document.getElementById('player-buttons'),
    winnerNames: document.getElementById('winner-names'),
    soundNameDisplay: document.getElementById('sound-name-display'),
    soundNameText: document.getElementById('sound-name-text'),
    gameAudio: document.getElementById('game-audio'),
    jingleAudio: document.getElementById('jingle-audio')
};

async function apiCall(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(API_BASE + endpoint, options);
    return await response.json();
}

function showScreen(screen) {
    Object.values(screens).forEach(s => s.classList.remove('active'));
    screen.classList.add('active');
}

function updateScoreboard(players) {
    if (!players || players.length === 0) return;

    const sorted = [...players].sort((a, b) => b.score - a.score);

    elements.scoreboardList.innerHTML = sorted.map((player, index) => {
        let placeClass = '';
        if (index === 0) placeClass = 'first-place';
        else if (index === 1) placeClass = 'second-place';
        else if (index === 2) placeClass = 'third-place';

        const imageUrl = `/api/game/player-image/${encodeURIComponent(player.name)}`;
        const initial = player.name.charAt(0).toUpperCase();

        return `
            <div class="player-score ${placeClass}">
                <div class="player-info">
                    <img src="${imageUrl}"
                         alt="${player.name}"
                         class="player-avatar"
                         onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                    <div class="player-avatar-placeholder" style="display: none;">${initial}</div>
                    <span class="player-name">${player.name}</span>
                </div>
                <span class="player-points">${player.score}</span>
            </div>
        `;
    }).join('');
}

function updateGameStatus(message) {
    elements.gameStatus.textContent = message;
}

async function startGame() {
    const response = await apiCall('/start', 'POST');
    gameState = response.state;

    updateScoreboard(gameState.players);
    showScreen(screens.playing);
    updateGameStatus('Game started! Press PLAY to hear the first sound.');

    buttons.play.style.display = 'inline-flex';
    buttons.stop.style.display = 'none';
    buttons.skip.style.display = 'none';
    elements.soundStatus.textContent = 'Press PLAY to start!';
}

async function playSound() {
    const response = await apiCall('/play', 'POST');

    if (response.sound) {
        gameState.currentSound = response.sound;

        elements.soundStatus.textContent = 'Listen carefully...';
        updateGameStatus('A sound is playing!');

        buttons.play.style.display = 'none';
        buttons.replay.style.display = 'inline-flex';
        buttons.stop.style.display = 'inline-flex';
        buttons.skip.style.display = 'inline-flex';

        elements.gameAudio.src = response.soundUrl;
        elements.gameAudio.play();

        // Loop sound with 2-second pause between plays
        elements.gameAudio.onended = () => {
            if (gameState.state === 'PLAYING') {
                setTimeout(() => {
                    if (gameState.state === 'PLAYING') {
                        elements.gameAudio.play();
                    }
                }, 2000);
            }
        };
    } else {
        updateGameStatus('No more sounds! Game ending...');
        setTimeout(checkGameEnd, 1000);
    }
}

function replaySound() {
    if (elements.gameAudio && elements.gameAudio.src) {
        elements.gameAudio.currentTime = 0;
        elements.gameAudio.play();
        elements.soundStatus.textContent = 'Playing again...';
    }
}

async function stopSound() {
    await apiCall('/stop', 'POST');

    if (elements.gameAudio) {
        elements.gameAudio.pause();
        elements.gameAudio.onended = null; // Stop looping
    }

    updateGameStatus('Someone got it! Select the player.');

    buttons.replay.style.display = 'none';
    buttons.stop.style.display = 'none';
    buttons.skip.style.display = 'none';

    renderPlayerButtons();
    showScreen(screens.playerSelection);
}


async function skipSound() {
    const response = await apiCall('/skip', 'POST');

    if (elements.gameAudio) {
        elements.gameAudio.pause();
        elements.gameAudio.onended = null; // Stop looping
    }

    playJingle('lose');

    updateGameStatus('Nobody got it! Moving to next sound...');

    buttons.replay.style.display = 'none';
    buttons.stop.style.display = 'none';
    buttons.skip.style.display = 'none';

    setTimeout(async () => {
        await refreshGameState();
        if (gameState.state === 'FINISHED') {
            checkGameEnd();
        } else if (gameState.state === 'BONUS_ROUND') {
            showBonusRound();
        } else {
            buttons.play.style.display = 'inline-flex';
            elements.soundStatus.textContent = 'Press PLAY for next sound!';
            updateGameStatus('Ready for next sound!');
        }
    }, 2000);
}

function renderPlayerButtons() {
    if (!gameState || !gameState.players) return;

    elements.playerButtons.innerHTML = gameState.players.map(player => `
        <button class="player-button" onclick="selectPlayer('${player.name}')">
            ${player.name}
        </button>
    `).join('');
}

async function selectPlayer(playerName) {
    // Store the guess info
    currentGuess.playerName = playerName;
    currentGuess.soundName = gameState.currentSound;

    // Get the sound name
    const soundName = formatSoundNameLocal(gameState.currentSound);

    // Setup the overlay
    document.getElementById('sound-name-text').textContent = soundName;
    document.getElementById('sound-name-text').style.display = 'none';
    document.getElementById('selected-player-name').textContent = `${playerName} thinks they know it!`;
    document.getElementById('reveal-section').style.display = 'block';
    document.getElementById('confirmation-section').style.display = 'none';

    // Show the overlay
    document.getElementById('sound-name-display').classList.add('show');
}

function revealAnswer() {
    // Show the sound name
    document.getElementById('sound-name-text').style.display = 'block';

    // Hide reveal button, show correct/incorrect buttons
    document.getElementById('reveal-section').style.display = 'none';
    document.getElementById('confirmation-section').style.display = 'flex';
}

function formatSoundNameLocal(filename) {
    if (!filename) return '';

    const nameWithoutExtension = filename.replace(/\.(mp3|wav|ogg|m4a)$/i, '');
    const formatted = nameWithoutExtension.replace(/[_-]/g, ' ');
    const words = formatted.split(/\s+/);

    return words.map(word =>
        word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
    ).join(' ');
}

async function handleCorrectGuess() {
    const playerName = currentGuess.playerName;

    // Hide the overlay
    document.getElementById('sound-name-display').classList.remove('show');

    // Play win jingle
    playJingle('win');

    // Call backend to award point
    const response = await apiCall('/guess', 'POST', { playerName });

    updateScoreboard(response.leaderboard);
    updateGameStatus(`${playerName} scored a point!`);

    await refreshGameState();

    if (gameState.state === 'FINISHED') {
        checkGameEnd();
    } else if (gameState.state === 'BONUS_ROUND') {
        showBonusRound();
    } else {
        showScreen(screens.playing);
        buttons.play.style.display = 'inline-flex';
        elements.soundStatus.textContent = 'Press PLAY for next sound!';
    }
}

async function handleIncorrectGuess() {
    // Hide the overlay
    document.getElementById('sound-name-display').classList.remove('show');

    // Play lose jingle
    playJingle('lose');

    // Call backend to skip (no point awarded)
    await apiCall('/skip', 'POST');

    updateGameStatus('Incorrect guess! Moving to next sound...');

    setTimeout(async () => {
        await refreshGameState();
        if (gameState.state === 'FINISHED') {
            checkGameEnd();
        } else if (gameState.state === 'BONUS_ROUND') {
            showBonusRound();
        } else {
            showScreen(screens.playing);
            buttons.play.style.display = 'inline-flex';
            elements.soundStatus.textContent = 'Press PLAY for next sound!';
            updateGameStatus('Ready for next sound!');
        }
    }, 2000);
}

function displaySoundName(soundName) {
    elements.soundNameText.textContent = soundName;
    elements.soundNameDisplay.classList.add('show');
}

function hideSoundName() {
    elements.soundNameDisplay.classList.remove('show');
}

function playJingle(type) {
    elements.jingleAudio.src = `/api/game/jingle/${type}`;
    elements.jingleAudio.play();
}

async function refreshGameState() {
    const response = await apiCall('/state', 'GET');
    gameState = response;
    return gameState;
}

function showBonusRound() {
    showScreen(screens.bonus);
    updateGameStatus('TIE GAME! Bonus round starting...');
}

async function startBonusRound() {
    showScreen(screens.playing);
    buttons.play.style.display = 'inline-flex';
    buttons.stop.style.display = 'none';
    buttons.skip.style.display = 'none';
    elements.soundStatus.textContent = 'BONUS ROUND - Press PLAY!';
    updateGameStatus('Bonus round in progress!');
}

async function checkGameEnd() {
    await refreshGameState();

    if (gameState.state === 'FINISHED') {
        const sorted = [...gameState.players].sort((a, b) => b.score - a.score);
        const maxScore = sorted[0].score;
        const winners = sorted.filter(p => p.score === maxScore);

        let winnerText = '';
        if (winners.length === 1) {
            winnerText = winners[0].name;
        } else {
            winnerText = winners.map(w => w.name).join(' & ');
        }

        elements.winnerNames.textContent = winnerText;
        showScreen(screens.winner);
        updateGameStatus('Game Over!');

        playJingle('win');
    }
}

async function loadConfig() {
    try {
        const config = await apiCall('/config', 'GET');
        if (config.companyName) {
            document.getElementById('company-name').textContent = config.companyName.toUpperCase();
            document.getElementById('page-title').textContent = `${config.companyName} Sounds`;

            if (config.companySubtitle) {
                document.getElementById('company-subtitle').textContent = config.companySubtitle;
            }
        }
    } catch (error) {
        console.error('Failed to load config:', error);
    }
}

function init() {
    buttons.startGame.addEventListener('click', startGame);
    buttons.play.addEventListener('click', playSound);
    buttons.replay.addEventListener('click', replaySound);
    buttons.stop.addEventListener('click', stopSound);
    buttons.skip.addEventListener('click', skipSound);
    buttons.playAgain.addEventListener('click', startGame);
    buttons.bonusStart.addEventListener('click', startBonusRound);

    document.getElementById('reveal-btn').addEventListener('click', revealAnswer);
    document.getElementById('correct-btn').addEventListener('click', handleCorrectGuess);
    document.getElementById('incorrect-btn').addEventListener('click', handleIncorrectGuess);

    updateGameStatus('Ready to play!');
    loadConfig();
}

window.selectPlayer = selectPlayer;

init();
