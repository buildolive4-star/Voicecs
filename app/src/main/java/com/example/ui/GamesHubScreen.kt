package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Family
import com.example.data.GameRecord
import com.example.data.SocialVerseCatalog
import com.example.data.UserPublicProfile
import com.example.ui.components.AvatarWithFrame
import com.example.ui.components.CoinBalancePill
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
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
import com.example.ui.theme.VioletGame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun GamesHubScreen(
    myProfile: UserPublicProfile,
    coinBalance: Long,
    gameRecords: List<GameRecord>,
    discoverUsers: List<UserPublicProfile>,
    families: List<Family>,
    onOpenWallet: () -> Unit,
    onRecordGame: (
        gameType: String,
        periodId: String,
        selection: String,
        stakeCoins: Long,
        wonCoins: Long,
        outcome: String,
        detail: String
    ) -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Color Prediction, 1 = Mini-Games, 2 = Leaderboards
    var activeMiniGame by remember { mutableStateOf<String?>(null) }

    if (activeMiniGame != null) {
        MiniGameRouterScreen(
            gameId = activeMiniGame!!,
            coinBalance = coinBalance,
            onBack = { activeMiniGame = null },
            onRecordGame = onRecordGame
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Gaming Arena", style = MaterialTheme.typography.headlineMedium)
                Text("Non-cash virtual coins only", style = MaterialTheme.typography.bodySmall, color = NeonCyan)
            }
            CoinBalancePill(coins = coinBalance, onClick = onOpenWallet)
        }

        // Compliance Notice Banner
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = ElectricViolet.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Entertainment Only: Games use non-cash virtual coins with no monetary wagering or withdrawals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = ObsidianSurface,
            contentColor = NeonCyan
        ) {
            Tab(selected = selectedSection == 0, onClick = { selectedSection = 0 }, text = { Text("Color Blitz") })
            Tab(selected = selectedSection == 1, onClick = { selectedSection = 1 }, text = { Text("Mini-Games") })
            Tab(selected = selectedSection == 2, onClick = { selectedSection = 2 }, text = { Text("Rankings") })
        }

        when (selectedSection) {
            0 -> ColorPredictionArena(
                coinBalance = coinBalance,
                recentRecords = gameRecords.filter { it.gameType == "COLOR_PREDICTION" },
                onRecordGame = onRecordGame
            )
            1 -> MiniGamesCatalogGrid(
                gameRecords = gameRecords,
                onLaunchGame = { activeMiniGame = it }
            )
            2 -> LeaderboardsSection(
                myProfile = myProfile,
                users = discoverUsers,
                families = families
            )
        }
    }
}

