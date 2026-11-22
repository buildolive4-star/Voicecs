package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Conversation
import com.example.data.DirectMessage
import com.example.data.Family
import com.example.data.FamilyMember
import com.example.data.FamilyMessage
import com.example.data.FollowRelation
import com.example.data.GameRecord
import com.example.data.GiftTransaction
import com.example.data.PrivateCall
import com.example.data.SocialRequest
import com.example.data.SocialVerseRepository
import com.example.data.SpeakerRequest
import com.example.data.UserNotification
import com.example.data.UserPrivateAccount
import com.example.data.UserPublicProfile
import com.example.data.VipPlan
import com.example.data.VirtualGift
import com.example.data.VoiceAudioEngine
import com.example.data.VoiceRoom
import com.example.data.VoiceRoomMessage
import com.example.data.VoiceRoomSeat
import com.example.data.WalletLedgerEntry
import com.example.ui.components.GiftBurstEvent
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class SocialVerseViewModel(
    private val repository: SocialVerseRepository,
    val currentUserId: String
) : ViewModel() {

    val audioEngine = VoiceAudioEngine()

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val toastEvents = _toastEvents.asSharedFlow()

    private val _giftBurst = MutableStateFlow<GiftBurstEvent?>(null)
    val giftBurst: StateFlow<GiftBurstEvent?> = _giftBurst.asStateFlow()

    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _activeFamilyId = MutableStateFlow<String?>(null)
    val activeFamilyId: StateFlow<String?> = _activeFamilyId.asStateFlow()

    private val _activeCallId = MutableStateFlow<String?>(null)
    val activeCallId: StateFlow<String?> = _activeCallId.asStateFlow()

    private fun <T> Flow<T>.toUiState(tag: String, fallback: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w("SocialVerseVM", "$tag stream error", rootError)
                emit(UiState.Error(rootError.message ?: fallback))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    private fun <T> Flow<T>.toSafeState(initial: T, tag: String): StateFlow<T> =
        this.catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w("SocialVerseVM", "$tag error", firestoreError ?: error)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = initial
        )

    val myProfileState: StateFlow<UiState<UserPublicProfile?>> =
        repository.observeCurrentUserProfile(currentUserId)
            .toUiState("Profile", "Failed to load profile")

    val myPrivateState: StateFlow<UserPrivateAccount?> =
        repository.observeCurrentUserPrivate(currentUserId)
            .toSafeState(null, "PrivateAccount")

    val discoverUsers: StateFlow<List<UserPublicProfile>> =
        repository.observeDiscoverableUsers(currentUserId)
            .toSafeState(emptyList(), "DiscoverUsers")

    val liveRoomsState: StateFlow<UiState<List<VoiceRoom>>> =
        repository.observeLiveRooms(currentUserId)
            .toUiState("LiveRooms", "Failed to load live rooms")

    val walletLedger: StateFlow<List<WalletLedgerEntry>> =
        repository.observeWalletLedger(currentUserId)
            .toSafeState(emptyList(), "WalletLedger")

    val notifications: StateFlow<List<UserNotification>> =
        repository.observeNotifications(currentUserId)
            .toSafeState(emptyList(), "Notifications")

    val friendRequests: StateFlow<List<SocialRequest>> =
        repository.observeFriendRequests(currentUserId)
            .toSafeState(emptyList(), "FriendRequests")

    val cpRequests: StateFlow<List<SocialRequest>> =
        repository.observeCpRequests(currentUserId)
            .toSafeState(emptyList(), "CpRequests")

    val follows: StateFlow<List<FollowRelation>> =
        repository.observeFollows(currentUserId)
            .toSafeState(emptyList(), "Follows")

    val conversations: StateFlow<List<Conversation>> =
        repository.observeConversations(currentUserId)
            .toSafeState(emptyList(), "Conversations")

    val privateCalls: StateFlow<List<PrivateCall>> =
        repository.observePrivateCalls(currentUserId)
            .toSafeState(emptyList(), "PrivateCalls")

    val giftTransactions: StateFlow<List<GiftTransaction>> =
        repository.observeGiftTransactions(currentUserId)
            .toSafeState(emptyList(), "GiftTx")

    val families: StateFlow<List<Family>> =
        repository.observeFamilies(currentUserId)
            .toSafeState(emptyList(), "Families")

    val gameRecords: StateFlow<List<GameRecord>> =
        repository.observeGameRecords(currentUserId)
            .toSafeState(emptyList(), "GameRecords")

    // Active Voice Room Streams
    val activeRoom: StateFlow<VoiceRoom?> = _activeRoomId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(null) else repository.observeRoom(id, currentUserId)
    }.toSafeState(null, "ActiveRoom")

    val activeRoomSeats: StateFlow<List<VoiceRoomSeat>> = _activeRoomId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(emptyList()) else repository.observeRoomSeats(id, currentUserId)
    }.toSafeState(emptyList(), "RoomSeats")

    val activeRoomMessages: StateFlow<List<VoiceRoomMessage>> = _activeRoomId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(emptyList()) else repository.observeRoomMessages(id, currentUserId)
    }.toSafeState(emptyList(), "RoomMessages")

    val activeSpeakerRequests: StateFlow<List<SpeakerRequest>> = _activeRoomId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(emptyList()) else repository.observeSpeakerRequests(id, currentUserId)
    }.toSafeState(emptyList(), "SpeakerRequests")

    // Active Conversation Stream
    val activeMessages: StateFlow<List<DirectMessage>> = _activeConversationId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(emptyList()) else repository.observeDirectMessages(id, currentUserId)
    }.toSafeState(emptyList(), "DirectMessages")

    // Active Family Streams
    val activeFamilyMembers: StateFlow<List<FamilyMember>> = _activeFamilyId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(emptyList()) else repository.observeFamilyMembers(id, currentUserId)
    }.toSafeState(emptyList(), "FamilyMembers")

    val activeFamilyMessages: StateFlow<List<FamilyMessage>> = _activeFamilyId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(emptyList()) else repository.observeFamilyMessages(id, currentUserId)
    }.toSafeState(emptyList(), "FamilyMessages")

    private fun currentProfileOrNull(): UserPublicProfile? =
        (myProfileState.value as? UiState.Success)?.data

    private fun notify(msg: String) {
        _toastEvents.tryEmit(msg)
    }

    fun clearGiftBurst() {
        _giftBurst.value = null
    }

    fun createProfile(
        fullName: String,
        username: String,
        dobIso: String,
        adultEligible: Boolean,
        country: String,
        language: String,
        avatarEmoji: String
    ) {
        viewModelScope.launch {
            repository.createInitialProfile(
                fullName, username, dobIso, adultEligible, country, language, avatarEmoji
            ).onSuccess {
                notify("Welcome to SocialVerse! +1,000 starter virtual coins credited.")
            }.onFailure {
                notify(it.localizedMessage ?: "Failed to create profile")
            }
        }
    }

    fun updateCustomization(
        displayName: String,
        bio: String,
        avatarEmoji: String,
        country: String,
        language: String,
        frame: String,
        nameColor: String,
        chatBubble: String
    ) {
        viewModelScope.launch {
            repository.updateProfileCustomization(
                displayName, bio, avatarEmoji, country, language, frame, nameColor, chatBubble
            ).onSuccess {
                notify("Profile customization saved!")
            }.onFailure {
                notify(it.localizedMessage ?: "Failed to update profile")
            }
        }
    }

    fun updatePrivacy(
        hideOnline: Boolean,
        hideLastSeen: Boolean,
        allowFriendRequests: Boolean,
        discoverable: Boolean
    ) {
        viewModelScope.launch {
            repository.updatePrivacySettings(hideOnline, hideLastSeen, allowFriendRequests, discoverable)
                .onSuccess { notify("Privacy settings updated") }
                .onFailure { notify(it.localizedMessage ?: "Failed to update privacy") }
        }
    }

    fun toggleBlockUser(targetUid: String, block: Boolean) {
        viewModelScope.launch {
            repository.toggleBlockUser(targetUid, block)
                .onSuccess { notify(if (block) "User blocked" else "User unblocked") }
                .onFailure { notify(it.localizedMessage ?: "Action failed") }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccountData()
                .onSuccess {
                    notify("Account profile removed")
                    onDeleted()
                }
                .onFailure { notify(it.localizedMessage ?: "Failed to delete account") }
        }
    }

    // --- Wallet & VIP ---

    fun claimDailyReward() {
        val isSvip = currentProfileOrNull()?.vipTier == "SVIP"
        viewModelScope.launch {
            repository.claimDailyReward(isSvip)
                .onSuccess { coins -> notify("Daily Check-In claimed! +$coins Virtual Coins & +50 XP") }
                .onFailure { notify(it.localizedMessage ?: "Daily reward unavailable") }
        }
    }

    fun claimPromoCoins(amount: Long, label: String) {
        viewModelScope.launch {
            repository.claimPromoVirtualCoins(amount, label)
                .onSuccess { notify("+$amount Virtual Coins added to your wallet!") }
                .onFailure { notify(it.localizedMessage ?: "Failed to credit coins") }
        }
    }

    fun activateVip(plan: VipPlan) {
        viewModelScope.launch {
            repository.activateVipPlan(plan)
                .onSuccess { notify("${plan.title} activated for ${plan.durationDays} days!") }
                .onFailure { notify(it.localizedMessage ?: "Could not activate membership") }
        }
    }

    fun markNotificationRead(notifId: String) {
        viewModelScope.launch { repository.markNotificationRead(notifId) }
    }

    // --- Voice Rooms ---

    fun createAndJoinRoom(
        title: String,
        category: String,
        roomType: String,
        passwordCode: String,
        language: String,
        announcement: String,
        themeKey: String,
        maxSeats: Int,
        adultOnly: Boolean
    ) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.createVoiceRoom(
                profile, title, category, roomType, passwordCode, language, announcement, themeKey, maxSeats, adultOnly
            ).onSuccess { roomId ->
                _activeRoomId.value = roomId
                notify("Live voice room created!")
            }.onFailure {
                notify(it.localizedMessage ?: "Failed to create room")
            }
        }
    }

    fun joinRoom(room: VoiceRoom) {
        val profile = currentProfileOrNull() ?: return
        if (room.adultOnly && !profile.adultEligible) {
            notify("Age safety guard: 18+ rooms are restricted for this account.")
            return
        }
        viewModelScope.launch {
            _activeRoomId.value = room.id
            repository.joinVoiceRoom(room.id, profile)
                .onFailure { notify(it.localizedMessage ?: "Failed to join room") }
        }
    }

    fun leaveActiveRoom() {
        val roomId = _activeRoomId.value ?: return
        val mySeat = activeRoomSeats.value.firstOrNull { it.occupantUid == currentUserId }
        audioEngine.stopCapture()
        _activeRoomId.value = null
        viewModelScope.launch {
            repository.leaveVoiceRoom(roomId, mySeat?.id)
        }
    }

    fun closeActiveRoom() {
        val roomId = _activeRoomId.value ?: return
        audioEngine.stopCapture()
        _activeRoomId.value = null
        viewModelScope.launch {
            repository.closeVoiceRoom(roomId)
                .onSuccess { notify("Voice room closed") }
                .onFailure { notify(it.localizedMessage ?: "Failed to close room") }
        }
    }

    fun takeSeat(seat: VoiceRoomSeat) {
        val roomId = _activeRoomId.value ?: return
        val profile = currentProfileOrNull() ?: return
        if (seat.locked) {
            notify("This microphone seat is locked by the host.")
            return
        }
        viewModelScope.launch {
            repository.takeMicSeat(roomId, seat.id, profile)
                .onSuccess { notify("You are now on Mic #${seat.seatIndex + 1}!") }
                .onFailure { notify(it.localizedMessage ?: "Could not take mic seat") }
        }
    }

    fun stepDownSeat(seat: VoiceRoomSeat) {
        val roomId = _activeRoomId.value ?: return
        audioEngine.stopCapture()
        viewModelScope.launch {
            repository.stepDownFromSeat(roomId, seat.id, seat.occupantUid)
        }
    }

    fun toggleSeatMute(seat: VoiceRoomSeat) {
        val roomId = _activeRoomId.value ?: return
        val nextMute = !seat.muted
        if (seat.occupantUid == currentUserId && nextMute) {
            audioEngine.stopCapture()
        }
        viewModelScope.launch {
            repository.toggleSeatMute(roomId, seat.id, nextMute)
        }
    }

    fun toggleSeatLock(seat: VoiceRoomSeat) {
        val roomId = _activeRoomId.value ?: return
        viewModelScope.launch {
            repository.toggleSeatLock(roomId, seat.id, !seat.locked)
        }
    }

    fun requestMic() {
        val roomId = _activeRoomId.value ?: return
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.requestMicSeat(roomId, profile)
                .onSuccess { notify("Microphone request sent to host!") }
                .onFailure { notify(it.localizedMessage ?: "Failed to request mic") }
        }
    }

    fun approveSpeakerRequest(req: SpeakerRequest, approve: Boolean) {
        val roomId = _activeRoomId.value ?: return
        val emptySeat = activeRoomSeats.value.firstOrNull { !it.locked && it.occupantUid.isBlank() }
        if (approve && emptySeat == null) {
            notify("No unlocked empty microphone seats available.")
            return
        }
        viewModelScope.launch {
            repository.respondSpeakerRequest(
                roomId, req.requesterId, approve, emptySeat, req.requesterName, req.requesterAvatar
            )
        }
    }

    fun sendRoomChat(text: String) {
        val roomId = _activeRoomId.value ?: return
        val profile = currentProfileOrNull() ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendRoomMessage(roomId, profile, text)
                .onFailure { notify(it.localizedMessage ?: "Message failed") }
        }
    }

    fun updateRoomSettings(title: String, announcement: String, themeKey: String, roomType: String, passwordCode: String) {
        val roomId = _activeRoomId.value ?: return
        viewModelScope.launch {
            repository.updateRoomHostSettings(roomId, title, announcement, themeKey, roomType, passwordCode)
                .onSuccess { notify("Room settings updated") }
                .onFailure { notify(it.localizedMessage ?: "Failed to update room") }
        }
    }

    // --- Virtual Gifts ---

    fun sendGift(recipientId: String, recipientName: String, gift: VirtualGift, roomId: String? = _activeRoomId.value) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.sendVirtualGift(profile, recipientId, recipientName, gift, roomId)
                .onSuccess {
                    _giftBurst.value = GiftBurstEvent(
                        emoji = gift.emoji,
                        giftName = gift.name,
                        senderName = profile.displayName,
                        recipientName = recipientName,
                        coinCost = gift.coinCost
                    )
                }
                .onFailure { notify(it.localizedMessage ?: "Could not send gift") }
        }
    }

    // --- Friends, CP & Follows ---

    fun sendFriendRequest(target: UserPublicProfile) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.sendSocialRequest("friend_requests", profile, target)
                .onSuccess { notify("Friend request sent to ${target.displayName}!") }
                .onFailure { notify(it.localizedMessage ?: "Request failed") }
        }
    }

    fun respondFriendRequest(req: SocialRequest, newStatus: String) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.respondSocialRequest("friend_requests", req, newStatus, profile)
                .onSuccess { notify("Friend request ${newStatus.lowercase()}") }
                .onFailure { notify(it.localizedMessage ?: "Update failed") }
        }
    }

    fun sendCpRequest(target: UserPublicProfile) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.sendSocialRequest("cp_requests", profile, target)
                .onSuccess { notify("CP Partner request sent to ${target.displayName}!") }
                .onFailure { notify(it.localizedMessage ?: "CP request failed") }
        }
    }

    fun respondCpRequest(req: SocialRequest, newStatus: String) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.respondSocialRequest("cp_requests", req, newStatus, profile)
                .onSuccess { notify("CP status updated") }
                .onFailure { notify(it.localizedMessage ?: "Update failed") }
        }
    }

    fun toggleFollow(target: UserPublicProfile) {
        val isFollowing = follows.value.any { it.followingId == target.userId }
        viewModelScope.launch {
            repository.toggleFollow(target, isFollowing)
                .onSuccess { notify(if (isFollowing) "Unfollowed ${target.displayName}" else "Following ${target.displayName}") }
                .onFailure { notify(it.localizedMessage ?: "Action failed") }
        }
    }

    // --- Messaging & Private Calls ---

    fun openDirectChat(other: UserPublicProfile) {
        val profile = currentProfileOrNull() ?: return
        val blocked = myPrivateState.value?.blockedUids.orEmpty()
        if (other.userId in blocked) {
            notify("Unblock ${other.displayName} first to start a conversation.")
            return
        }
        viewModelScope.launch {
            repository.openOrCreateDirectConversation(profile, other)
                .onSuccess { convId -> _activeConversationId.value = convId }
                .onFailure { notify(it.localizedMessage ?: "Failed to open chat") }
        }
    }

    fun openConversation(convId: String) {
        _activeConversationId.value = convId
    }

    fun closeConversation() {
        _activeConversationId.value = null
    }

    fun createGroupChat(title: String, announcement: String, memberUids: List<String>) {
        viewModelScope.launch {
            repository.createGroupConversation(title, announcement, memberUids)
                .onSuccess { convId ->
                    _activeConversationId.value = convId
                    notify("Group chat created!")
                }
                .onFailure { notify(it.localizedMessage ?: "Failed to create group") }
        }
    }

    fun sendMessage(
        conversation: Conversation,
        text: String,
        messageType: String = "TEXT",
        mediaUri: String? = null,
        replyToText: String? = null
    ) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.sendDirectMessage(conversation, profile, text, messageType, mediaUri, replyToText)
                .onFailure { notify(it.localizedMessage ?: "Send failed") }
        }
    }

    fun reactMessage(conversationId: String, messageId: String, emoji: String) {
        viewModelScope.launch { repository.reactToDirectMessage(conversationId, messageId, emoji) }
    }

    fun deleteMessage(conversationId: String, messageId: String) {
        viewModelScope.launch { repository.deleteDirectMessage(conversationId, messageId) }
    }

    fun startPrivateCall(callee: UserPublicProfile) {
        val profile = currentProfileOrNull() ?: return
        val blocked = myPrivateState.value?.blockedUids.orEmpty()
        if (callee.userId in blocked) {
            notify("Cannot call a blocked user.")
            return
        }
        viewModelScope.launch {
            repository.startPrivateCall(profile, callee)
                .onSuccess { callId -> _activeCallId.value = callId }
                .onFailure { notify(it.localizedMessage ?: "Call failed") }
        }
    }

    fun setActiveCallId(callId: String?) {
        _activeCallId.value = callId
    }

    fun updateCallStatus(callId: String, status: String, durationSeconds: Long) {
        if (status in listOf("ENDED", "DECLINED", "MISSED")) {
            audioEngine.stopCapture()
            if (_activeCallId.value == callId) _activeCallId.value = null
        }
        viewModelScope.launch {
            repository.updatePrivateCall(callId, status, durationSeconds)
        }
    }

    // --- Families ---

    fun selectFamily(familyId: String?) {
        _activeFamilyId.value = familyId
    }

    fun createFamily(name: String, description: String, emblemEmoji: String) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.createFamily(profile, name, description, emblemEmoji)
                .onSuccess { id ->
                    _activeFamilyId.value = id
                    notify("Family clan created!")
                }
                .onFailure { notify(it.localizedMessage ?: "Could not create family") }
        }
    }

    fun joinFamily(family: Family) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.joinFamily(family, profile)
                .onSuccess {
                    _activeFamilyId.value = family.id
                    notify("Joined ${family.name}!")
                }
                .onFailure { notify(it.localizedMessage ?: "Could not join family") }
        }
    }

    fun leaveFamily(familyId: String) {
        viewModelScope.launch {
            repository.leaveFamily(familyId)
                .onSuccess {
                    if (_activeFamilyId.value == familyId) _activeFamilyId.value = null
                    notify("Left family")
                }
                .onFailure { notify(it.localizedMessage ?: "Failed to leave family") }
        }
    }

    fun sendFamilyChat(familyId: String, text: String) {
        val profile = currentProfileOrNull() ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendFamilyMessage(familyId, profile, text)
                .onFailure { notify(it.localizedMessage ?: "Failed to send") }
        }
    }

    // --- Mini Games ---

    fun recordGameOutcome(
        gameType: String,
        periodId: String,
        selection: String,
        stakeCoins: Long,
        wonCoins: Long,
        outcome: String,
        detail: String,
        onDone: (Long?) -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.playMiniGame(gameType, periodId, selection, stakeCoins, wonCoins, outcome, detail)
                .onSuccess { newBal ->
                    onDone(newBal)
                }
                .onFailure {
                    notify(it.localizedMessage ?: "Game transaction failed")
                    onDone(null)
                }
        }
    }

    // --- Safety Reports ---

    fun submitReport(targetId: String, targetType: String, reason: String, details: String) {
        viewModelScope.launch {
            repository.submitSafetyReport(targetId, targetType, reason, details)
                .onSuccess { notify("Safety report submitted. Thank you for keeping SocialVerse safe.") }
                .onFailure { notify(it.localizedMessage ?: "Report failed") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopCapture()
    }
}
