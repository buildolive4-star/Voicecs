package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SocialVerseCatalog
import com.example.data.SpeakerRequest
import com.example.data.UserPublicProfile
import com.example.data.VoiceAudioEngine
import com.example.data.VoiceRoom
import com.example.data.VoiceRoomMessage
import com.example.data.VoiceRoomSeat
import com.example.data.VirtualGift
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.components.SafetyReportDialog
import com.example.ui.theme.CyanDeep
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.RoseMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLobbyScreen(
    myProfile: UserPublicProfile,
    roomsState: UiState<List<VoiceRoom>>,
    onJoinRoom: (VoiceRoom) -> Unit,
    onCreateRoom: (
        title: String,
        category: String,
        roomType: String,
        passwordCode: String,
        language: String,
        announcement: String,
        themeKey: String,
        maxSeats: Int,
        adultOnly: Boolean
    ) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var showCreateSheet by remember { mutableStateOf(false) }
    var passwordPromptRoom by remember { mutableStateOf<VoiceRoom?>(null) }

    val filterTabs = listOf("All", "Public", "Music", "Friendship", "Gaming", "Password", "VIP")
    val rooms = (roomsState as? UiState.Success)?.data.orEmpty()
    val filteredRooms = remember(rooms, selectedFilter) {
        when (selectedFilter) {
            "All" -> rooms
            "Public" -> rooms.filter { it.roomType == "PUBLIC" }
            "Password" -> rooms.filter { it.roomType == "PASSWORD" }
            "VIP" -> rooms.filter { it.roomType == "VIP" }
            else -> rooms.filter { it.category.equals(selectedFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = ElectricViolet,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_voice_room_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Host Voice Room")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Live Voice Rooms", style = MaterialTheme.typography.headlineMedium)
                Text("Join 8-seat audio stages or launch your own club", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterTabs) { tab ->
                        FilterChip(
                            selected = selectedFilter == tab,
                            onClick = { selectedFilter = tab },
                            label = { Text(tab) }
                        )
                    }
                }
            }
            if (filteredRooms.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Filled.GraphicEq,
                        title = "Start a Live Voice Stage",
                        description = "Tap below to host a Public, Password, Friends-Only, or VIP voice room with up to 12 mic seats.",
                        actionLabel = "Host Room",
                        onAction = { showCreateSheet = true }
                    )
                }
            } else {
                items(filteredRooms, key = { it.id }) { room ->
                    VoiceRoomCard(
                        room = room,
                        onClick = {
                            if (room.roomType == "PASSWORD" && room.passwordCode.isNotBlank() && room.hostId != myProfile.userId) {
                                passwordPromptRoom = room
                            } else {
                                onJoinRoom(room)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateVoiceRoomSheet(
            myProfile = myProfile,
            onDismiss = { showCreateSheet = false },
            onCreate = { title, cat, rType, pass, lang, ann, theme, seats, adult ->
                showCreateSheet = false
                onCreateRoom(title, cat, rType, pass, lang, ann, theme, seats, adult)
            }
        )
    }

    passwordPromptRoom?.let { room ->
        var entered by remember { mutableStateOf("") }
        var wrong by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { passwordPromptRoom = null },
            containerColor = ObsidianSurface,
            title = { Text("Password Required") },
            text = {
                Column {
                    OutlinedTextField(
                        value = entered,
                        onValueChange = { entered = it; wrong = false },
                        label = { Text("Room Password") },
                        isError = wrong
                    )
                    if (wrong) Text("Invalid password", color = RoseMagenta, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (entered.trim() == room.passwordCode) {
                        passwordPromptRoom = null
                        onJoinRoom(room)
                    } else wrong = true
                }) { Text("Join Room") }
            },
            dismissButton = {
                TextButton(onClick = { passwordPromptRoom = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoiceRoomSheet(
    myProfile: UserPublicProfile,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, String, String, Int, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("${myProfile.displayName}'s Lounge") }
    var category by remember { mutableStateOf("Music") }
    var roomType by remember { mutableStateOf("PUBLIC") }
    var password by remember { mutableStateOf("") }
    var language by remember { mutableStateOf(myProfile.language.ifBlank { "English" }) }
    var announcement by remember { mutableStateOf("Welcome! Grab a mic seat and vibe together.") }
    var themeKey by remember { mutableStateOf(SocialVerseCatalog.roomThemes.first()) }
    var maxSeats by remember { mutableIntStateOf(8) }
    var adultOnly by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ObsidianSurface) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item { Text("Host Live Voice Room", style = MaterialTheme.typography.headlineSmall) }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Room Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("create_room_title_input")
                )
            }
            item {
                Text("Category", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Music", "Friendship", "Entertainment", "Gaming", "International")) { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                    }
                }
            }
            item {
                Text("Room Privacy Type", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("PUBLIC", "PRIVATE", "PASSWORD", "FRIENDS", "VIP")) { t ->
                        FilterChip(selected = roomType == t, onClick = { roomType = t }, label = { Text(t) })
                    }
                }
            }
            if (roomType == "PASSWORD") {
                item {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Room Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                Text("Mic Capacity", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(4, 8, 12).forEach { count ->
                        FilterChip(selected = maxSeats == count, onClick = { maxSeats = count }, label = { Text("$count Mics") })
                    }
                }
            }
            item {
                Text("Room Theme", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SocialVerseCatalog.roomThemes) { th ->
                        FilterChip(selected = themeKey == th, onClick = { themeKey = th }, label = { Text(th.replace("_", " ")) })
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = language,
                        onValueChange = { language = it },
                        label = { Text("Language") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = announcement,
                        onValueChange = { announcement = it },
                        label = { Text("Announcement") },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
            if (myProfile.adultEligible) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("18+ Mature Lounge", style = MaterialTheme.typography.titleSmall)
                            Text("Hidden from accounts under 18", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Switch(checked = adultOnly, onCheckedChange = { adultOnly = it })
                    }
                }
            }
            item {
                GradientButton(
                    text = "Launch Live Room",
                    onClick = { onCreate(title, category, roomType, password, language, announcement, themeKey, maxSeats, adultOnly) },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_create_room_button")
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRoomScreen(
    room: VoiceRoom,
    seats: List<VoiceRoomSeat>,
    messages: List<VoiceRoomMessage>,
    speakerRequests: List<SpeakerRequest>,
    myProfile: UserPublicProfile,
    coinBalance: Long,
    audioEngine: VoiceAudioEngine,
    onLeaveRoom: () -> Unit,
    onCloseRoom: () -> Unit,
    onTakeSeat: (VoiceRoomSeat) -> Unit,
    onStepDownSeat: (VoiceRoomSeat) -> Unit,
    onToggleSeatMute: (VoiceRoomSeat) -> Unit,
    onToggleSeatLock: (VoiceRoomSeat) -> Unit,
    onRequestMic: () -> Unit,
    onApproveRequest: (SpeakerRequest, Boolean) -> Unit,
    onSendChat: (String) -> Unit,
    onSendGift: (recipientId: String, recipientName: String, gift: VirtualGift) -> Unit,
    onUpdateRoomSettings: (String, String, String, String, String) -> Unit,
    onSubmitReport: (String, String) -> Unit
) {
    BackHandler { onLeaveRoom() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ampLevel by audioEngine.amplitudeLevel.collectAsStateWithLifecycle()
    val isHostOrMod = room.hostId == myProfile.userId || myProfile.userId in room.moderatorUids
    val mySeat = seats.firstOrNull { it.occupantUid == myProfile.userId }

    var speakerOutputMuted by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showRequestsSheet by remember { mutableStateOf(false) }
    var showHostSettings by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var seatActionTarget by remember { mutableStateOf<VoiceRoomSeat?>(null) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted && mySeat != null && !mySeat.muted) {
            audioEngine.startCapture(scope, true)
        }
    }

    LaunchedEffect(mySeat?.occupantUid, mySeat?.muted, hasMicPermission) {
        if (mySeat != null && !mySeat.muted && hasMicPermission) {
            audioEngine.startCapture(scope, true)
        } else {
            audioEngine.stopCapture()
        }
    }
    DisposableEffect(Unit) {
        onDispose { audioEngine.stopCapture() }
    }

    val roomGradient = when (room.themeKey) {
        "CYBER_CYAN" -> listOf(ObsidianDeep, Color(0xFF082032), ObsidianBg)
        "IMPERIAL_GOLD" -> listOf(ObsidianDeep, Color(0xFF261A0B), ObsidianBg)
        "MIDNIGHT_ROSE" -> listOf(ObsidianDeep, Color(0xFF2A0C22), ObsidianBg)
        else -> listOf(ObsidianDeep, Color(0xFF1A0F3B), ObsidianBg)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(roomGradient))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Room Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = onLeaveRoom) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Leave Room")
                }
                Column {
                    Text(room.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${room.category} • ${room.participantCount} online • ${room.roomType}",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isHostOrMod) {
                    IconButton(onClick = { showRequestsSheet = true }) {
                        BadgedBox(badge = {
                            if (speakerRequests.isNotEmpty()) Badge(containerColor = RoseMagenta) { Text("${speakerRequests.size}") }
                        }) {
                            Icon(Icons.Filled.PanTool, contentDescription = "Speaker Requests", tint = ImperialGold)
                        }
                    }
                    IconButton(onClick = { showHostSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Room Settings")
                    }
                }
                IconButton(onClick = { showReportDialog = true }) {
                    Icon(Icons.Filled.Flag, contentDescription = "Report Room", tint = TextMuted)
                }
                IconButton(onClick = if (room.hostId == myProfile.userId) onCloseRoom else onLeaveRoom) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit", tint = RoseMagenta)
                }
            }
        }

        // Announcement Banner
        if (room.announcement.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = ObsidianSurface.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Text(
                    text = "📢 ${room.announcement}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 8-Seat Microphone Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().height(216.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(seats, key = { it.id }) { seat ->
                val isMine = seat.occupantUid == myProfile.userId
                val livePulse by animateFloatAsState(
                    targetValue = if (isMine && !seat.muted && hasMicPermission) 1f + (ampLevel * 0.18f) else 1f,
                    label = "micPulse"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        when {
                            seat.occupantUid.isBlank() -> {
                                if (isHostOrMod) seatActionTarget = seat else onTakeSeat(seat)
                            }
                            isMine || isHostOrMod -> seatActionTarget = seat
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .scale(livePulse)
                            .clip(CircleShape)
                            .background(ObsidianElevated.copy(alpha = 0.9f))
                            .border(
                                width = 2.dp,
                                color = when {
                                    seat.occupantUid.isNotBlank() && !seat.muted -> NeonCyan
                                    seat.locked -> RoseMagenta.copy(alpha = 0.7f)
                                    else -> GlassBorder
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            seat.occupantUid.isNotBlank() -> Text(seat.occupantAvatar.ifBlank { "🌟" }, fontSize = 24.sp)
                            seat.locked -> Icon(Icons.Filled.Lock, contentDescription = "Locked Seat", tint = RoseMagenta, modifier = Modifier.size(20.dp))
                            else -> Icon(Icons.Filled.Mic, contentDescription = "Empty Seat", tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                        if (seat.occupantUid.isNotBlank() && seat.muted) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(RoseMagenta),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.MicOff, contentDescription = "Muted", tint = Color.White, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when {
                            seat.occupantUid == room.hostId -> "👑 ${seat.occupantName}"
                            seat.occupantUid.isNotBlank() -> seat.occupantName
                            seat.locked -> "Locked"
                            else -> "Seat ${seat.seatIndex + 1}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (seat.occupantUid.isNotBlank()) Color.White else TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Live Room Chat Stream
        GlassCard(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(10.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val bgColor = when (msg.messageType) {
                        "GIFT" -> ImperialGold.copy(alpha = 0.2f)
                        "SYSTEM" -> ElectricViolet.copy(alpha = 0.16f)
                        else -> ObsidianElevated.copy(alpha = 0.85f)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = if (msg.messageType == "GIFT") BorderStroke(1.dp, ImperialGold.copy(alpha = 0.6f)) else null
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            if (msg.senderVip != "NONE" && msg.messageType == "TEXT") {
                                Text("👑 ", fontSize = 12.sp)
                            }
                            Text(
                                text = if (msg.messageType == "SYSTEM") msg.text else "${msg.senderName}: ${msg.text}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (msg.messageType == "GIFT") ImperialGold else Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Bottom Controls Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Say something...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                onSendChat(chatInput)
                chatInput = ""
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Chat", tint = NeonCyan)
            }
            if (mySeat != null) {
                IconButton(
                    onClick = {
                        if (!hasMicPermission && mySeat.muted) {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else if (!hasMicPermission) {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            onToggleSeatMute(mySeat)
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (mySeat.muted || !hasMicPermission) RoseMagenta.copy(alpha = 0.25f) else EmeraldWin.copy(alpha = 0.25f))
                ) {
                    Icon(
                        if (mySeat.muted || !hasMicPermission) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = "Toggle Mic",
                        tint = if (mySeat.muted || !hasMicPermission) RoseMagenta else EmeraldWin
                    )
                }
            } else {
                IconButton(
                    onClick = onRequestMic,
                    modifier = Modifier.clip(CircleShape).background(ElectricViolet.copy(alpha = 0.25f))
                ) {
                    Icon(Icons.Filled.PanTool, contentDescription = "Request Mic", tint = NeonCyan)
                }
            }
            IconButton(
                onClick = { speakerOutputMuted = !speakerOutputMuted },
                modifier = Modifier.clip(CircleShape).background(ObsidianElevated)
            ) {
                Icon(
                    if (speakerOutputMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Speaker Mute",
                    tint = if (speakerOutputMuted) RoseMagenta else TextSecondary
                )
            }
            IconButton(
                onClick = { showGiftSheet = true },
                modifier = Modifier.clip(CircleShape).background(ImperialGold.copy(alpha = 0.25f))
            ) {
                Icon(Icons.Filled.CardGiftcard, contentDescription = "Send Gift", tint = ImperialGold)
            }
        }
    }

    // Seat Options Dialog (Step down, Host Mute/Lock/Kick)
    seatActionTarget?.let { targetSeat ->
        AlertDialog(
            onDismissRequest = { seatActionTarget = null },
            containerColor = ObsidianSurface,
            title = { Text("Mic Seat #${targetSeat.seatIndex + 1}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (targetSeat.occupantUid.isNotBlank()) {
                        Text("Speaker: ${targetSeat.occupantName}", color = NeonCyan)
                    } else {
                        Text("Empty seat", color = TextSecondary)
                    }
                    if (targetSeat.occupantUid == myProfile.userId) {
                        Button(onClick = {
                            if (!hasMicPermission) micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            else onToggleSeatMute(targetSeat)
                            seatActionTarget = null
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (targetSeat.muted) "Unmute My Mic" else "Mute My Mic")
                        }
                        Button(
                            onClick = { onStepDownSeat(targetSeat); seatActionTarget = null },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseMagenta),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Leave Mic Seat")
                        }
                    } else if (isHostOrMod) {
                        if (targetSeat.occupantUid.isBlank() && !targetSeat.locked) {
                            Button(onClick = { onTakeSeat(targetSeat); seatActionTarget = null }, modifier = Modifier.fillMaxWidth()) {
                                Text("Sit on Mic #${targetSeat.seatIndex + 1}")
                            }
                        }
                        if (targetSeat.occupantUid.isNotBlank()) {
                            Button(onClick = { onToggleSeatMute(targetSeat); seatActionTarget = null }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (targetSeat.muted) "Unmute Speaker" else "Mute Speaker")
                            }
                            Button(
                                onClick = { onStepDownSeat(targetSeat); seatActionTarget = null },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseMagenta),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Remove Speaker")
                            }
                        }
                        Button(onClick = { onToggleSeatLock(targetSeat); seatActionTarget = null }, modifier = Modifier.fillMaxWidth()) {
                            Icon(if (targetSeat.locked) Icons.Filled.LockOpen else Icons.Filled.Lock, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (targetSeat.locked) "Unlock Seat" else "Lock Seat")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { seatActionTarget = null }) { Text("Done") } }
        )
    }

    // Virtual Gift Panel Bottom Sheet
    if (showGiftSheet) {
        val speakers = seats.filter { it.occupantUid.isNotBlank() }
        var selectedRecipientUid by remember { mutableStateOf(room.hostId) }
        var selectedRecipientName by remember { mutableStateOf(room.hostName) }
        var selectedCategory by remember { mutableStateOf("All") }
        val giftCategories = listOf("All", "Basic", "Romantic", "Luxury", "Premium", "Special")
        val shownGifts = SocialVerseCatalog.gifts.filter { selectedCategory == "All" || it.category == selectedCategory }

        ModalBottomSheet(onDismissRequest = { showGiftSheet = false }, containerColor = ObsidianSurface) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Send Virtual Gift", style = MaterialTheme.typography.headlineSmall)
                    Text("Balance: %,d coins".format(coinBalance), style = MaterialTheme.typography.labelSmall, color = ImperialGold)
                }
                Text("Recipient:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(speakers.ifEmpty {
                        listOf(VoiceRoomSeat(roomId = room.id, occupantUid = room.hostId, occupantName = room.hostName))
                    }, key = { it.occupantUid }) { s ->
                        FilterChip(
                            selected = selectedRecipientUid == s.occupantUid,
                            onClick = {
                                selectedRecipientUid = s.occupantUid
                                selectedRecipientName = s.occupantName
                            },
                            label = { Text(s.occupantName) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(giftCategories) { cat ->
                        FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) })
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(210.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(shownGifts, key = { it.id }) { gift ->
                        GlassCard(
                            contentPadding = PaddingValues(8.dp),
                            borderColor = if (gift.vipOnly) ImperialGold else GlassBorder,
                            onClick = {
                                showGiftSheet = false
                                onSendGift(selectedRecipientUid, selectedRecipientName, gift)
                            }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(gift.emoji, fontSize = 26.sp)
                                Text(gift.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${gift.coinCost}c", style = MaterialTheme.typography.labelSmall, color = ImperialGold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Speaker Requests Sheet (Host/Mod)
    if (showRequestsSheet) {
        ModalBottomSheet(onDismissRequest = { showRequestsSheet = false }, containerColor = ObsidianSurface) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Microphone Requests", style = MaterialTheme.typography.headlineSmall)
                if (speakerRequests.isEmpty()) {
                    Text("No pending speaker requests.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                } else {
                    speakerRequests.forEach { req ->
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${req.requesterAvatar} ${req.requesterName}", style = MaterialTheme.typography.titleSmall)
                                Row {
                                    IconButton(onClick = { onApproveRequest(req, true) }) {
                                        Icon(Icons.Filled.Check, contentDescription = "Approve", tint = EmeraldWin)
                                    }
                                    IconButton(onClick = { onApproveRequest(req, false) }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Reject", tint = RoseMagenta)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Host Settings Dialog
    if (showHostSettings) {
        var newTitle by remember { mutableStateOf(room.title) }
        var newAnn by remember { mutableStateOf(room.announcement) }
        var newTheme by remember { mutableStateOf(room.themeKey) }
        var newType by remember { mutableStateOf(room.roomType) }
        var newPass by remember { mutableStateOf(room.passwordCode) }
        AlertDialog(
            onDismissRequest = { showHostSettings = false },
            containerColor = ObsidianSurface,
            title = { Text("Room Host Controls") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Title") })
                    OutlinedTextField(value = newAnn, onValueChange = { newAnn = it }, label = { Text("Announcement") })
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SocialVerseCatalog.roomThemes) { th ->
                            FilterChip(selected = newTheme == th, onClick = { newTheme = th }, label = { Text(th.take(8)) })
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("PUBLIC", "PRIVATE", "PASSWORD", "VIP")) { rt ->
                            FilterChip(selected = newType == rt, onClick = { newType = rt }, label = { Text(rt) })
                        }
                    }
                    if (newType == "PASSWORD") {
                        OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("Password") })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateRoomSettings(newTitle, newAnn, newTheme, newType, newPass)
                    showHostSettings = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = onCloseRoom) { Text("Close Room", color = RoseMagenta) }
            }
        )
    }

    if (showReportDialog) {
        SafetyReportDialog(
            targetTitle = room.title,
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, details ->
                showReportDialog = false
                onSubmitReport(reason, details)
            }
        )
    }
}