@Composable
fun ColorPredictionArena(
    coinBalance: Long,
    recentRecords: List<GameRecord>,
    onRecordGame: (String, String, String, Long, Long, String, String) -> Unit
) {
    val durations = listOf(30, 60, 180, 300)
    var selectedDurationSec by remember { mutableIntStateOf(30) }
    var currentEpoch by remember { mutableLongStateOf(System.currentTimeMillis() / 1000L) }
    var selectedPrediction by remember { mutableStateOf<String?>(null) }
    var selectedStake by remember { mutableLongStateOf(50L) }
    var lockedRoundId by remember { mutableStateOf<String?>(null) }
    var lastRoundBanner by remember { mutableStateOf<String?>(null) }
    val recentColorDots = remember { mutableStateListOf("GREEN", "RED", "VIOLET", "GREEN", "RED", "GREEN") }

    LaunchedEffect(Unit) {
        while (true) {
            currentEpoch = System.currentTimeMillis() / 1000L
            delay(500L)
        }
    }

    val periodIndex = currentEpoch / selectedDurationSec
    val periodId = "${selectedDurationSec}S-$periodIndex"
    val secondsRemaining = (selectedDurationSec - (currentEpoch % selectedDurationSec)).toInt()
    val isLockedWindow = secondsRemaining <= 5

    // Auto-resolve locked prediction when period rolls over
    LaunchedEffect(periodId) {
        val pendingSelection = selectedPrediction
        val prevRound = lockedRoundId
        if (pendingSelection != null && prevRound != null && prevRound != periodId) {
            val winningNumber = Random.nextInt(0, 10)
            val winningColor = when {
                winningNumber == 0 || winningNumber == 5 -> "VIOLET"
                winningNumber % 2 == 1 -> "GREEN"
                else -> "RED"
            }
            recentColorDots.add(0, winningColor)
            if (recentColorDots.size > 12) recentColorDots.removeAt(recentColorDots.lastIndex)

            val isWin = when (pendingSelection) {
                "GREEN" -> winningColor == "GREEN"
                "RED" -> winningColor == "RED"
                "VIOLET" -> winningColor == "VIOLET"
                else -> pendingSelection == winningNumber.toString()
            }
            val multiplier = when (pendingSelection) {
                "VIOLET" -> 4.5
                "GREEN", "RED" -> 2.0
                else -> 8.0
            }
            val wonCoins = if (isWin) (selectedStake * multiplier).toLong() else 0L
            val outcome = if (isWin) "WIN" else "LOSS"
            val detail = "Result: $winningColor (#$winningNumber) • Picked $pendingSelection"
            lastRoundBanner = if (isWin) "🎉 Won +$wonCoins coins! ($detail)" else "Round ended ($detail)"

            onRecordGame(
                "COLOR_PREDICTION",
                prevRound,
                pendingSelection,
                selectedStake,
                wonCoins,
                outcome,
                detail
            )
            selectedPrediction = null
            lockedRoundId = null
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Duration Modes: 30s, 60s, 3m, 5m
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                durations.forEach { sec ->
                    val label = if (sec < 60) "${sec}s" else "${sec / 60}m"
                    FilterChip(
                        selected = selectedDurationSec == sec,
                        onClick = {
                            if (lockedRoundId == null) selectedDurationSec = sec
                        },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Countdown & Period ID Card
        item {
            GlassCard(borderColor = if (isLockedWindow) RoseMagenta else NeonCyan) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Period ID", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                        Text(periodId, style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            recentColorDots.take(8).forEach { dot ->
                                val c = when (dot) {
                                    "GREEN" -> EmeraldWin
                                    "VIOLET" -> VioletGame
                                    else -> RubyRed
                                }
                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(c))
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            if (isLockedWindow) "LOCKED" else "COUNTDOWN",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isLockedWindow) RoseMagenta else ImperialGold
                        )
                        Text(
                            text = "%02d:%02d".format(secondsRemaining / 60, secondsRemaining % 60),
                            style = MaterialTheme.typography.displaySmall,
                            color = if (isLockedWindow) RoseMagenta else Color.White
                        )
                    }
                }
                if (lastRoundBanner != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(lastRoundBanner!!, style = MaterialTheme.typography.labelLarge, color = ImperialGold)
                }
            }
        }

        // Color Picks: Green, Violet, Red
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val colorButtons = listOf(
                    Triple("GREEN", EmeraldWin, "2x"),
                    Triple("VIOLET", VioletGame, "4.5x"),
                    Triple("RED", RubyRed, "2x")
                )
                colorButtons.forEach { (label, color, mult) ->
                    val isPicked = selectedPrediction == label
                    Button(
                        onClick = {
                            if (!isLockedWindow && lockedRoundId == null) {
                                selectedPrediction = label
                            }
                        },
                        enabled = !isLockedWindow && lockedRoundId == null,
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        border = if (isPicked) BorderStroke(3.dp, Color.White) else null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(52.dp).testTag("color_pick_$label")
                    ) {
                        Text("$label $mult", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Number Picks 0..9
        item {
            Text("Or Predict Exact Number (8x)", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0..4).forEach { n ->
                    val picked = selectedPrediction == n.toString()
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable(enabled = !isLockedWindow && lockedRoundId == null) {
                                selectedPrediction = n.toString()
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (picked) ElectricViolet else ObsidianElevated,
                        border = BorderStroke(1.dp, if (picked) NeonCyan else GlassBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text("$n", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (5..9).forEach { n ->
                    val picked = selectedPrediction == n.toString()
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable(enabled = !isLockedWindow && lockedRoundId == null) {
                                selectedPrediction = n.toString()
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (picked) ElectricViolet else ObsidianElevated,
                        border = BorderStroke(1.dp, if (picked) NeonCyan else GlassBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text("$n", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        // Virtual Coin Stake & Lock Prediction Button
        item {
            Text("Virtual Coin Entry", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10L, 50L, 100L, 500L).forEach { amt ->
                    FilterChip(
                        selected = selectedStake == amt,
                        onClick = { if (lockedRoundId == null) selectedStake = amt },
                        label = { Text("${amt}c") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            GradientButton(
                text = when {
                    lockedRoundId != null -> "Locked: $selectedPrediction (${selectedStake}c) • Wait ${secondsRemaining}s"
                    isLockedWindow -> "Round Locked — Next Period Soon"
                    selectedPrediction == null -> "Select a Color or Number"
                    coinBalance < selectedStake -> "Insufficient Coins"
                    else -> "Lock Prediction ($selectedPrediction • ${selectedStake}c)"
                },
                icon = Icons.Filled.Lock,
                enabled = !isLockedWindow && lockedRoundId == null && selectedPrediction != null && coinBalance >= selectedStake,
                onClick = {
                    lockedRoundId = periodId
                },
                modifier = Modifier.fillMaxWidth().testTag("lock_prediction_button")
            )
        }

        // Instant Quick Resolve option for convenience
        if (lockedRoundId != null) {
            item {
                Button(
                    onClick = {
                        val pick = selectedPrediction ?: return@Button
                        val round = lockedRoundId ?: return@Button
                        val winningNumber = Random.nextInt(0, 10)
                        val winningColor = when {
                            winningNumber == 0 || winningNumber == 5 -> "VIOLET"
                            winningNumber % 2 == 1 -> "GREEN"
                            else -> "RED"
                        }
                        recentColorDots.add(0, winningColor)
                        if (recentColorDots.size > 12) recentColorDots.removeAt(recentColorDots.lastIndex)
                        val isWin = pick == winningColor || pick == winningNumber.toString()
                        val mult = when (pick) {
                            "VIOLET" -> 4.5
                            "GREEN", "RED" -> 2.0
                            else -> 8.0
                        }
                        val won = if (isWin) (selectedStake * mult).toLong() else 0L
                        val detail = "Result: $winningColor (#$winningNumber) • Picked $pick"
                        lastRoundBanner = if (isWin) "🎉 Won +$won coins! ($detail)" else "Round ended ($detail)"
                        onRecordGame("COLOR_PREDICTION", round, pick, selectedStake, won, if (isWin) "WIN" else "LOSS", detail)
                        selectedPrediction = null
                        lockedRoundId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reveal Round Result Now", color = NeonCyan)
                }
            }
        }

        // History
        item {
            Text("Your Recent Rounds", style = MaterialTheme.typography.titleMedium)
        }
        items(recentRecords.take(8), key = { it.id }) { rec ->
            GlassCard(contentPadding = PaddingValues(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${rec.periodId} • Pick: ${rec.selection}", style = MaterialTheme.typography.titleSmall)
                        Text(rec.detail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Text(
                        text = if (rec.outcome == "WIN") "+${rec.wonCoins}c" else "-${rec.stakeCoins}c",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (rec.outcome == "WIN") EmeraldWin else RoseMagenta,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MiniGamesCatalogGrid(
    gameRecords: List<GameRecord>,
    onLaunchGame: (String) -> Unit
) {
    val games = listOf(
        Triple("LUDO", "🎲 Ludo Quick Arena", "2-player interactive dice & token capture • +120c prize"),
        Triple("SPIN_WHEEL", "🎡 Spin Wheel", "Spin the neon prize wheel for virtual coins"),
        Triple("TIC_TAC_TOE", "❌ Tic Tac Toe", "Beat SocialVerse AI • +60c reward"),
        Triple("QUIZ", "🧠 Social Trivia Quiz", "Answer 5 rapid-fire questions • +100c reward"),
        Triple("NUMBER_GUESS", "🔢 Number Guess", "Find the secret 1–50 number in 5 tries"),
        Triple("WORD_GAME", "🔤 Word Scramble", "Unscramble entertainment & tech words"),
        Triple("MEMORY", "🃏 Memory Match", "Flip & pair all 6 neon emoji cards")
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("All Mini-Games (${gameRecords.size} rounds played)", style = MaterialTheme.typography.titleMedium)
        }
        items(games, key = { it.first }) { (id, title, desc) ->
            GlassCard(
                borderColor = ElectricViolet.copy(alpha = 0.45f),
                onClick = { onLaunchGame(id) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Spacer(Modifier.width(10.dp))
                    GradientButton(text = "Play", onClick = { onLaunchGame(id) }, modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun LeaderboardsSection(
    myProfile: UserPublicProfile,
    users: List<UserPublicProfile>,
    families: List<Family>
) {
    val categories = listOf("Popularity", "Gifts", "Hosts", "Levels", "Gaming", "Families")
    var selectedCat by remember { mutableStateOf("Popularity") }
    var periodFilter by remember { mutableStateOf("Weekly") }

    val sortedUsers = remember(users, selectedCat) {
        when (selectedCat) {
            "Gifts" -> users.sortedByDescending { it.giftsReceivedValue + it.popularityScore }
            "Levels" -> users.sortedByDescending { it.xp }
            "Hosts", "Gaming" -> users.sortedByDescending { it.popularityScore + it.level * 50 }
            else -> users.sortedByDescending { it.popularityScore }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Weekly", "Monthly").forEach { p ->
                    FilterChip(selected = periodFilter == p, onClick = { periodFilter = p }, label = { Text(p) })
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(selected = selectedCat == cat, onClick = { selectedCat = cat }, label = { Text(cat) })
                }
            }
        }

        if (selectedCat == "Families") {
            itemsIndexed(families, key = { _, f -> f.id }) { idx, fam ->
                GlassCard(borderColor = if (idx == 0) ImperialGold else GlassBorder) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("#${idx + 1}", style = MaterialTheme.typography.titleMedium, color = ImperialGold)
                            Spacer(Modifier.width(12.dp))
                            Text(fam.emblemEmoji, fontSize = 26.sp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(fam.name, style = MaterialTheme.typography.titleSmall)
                                Text("${fam.memberCount} members", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        Text("${fam.activityScore} pts", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                    }
                }
            }
        } else {
            itemsIndexed(sortedUsers, key = { _, u -> u.userId }) { idx, user ->
                val isMe = user.userId == myProfile.userId
                GlassCard(borderColor = if (idx == 0) ImperialGold else if (isMe) NeonCyan else GlassBorder) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                "#${idx + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (idx < 3) ImperialGold else TextMuted
                            )
                            Spacer(Modifier.width(10.dp))
                            AvatarWithFrame(user.avatarEmoji, user.vipTier, user.profileFrame, size = 44.dp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    if (isMe) "${user.displayName} (You)" else user.displayName,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Lv.${user.level} ${SocialVerseCatalog.levelTitle(user.level)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonCyan
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = ImperialGold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = when (selectedCat) {
                                    "Levels" -> "${user.xp} XP"
                                    else -> "${user.popularityScore} pts"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = ImperialGold
                            )
                        }
                    }
                }
            }
        }
    }
}
