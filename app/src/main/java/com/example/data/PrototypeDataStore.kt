package com.example.data

import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * In-memory interactive data store for Prototype Showcase Mode (no sign-in required).
 * Keeps zero Firestore calls while unauthenticated so every screen, game, voice stage,
 * wallet ledger, chat, and clan interaction is live and testable during prototype demos.
 */
class PrototypeDataStore(val demoUid: String = "demo_user_aarav") {

    private fun nowTs(): Timestamp = Timestamp.now()
    private fun nextId(prefix: String): String = "${prefix}_${UUID.randomUUID().toString().take(8)}"

    private val initialProfile = UserPublicProfile(
        id = demoUid,
        userId = demoUid,
        username = "aarav_live",
        displayName = "Aarav Sharma",
        avatarEmoji = "🎧",
        avatarColorHex = "#8B5CF6",
        bio = "Music host, Color Arena tactician & VIP lounge regular.",
        country = "India",
        language = "Hindi",
        level = 4L,
        xp = 1350L,
        vipTier = "VIP",
        profileFrame = "ROYAL_GOLD",
        nameColor = "GOLD_ELITE",
        chatBubbleStyle = "GLASS_VIOLET",
        familyId = "fam_phoenix",
        familyName = "Royal Phoenix",
        cpPartnerId = "",
        cpPartnerName = "",
        followersCount = 128L,
        friendsCount = 2L,
        popularityScore = 2480L,
        giftsReceivedValue = 1850L,
        discoverable = true,
        adultEligible = true,
        onlineVisible = true,
        onlineStatus = "ONLINE",
        createdAt = nowTs(),
        updatedAt = nowTs()
    )

    private val initialPrivate = UserPrivateAccount(
        id = demoUid,
        userId = demoUid,
        email = "prototype@socialverse.demo",
        fullName = "Aarav Sharma",
        dobIso = "1999-08-14",
        coinBalance = 5000L,
        vipTier = "VIP",
        vipExpiryMillis = System.currentTimeMillis() + 25L * 86_400_000L,
        lastDailyClaimDay = "",
        blockedUids = emptyList(),
        hideOnline = false,
        hideLastSeen = false,
        allowFriendRequests = true,
        lastDeviceLabel = "Prototype Demo Device",
        createdAt = nowTs(),
        updatedAt = nowTs()
    )

    private val seedUsers = listOf(
        initialProfile,
        UserPublicProfile(
            id = "host_riya", userId = "host_riya", username = "riya_melody", displayName = "Riya Verma",
            avatarEmoji = "🦋", bio = "Late night acoustic & lo-fi host.", country = "India", language = "Hindi",
            level = 7L, xp = 6800L, vipTier = "SVIP", profileFrame = "SVIP_CROWN",
            familyId = "fam_phoenix", familyName = "Royal Phoenix", followersCount = 840L, friendsCount = 45L,
            popularityScore = 9420L, giftsReceivedValue = 12400L, discoverable = true, adultEligible = true,
            onlineVisible = true, onlineStatus = "ONLINE", createdAt = nowTs(), updatedAt = nowTs()
        ),
        UserPublicProfile(
            id = "host_kabir", userId = "host_kabir", username = "kabir_beats", displayName = "Kabir Beats",
            avatarEmoji = "🔥", bio = "DJ drops, trivia & voice challenges.", country = "UAE", language = "English",
            level = 6L, xp = 4100L, vipTier = "VIP", profileFrame = "NEON_RING",
            familyId = "fam_neon", familyName = "Neon Legends", followersCount = 520L, friendsCount = 31L,
            popularityScore = 7150L, giftsReceivedValue = 8200L, discoverable = true, adultEligible = true,
            onlineVisible = true, onlineStatus = "ONLINE", createdAt = nowTs(), updatedAt = nowTs()
        ),
        UserPublicProfile(
            id = "host_zara", userId = "host_zara", username = "zara_arena", displayName = "Zara Gaming",
            avatarEmoji = "🎮", bio = "Ludo champion & Color Arena pro.", country = "India", language = "English",
            level = 5L, xp = 2900L, vipTier = "SVIP", profileFrame = "CYBER_PRISM",
            familyId = "fam_neon", familyName = "Neon Legends", followersCount = 610L, friendsCount = 29L,
            popularityScore = 6380L, giftsReceivedValue = 6900L, discoverable = true, adultEligible = true,
            onlineVisible = true, onlineStatus = "ONLINE", createdAt = nowTs(), updatedAt = nowTs()
        ),
        UserPublicProfile(
            id = "host_dev", userId = "host_dev", username = "dev_shayari", displayName = "Dev Malhotra",
            avatarEmoji = "🌙", bio = "Poetry, ghazals & midnight chill.", country = "India", language = "Hindi",
            level = 4L, xp = 1600L, vipTier = "NONE", profileFrame = "DEFAULT",
            familyId = "fam_midnight", familyName = "Midnight Vibes", followersCount = 290L, friendsCount = 19L,
            popularityScore = 3850L, giftsReceivedValue = 2100L, discoverable = true, adultEligible = true,
            onlineVisible = true, onlineStatus = "ONLINE", createdAt = nowTs(), updatedAt = nowTs()
        ),
        UserPublicProfile(
            id = "host_maya", userId = "host_maya", username = "maya_vibes", displayName = "Maya Lounge",
            avatarEmoji = "💎", bio = "International music & chill hangout.", country = "UK", language = "English",
            level = 6L, xp = 4800L, vipTier = "VIP", profileFrame = "ROYAL_GOLD",
            familyId = "fam_phoenix", familyName = "Royal Phoenix", followersCount = 470L, friendsCount = 26L,
            popularityScore = 5940L, giftsReceivedValue = 5100L, discoverable = true, adultEligible = true,
            onlineVisible = true, onlineStatus = "ONLINE", createdAt = nowTs(), updatedAt = nowTs()
        )
    )

