package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Family
import com.example.data.FollowRelation
import com.example.data.SocialVerseCatalog
import com.example.data.UserNotification
import com.example.data.UserPrivateAccount
import com.example.data.UserPublicProfile
import com.example.data.VoiceRoom
import com.example.ui.components.AvatarWithFrame
import com.example.ui.components.CoinBalancePill
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.components.SafetyReportDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.VipAndLevelBadges
import com.example.ui.theme.CyanDeep
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.RoseMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    myProfile: UserPublicProfile,
    myPrivate: UserPrivateAccount?,
    roomsState: UiState<List<VoiceRoom>>,
    discoverUsers: List<UserPublicProfile>,
    families: List<Family>,
    notifications: List<UserNotification>,
    follows: List<FollowRelation>,
    onClaimDaily: () -> Unit,
    onJoinRoom: (VoiceRoom) -> Unit,
    onCreateRoomClick: () -> Unit,
    onOpenGamesTab: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenProfileTab: () -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onOpenChatWithUser: (UserPublicProfile) -> Unit,
    onStartCallWithUser: (UserPublicProfile) -> Unit,
    onSendFriendRequest: (UserPublicProfile) -> Unit,
    onSendCpRequest: (UserPublicProfile) -> Unit,
    onToggleFollow: (UserPublicProfile) -> Unit,
    onToggleBlock: (String, Boolean) -> Unit,
    onSendGiftToUser: (UserPublicProfile, com.example.data.VirtualGift) -> Unit,
    onJoinFamily: (Family) -> Unit,
    onSubmitReport: (String, String, String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showNotifSheet by remember { mutableStateOf(false) }
    var inspectedUser by remember { mutableStateOf<UserPublicProfile?>(null) }
    var pendingPasswordRoom by remember { mutableStateOf<VoiceRoom?>(null) }

    val unreadCount = notifications.count { !it.read }
    val allRooms = (roomsState as? UiState.Success)?.data.orEmpty()
    val filteredRooms = remember(allRooms, selectedCategory) {
        when (selectedCategory) {
            "All", "Popular" -> allRooms
            "New" -> allRooms.sortedByDescending { it.createdAt?.seconds ?: 0L }
            else -> allRooms.filter {
                it.category.equals(selectedCategory, ignoreCase = true) ||
                    it.language.equals(selectedCategory, ignoreCase = true)
            }
        }
    }
    val otherUsers = remember(discoverUsers, myProfile.userId) {
        discoverUsers.filter { it.userId != myProfile.userId }
            .sortedByDescending { it.popularityScore }
    }
    val vipUsers = remember(discoverUsers) {
        discoverUsers.filter { it.vipTier != "NONE" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(ElectricViolet, NeonCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.GraphicEq, contentDescription = "SocialVerse", tint = Color.White)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("SocialVerse", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text(
                            "Lv.${myProfile.level} ${SocialVerseCatalog.levelTitle(myProfile.level)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { showSearchSheet = true },
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Global Search", tint = TextSecondary)
                    }
                    IconButton(
                        onClick = { showNotifSheet = true },
                        modifier = Modifier.testTag("home_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = RoseMagenta) {
                                        Text(unreadCount.coerceAtMost(99).toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = TextSecondary)
                        }
                    }
                    CoinBalancePill(
                        coins = myPrivate?.coinBalance ?: 0L,
                        onClick = onOpenWallet
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(modifier = Modifier.clickable(onClick = onOpenProfileTab)) {
                        AvatarWithFrame(
                            emoji = myProfile.avatarEmoji,
                            vipTier = myProfile.vipTier,
                            profileFrame = myProfile.profileFrame,
                            size = 38.dp,
                            isOnline = myProfile.onlineVisible
                        )
                    }
                }
            }
        }

        // Hero Banner & Featured Events
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onCreateRoomClick)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "SocialVerse Live Stage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ObsidianBg.copy(alpha = 0.92f), ObsidianBg.copy(alpha = 0.35f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(18.dp)
                        .fillMaxWidth(0.75f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = RoseMagenta.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, RoseMagenta)
                    ) {
                        Text(
                            "LIVE ENTERTAINMENT",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFDA4AF),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Host Your Voice Stage", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(
                        "8 mic seats, virtual gifts, live chat & community XP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    GradientButton(
                        text = "Go Live Now",
                        icon = Icons.Filled.Add,
                        onClick = onCreateRoomClick,
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }

        // Daily Check-in & Activities
        item {
            GlassCard(
                borderColor = ImperialGold.copy(alpha = 0.45f),
                containerColor = ObsidianElevated.copy(alpha = 0.92f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Check-In & XP Boost", style = MaterialTheme.typography.titleMedium, color = ImperialGold)
                        Text(
                            if (myProfile.vipTier == "SVIP") "SVIP 2x Bonus: +300 Virtual Coins & +50 XP"
                            else "Claim +150 Virtual Coins & +50 XP today",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onClaimDaily,
                        colors = ButtonDefaults.buttonColors(containerColor = ImperialGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("daily_checkin_button")
                    ) {
                        Text("Claim", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Category Filters
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SocialVerseCatalog.roomCategories) { category ->
                    val selected = selectedCategory == category
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricViolet,
                            selectedLabelColor = Color.White,
                            containerColor = ObsidianSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = GlassBorder,
                            selectedBorderColor = NeonCyan
                        )
                    )
                }
            }
        }

        // Recommended & Trending Rooms
        item {
            SectionHeader(
                title = "Live Voice Rooms",
                subtitle = "${filteredRooms.size} active rooms right now",
                actionLabel = "+ Host Room",
                onAction = onCreateRoomClick
            )
        }

        if (filteredRooms.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.GraphicEq,
                    title = "No Live Rooms in $selectedCategory Yet",
                    description = "Be the first host on stage! Open an 8-seat room for music, gaming, or chill chat.",
                    actionLabel = "Create Voice Room",
                    onAction = onCreateRoomClick
                )
            }
        } else {
            items(filteredRooms, key = { it.id }) { room ->
                VoiceRoomCard(
                    room = room,
                    onClick = {
                        if (room.roomType == "PASSWORD" && room.passwordCode.isNotBlank() && room.hostId != myProfile.userId) {
                            pendingPasswordRoom = room
                        } else {
                            onJoinRoom(room)
                        }
                    }
                )
            }
        }

        // Featured Events
        item {
            SectionHeader(title = "Featured Events", subtitle = "Compete for badges, XP & leaderboard glory")
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(SocialVerseCatalog.communityEvents, key = { it.id }) { evt ->
                    GlassCard(
                        modifier = Modifier.width(248.dp),
                        borderColor = NeonCyan.copy(alpha = 0.35f),
                        onClick = {
                            if (evt.id == "evt_color_arena") onOpenGamesTab() else onCreateRoomClick()
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(evt.badgeEmoji, fontSize = 28.sp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(evt.category, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                                Text(evt.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(evt.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElectricViolet.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = evt.rewardLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = ImperialGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Popular Hosts & Friends Online
        item {
            SectionHeader(
                title = "Popular Hosts & Community",
                subtitle = if (vipUsers.isNotEmpty()) "${vipUsers.size} VIP / SVIP members active" else "Connect, follow & send gifts"
            )
        }
        if (otherUsers.isEmpty()) {
            item {
                GlassCard {
                    Text(
                        "You're pioneering this SocialVerse space! Invite friends to join via Google Sign-In to appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(otherUsers, key = { it.userId }) { user ->
                        GlassCard(
                            modifier = Modifier.width(156.dp),
                            onClick = { inspectedUser = user }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarWithFrame(
                                    emoji = user.avatarEmoji,
                                    vipTier = user.vipTier,
                                    profileFrame = user.profileFrame,
                                    size = 56.dp,
                                    isOnline = user.onlineVisible && user.onlineStatus == "ONLINE"
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(user.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = TextMuted, maxLines = 1)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Lv.${user.level} • ${user.country}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeonCyan,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Popular Families
        item {
            SectionHeader(title = "Popular Families", subtitle = "Join a community clan & climb rankings")
        }
        if (families.isEmpty()) {
            item {
                GlassCard {
                    Text("No families formed yet. Create your own Family clan in your Profile tab!", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(families, key = { it.id }) { fam ->
                        GlassCard(
                            modifier = Modifier.width(210.dp),
                            onClick = { onJoinFamily(fam) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(fam.emblemEmoji, fontSize = 28.sp)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fam.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${fam.memberCount} members • ${fam.activityScore} pts", style = MaterialTheme.typography.bodySmall, color = NeonCyan)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(fam.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Recommended Mini-Games Card
        item {
            GlassCard(
                borderColor = NeonCyan.copy(alpha = 0.5f),
                onClick = onOpenGamesTab
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(CyanDeep, ElectricViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("SocialVerse Gaming Arena", style = MaterialTheme.typography.titleMedium)
                            Text("Color Prediction, Ludo, Spin Wheel, Quiz & 4 more games", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Text("Play →", color = NeonCyan, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    // Global Search & Discovery Sheet
    if (showSearchSheet) {
        BackHandler { showSearchSheet = false }
        GlobalSearchSheet(
            rooms = allRooms,
            users = otherUsers,
            families = families,
            onDismiss = { showSearchSheet = false },
            onJoinRoom = {
                showSearchSheet = false
                onJoinRoom(it)
            },
            onSelectUser = {
                showSearchSheet = false
                inspectedUser = it
            },
            onSelectGame = {
                showSearchSheet = false
                onOpenGamesTab()
            }
        )
    }

    // Notifications Inbox Sheet
    if (showNotifSheet) {
        BackHandler { showNotifSheet = false }
        ModalBottomSheet(
            onDismissRequest = { showNotifSheet = false },
            containerColor = ObsidianSurface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Notifications", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                if (notifications.isEmpty()) {
                    Text("No notifications yet.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                } else {
                    LazyColumn(
                        modifier = Modifier.height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.id }) { n ->
                            GlassCard(
                                borderColor = if (n.read) GlassBorder else NeonCyan,
                                onClick = { onMarkNotificationRead(n.id) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(n.title, style = MaterialTheme.typography.titleSmall, color = if (n.read) TextSecondary else NeonCyan)
                                    if (!n.read) {
                                        Text("NEW", style = MaterialTheme.typography.labelMedium, color = RoseMagenta)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(n.body, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    // Password Room Prompt
    pendingPasswordRoom?.let { room ->
        var enteredPass by remember { mutableStateOf("") }
        var passError by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { pendingPasswordRoom = null },
            containerColor = ObsidianSurface,
            icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = ImperialGold) },
            title = { Text("Enter Room Password") },
            text = {
                Column {
                    Text("Room: ${room.title}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredPass,
                        onValueChange = { enteredPass = it; passError = false },
                        label = { Text("Password") },
                        singleLine = true,
                        isError = passError
                    )
                    if (passError) {
                        Text("Incorrect password", color = RoseMagenta, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (enteredPass.trim() == room.passwordCode) {
                        pendingPasswordRoom = null
                        onJoinRoom(room)
                    } else {
                        passError = true
                    }
                }) { Text("Join") }
            },
            dismissButton = {
                TextButton(onClick = { pendingPasswordRoom = null }) { Text("Cancel") }
            }
        )
    }

    // User Detail & Actions Bottom Sheet
    inspectedUser?.let { target ->
        val isFollowing = follows.any { it.followingId == target.userId }
        val isBlocked = target.userId in (myPrivate?.blockedUids.orEmpty())
        UserProfileModalSheet(
            target = target,
            isFollowing = isFollowing,
            isBlocked = isBlocked,
            onDismiss = { inspectedUser = null },
            onMessage = {
                inspectedUser = null
                onOpenChatWithUser(target)
            },
            onCall = {
                inspectedUser = null
                onStartCallWithUser(target)
            },
            onAddFriend = { onSendFriendRequest(target) },
            onSendCp = { onSendCpRequest(target) },
            onToggleFollow = { onToggleFollow(target) },
            onToggleBlock = { onToggleBlock(target.userId, !isBlocked) },
            onSendGift = { gift -> onSendGiftToUser(target, gift) },
            onReport = { reason, details ->
                onSubmitReport(target.userId, "USER", reason, details)
            }
        )
    }
}

@Composable
fun VoiceRoomCard(room: VoiceRoom, onClick: () -> Unit) {
    val coverRes = if (room.category.equals("Gaming", true)) R.drawable.img_room_gaming else R.drawable.img_room_music
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = ElectricViolet.copy(alpha = 0.45f),
        onClick = onClick,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = coverRes),
                    contentDescription = room.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = RoseMagenta
                ) {
                    Text(
                        "LIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElectricViolet.copy(alpha = 0.25f)
                    ) {
                        Text(
                            room.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    if (room.roomType != "PUBLIC") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ImperialGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                room.roomType,
                                style = MaterialTheme.typography.labelMedium,
                                color = ImperialGold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(room.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Host: ${room.hostName} • ${room.language}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Groups, contentDescription = null, tint = EmeraldWin, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${room.participantCount} listening • ${room.maxSeats} seats",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldWin
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchSheet(
    rooms: List<VoiceRoom>,
    users: List<UserPublicProfile>,
    families: List<Family>,
    onDismiss: () -> Unit,
    onJoinRoom: (VoiceRoom) -> Unit,
    onSelectUser: (UserPublicProfile) -> Unit,
    onSelectGame: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var onlineOnly by remember { mutableStateOf(false) }

    val matchingRooms = rooms.filter {
        query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true) || it.language.contains(query, true)
    }
    val matchingUsers = users.filter {
        (!onlineOnly || it.onlineStatus == "ONLINE") &&
            (query.isBlank() || it.displayName.contains(query, true) || it.username.contains(query, true))
    }
    val matchingFamilies = families.filter {
        query.isBlank() || it.name.contains(query, true)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ObsidianSurface) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Discover SocialVerse", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search users, rooms, families, games...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            FilterChip(
                selected = onlineOnly,
                onClick = { onlineOnly = !onlineOnly },
                label = { Text("Online Users Only") }
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.height(360.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (matchingRooms.isNotEmpty()) {
                    item { Text("Voice Rooms", style = MaterialTheme.typography.titleSmall, color = NeonCyan) }
                    items(matchingRooms.take(5), key = { "sr_${it.id}" }) { r ->
                        GlassCard(onClick = { onJoinRoom(r) }) {
                            Text(r.title, style = MaterialTheme.typography.titleSmall)
                            Text("${r.category} • ${r.participantCount} online", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
                if (matchingUsers.isNotEmpty()) {
                    item { Text("Users", style = MaterialTheme.typography.titleSmall, color = NeonCyan) }
                    items(matchingUsers.take(8), key = { "su_${it.userId}" }) { u ->
                        GlassCard(onClick = { onSelectUser(u) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(u.avatarEmoji, fontSize = 22.sp)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(u.displayName, style = MaterialTheme.typography.titleSmall)
                                    Text("@${u.username} • Lv.${u.level}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
                if (matchingFamilies.isNotEmpty()) {
                    item { Text("Families", style = MaterialTheme.typography.titleSmall, color = NeonCyan) }
                    items(matchingFamilies.take(4), key = { "sf_${it.id}" }) { f ->
                        GlassCard {
                            Text("${f.emblemEmoji} ${f.name}", style = MaterialTheme.typography.titleSmall)
                            Text("${f.memberCount} members", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
                if (query.contains("color", true) || query.contains("ludo", true) || query.contains("game", true)) {
                    item {
                        GlassCard(onClick = onSelectGame) {
                            Text("🎮 Open Gaming Arena (Color Prediction, Ludo, Quiz & more)", style = MaterialTheme.typography.titleSmall, color = ImperialGold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileModalSheet(
    target: UserPublicProfile,
    isFollowing: Boolean,
    isBlocked: Boolean,
    onDismiss: () -> Unit,
    onMessage: () -> Unit,
    onCall: () -> Unit,
    onAddFriend: () -> Unit,
    onSendCp: () -> Unit,
    onToggleFollow: () -> Unit,
    onToggleBlock: () -> Unit,
    onSendGift: (com.example.data.VirtualGift) -> Unit,
    onReport: (String, String) -> Unit
) {
    var showReport by remember { mutableStateOf(false) }
    var showGiftPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ObsidianSurface) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarWithFrame(
                    emoji = target.avatarEmoji,
                    vipTier = target.vipTier,
                    profileFrame = target.profileFrame,
                    size = 68.dp,
                    isOnline = target.onlineVisible && target.onlineStatus == "ONLINE"
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(target.displayName, style = MaterialTheme.typography.headlineSmall)
                    Text("@${target.username} • ${target.country}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Spacer(Modifier.height(6.dp))
                    VipAndLevelBadges(target)
                }
            }
            if (target.bio.isNotBlank()) {
                Text(target.bio, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(text = "Message", icon = Icons.Filled.Message, onClick = onMessage, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onCall, modifier = Modifier.weight(1f).height(48.dp)) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Call")
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAddFriend, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Friend")
                }
                OutlinedButton(onClick = onToggleFollow, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isFollowing) "Following" else "Follow")
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onSendCp, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = RoseMagenta, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("CP Request")
                }
                OutlinedButton(onClick = { showGiftPicker = !showGiftPicker }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = ImperialGold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Send Gift")
                }
            }
            if (showGiftPicker) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SocialVerseCatalog.gifts, key = { it.id }) { gift ->
                        GlassCard(
                            contentPadding = PaddingValues(10.dp),
                            onClick = { onSendGift(gift) }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(gift.emoji, fontSize = 24.sp)
                                Text(gift.name, style = MaterialTheme.typography.labelMedium)
                                Text("${gift.coinCost}c", style = MaterialTheme.typography.labelSmall, color = ImperialGold)
                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onToggleBlock) {
                    Icon(Icons.Filled.Block, contentDescription = null, tint = RoseMagenta, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isBlocked) "Unblock User" else "Block User", color = RoseMagenta)
                }
                TextButton(onClick = { showReport = true }) {
                    Icon(Icons.Filled.Flag, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Report", color = TextMuted)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showReport) {
        SafetyReportDialog(
            targetTitle = target.displayName,
            onDismiss = { showReport = false },
            onSubmit = { reason, details ->
                showReport = false
                onReport(reason, details)
            }
        )
    }
}
