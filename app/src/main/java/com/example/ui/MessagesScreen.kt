package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.Conversation
import com.example.data.DirectMessage
import com.example.data.FollowRelation
import com.example.data.PrivateCall
import com.example.data.SocialRequest
import com.example.data.SocialVerseCatalog
import com.example.data.UserPublicProfile
import com.example.data.VoiceAudioEngine
import com.example.ui.components.AvatarWithFrame
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
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
import kotlinx.coroutines.delay

@Composable
fun MessagesScreen(
    myProfile: UserPublicProfile,
    conversations: List<Conversation>,
    activeConversation: Conversation?,
    activeMessages: List<DirectMessage>,
    friendRequests: List<SocialRequest>,
    follows: List<FollowRelation>,
    discoverUsers: List<UserPublicProfile>,
    privateCalls: List<PrivateCall>,
    blockedUids: List<String>,
    onOpenConversation: (String) -> Unit,
    onCloseConversation: () -> Unit,
    onOpenDirectChat: (UserPublicProfile) -> Unit,
    onCreateGroupChat: (String, String, List<String>) -> Unit,
    onSendMessage: (Conversation, String, String, String?, String?) -> Unit,
    onReactMessage: (String, String, String) -> Unit,
    onDeleteMessage: (String, String) -> Unit,
    onSendFriendRequest: (UserPublicProfile) -> Unit,
    onRespondFriendRequest: (SocialRequest, String) -> Unit,
    onStartCall: (UserPublicProfile) -> Unit
) {
    if (activeConversation != null) {
        ChatDetailView(
            conversation = activeConversation,
            messages = activeMessages,
            myProfile = myProfile,
            onBack = onCloseConversation,
            onSend = { text, type, uri, reply -> onSendMessage(activeConversation, text, type, uri, reply) },
            onReact = { msgId, emoji -> onReactMessage(activeConversation.id, msgId, emoji) },
            onDelete = { msgId -> onDeleteMessage(activeConversation.id, msgId) }
        )
        return
    }

    var subTab by remember { mutableIntStateOf(0) } // 0 = Chats, 1 = Friends, 2 = Calls
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    val otherUsers = remember(discoverUsers, myProfile.userId, blockedUids) {
        discoverUsers.filter { it.userId != myProfile.userId && it.userId !in blockedUids }
    }
    val acceptedFriends = remember(friendRequests, otherUsers, myProfile.userId) {
        val friendUids = friendRequests.filter { it.status == "ACCEPTED" }
            .map { if (it.fromUserId == myProfile.userId) it.toUserId else it.fromUserId }
            .toSet()
        otherUsers.filter { it.userId in friendUids }
    }
    val incomingFriendReqs = remember(friendRequests, myProfile.userId) {
        friendRequests.filter { it.status == "PENDING" && it.toUserId == myProfile.userId }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Messages & Friends", style = MaterialTheme.typography.headlineMedium)
                Text("${acceptedFriends.size} friends • ${follows.size} following", style = MaterialTheme.typography.bodySmall, color = NeonCyan)
            }
            IconButton(onClick = { showCreateGroupDialog = true }) {
                Icon(Icons.Filled.GroupAdd, contentDescription = "Create Group Chat", tint = NeonCyan)
            }
        }

        TabRow(selectedTabIndex = subTab, containerColor = ObsidianSurface, contentColor = NeonCyan) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Chats (${conversations.size})") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Friends (${acceptedFriends.size})") })
            Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Calls (${privateCalls.size})") })
        }

        when (subTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (conversations.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.AutoMirrored.Filled.Send,
                                title = "No Conversations Yet",
                                description = "Open the Friends tab or tap any user on Home to start a private chat or create a group!",
                                actionLabel = "Browse People",
                                onAction = { subTab = 1 }
                            )
                        }
                    } else {
                        items(conversations, key = { it.id }) { conv ->
                            GlassCard(onClick = { onOpenConversation(conv.id) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                if (conv.type == "GROUP") "👥 ${conv.title}" else conv.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(conv.lastMessage, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                                    }
                                    Text("Open →", color = NeonCyan, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (incomingFriendReqs.isNotEmpty()) {
                        item { Text("Pending Requests", style = MaterialTheme.typography.titleMedium, color = ImperialGold) }
                        items(incomingFriendReqs, key = { it.id }) { req ->
                            GlassCard(borderColor = ImperialGold) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("${req.fromAvatar} ${req.fromName}", style = MaterialTheme.typography.titleSmall)
                                    Row {
                                        IconButton(onClick = { onRespondFriendRequest(req, "ACCEPTED") }) {
                                            Icon(Icons.Filled.Check, contentDescription = "Accept", tint = EmeraldWin)
                                        }
                                        IconButton(onClick = { onRespondFriendRequest(req, "REJECTED") }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Reject", tint = RoseMagenta)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Text("Friends", style = MaterialTheme.typography.titleMedium) }
                    if (acceptedFriends.isEmpty()) {
                        item {
                            Text("No accepted friends yet. Send a friend request below!", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    } else {
                        items(acceptedFriends, key = { "fr_${it.userId}" }) { f ->
                            GlassCard {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        AvatarWithFrame(f.avatarEmoji, f.vipTier, f.profileFrame, size = 44.dp, isOnline = f.onlineVisible && f.onlineStatus == "ONLINE")
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(f.displayName, style = MaterialTheme.typography.titleSmall)
                                            Text("Lv.${f.level}", style = MaterialTheme.typography.bodySmall, color = NeonCyan)
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = { onOpenDirectChat(f) }) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Message", tint = NeonCyan)
                                        }
                                        IconButton(onClick = { onStartCall(f) }) {
                                            Icon(Icons.Filled.Call, contentDescription = "Voice Call", tint = EmeraldWin)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Text("Discover People", style = MaterialTheme.typography.titleMedium) }
                    items(otherUsers, key = { "disc_${it.userId}" }) { user ->
                        GlassCard {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    AvatarWithFrame(user.avatarEmoji, user.vipTier, user.profileFrame, size = 42.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(user.displayName, style = MaterialTheme.typography.titleSmall)
                                        Text("@${user.username} • ${user.country}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { onSendFriendRequest(user) }) {
                                        Icon(Icons.Filled.PersonAdd, contentDescription = "Add Friend", tint = ImperialGold)
                                    }
                                    IconButton(onClick = { onOpenDirectChat(user) }) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Chat", tint = NeonCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (privateCalls.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.Call,
                                title = "No Call History",
                                description = "Tap the Call icon next to any friend or profile to start a private 1-on-1 voice call."
                            )
                        }
                    } else {
                        items(privateCalls, key = { it.id }) { call ->
                            val isOutgoing = call.callerId == myProfile.userId
                            val peerName = if (isOutgoing) call.calleeName else call.callerName
                            GlassCard {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("${if (isOutgoing) "↗ Outgoing" else "↙ Incoming"} • $peerName", style = MaterialTheme.typography.titleSmall)
                                        Text("${call.status} • ${call.durationSeconds}s", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    Icon(
                                        Icons.Filled.Call,
                                        contentDescription = null,
                                        tint = if (call.status == "ENDED" || call.status == "ACTIVE") EmeraldWin else RoseMagenta
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateGroupDialog) {
        var groupTitle by remember { mutableStateOf("") }
        var groupAnn by remember { mutableStateOf("") }
        val selectedUids = remember { mutableStateListOf<String>() }

        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("Create Group Chat") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = groupTitle, onValueChange = { groupTitle = it }, label = { Text("Group Name") })
                    OutlinedTextField(value = groupAnn, onValueChange = { groupAnn = it }, label = { Text("Announcement") })
                    Text("Select Members:", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                    LazyColumn(modifier = Modifier.height(160.dp)) {
                        items(otherUsers, key = { it.userId }) { u ->
                            val checked = u.userId in selectedUids
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (checked) selectedUids.remove(u.userId) else selectedUids.add(u.userId)
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = checked, onCheckedChange = {
                                    if (it) selectedUids.add(u.userId) else selectedUids.remove(u.userId)
                                })
                                Text("${u.avatarEmoji} ${u.displayName}")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedUids.isNotEmpty() && groupTitle.isNotBlank(),
                    onClick = {
                        onCreateGroupChat(groupTitle, groupAnn, selectedUids.toList())
                        showCreateGroupDialog = false
                    }
                ) { Text("Create Group") }
            },
            dismissButton = { TextButton(onClick = { showCreateGroupDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ChatDetailView(
    conversation: Conversation,
    messages: List<DirectMessage>,
    myProfile: UserPublicProfile,
    onBack: () -> Unit,
    onSend: (text: String, type: String, mediaUri: String?, replyTo: String?) -> Unit,
    onReact: (messageId: String, emoji: String) -> Unit,
    onDelete: (messageId: String) -> Unit
) {
    BackHandler { onBack() }

    var input by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<DirectMessage?>(null) }
    var showStickers by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onSend("📷 Shared a photo", "IMAGE", uri.toString(), replyTarget?.text)
                replyTarget = null
            }
        }
    )

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(conversation.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (conversation.announcement.isNotBlank()) {
                    Text(conversation.announcement, style = MaterialTheme.typography.bodySmall, color = NeonCyan, maxLines = 1)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderId == myProfile.userId
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isMine) ElectricViolet.copy(alpha = 0.35f) else ObsidianElevated,
                        border = BorderStroke(1.dp, if (isMine) NeonCyan.copy(alpha = 0.4f) else GlassBorder),
                        modifier = Modifier.clickable { replyTarget = msg }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (!isMine) {
                                Text(msg.senderName, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                            }
                            if (msg.replyToText.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.25f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        "↩ ${msg.replyToText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        modifier = Modifier.padding(6.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                            if (msg.messageType == "IMAGE" && msg.mediaUri.isNotBlank() && !msg.deleted) {
                                AsyncImage(
                                    model = msg.mediaUri,
                                    contentDescription = "Shared photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(160.dp).clip(RoundedCornerShape(10.dp))
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            Text(
                                text = msg.text,
                                style = if (msg.messageType == "STICKER") MaterialTheme.typography.displaySmall else MaterialTheme.typography.bodyMedium,
                                color = if (msg.deleted) TextMuted else Color.White
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (msg.reaction.isNotBlank()) {
                                    Text(msg.reaction, fontSize = 14.sp)
                                }
                                Text(
                                    "👍",
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable { onReact(msg.id, "❤️") }
                                )
                                Icon(
                                    Icons.Filled.Reply,
                                    contentDescription = "Reply",
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp).clickable { replyTarget = msg }
                                )
                                if (isMine && !msg.deleted) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = RoseMagenta,
                                        modifier = Modifier.size(14.dp).clickable { onDelete(msg.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (replyTarget != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                color = ObsidianElevated,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Replying to: ${replyTarget?.text}", style = MaterialTheme.typography.bodySmall, maxLines = 1, modifier = Modifier.weight(1f))
                    IconButton(onClick = { replyTarget = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear Reply")
                    }
                }
            }
        }

        if (showStickers) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SocialVerseCatalog.stickers) { st ->
                    Text(
                        st,
                        fontSize = 28.sp,
                        modifier = Modifier.clickable {
                            onSend(st, "STICKER", null, replyTarget?.text)
                            replyTarget = null
                            showStickers = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = { showStickers = !showStickers }) {
                Icon(Icons.Filled.EmojiEmotions, contentDescription = "Stickers", tint = ImperialGold)
            }
            IconButton(onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Icon(Icons.Filled.Image, contentDescription = "Pick Photo", tint = NeonCyan)
            }
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Message...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                if (input.isNotBlank()) {
                    onSend(input, "TEXT", null, replyTarget?.text)
                    input = ""
                    replyTarget = null
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = NeonCyan)
            }
        }
    }
}

@Composable
fun PrivateCallOverlay(
    call: PrivateCall,
    myUid: String,
    audioEngine: VoiceAudioEngine,
    onUpdateCall: (status: String, durationSeconds: Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isCaller = call.callerId == myUid
    val peerName = if (isCaller) call.calleeName else call.callerName

    var micMuted by remember { mutableStateOf(false) }
    var elapsedSec by remember { mutableLongStateOf(0L) }
    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
        if (granted && !micMuted && call.status == "ACTIVE") {
            audioEngine.startCapture(scope, true)
        }
    }

    LaunchedEffect(call.status, micMuted, hasMicPermission) {
        if (call.status == "ACTIVE" && !micMuted && hasMicPermission) {
            audioEngine.startCapture(scope, true)
        } else {
            audioEngine.stopCapture()
        }
    }

    LaunchedEffect(call.status) {
        if (call.status == "ACTIVE") {
            while (true) {
                delay(1000L)
                elapsedSec++
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { audioEngine.stopCapture() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ObsidianDeep, Color(0xFF140D32), ObsidianBg))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = when (call.status) {
                    "RINGING" -> if (isCaller) "Calling..." else "Incoming Private Voice Call"
                    "ACTIVE" -> "Connected • %02d:%02d".format(elapsedSec / 60, elapsedSec % 60)
                    else -> call.status
                },
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            Box(
                modifier = Modifier.size(110.dp).clip(CircleShape).background(ElectricViolet.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎧", fontSize = 52.sp)
            }
            Text(peerName, style = MaterialTheme.typography.headlineMedium, color = Color.White)

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                if (!isCaller && call.status == "RINGING") {
                    Button(
                        onClick = { onUpdateCall("ACTIVE", 0L) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldWin),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "Accept")
                    }
                    Button(
                        onClick = { onUpdateCall("DECLINED", 0L) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseMagenta),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Filled.CallEnd, contentDescription = "Decline")
                    }
                } else {
                    Button(
                        onClick = {
                            if (!hasMicPermission) permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            else micMuted = !micMuted
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianElevated),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(if (micMuted) Icons.Filled.MicOff else Icons.Filled.Mic, contentDescription = "Mute")
                    }
                    Button(
                        onClick = { onUpdateCall("ENDED", elapsedSec) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseMagenta),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Filled.CallEnd, contentDescription = "End Call")
                    }
                }
            }
        }
    }
}