    private val seedRooms = listOf(
        VoiceRoom(
            id = "room_lofi",
            hostId = "host_riya",
            hostName = "Riya Verma",
            title = "Midnight Lo-Fi & Unplugged Chill",
            category = "Music",
            roomType = "PUBLIC",
            language = "Hindi",
            announcement = "Grab a mic seat, sing or send virtual roses!",
            themeKey = "NEON_VIOLET",
            maxSeats = 8L,
            participantCount = 42L,
            status = "LIVE",
            publicListed = true,
            adultOnly = false,
            seatUids = listOf("host_riya", "host_maya"),
            createdAt = nowTs(),
            updatedAt = nowTs()
        ),
        VoiceRoom(
            id = "room_gaming",
            hostId = "host_zara",
            hostName = "Zara Gaming",
            title = "Gaming Squad & Trivia Blitz",
            category = "Gaming",
            roomType = "PUBLIC",
            language = "English",
            announcement = "Color Arena & Ludo tips live now!",
            themeKey = "CYBER_CYAN",
            maxSeats = 8L,
            participantCount = 31L,
            status = "LIVE",
            publicListed = true,
            adultOnly = false,
            seatUids = listOf("host_zara", "host_kabir"),
            createdAt = nowTs(),
            updatedAt = nowTs()
        ),
        VoiceRoom(
            id = "room_vip",
            hostId = "host_kabir",
            hostName = "Kabir Beats",
            title = "SVIP Diamond Afterparty",
            category = "Entertainment",
            roomType = "VIP",
            language = "English",
            announcement = "High-energy DJ lounge & luxury virtual gifts.",
            themeKey = "IMPERIAL_GOLD",
            maxSeats = 8L,
            participantCount = 24L,
            status = "LIVE",
            publicListed = true,
            adultOnly = false,
            seatUids = listOf("host_kabir"),
            createdAt = nowTs(),
            updatedAt = nowTs()
        ),
        VoiceRoom(
            id = "room_shayari",
            hostId = "host_dev",
            hostName = "Dev Malhotra",
            title = "Friendship & Late Night Shayari",
            category = "Friendship",
            roomType = "PUBLIC",
            language = "Hindi",
            announcement = "Share your favorite lines on the open mic.",
            themeKey = "MIDNIGHT_ROSE",
            maxSeats = 8L,
            participantCount = 18L,
            status = "LIVE",
            publicListed = true,
            adultOnly = false,
            seatUids = listOf("host_dev"),
            createdAt = nowTs(),
            updatedAt = nowTs()
        )
    )

    private fun buildInitialSeats(room: VoiceRoom): List<VoiceRoomSeat> {
        val hostProfile = seedUsers.firstOrNull { it.userId == room.hostId }
        val secondUid = room.seatUids.getOrNull(1)
        val secondProfile = seedUsers.firstOrNull { it.userId == secondUid }
        return (0 until room.maxSeats.toInt()).map { idx ->
            when {
                idx == 0 && hostProfile != null -> VoiceRoomSeat(
                    id = "seat_0", roomId = room.id, seatIndex = 0L,
                    occupantUid = hostProfile.userId, occupantName = hostProfile.displayName,
                    occupantAvatar = hostProfile.avatarEmoji, occupantVip = hostProfile.vipTier,
                    muted = false, locked = false, speaking = true, updatedAt = nowTs()
                )
                idx == 1 && secondProfile != null -> VoiceRoomSeat(
                    id = "seat_1", roomId = room.id, seatIndex = 1L,
                    occupantUid = secondProfile.userId, occupantName = secondProfile.displayName,
                    occupantAvatar = secondProfile.avatarEmoji, occupantVip = secondProfile.vipTier,
                    muted = false, locked = false, speaking = false, updatedAt = nowTs()
                )
                else -> VoiceRoomSeat(
                    id = "seat_$idx", roomId = room.id, seatIndex = idx.toLong(),
                    occupantUid = "", occupantName = "", occupantAvatar = "", occupantVip = "NONE",
                    muted = false, locked = false, speaking = false, updatedAt = nowTs()
                )
            }
        }
    }

    private val _myProfile = MutableStateFlow(initialProfile)
    val myProfile: StateFlow<UserPublicProfile> = _myProfile.asStateFlow()

    private val _myPrivate = MutableStateFlow(initialPrivate)
    val myPrivate: StateFlow<UserPrivateAccount> = _myPrivate.asStateFlow()

    private val _discoverUsers = MutableStateFlow(seedUsers)
    val discoverUsers: StateFlow<List<UserPublicProfile>> = _discoverUsers.asStateFlow()

    private val _rooms = MutableStateFlow(seedRooms)
    val rooms: StateFlow<List<VoiceRoom>> = _rooms.asStateFlow()

    private val _roomSeatsMap = MutableStateFlow(seedRooms.associate { it.id to buildInitialSeats(it) })
    private val _roomMessagesMap = MutableStateFlow(
        seedRooms.associate { room ->
            room.id to listOf(
                VoiceRoomMessage(
                    id = nextId("rmsg"), roomId = room.id, senderId = room.hostId,
                    senderName = room.hostName, senderVip = "SVIP",
                    text = "🎙️ Welcome to ${room.title}! Tap any empty seat to join the mic.",
                    messageType = "SYSTEM", createdAt = nowTs()
                ),
                VoiceRoomMessage(
                    id = nextId("rmsg"), roomId = room.id, senderId = "host_maya",
                    senderName = "Maya Lounge", senderVip = "VIP",
                    text = "Loving the vibes tonight! 🔥",
                    messageType = "TEXT", createdAt = nowTs()
                )
            )
        }
    )
    private val _speakerRequestsMap = MutableStateFlow(
        mapOf(
            "room_lofi" to listOf(
                SpeakerRequest(
                    id = "host_dev", roomId = "room_lofi", requesterId = "host_dev",
                    requesterName = "Dev Malhotra", requesterAvatar = "🌙",
                    status = "PENDING", createdAt = nowTs(), updatedAt = nowTs()
                )
            )
        )
    )

