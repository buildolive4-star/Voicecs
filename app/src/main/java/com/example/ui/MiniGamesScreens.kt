package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.theme.CyanDeep
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.RoseMagenta
import com.example.ui.theme.RubyRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun MiniGameRouterScreen(
    gameId: String,
    coinBalance: Long,
    onBack: () -> Unit,
    onRecordGame: (String, String, String, Long, Long, String, String) -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Games")
                }
                Text(
                    text = gameId.replace("_", " "),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Text("%,d coins".format(coinBalance), style = MaterialTheme.typography.labelSmall, color = ImperialGold)
        }
        Spacer(Modifier.height(10.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (gameId) {
                "SPIN_WHEEL" -> SpinWheelGame(coinBalance, onRecordGame)
                "TIC_TAC_TOE" -> TicTacToeGame(onRecordGame)
                "LUDO" -> LudoQuickGame(onRecordGame)
                "QUIZ" -> TriviaQuizGame(onRecordGame)
                "NUMBER_GUESS" -> NumberGuessGame(onRecordGame)
                "WORD_GAME" -> WordScrambleGame(onRecordGame)
                "MEMORY" -> MemoryMatchGame(onRecordGame)
            }
        }
    }
}

@Composable
fun SpinWheelGame(
    coinBalance: Long,
    onRecordGame: (String, String, String, Long, Long, String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val prizes = listOf(10L, 100L, 0L, 250L, 20L, 500L, 5L, 150L)
    val sliceColors = listOf(
        ElectricViolet, ImperialGold, ObsidianElevated, EmeraldWin,
        CyanDeep, RoseMagenta, ObsidianSurface, NeonCyan
    )
    val rotation = remember { Animatable(0f) }
    var spinning by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("Spin for 25 virtual coins!") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("▼", fontSize = 28.sp, color = ImperialGold)
        Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sweep = 360f / prizes.size
                rotate(rotation.value) {
                    prizes.forEachIndexed { idx, _ ->
                        drawArc(
                            color = sliceColors[idx],
                            startAngle = idx * sweep - 90f,
                            sweepAngle = sweep,
                            useCenter = true,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height)
                        )
                    }
                }
                drawCircle(color = ObsidianSurface, radius = 34.dp.toPx())
            }
            Text("SPIN", fontWeight = FontWeight.Bold, color = ImperialGold)
        }

        Text(resultText, style = MaterialTheme.typography.titleMedium, color = NeonCyan, textAlign = TextAlign.Center)

        GradientButton(
            text = if (spinning) "Spinning..." else "Spin Wheel (25 Coins)",
            enabled = !spinning && coinBalance >= 25L,
            onClick = {
                spinning = true
                scope.launch {
                    val idx = Random.nextInt(prizes.size)
                    val sweep = 360f / prizes.size
                    val targetAngle = rotation.value + 1080f + (360f - (idx * sweep + sweep / 2f))
                    rotation.animateTo(targetAngle, tween(2200, easing = FastOutSlowInEasing))
                    val won = prizes[idx]
                    val outcome = if (won >= 25L) "WIN" else "LOSS"
                    resultText = if (won > 0) "🎉 Wheel landed on +$won virtual coins!" else "Unlucky 0c slice — try again!"
                    onRecordGame("SPIN_WHEEL", "spin_${System.currentTimeMillis()}", "25c", 25L, won, outcome, "Won $won coins")
                    spinning = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TicTacToeGame(onRecordGame: (String, String, String, Long, Long, String, String) -> Unit) {
    val board = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    var status by remember { mutableStateOf("Your Turn (X)") }
    var gameOver by remember { mutableStateOf(false) }

    fun checkWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (l in lines) {
            if (b[l[0]].isNotEmpty() && b[l[0]] == b[l[1]] && b[l[1]] == b[l[2]]) return b[l[0]]
        }
        return if (b.none { it.isEmpty() }) "DRAW" else null
    }

    fun reset() {
        for (i in 0..8) board[i] = ""
        status = "Your Turn (X)"
        gameOver = false
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(status, style = MaterialTheme.typography.titleLarge, color = NeonCyan)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(276.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(board) { idx, cell ->
                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable(enabled = !gameOver && cell.isEmpty()) {
                            board[idx] = "X"
                            val w = checkWinner(board)
                            if (w != null) {
                                gameOver = true
                                val won = if (w == "X") 60L else if (w == "DRAW") 15L else 0L
                                status = if (w == "X") "🎉 You Won +60 Coins!" else "Draw! +15 Coins"
                                onRecordGame("TIC_TAC_TOE", "ttt_${System.currentTimeMillis()}", "X", 0L, won, if (w == "X") "WIN" else "DRAW", status)
                            } else {
                                val empty = board.indices.filter { board[it].isEmpty() }
                                if (empty.isNotEmpty()) {
                                    board[empty.random()] = "O"
                                    val w2 = checkWinner(board)
                                    if (w2 != null) {
                                        gameOver = true
                                        val outcome = if (w2 == "O") "LOSS" else "DRAW"
                                        val won = if (w2 == "DRAW") 15L else 0L
                                        status = if (w2 == "O") "Bot Won! Try again" else "Draw! +15 Coins"
                                        onRecordGame("TIC_TAC_TOE", "ttt_${System.currentTimeMillis()}", "X", 0L, won, outcome, status)
                                    }
                                }
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianElevated,
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = cell,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cell == "X") NeonCyan else RoseMagenta
                        )
                    }
                }
            }
        }
        GradientButton(text = "New Match", icon = Icons.Filled.Refresh, onClick = { reset() })
    }
}

