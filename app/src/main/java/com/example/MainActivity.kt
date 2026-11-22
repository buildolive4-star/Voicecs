package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.SocialVerseRepository
import com.example.ui.AuthScreen
import com.example.ui.CreateVoiceRoomSheet
import com.example.ui.GamesHubScreen
import com.example.ui.HomeScreen
import com.example.ui.MessagesScreen
import com.example.ui.PrivateCallOverlay
import com.example.ui.ProfileScreen
import com.example.ui.ProfileSetupScreen
import com.example.ui.ProfileSubSection
import com.example.ui.SocialVerseViewModel
import com.example.ui.UiState
import com.example.ui.VoiceLobbyScreen
import com.example.ui.VoiceRoomScreen
import com.example.ui.components.AnimatedGiftBurstOverlay
import com.example.ui.components.GlassCard
import com.example.ui.signOutUser
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.RoseMagenta
import com.example.ui.theme.SocialVerseTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialVerseTheme {
                SocialVerseRoot()
            }
        }
    }
}

private enum class SocialTab(
    val label: String,
    val icon: ImageVector,
    val tag: String
) {
    HOME("Home", Icons.Filled.Home, "nav_home"),
    VOICE("Voice", Icons.Filled.GraphicEq, "nav_voice"),
    GAMES("Games", Icons.Filled.SportsEsports, "nav_games"),
    MESSAGES("Messages", Icons.Filled.ChatBubble, "nav_messages"),
    PROFILE("Profile", Icons.Filled.Person, "nav_profile")
}