    private val _walletLedger = MutableStateFlow(
        listOf(
            WalletLedgerEntry(
                id = "led_3", userId = demoUid, entryType = "VIP_UNLOCK", amountCoins = -1500L,
                balanceAfter = 5000L, description = "Unlocked SocialVerse VIP (30 days)",
                referenceId = "VIP", createdAt = nowTs()
            ),
            WalletLedgerEntry(
                id = "led_2", userId = demoUid, entryType = "GAME_WIN", amountCoins = 500L,
                balanceAfter = 6500L, description = "Color Prediction: Won on GREEN",
                referenceId = "demo_round_1", createdAt = nowTs()
            ),
            WalletLedgerEntry(
                id = "led_1", userId = demoUid, entryType = "WELCOME_BONUS", amountCoins = 6000L,
                balanceAfter = 6000L, description = "Prototype Starter Virtual Coins",
                referenceId = demoUid, createdAt = nowTs()
            )
        )
    )
    val walletLedger: StateFlow<List<WalletLedgerEntry>> = _walletLedger.asStateFlow()

    private val _notifications = MutableStateFlow(
        listOf(
            UserNotification(
                id = "notif_1", recipientId = demoUid, senderId = "host_riya", senderName = "Riya Verma",
                type = "CP_REQUEST", title = "CP Partner Invite 💞",
                body = "Riya Verma invited you to become virtual CP Partners!", read = false,
                targetId = "cp_riya_demo", createdAt = nowTs()
            ),
            UserNotification(
                id = "notif_2", recipientId = demoUid, senderId = "host_zara", senderName = "Zara Gaming",
                type = "GAME", title = "Color Arena Blitz Live 🎨",
                body = "30s & 60s prediction rounds are open with virtual coins!", read = false,
                targetId = "games", createdAt = nowTs()
            )
        )
    )
    val notifications: StateFlow<List<UserNotification>> = _notifications.asStateFlow()

    private val _friendRequests = MutableStateFlow(
        listOf(
            SocialRequest(
                id = "fr_riya", fromUserId = demoUid, fromName = "Aarav Sharma", fromAvatar = "🎧",
                toUserId = "host_riya", toName = "Riya Verma", toAvatar = "🦋",
                participantUids = listOf(demoUid, "host_riya"), status = "ACCEPTED",
                createdAt = nowTs(), updatedAt = nowTs()
            ),
            SocialRequest(
                id = "fr_zara", fromUserId = "host_zara", fromName = "Zara Gaming", fromAvatar = "🎮",
                toUserId = demoUid, toName = "Aarav Sharma", toAvatar = "🎧",
                participantUids = listOf("host_zara", demoUid), status = "ACCEPTED",
                createdAt = nowTs(), updatedAt = nowTs()
            ),
            SocialRequest(
                id = "fr_kabir", fromUserId = "host_kabir", fromName = "Kabir Beats", fromAvatar = "🔥",
                toUserId = demoUid, toName = "Aarav Sharma", toAvatar = "🎧",
                participantUids = listOf("host_kabir", demoUid), status = "PENDING",
                createdAt = nowTs(), updatedAt = nowTs()
            )
        )
    )
    val friendRequests: StateFlow<List<SocialRequest>> = _friendRequests.asStateFlow()

    private val _cpRequests = MutableStateFlow(
        listOf(
            SocialRequest(
                id = "cp_riya_demo", fromUserId = "host_riya", fromName = "Riya Verma", fromAvatar = "🦋",
                toUserId = demoUid, toName = "Aarav Sharma", toAvatar = "🎧",
                participantUids = listOf("host_riya", demoUid), status = "PENDING",
                createdAt = nowTs(), updatedAt = nowTs()
            )
        )
    )
    val cpRequests: StateFlow<List<SocialRequest>> = _cpRequests.asStateFlow()

    private val _follows = MutableStateFlow(
        listOf(
            FollowRelation(
                id = "${demoUid}_host_riya", followerId = demoUid, followingId = "host_riya",
                followingName = "Riya Verma", followingAvatar = "🦋", createdAt = nowTs()
            )
        )
    )
    val follows: StateFlow<List<FollowRelation>> = _follows.asStateFlow()

    private val _conversations = MutableStateFlow(
        listOf(
            Conversation(
                id = "conv_riya", memberUids = listOf(demoUid, "host_riya"), type = "DIRECT",
                title = "Riya Verma", announcement = "", adminUid = demoUid,
                lastMessage = "Join my Midnight Lo-Fi room!", createdAt = nowTs(), updatedAt = nowTs()
            ),
            Conversation(
                id = "conv_squad", memberUids = listOf(demoUid, "host_riya", "host_zara"), type = "GROUP",
                title = "Phoenix Gaming Squad", announcement = "Daily Ludo & Color Arena at 9 PM",
                adminUid = demoUid, lastMessage = "Ready for the next 60s round?",
                createdAt = nowTs(), updatedAt = nowTs()
            )
        )
    )
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _directMessagesMap = MutableStateFlow(
        mapOf(
            "conv_riya" to listOf(
                DirectMessage(
                    id = "dm_1", memberUids = listOf(demoUid, "host_riya"),
                    senderId = "host_riya", senderName = "Riya Verma",
                    text = "Hey Aarav! Join my Midnight Lo-Fi room!", messageType = "TEXT",
                    createdAt = nowTs()
                ),
                DirectMessage(
                    id = "dm_2", memberUids = listOf(demoUid, "host_riya"),
                    senderId = demoUid, senderName = "Aarav Sharma",
                    text = "On my way! Bringing virtual roses 🌹", messageType = "TEXT",
                    reaction = "🔥", createdAt = nowTs()
                )
            ),
            "conv_squad" to listOf(
                DirectMessage(
                    id = "dm_3", memberUids = listOf(demoUid, "host_riya", "host_zara"),
                    senderId = "host_zara", senderName = "Zara Gaming",
                    text = "Ready for the next 60s round?", messageType = "TEXT",
                    createdAt = nowTs()
                )
            )
        )
    )