@Composable
fun LudoQuickGame(onRecordGame: (String, String, String, Long, Long, String, String) -> Unit) {
    // Playable 24-tile linear Ludo Arena race (2 tokens per player + capture!)
    val trackLength = 24
    var pTokens by remember { mutableStateOf(listOf(0, 0)) }
    var botTokens by remember { mutableStateOf(listOf(0, 0)) }
    var lastDice by remember { mutableIntStateOf(0) }
    var playerTurn by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("Roll the dice to advance your 2 tokens to tile $trackLength!") }
    var finished by remember { mutableStateOf(false) }

    fun botTurn() {
        val roll = Random.nextInt(1, 7)
        val idx = if (botTokens[0] < trackLength) 0 else 1
        val next = (botTokens[idx] + roll).coerceAtMost(trackLength)
        val updatedBot = botTokens.toMutableList().also { it[idx] = next }
        // Capture check
        val updatedPlayer = pTokens.map { if (next in 1 until trackLength && it == next) 0 else it }
        botTokens = updatedBot
        pTokens = updatedPlayer
        if (updatedBot.all { it >= trackLength }) {
            finished = true
            status = "Bot finished both tokens! Play again."
            onRecordGame("LUDO", "ludo_${System.currentTimeMillis()}", "BLUE", 0L, 0L, "LOSS", "Bot won Ludo Quick")
        } else {
            playerTurn = true
            status = "Bot rolled $roll! Your turn."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassCard(borderColor = NeonCyan) {
            Text(status, style = MaterialTheme.typography.titleSmall, color = NeonCyan)
            Spacer(Modifier.height(6.dp))
            Text("You (Cyan): Token A ${pTokens[0]}/$trackLength • Token B ${pTokens[1]}/$trackLength", style = MaterialTheme.typography.bodySmall)
            Text("Bot (Rose): Token A ${botTokens[0]}/$trackLength • Token B ${botTokens[1]}/$trackLength", style = MaterialTheme.typography.bodySmall, color = RoseMagenta)
        }

        // Visual 24-tile track grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.fillMaxWidth().height(210.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(trackLength) { index ->
                val tile = index + 1
                val hasP = pTokens.contains(tile)
                val hasB = botTokens.contains(tile)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        tile == trackLength -> ImperialGold.copy(alpha = 0.28f)
                        hasP && hasB -> ElectricViolet.copy(alpha = 0.4f)
                        hasP -> NeonCyan.copy(alpha = 0.3f)
                        hasB -> RoseMagenta.copy(alpha = 0.3f)
                        else -> ObsidianElevated
                    },
                    border = BorderStroke(1.dp, if (tile == trackLength) ImperialGold else GlassBorder),
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val badge = buildString {
                            if (hasP) append("🔵")
                            if (hasB) append("🔴")
                            if (isEmpty()) append(if (tile == trackLength) "🏁" else "$tile")
                        }
                        Text(badge, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (!finished && lastDice > 0 && playerTurn) {
            Text("Rolled $lastDice! Choose which token to move:", style = MaterialTheme.typography.titleSmall, color = ImperialGold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(0, 1).forEach { tokenIdx ->
                    Button(
                        enabled = pTokens[tokenIdx] < trackLength,
                        onClick = {
                            val next = (pTokens[tokenIdx] + lastDice).coerceAtMost(trackLength)
                            val updatedP = pTokens.toMutableList().also { it[tokenIdx] = next }
                            val updatedB = botTokens.map { if (next in 1 until trackLength && it == next) 0 else it }
                            pTokens = updatedP
                            botTokens = updatedB
                            lastDice = 0
                            if (updatedP.all { it >= trackLength }) {
                                finished = true
                                status = "🏆 Victory! Both tokens home! +120 Virtual Coins"
                                onRecordGame("LUDO", "ludo_${System.currentTimeMillis()}", "CYAN", 0L, 120L, "WIN", "Won Ludo Quick Arena")
                            } else {
                                playerTurn = false
                                botTurn()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Move Token ${if (tokenIdx == 0) "A" else "B"}")
                    }
                }
            }
        } else if (!finished) {
            GradientButton(
                text = "Roll Dice",
                icon = Icons.Filled.Casino,
                enabled = playerTurn && lastDice == 0,
                onClick = { lastDice = Random.nextInt(1, 7) },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            GradientButton(
                text = "Play Again",
                icon = Icons.Filled.Refresh,
                onClick = {
                    pTokens = listOf(0, 0)
                    botTokens = listOf(0, 0)
                    lastDice = 0
                    playerTurn = true
                    finished = false
                    status = "Roll the dice to advance your 2 tokens to tile $trackLength!"
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TriviaQuizGame(onRecordGame: (String, String, String, Long, Long, String, String) -> Unit) {
    val questions = remember {
        listOf(
            Triple("Which planet is known as the Red Planet?", listOf("Venus", "Mars", "Jupiter", "Saturn"), 1),
            Triple("What does 'VIP' stand for?", listOf("Very Important Person", "Virtual Input Protocol", "Voice Interface Port", "Visual Icon Pack"), 0),
            Triple("How many mic seats does a standard SocialVerse room have?", listOf("4", "6", "8", "16"), 2),
            Triple("Which color gives 4.5x in SocialVerse Color Blitz?", listOf("Red", "Green", "Violet", "Blue"), 2),
            Triple("Which gift costs 10,000 virtual coins?", listOf("Rose", "Crown", "Luxury Car", "Private Jet"), 3)
        )
    }
    var qIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!done) {
            val q = questions[qIndex]
            GlassCard {
                Text("Question ${qIndex + 1} / ${questions.size}", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                Spacer(Modifier.height(8.dp))
                Text(q.first, style = MaterialTheme.typography.titleLarge)
            }
            q.second.forEachIndexed { idx, opt ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (idx == q.third) score++
                        if (qIndex < questions.lastIndex) {
                            qIndex++
                        } else {
                            done = true
                            val coins = score * 20L
                            onRecordGame("QUIZ", "quiz_${System.currentTimeMillis()}", "$score/5", 0L, coins, if (score >= 3) "WIN" else "DRAW", "Scored $score/5")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianElevated,
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Text(opt, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            GlassCard(borderColor = ImperialGold) {
                Text("Quiz Complete!", style = MaterialTheme.typography.headlineSmall, color = ImperialGold)
                Text("You answered $score / ${questions.size} correctly (+${score * 20} Virtual Coins)", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                GradientButton(text = "Play Again", onClick = { qIndex = 0; score = 0; done = false })
            }
        }
    }
}

@Composable
fun NumberGuessGame(onRecordGame: (String, String, String, Long, Long, String, String) -> Unit) {
    var secret by remember { mutableIntStateOf(Random.nextInt(1, 51)) }
    var guessInput by remember { mutableStateOf("") }
    var attemptsLeft by remember { mutableIntStateOf(5) }
    var hint by remember { mutableStateOf("Guess a secret number between 1 and 50!") }
    var finished by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard {
            Text(hint, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
            Text("Attempts left: $attemptsLeft", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        OutlinedTextField(
            value = guessInput,
            onValueChange = { guessInput = it.filter(Char::isDigit).take(2) },
            label = { Text("Your Guess (1–50)") },
            singleLine = true,
            enabled = !finished,
            modifier = Modifier.fillMaxWidth()
        )
        if (!finished) {
            GradientButton(
                text = "Submit Guess",
                onClick = {
                    val g = guessInput.toIntOrNull() ?: return@GradientButton
                    guessInput = ""
                    attemptsLeft--
                    when {
                        g == secret -> {
                            finished = true
                            hint = "🎯 Bullseye! $secret was right! +80 Virtual Coins"
                            onRecordGame("NUMBER_GUESS", "ng_${System.currentTimeMillis()}", "$g", 0L, 80L, "WIN", "Guessed $secret")
                        }
                        attemptsLeft <= 0 -> {
                            finished = true
                            hint = "Out of tries! Secret number was $secret."
                            onRecordGame("NUMBER_GUESS", "ng_${System.currentTimeMillis()}", "$g", 0L, 0L, "LOSS", "Missed $secret")
                        }
                        g < secret -> hint = "Higher than $g!"
                        else -> hint = "Lower than $g!"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            GradientButton(
                text = "New Round",
                onClick = {
                    secret = Random.nextInt(1, 51)
                    attemptsLeft = 5
                    finished = false
                    hint = "Guess a secret number between 1 and 50!"
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun WordScrambleGame(onRecordGame: (String, String, String, Long, Long, String, String) -> Unit) {
    val words = listOf("SOCIALVERSE", "DIAMOND", "LOUNGE", "TROPHY", "MELODY", "GALAXY")
    var target by remember { mutableStateOf(words.random()) }
    var scrambled by remember(target) { mutableStateOf(target.toList().shuffled().joinToString("")) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("Unscramble the word to earn +50 coins!") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard {
            Text(scrambled, style = MaterialTheme.typography.displaySmall, color = ImperialGold)
            Spacer(Modifier.height(6.dp))
            Text(feedback, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it.uppercase() },
            label = { Text("Unscrambled Word") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        GradientButton(
            text = "Check Word",
            onClick = {
                if (answer.trim() == target) {
                    feedback = "✅ Correct! $target (+50 Virtual Coins)"
                    onRecordGame("WORD_GAME", "wg_${System.currentTimeMillis()}", target, 0L, 50L, "WIN", "Solved $target")
                    target = words.random()
                    answer = ""
                } else {
                    feedback = "Not quite — try again!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MemoryMatchGame(onRecordGame: (String, String, String, Long, Long, String, String) -> Unit) {
    val basePairs = listOf("🎧", "👑", "💎", "🔥", "🌹", "🚀")
    var cards by remember { mutableStateOf((basePairs + basePairs).shuffled()) }
    val matched = remember { mutableStateListOf<Int>() }
    var firstPick by remember { mutableStateOf<Int?>(null) }
    var secondPick by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Pairs matched: ${matched.size / 2} / ${basePairs.size} • Moves: $moves", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(cards) { idx, emoji ->
                val revealed = idx in matched || idx == firstPick || idx == secondPick
                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable(enabled = !revealed && secondPick == null) {
                            if (firstPick == null) {
                                firstPick = idx
                            } else {
                                secondPick = idx
                                moves++
                                val f = firstPick!!
                                if (cards[f] == cards[idx]) {
                                    matched.add(f)
                                    matched.add(idx)
                                    firstPick = null
                                    secondPick = null
                                    if (matched.size == cards.size) {
                                        onRecordGame("MEMORY", "mem_${System.currentTimeMillis()}", "$moves moves", 0L, 90L, "WIN", "Matched all 6 pairs in $moves moves")
                                    }
                                } else {
                                    scope.launch {
                                        kotlinx.coroutines.delay(650L)
                                        firstPick = null
                                        secondPick = null
                                    }
                                }
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = if (idx in matched) EmeraldWin.copy(alpha = 0.25f) else ObsidianElevated,
                    border = BorderStroke(1.dp, if (revealed) NeonCyan else GlassBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (revealed) emoji else "?", fontSize = 24.sp)
                    }
                }
            }
        }
        GradientButton(
            text = "Reset Board",
            onClick = {
                cards = (basePairs + basePairs).shuffled()
                matched.clear()
                firstPick = null
                secondPick = null
                moves = 0
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