@Composable
fun SocialVerseRoot() {
    val auth = remember { Firebase.auth }
    var signedInUid by remember { mutableStateOf(auth.currentUser?.uid) }
    var showGoogleAuth by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            signedInUid = firebaseAuth.currentUser?.uid
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val currentUid = signedInUid
    if (showGoogleAuth && currentUid.isNullOrBlank()) {
        AuthScreen(
            onAuthSuccess = {
                signedInUid = auth.currentUser?.uid
                showGoogleAuth = false
            },
            onSkipToPrototype = {
                showGoogleAuth = false
            }
        )
    } else {
        val activeUid = currentUid?.takeIf { it.isNotBlank() } ?: "demo_user_aarav"
        val isPrototype = currentUid.isNullOrBlank()
        key(activeUid, isPrototype) {
            AuthenticatedSocialVerseApp(
                uid = activeUid,
                isPrototypeMode = isPrototype,
                onSignedOut = {
                    signedInUid = null
                    showGoogleAuth = false
                },
                onRequestGoogleAuth = {
                    showGoogleAuth = true
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedSocialVerseApp(
    uid: String,
    isPrototypeMode: Boolean,
    onSignedOut: () -> Unit,
    onRequestGoogleAuth: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: SocialVerseViewModel = viewModel(
        key = "${uid}_$isPrototypeMode",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                SocialVerseViewModel(
                    repository = if (isPrototypeMode) null else SocialVerseRepository(app),
                    currentUserId = uid
                )
            }
        }
    )

    val myProfileState by viewModel.myProfileState.collectAsStateWithLifecycle()
    val myPrivate by viewModel.myPrivateState.collectAsStateWithLifecycle()
    val roomsState by viewModel.liveRoomsState.collectAsStateWithLifecycle()
    val discoverUsers by viewModel.discoverUsers.collectAsStateWithLifecycle()
    val families by viewModel.families.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val walletLedger by viewModel.walletLedger.collectAsStateWithLifecycle()
    val friendRequests by viewModel.friendRequests.collectAsStateWithLifecycle()
    val cpRequests by viewModel.cpRequests.collectAsStateWithLifecycle()
    val follows by viewModel.follows.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val privateCalls by viewModel.privateCalls.collectAsStateWithLifecycle()
    val giftTransactions by viewModel.giftTransactions.collectAsStateWithLifecycle()
    val gameRecords by viewModel.gameRecords.collectAsStateWithLifecycle()

    val activeRoom by viewModel.activeRoom.collectAsStateWithLifecycle()
    val activeRoomSeats by viewModel.activeRoomSeats.collectAsStateWithLifecycle()
    val activeRoomMessages by viewModel.activeRoomMessages.collectAsStateWithLifecycle()
    val activeSpeakerRequests by viewModel.activeSpeakerRequests.collectAsStateWithLifecycle()
    val activeConvId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val activeFamilyMembers by viewModel.activeFamilyMembers.collectAsStateWithLifecycle()
    val activeFamilyMessages by viewModel.activeFamilyMessages.collectAsStateWithLifecycle()
    val activeCallId by viewModel.activeCallId.collectAsStateWithLifecycle()
    val giftBurst by viewModel.giftBurst.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(SocialTab.HOME) }
    var profileSubSection by remember { mutableStateOf(ProfileSubSection.MAIN) }
    var showHomeCreateRoomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.toastEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val performSignOut: () -> Unit = {
        viewModel.leaveActiveRoom()
        if (isPrototypeMode) {
            onRequestGoogleAuth()
        } else {
            signOutUser(
                credentialManager = credentialManager,
                scope = scope,
                onSignOutComplete = onSignedOut
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ObsidianDeep, ObsidianBg)))
    ) {
        when (val state = myProfileState) {
            UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NeonCyan)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading SocialVerse...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(borderColor = RoseMagenta) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Connection Issue", style = MaterialTheme.typography.titleLarge, color = RoseMagenta)
                            Text(state.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
                            OutlinedButton(onClick = performSignOut) {
                                Text("Sign Out")
                            }
                        }
                    }
                }
            }

            is UiState.Success -> {
                val myProfile = state.data
                if (myProfile == null) {
                    val user = Firebase.auth.currentUser
                    ProfileSetupScreen(
                        defaultDisplayName = user?.displayName ?: "",
                        defaultEmail = user?.email ?: "",
                        onCreateProfile = { fullName, username, dobIso, adultEligible, country, language, avatarEmoji ->
                            viewModel.createProfile(
                                fullName = fullName,
                                username = username,
                                dobIso = dobIso,
                                adultEligible = adultEligible,
                                country = country,
                                language = language,
                                avatarEmoji = avatarEmoji
                            )
                        }
                    )
                } else {
                    val unreadBadgeCount = remember(friendRequests, notifications) {
                        friendRequests.count { it.status == "PENDING" && it.toUserId == uid } +
                            notifications.count { !it.read && it.type == "MESSAGE" }
                    }

                    if (currentTab != SocialTab.HOME && activeRoom == null && activeConvId == null) {
                        BackHandler { currentTab = SocialTab.HOME }
                    }

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val useRail = maxWidth >= 600.dp

                        Scaffold(
                            containerColor = Color.Transparent,
                            contentWindowInsets = WindowInsets.safeDrawing,
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                            bottomBar = {
                                if (!useRail && activeRoom == null && activeConvId == null) {
                                    NavigationBar(
                                        containerColor = ObsidianSurface.copy(alpha = 0.96f),
                                        tonalElevation = 8.dp
                                    ) {
                                        SocialTab.entries.forEach { tab ->
                                            val selected = currentTab == tab
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = {
                                                    if (tab == SocialTab.PROFILE) {
                                                        profileSubSection = ProfileSubSection.MAIN
                                                    }
                                                    currentTab = tab
                                                },
                                                modifier = Modifier.testTag(tab.tag),
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = NeonCyan,
                                                    selectedTextColor = NeonCyan,
                                                    indicatorColor = ElectricViolet.copy(alpha = 0.25f),
                                                    unselectedIconColor = TextMuted,
                                                    unselectedTextColor = TextMuted
                                                ),
                                                icon = {
                                                    if (tab == SocialTab.MESSAGES && unreadBadgeCount > 0) {
                                                        BadgedBox(badge = { Badge { Text("$unreadBadgeCount") } }) {
                                                            Icon(tab.icon, contentDescription = tab.label)
                                                        }
                                                    } else {
                                                        Icon(tab.icon, contentDescription = tab.label)
                                                    }
                                                },
                                                label = {
                                                    Text(
                                                        tab.label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                if (useRail && activeRoom == null) {
                                    NavigationRail(
                                        containerColor = ObsidianSurface.copy(alpha = 0.96f),
                                        modifier = Modifier.fillMaxHeight()
                                    ) {
                                        Spacer(Modifier.height(12.dp))
                                        SocialTab.entries.forEach { tab ->
                                            val selected = currentTab == tab
                                            NavigationRailItem(
                                                selected = selected,
                                                onClick = {
                                                    if (tab == SocialTab.PROFILE) {
                                                        profileSubSection = ProfileSubSection.MAIN
                                                    }
                                                    currentTab = tab
                                                },
                                                modifier = Modifier.testTag(tab.tag),
                                                colors = NavigationRailItemDefaults.colors(
                                                    selectedIconColor = NeonCyan,
                                                    selectedTextColor = NeonCyan,
                                                    indicatorColor = ElectricViolet.copy(alpha = 0.25f),
                                                    unselectedIconColor = TextMuted,
                                                    unselectedTextColor = TextMuted
                                                ),
                                                icon = {
                                                    if (tab == SocialTab.MESSAGES && unreadBadgeCount > 0) {
                                                        BadgedBox(badge = { Badge { Text("$unreadBadgeCount") } }) {
                                                            Icon(tab.icon, contentDescription = tab.label)
                                                        }
                                                    } else {
                                                        Icon(tab.icon, contentDescription = tab.label)
                                                    }
                                                },
                                                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) }
                                            )
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    when (currentTab) {
                                        SocialTab.HOME -> HomeScreen(
                                            myProfile = myProfile,
                                            myPrivate = myPrivate,
                                            roomsState = roomsState,
                                            discoverUsers = discoverUsers,
                                            families = families,
                                            notifications = notifications,
                                            follows = follows,
                                            onClaimDaily = { viewModel.claimDailyReward() },
                                            onJoinRoom = { room -> viewModel.joinRoom(room) },
                                            onCreateRoomClick = { showHomeCreateRoomSheet = true },
                                            onOpenGamesTab = { currentTab = SocialTab.GAMES },
                                            onOpenWallet = {
                                                profileSubSection = ProfileSubSection.WALLET
                                                currentTab = SocialTab.PROFILE
                                            },
                                            onOpenProfileTab = {
                                                profileSubSection = ProfileSubSection.MAIN
                                                currentTab = SocialTab.PROFILE
                                            },
                                            onMarkNotificationRead = { viewModel.markNotificationRead(it) },
                                            onOpenChatWithUser = { target ->
                                                viewModel.openDirectChat(target)
                                                currentTab = SocialTab.MESSAGES
                                            },
                                            onStartCallWithUser = { target -> viewModel.startPrivateCall(target) },
                                            onSendFriendRequest = { target -> viewModel.sendFriendRequest(target) },
                                            onSendCpRequest = { target -> viewModel.sendCpRequest(target) },
                                            onToggleFollow = { target -> viewModel.toggleFollow(target) },
                                            onToggleBlock = { targetUid, block -> viewModel.toggleBlockUser(targetUid, block) },
                                            onSendGiftToUser = { target, gift ->
                                                viewModel.sendGift(target.userId, target.displayName, gift, roomId = null)
                                            },
                                            onJoinFamily = { fam ->
                                                viewModel.joinFamily(fam)
                                                profileSubSection = ProfileSubSection.FAMILY
                                                currentTab = SocialTab.PROFILE
                                            },
                                            onSubmitReport = { tId, tType, reason, details ->
                                                viewModel.submitReport(tId, tType, reason, details)
                                            }
                                        )

                                        SocialTab.VOICE -> VoiceLobbyScreen(
                                            myProfile = myProfile,
                                            roomsState = roomsState,
                                            onJoinRoom = { room -> viewModel.joinRoom(room) },
                                            onCreateRoom = { title, cat, rType, pass, lang, ann, theme, seats, adult ->
                                                viewModel.createAndJoinRoom(title, cat, rType, pass, lang, ann, theme, seats, adult)
                                            }
                                        )

                                        SocialTab.GAMES -> GamesHubScreen(
                                            myProfile = myProfile,
                                            coinBalance = myPrivate?.coinBalance ?: 0L,
                                            gameRecords = gameRecords,
                                            discoverUsers = discoverUsers,
                                            families = families,
                                            onOpenWallet = {
                                                profileSubSection = ProfileSubSection.WALLET
                                                currentTab = SocialTab.PROFILE
                                            },
                                            onRecordGame = { gameType, periodId, selection, stake, won, outcome, detail ->
                                                viewModel.recordGameOutcome(gameType, periodId, selection, stake, won, outcome, detail)
                                            }
                                        )

                                        SocialTab.MESSAGES -> {
                                            val activeConv = conversations.firstOrNull { it.id == activeConvId }
                                            MessagesScreen(
                                                myProfile = myProfile,
                                                conversations = conversations,
                                                activeConversation = activeConv,
                                                activeMessages = activeMessages,
                                                friendRequests = friendRequests,
                                                follows = follows,
                                                discoverUsers = discoverUsers,
                                                privateCalls = privateCalls,
                                                blockedUids = myPrivate?.blockedUids.orEmpty(),
                                                onOpenConversation = { viewModel.openConversation(it) },
                                                onCloseConversation = { viewModel.closeConversation() },
                                                onOpenDirectChat = { viewModel.openDirectChat(it) },
                                                onCreateGroupChat = { title, ann, members ->
                                                    viewModel.createGroupChat(title, ann, members)
                                                },
                                                onSendMessage = { conv, text, type, uri, reply ->
                                                    viewModel.sendMessage(conv, text, type, uri, reply)
                                                },
                                                onReactMessage = { convId, msgId, emoji ->
                                                    viewModel.reactMessage(convId, msgId, emoji)
                                                },
                                                onDeleteMessage = { convId, msgId ->
                                                    viewModel.deleteMessage(convId, msgId)
                                                },
                                                onSendFriendRequest = { viewModel.sendFriendRequest(it) },
                                                onRespondFriendRequest = { req, status ->
                                                    viewModel.respondFriendRequest(req, status)
                                                },
                                                onStartCall = { viewModel.startPrivateCall(it) }
                                            )
                                        }

                                        SocialTab.PROFILE -> ProfileScreen(
                                            initialSubSection = profileSubSection,
                                            myProfile = myProfile,
                                            myPrivate = myPrivate,
                                            walletLedger = walletLedger,
                                            giftTransactions = giftTransactions,
                                            cpRequests = cpRequests,
                                            families = families,
                                            activeFamilyMembers = activeFamilyMembers,
                                            activeFamilyMessages = activeFamilyMessages,
                                            onSelectFamily = { viewModel.selectFamily(it) },
                                            onClaimDaily = { viewModel.claimDailyReward() },
                                            onClaimPromoCoins = { amt, label -> viewModel.claimPromoCoins(amt, label) },
                                            onActivateVip = { viewModel.activateVip(it) },
                                            onRespondCp = { req, status -> viewModel.respondCpRequest(req, status) },
                                            onCreateFamily = { name, desc, emblem -> viewModel.createFamily(name, desc, emblem) },
                                            onJoinFamily = { viewModel.joinFamily(it) },
                                            onLeaveFamily = { viewModel.leaveFamily(it) },
                                            onSendFamilyChat = { famId, text -> viewModel.sendFamilyChat(famId, text) },
                                            onUpdateCustomization = { name, bio, emoji, country, lang, frame, nameCol, bubble ->
                                                viewModel.updateCustomization(name, bio, emoji, country, lang, frame, nameCol, bubble)
                                            },
                                            onUpdatePrivacy = { hideOn, hideLast, allowFriend, disc ->
                                                viewModel.updatePrivacy(hideOn, hideLast, allowFriend, disc)
                                            },
                                            onUnblockUser = { targetUid -> viewModel.toggleBlockUser(targetUid, false) },
                                            onDeleteAccount = { viewModel.deleteAccount(onDeleted = performSignOut) },
                                            onSignOut = performSignOut,
                                            isPrototypeMode = isPrototypeMode,
                                            onConnectGoogleAccount = onRequestGoogleAuth
                                        )
                                    }
                                }
                            }
                        }

                        // Full-screen Live Voice Room Overlay
                        val currentRoom = activeRoom
                        if (currentRoom != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                            ) {
                                VoiceRoomScreen(
                                    room = currentRoom,
                                    seats = activeRoomSeats,
                                    messages = activeRoomMessages,
                                    speakerRequests = activeSpeakerRequests,
                                    myProfile = myProfile,
                                    coinBalance = myPrivate?.coinBalance ?: 0L,
                                    audioEngine = viewModel.audioEngine,
                                    onLeaveRoom = { viewModel.leaveActiveRoom() },
                                    onCloseRoom = { viewModel.closeActiveRoom() },
                                    onTakeSeat = { seat -> viewModel.takeSeat(seat) },
                                    onStepDownSeat = { seat -> viewModel.stepDownSeat(seat) },
                                    onToggleSeatMute = { seat -> viewModel.toggleSeatMute(seat) },
                                    onToggleSeatLock = { seat -> viewModel.toggleSeatLock(seat) },
                                    onRequestMic = { viewModel.requestMic() },
                                    onApproveRequest = { req, approve -> viewModel.approveSpeakerRequest(req, approve) },
                                    onSendChat = { text -> viewModel.sendRoomChat(text) },
                                    onSendGift = { recId, recName, gift ->
                                        viewModel.sendGift(recId, recName, gift, roomId = currentRoom.id)
                                    },
                                    onUpdateRoomSettings = { t, a, th, rt, p ->
                                        viewModel.updateRoomSettings(t, a, th, rt, p)
                                    },
                                    onSubmitReport = { reason, details ->
                                        viewModel.submitReport(currentRoom.id, "ROOM", reason, details)
                                    }
                                )
                            }
                        }

                        // Create Room Sheet triggered from Home Quick Action
                        if (showHomeCreateRoomSheet) {
                            CreateVoiceRoomSheet(
                                myProfile = myProfile,
                                onDismiss = { showHomeCreateRoomSheet = false },
                                onCreate = { title, cat, rType, pass, lang, ann, theme, seats, adult ->
                                    showHomeCreateRoomSheet = false
                                    viewModel.createAndJoinRoom(title, cat, rType, pass, lang, ann, theme, seats, adult)
                                }
                            )
                        }

                        // Incoming / Active Private Voice Call Overlay
                        val activeCall = remember(privateCalls, activeCallId, uid) {
                            privateCalls.firstOrNull {
                                (it.id == activeCallId && it.status in listOf("RINGING", "ACTIVE")) ||
                                    (it.calleeId == uid && it.status == "RINGING")
                            }
                        }
                        if (activeCall != null) {
                            PrivateCallOverlay(
                                call = activeCall,
                                myUid = uid,
                                audioEngine = viewModel.audioEngine,
                                onUpdateCall = { status, duration ->
                                    viewModel.updateCallStatus(activeCall.id, status, duration)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Top-level Animated Virtual Gift Overlay
        AnimatedGiftBurstOverlay(
            event = giftBurst,
            onFinished = { viewModel.clearGiftBurst() }
        )
    }
}