    private val _privateCalls = MutableStateFlow<List<PrivateCall>>(emptyList())
    val privateCalls: StateFlow<List<PrivateCall>> = _privateCalls.asStateFlow()

    private val _giftTransactions = MutableStateFlow(
        listOf(
            GiftTransaction(
                id = "gtx_1", senderId = demoUid, senderName = "Aarav Sharma",
                recipientId = "host_riya", recipientName = "Riya Verma",
                giftId = "crown", giftName = "Royal Crown", giftEmoji = "👑",
                coinCost = 500L, roomId = "room_lofi", participantUids = listOf(demoUid, "host_riya"),
                createdAt = nowTs()
            )
        )
    )
    val giftTransactions: StateFlow<List<GiftTransaction>> = _giftTransactions.asStateFlow()

    private val _families = MutableStateFlow(
        listOf(
            Family(
                id = "fam_phoenix", ownerId = "host_riya", name = "Royal Phoenix",
                description = "Top music & late-night chill family on SocialVerse.",
                emblemEmoji = "🔥", announcement = "Daily check-in & voice party at 9 PM!",
                memberCount = 48L, activityScore = 9850L, listed = true,
                memberUids = listOf("host_riya", demoUid, "host_maya"), createdAt = nowTs(), updatedAt = nowTs()
            ),
            Family(
                id = "fam_neon", ownerId = "host_zara", name = "Neon Legends",
                description = "Color Prediction, Ludo & Trivia competitive gaming clan.",
                emblemEmoji = "⚡", announcement = "Weekend Arena cup live now!",
                memberCount = 36L, activityScore = 8120L, listed = true,
                memberUids = listOf("host_zara", "host_kabir"), createdAt = nowTs(), updatedAt = nowTs()
            ),
            Family(
                id = "fam_midnight", ownerId = "host_dev", name = "Midnight Vibes",
                description = "Shayari, podcasts, and friendly acoustic rooms.",
                emblemEmoji = "🌙", announcement = "Open mic every night!",
                memberCount = 22L, activityScore = 5400L, listed = true,
                memberUids = listOf("host_dev"), createdAt = nowTs(), updatedAt = nowTs()
            )
        )
    )
    val families: StateFlow<List<Family>> = _families.asStateFlow()

    private val _familyMembersMap = MutableStateFlow(
        mapOf(
            "fam_phoenix" to listOf(
                FamilyMember(id = "host_riya", familyId = "fam_phoenix", userId = "host_riya", displayName = "Riya Verma", avatarEmoji = "🦋", role = "OWNER", status = "ACTIVE"),
                FamilyMember(id = demoUid, familyId = "fam_phoenix", userId = demoUid, displayName = "Aarav Sharma", avatarEmoji = "🎧", role = "CO_OWNER", status = "ACTIVE"),
                FamilyMember(id = "host_maya", familyId = "fam_phoenix", userId = "host_maya", displayName = "Maya Lounge", avatarEmoji = "💎", role = "MEMBER", status = "ACTIVE")
            ),
            "fam_neon" to listOf(
                FamilyMember(id = "host_zara", familyId = "fam_neon", userId = "host_zara", displayName = "Zara Gaming", avatarEmoji = "🎮", role = "OWNER", status = "ACTIVE"),
                FamilyMember(id = "host_kabir", familyId = "fam_neon", userId = "host_kabir", displayName = "Kabir Beats", avatarEmoji = "🔥", role = "ADMIN", status = "ACTIVE")
            ),
            "fam_midnight" to listOf(
                FamilyMember(id = "host_dev", familyId = "fam_midnight", userId = "host_dev", displayName = "Dev Malhotra", avatarEmoji = "🌙", role = "OWNER", status = "ACTIVE")
            )
        )
    )

    private val _familyMessagesMap = MutableStateFlow(
        mapOf(
            "fam_phoenix" to listOf(
                FamilyMessage(id = "fm_1", familyId = "fam_phoenix", senderId = "host_riya", senderName = "Riya Verma", text = "Welcome everyone to Royal Phoenix! 🔥", createdAt = nowTs()),
                FamilyMessage(id = "fm_2", familyId = "fam_phoenix", senderId = "host_maya", senderName = "Maya Lounge", text = "Let's hit 10,000 family glory tonight!", createdAt = nowTs())
            )
        )
    )

    private val _gameRecords = MutableStateFlow(
        listOf(
            GameRecord(
                id = "gr_1", userId = demoUid, gameType = "COLOR_PREDICTION",
                periodId = "30s_demo_1", selection = "GREEN", stakeCoins = 250L, wonCoins = 500L,
                outcome = "WIN", detail = "Result 3 (GREEN)", createdAt = nowTs()
            )
        )
    )
    val gameRecords: StateFlow<List<GameRecord>> = _gameRecords.asStateFlow()

    fun getRoom(roomId: String): VoiceRoom? = _rooms.value.firstOrNull { it.id == roomId }
    fun getRoomSeats(roomId: String): List<VoiceRoomSeat> = _roomSeatsMap.value[roomId].orEmpty()
    fun getRoomMessages(roomId: String): List<VoiceRoomMessage> = _roomMessagesMap.value[roomId].orEmpty()
    fun getSpeakerRequests(roomId: String): List<SpeakerRequest> =
        _speakerRequestsMap.value[roomId].orEmpty().filter { it.status == "PENDING" }
    fun getDirectMessages(convId: String): List<DirectMessage> = _directMessagesMap.value[convId].orEmpty()
    fun getFamilyMembers(familyId: String): List<FamilyMember> = _familyMembersMap.value[familyId].orEmpty()
    fun getFamilyMessages(familyId: String): List<FamilyMessage> = _familyMessagesMap.value[familyId].orEmpty()

    private fun syncMyProfileInDiscover() {
        val me = _myProfile.value
        _discoverUsers.update { list -> list.map { if (it.userId == demoUid) me else it } }
    }

    private fun addXp(deltaXp: Long, popularityDelta: Long = 0L) {
        _myProfile.update { current ->
            val newXp = current.xp + deltaXp
            current.copy(
                xp = newXp,
                level = SocialVerseCatalog.calculateLevelFromXp(newXp),
                popularityScore = current.popularityScore + popularityDelta,
                updatedAt = nowTs()
            )
        }
        syncMyProfileInDiscover()
    }

    private fun appendLedger(entryType: String, deltaCoins: Long, newBalance: Long, description: String, refId: String) {
        val entry = WalletLedgerEntry(
            id = nextId("led"),
            userId = demoUid,
            entryType = entryType,
            amountCoins = deltaCoins,
            balanceAfter = newBalance,
            description = description,
            referenceId = refId,
            createdAt = nowTs()
        )
        _walletLedger.update { listOf(entry) + it }
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
        _myProfile.update {
            it.copy(
                displayName = displayName.trim().take(38).ifBlank { it.displayName },
                bio = bio.trim().take(180),
                avatarEmoji = avatarEmoji.ifBlank { it.avatarEmoji },
                country = country.trim().take(36).ifBlank { it.country },
                language = language.trim().take(28).ifBlank { it.language },
                profileFrame = frame,
                nameColor = nameColor,
                chatBubbleStyle = chatBubble,
                updatedAt = nowTs()
            )
        }
        syncMyProfileInDiscover()
    }

    fun updatePrivacy(hideOnline: Boolean, hideLastSeen: Boolean, allowFriendRequests: Boolean, discoverable: Boolean) {
        _myPrivate.update {
            it.copy(hideOnline = hideOnline, hideLastSeen = hideLastSeen, allowFriendRequests = allowFriendRequests, updatedAt = nowTs())
        }
        _myProfile.update {
            it.copy(onlineVisible = !hideOnline, onlineStatus = if (hideOnline) "OFFLINE" else "ONLINE", discoverable = discoverable, updatedAt = nowTs())
        }
        syncMyProfileInDiscover()
    }

    fun toggleBlockUser(targetUid: String, block: Boolean) {
        _myPrivate.update { priv ->
            val next = if (block) (priv.blockedUids + targetUid).distinct() else priv.blockedUids.filter { it != targetUid }
            priv.copy(blockedUids = next, updatedAt = nowTs())
        }
    }

    fun claimDailyReward(): Result<Long> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (_myPrivate.value.lastDailyClaimDay == today) {
            return Result.failure(IllegalStateException("Daily reward already claimed today!"))
        }
        val reward = if (_myProfile.value.vipTier == "SVIP") 300L else 150L
        val newBal = _myPrivate.value.coinBalance + reward
        _myPrivate.update { it.copy(coinBalance = newBal, lastDailyClaimDay = today, updatedAt = nowTs()) }
        addXp(50L)
        appendLedger("DAILY_REWARD", reward, newBal, "Daily check-in reward (+50 XP)", today)
        return Result.success(reward)
    }

    fun claimPromoCoins(amount: Long, label: String) {
        val newBal = _myPrivate.value.coinBalance + amount
        _myPrivate.update { it.copy(coinBalance = newBal, updatedAt = nowTs()) }
        appendLedger("PROMO_CREDIT", amount, newBal, label, "promo_${System.currentTimeMillis()}")
    }

    fun activateVip(plan: VipPlan): Result<Unit> {
        val bal = _myPrivate.value.coinBalance
        if (bal < plan.virtualCoinUnlock) {
            return Result.failure(IllegalStateException("Need ${plan.virtualCoinUnlock} virtual coins to unlock ${plan.title}."))
        }
        val newBal = bal - plan.virtualCoinUnlock
        val expiry = System.currentTimeMillis() + plan.durationDays * 86_400_000L
        _myPrivate.update { it.copy(coinBalance = newBal, vipTier = plan.tier, vipExpiryMillis = expiry, updatedAt = nowTs()) }
        _myProfile.update {
            it.copy(
                vipTier = plan.tier,
                profileFrame = if (plan.tier == "SVIP") "SVIP_CROWN" else "ROYAL_GOLD",
                updatedAt = nowTs()
            )
        }
        addXp(250L)
        appendLedger("VIP_UNLOCK", -plan.virtualCoinUnlock, newBal, "Activated ${plan.title} (${plan.durationDays} days)", plan.tier)
        return Result.success(Unit)
    }

    fun createVoiceRoom(
        title: String,
        category: String,
        roomType: String,
        passwordCode: String,
        language: String,
        announcement: String,
        themeKey: String,
        maxSeats: Int,
        adultOnly: Boolean
    ): String {
        val me = _myProfile.value
        val roomId = nextId("room")
        val seatsCount = maxSeats.coerceIn(4, 12)
        val room = VoiceRoom(
            id = roomId,
            hostId = demoUid,
            hostName = me.displayName,
            title = title.trim().ifBlank { "${me.displayName}'s Lounge" },
            category = category.ifBlank { "Popular" },
            roomType = roomType,
            passwordCode = passwordCode.trim(),
            language = language.ifBlank { "English" },
            announcement = announcement.trim().ifBlank { "Welcome! Grab a mic seat." },
            themeKey = themeKey,
            maxSeats = seatsCount.toLong(),
            participantCount = 1L,
            status = "LIVE",
            publicListed = true,
            adultOnly = adultOnly,
            seatUids = listOf(demoUid),
            createdAt = nowTs(),
            updatedAt = nowTs()
        )
        val seats = (0 until seatsCount).map { i ->
            val isHost = i == 0
            VoiceRoomSeat(
                id = "seat_$i", roomId = roomId, seatIndex = i.toLong(),
                occupantUid = if (isHost) demoUid else "",
                occupantName = if (isHost) me.displayName else "",
                occupantAvatar = if (isHost) me.avatarEmoji else "",
                occupantVip = if (isHost) me.vipTier else "NONE",
                muted = false, locked = false, speaking = false, updatedAt = nowTs()
            )
        }
        _rooms.update { listOf(room) + it }
        _roomSeatsMap.update { it + (roomId to seats) }
        _roomMessagesMap.update {
            it + (roomId to listOf(
                VoiceRoomMessage(
                    id = nextId("rmsg"), roomId = roomId, senderId = demoUid,
                    senderName = me.displayName, senderVip = me.vipTier,
                    text = "🎙️ ${me.displayName} opened the live voice stage!", messageType = "SYSTEM", createdAt = nowTs()
                )
            ))
        }
        addXp(40L)
        return roomId
    }

    fun joinRoom(roomId: String) {
        val me = _myProfile.value
        _rooms.update { list ->
            list.map { r -> if (r.id == roomId) r.copy(participantCount = r.participantCount + 1L) else r }
        }
        val entrance = if (me.vipTier != "NONE") "✨ ${me.vipTier} ${me.displayName} made a grand entrance!" else "👋 ${me.displayName} joined the room"
        appendRoomMessage(roomId, me, entrance, "SYSTEM")
        addXp(15L)
    }

    fun leaveRoom(roomId: String) {
        _rooms.update { list ->
            list.map { r ->
                if (r.id == roomId) r.copy(
                    participantCount = (r.participantCount - 1L).coerceAtLeast(1L),
                    seatUids = r.seatUids.filter { it != demoUid }
                ) else r
            }
        }
        _roomSeatsMap.update { map ->
            val seats = map[roomId].orEmpty().map { s ->
                if (s.occupantUid == demoUid) s.copy(occupantUid = "", occupantName = "", occupantAvatar = "", occupantVip = "NONE", muted = false, speaking = false)
                else s
            }
            map + (roomId to seats)
        }
    }

    fun closeRoom(roomId: String) {
        _rooms.update { list -> list.filterNot { it.id == roomId } }
    }

    fun takeSeat(roomId: String, seatId: String) {
        val me = _myProfile.value
        _roomSeatsMap.update { map ->
            val seats = map[roomId].orEmpty().map { s ->
                when {
                    s.id == seatId -> s.copy(
                        occupantUid = demoUid, occupantName = me.displayName,
                        occupantAvatar = me.avatarEmoji, occupantVip = me.vipTier,
                        muted = false, updatedAt = nowTs()
                    )
                    s.occupantUid == demoUid -> s.copy(
                        occupantUid = "", occupantName = "", occupantAvatar = "", occupantVip = "NONE", muted = false
                    )
                    else -> s
                }
            }
            map + (roomId to seats)
        }
    }

    fun stepDownSeat(roomId: String, seatId: String) {
        _roomSeatsMap.update { map ->
            val seats = map[roomId].orEmpty().map { s ->
                if (s.id == seatId) s.copy(occupantUid = "", occupantName = "", occupantAvatar = "", occupantVip = "NONE", muted = false, speaking = false)
                else s
            }
            map + (roomId to seats)
        }
    }

    fun toggleSeatMute(roomId: String, seatId: String) {
        _roomSeatsMap.update { map ->
            val seats = map[roomId].orEmpty().map { s ->
                if (s.id == seatId) s.copy(muted = !s.muted, speaking = false) else s
            }
            map + (roomId to seats)
        }
    }

    fun toggleSeatLock(roomId: String, seatId: String) {
        _roomSeatsMap.update { map ->
            val seats = map[roomId].orEmpty().map { s ->
                if (s.id == seatId) s.copy(locked = !s.locked) else s
            }
            map + (roomId to seats)
        }
    }

    fun requestMic(roomId: String) {
        val me = _myProfile.value
        val req = SpeakerRequest(
            id = demoUid, roomId = roomId, requesterId = demoUid,
            requesterName = me.displayName, requesterAvatar = me.avatarEmoji,
            status = "PENDING", createdAt = nowTs(), updatedAt = nowTs()
        )
        _speakerRequestsMap.update { map ->
            val cur = map[roomId].orEmpty().filterNot { it.requesterId == demoUid }
            map + (roomId to (cur + req))
        }
    }

    fun approveSpeakerRequest(roomId: String, req: SpeakerRequest, approve: Boolean, emptySeat: VoiceRoomSeat?) {
        _speakerRequestsMap.update { map ->
            val updated = map[roomId].orEmpty().map {
                if (it.requesterId == req.requesterId) it.copy(status = if (approve) "APPROVED" else "REJECTED") else it
            }
            map + (roomId to updated)
        }
        if (approve && emptySeat != null) {
            _roomSeatsMap.update { map ->
                val seats = map[roomId].orEmpty().map { s ->
                    if (s.id == emptySeat.id) s.copy(
                        occupantUid = req.requesterId, occupantName = req.requesterName,
                        occupantAvatar = req.requesterAvatar, muted = false
                    ) else s
                }
                map + (roomId to seats)
            }
        }
    }

    fun appendRoomMessage(roomId: String, profile: UserPublicProfile, text: String, type: String = "TEXT") {
        val msg = VoiceRoomMessage(
            id = nextId("rmsg"), roomId = roomId, senderId = profile.userId,
            senderName = profile.displayName, senderVip = profile.vipTier,
            senderChatBubble = profile.chatBubbleStyle, text = text, messageType = type, createdAt = nowTs()
        )
        _roomMessagesMap.update { map ->
            val cur = map[roomId].orEmpty()
            map + (roomId to (cur + msg))
        }
    }

    fun updateRoomSettings(roomId: String, title: String, announcement: String, themeKey: String, roomType: String, passwordCode: String) {
        _rooms.update { list ->
            list.map { r ->
                if (r.id == roomId) r.copy(
                    title = title.trim().ifBlank { r.title },
                    announcement = announcement.trim(),
                    themeKey = themeKey,
                    roomType = roomType,
                    passwordCode = passwordCode.trim(),
                    updatedAt = nowTs()
                ) else r
            }
        }
    }

    fun sendGift(recipientId: String, recipientName: String, gift: VirtualGift, roomId: String?): Result<Unit> {
        val me = _myProfile.value
        if (gift.vipOnly && me.vipTier == "NONE") {
            return Result.failure(IllegalStateException("${gift.name} is exclusive to VIP/SVIP members."))
        }
        val bal = _myPrivate.value.coinBalance
        if (bal < gift.coinCost) {
            return Result.failure(IllegalStateException("Insufficient virtual coins! Need ${gift.coinCost} coins."))
        }
        val newBal = bal - gift.coinCost
        _myPrivate.update { it.copy(coinBalance = newBal, updatedAt = nowTs()) }
        addXp((gift.coinCost / 5L).coerceAtLeast(15L), gift.coinCost / 10L + 5L)

        val gtx = GiftTransaction(
            id = nextId("gtx"), senderId = demoUid, senderName = me.displayName,
            recipientId = recipientId, recipientName = recipientName,
            giftId = gift.id, giftName = gift.name, giftEmoji = gift.emoji,
            coinCost = gift.coinCost, roomId = roomId ?: "",
            participantUids = listOf(demoUid, recipientId).distinct(), createdAt = nowTs()
        )
        _giftTransactions.update { listOf(gtx) + it }
        appendLedger("GIFT_SENT", -gift.coinCost, newBal, "Sent ${gift.emoji} ${gift.name} to $recipientName", gtx.id)
        if (!roomId.isNullOrBlank()) {
            appendRoomMessage(roomId, me, "${gift.emoji} sent ${gift.name} (${gift.coinCost} coins) to $recipientName!", "GIFT")
        }
        return Result.success(Unit)
    }

    fun sendSocialRequest(isCp: Boolean, target: UserPublicProfile) {
        val me = _myProfile.value
        val req = SocialRequest(
            id = listOf(demoUid, target.userId).sorted().joinToString("_"),
            fromUserId = demoUid, fromName = me.displayName, fromAvatar = me.avatarEmoji,
            toUserId = target.userId, toName = target.displayName, toAvatar = target.avatarEmoji,
            participantUids = listOf(demoUid, target.userId), status = "PENDING",
            createdAt = nowTs(), updatedAt = nowTs()
        )
        val flow = if (isCp) _cpRequests else _friendRequests
        flow.update { list -> list.filterNot { it.id == req.id } + req }
    }

    fun respondSocialRequest(isCp: Boolean, req: SocialRequest, newStatus: String) {
        val flow = if (isCp) _cpRequests else _friendRequests
        flow.update { list -> list.map { if (it.id == req.id) it.copy(status = newStatus, updatedAt = nowTs()) else it } }
        if (newStatus == "ACCEPTED") {
            if (isCp) {
                val partnerId = if (req.fromUserId == demoUid) req.toUserId else req.fromUserId
                val partnerName = if (req.fromUserId == demoUid) req.toName else req.fromName
                _myProfile.update { it.copy(cpPartnerId = partnerId, cpPartnerName = partnerName, updatedAt = nowTs()) }
            } else {
                _myProfile.update { it.copy(friendsCount = it.friendsCount + 1L, updatedAt = nowTs()) }
            }
            syncMyProfileInDiscover()
        } else if (newStatus == "ENDED" && isCp) {
            _myProfile.update { it.copy(cpPartnerId = "", cpPartnerName = "", updatedAt = nowTs()) }
            syncMyProfileInDiscover()
        }
    }

    fun toggleFollow(target: UserPublicProfile, currentlyFollowing: Boolean) {
        if (currentlyFollowing) {
            _follows.update { list -> list.filterNot { it.followingId == target.userId } }
        } else {
            val rel = FollowRelation(
                id = "${demoUid}_${target.userId}", followerId = demoUid,
                followingId = target.userId, followingName = target.displayName,
                followingAvatar = target.avatarEmoji, createdAt = nowTs()
            )
            _follows.update { it + rel }
        }
    }

    fun openOrCreateDirectConversation(other: UserPublicProfile): String {
        val existing = _conversations.value.firstOrNull {
            it.type == "DIRECT" && other.userId in it.memberUids
        }
        if (existing != null) return existing.id
        val convId = listOf(demoUid, other.userId).sorted().joinToString("_")
        val conv = Conversation(
            id = convId, memberUids = listOf(demoUid, other.userId),
            type = "DIRECT", title = other.displayName, adminUid = demoUid,
            lastMessage = "Say hello!", createdAt = nowTs(), updatedAt = nowTs()
        )
        _conversations.update { listOf(conv) + it }
        return convId
    }

    fun createGroupConversation(title: String, announcement: String, memberUids: List<String>): String {
        val convId = nextId("grp")
        val conv = Conversation(
            id = convId, memberUids = (listOf(demoUid) + memberUids).distinct(),
            type = "GROUP", title = title.trim().ifBlank { "SocialVerse Squad" },
            announcement = announcement.trim(), adminUid = demoUid,
            lastMessage = "Group created", createdAt = nowTs(), updatedAt = nowTs()
        )
        _conversations.update { listOf(conv) + it }
        return convId
    }

    fun sendDirectMessage(
        conversation: Conversation,
        text: String,
        messageType: String,
        mediaUri: String?,
        replyToText: String?
    ) {
        val me = _myProfile.value
        val cleanText = text.trim().ifBlank { if (messageType == "IMAGE") "📷 Photo" else "🎙️ Voice Clip" }
        val msg = DirectMessage(
            id = nextId("dm"), memberUids = conversation.memberUids,
            senderId = demoUid, senderName = me.displayName, text = cleanText,
            messageType = messageType, mediaUri = mediaUri ?: "",
            replyToText = replyToText ?: "", deleted = false, createdAt = nowTs()
        )
        _directMessagesMap.update { map ->
            map + (conversation.id to (map[conversation.id].orEmpty() + msg))
        }
        _conversations.update { list ->
            list.map { if (it.id == conversation.id) it.copy(lastMessage = cleanText, updatedAt = nowTs()) else it }
        }
    }

    fun reactMessage(convId: String, msgId: String, emoji: String) {
        _directMessagesMap.update { map ->
            val updated = map[convId].orEmpty().map { if (it.id == msgId) it.copy(reaction = emoji) else it }
            map + (convId to updated)
        }
    }

    fun deleteMessage(convId: String, msgId: String) {
        _directMessagesMap.update { map ->
            val updated = map[convId].orEmpty().map { if (it.id == msgId) it.copy(deleted = true, text = "Message deleted") else it }
            map + (convId to updated)
        }
    }

    fun startPrivateCall(callee: UserPublicProfile): String {
        val me = _myProfile.value
        val callId = nextId("call")
        val call = PrivateCall(
            id = callId, callerId = demoUid, callerName = me.displayName,
            calleeId = callee.userId, calleeName = callee.displayName,
            participantUids = listOf(demoUid, callee.userId), status = "ACTIVE",
            durationSeconds = 0L, createdAt = nowTs(), updatedAt = nowTs()
        )
        _privateCalls.update { listOf(call) + it }
        return callId
    }

    fun updatePrivateCall(callId: String, status: String, durationSeconds: Long) {
        _privateCalls.update { list ->
            list.map { if (it.id == callId) it.copy(status = status, durationSeconds = durationSeconds, updatedAt = nowTs()) else it }
        }
    }

    fun createFamily(name: String, description: String, emblemEmoji: String): String {
        val me = _myProfile.value
        val famId = nextId("fam")
        val cleanName = name.trim().ifBlank { "${me.displayName}'s Clan" }
        val fam = Family(
            id = famId, ownerId = demoUid, name = cleanName,
            description = description.trim().ifBlank { "United SocialVerse family!" },
            emblemEmoji = emblemEmoji.ifBlank { "👑" }, announcement = "Welcome to $cleanName!",
            memberCount = 1L, activityScore = 100L, listed = true, memberUids = listOf(demoUid),
            createdAt = nowTs(), updatedAt = nowTs()
        )
        _families.update { listOf(fam) + it }
        _familyMembersMap.update {
            it + (famId to listOf(
                FamilyMember(id = demoUid, familyId = famId, userId = demoUid, displayName = me.displayName, avatarEmoji = me.avatarEmoji, role = "OWNER", status = "ACTIVE")
            ))
        }
        _myProfile.update { it.copy(familyId = famId, familyName = cleanName, updatedAt = nowTs()) }
        syncMyProfileInDiscover()
        return famId
    }

    fun joinFamily(family: Family) {
        val me = _myProfile.value
        _families.update { list ->
            list.map {
                if (it.id == family.id && demoUid !in it.memberUids) {
                    it.copy(memberCount = it.memberCount + 1L, activityScore = it.activityScore + 25L, memberUids = it.memberUids + demoUid)
                } else it
            }
        }
        _familyMembersMap.update { map ->
            val members = map[family.id].orEmpty().filterNot { it.userId == demoUid } +
                FamilyMember(id = demoUid, familyId = family.id, userId = demoUid, displayName = me.displayName, avatarEmoji = me.avatarEmoji, role = "MEMBER", status = "ACTIVE")
            map + (family.id to members)
        }
        _myProfile.update { it.copy(familyId = family.id, familyName = family.name, updatedAt = nowTs()) }
        syncMyProfileInDiscover()
    }

    fun leaveFamily(familyId: String) {
        _families.update { list ->
            list.map {
                if (it.id == familyId) it.copy(
                    memberCount = (it.memberCount - 1L).coerceAtLeast(1L),
                    memberUids = it.memberUids.filter { uid -> uid != demoUid }
                ) else it
            }
        }
        _familyMembersMap.update { map ->
            map + (familyId to map[familyId].orEmpty().filterNot { it.userId == demoUid })
        }
        _myProfile.update { it.copy(familyId = "", familyName = "", updatedAt = nowTs()) }
        syncMyProfileInDiscover()
    }

    fun sendFamilyMessage(familyId: String, text: String) {
        val me = _myProfile.value
        val msg = FamilyMessage(
            id = nextId("fmsg"), familyId = familyId, senderId = demoUid,
            senderName = me.displayName, text = text.trim(), createdAt = nowTs()
        )
        _familyMessagesMap.update { map -> map + (familyId to (map[familyId].orEmpty() + msg)) }
    }

    fun playMiniGame(
        gameType: String,
        periodId: String,
        selection: String,
        stakeCoins: Long,
        wonCoins: Long,
        outcome: String,
        detail: String
    ): Result<Long> {
        val currentBal = _myPrivate.value.coinBalance
        if (stakeCoins > 0 && currentBal < stakeCoins) {
            return Result.failure(IllegalStateException("Need $stakeCoins virtual coins to play."))
        }
        val netDelta = wonCoins - stakeCoins
        val updatedBalance = (currentBal + netDelta).coerceAtLeast(0L)
        _myPrivate.update { it.copy(coinBalance = updatedBalance, updatedAt = nowTs()) }
        addXp(if (outcome == "WIN") 35L else 15L, if (outcome == "WIN") 10L else 2L)

        val record = GameRecord(
            id = nextId("gr"), userId = demoUid, gameType = gameType,
            periodId = periodId, selection = selection, stakeCoins = stakeCoins,
            wonCoins = wonCoins, outcome = outcome, detail = detail, createdAt = nowTs()
        )
        _gameRecords.update { listOf(record) + it }
        if (netDelta != 0L) {
            appendLedger(
                entryType = if (netDelta > 0) "GAME_WIN" else "GAME_ENTRY",
                deltaCoins = netDelta,
                newBalance = updatedBalance,
                description = "$gameType: $detail",
                refId = record.id
            )
        }
        return Result.success(updatedBalance)
    }

    fun markNotificationRead(notifId: String) {
        _notifications.update { list -> list.map { if (it.id == notifId) it.copy(read = true) else it } }
    }
}
