package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserPublicProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarEmoji: String = "🌟",
    val avatarColorHex: String = "#8B5CF6",
    val bio: String = "",
    val country: String = "India",
    val language: String = "English",
    val level: Long = 1L,
    val xp: Long = 0L,
    val vipTier: String = "NONE", // NONE, VIP, SVIP
    val profileFrame: String = "DEFAULT",
    val nameColor: String = "DEFAULT",
    val chatBubbleStyle: String = "DEFAULT",
    val familyId: String = "",
    val familyName: String = "",
    val cpPartnerId: String = "",
    val cpPartnerName: String = "",
    val followersCount: Long = 0L,
    val friendsCount: Long = 0L,
    val popularityScore: Long = 0L,
    val giftsReceivedValue: Long = 0L,
    val discoverable: Boolean = true,
    val adultEligible: Boolean = false,
    val onlineVisible: Boolean = true,
    val onlineStatus: String = "ONLINE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserPrivateAccount(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val fullName: String = "",
    val dobIso: String = "",
    val coinBalance: Long = 0L,
    val vipTier: String = "NONE",
    val vipExpiryMillis: Long = 0L,
    val lastDailyClaimDay: String = "",
    val blockedUids: List<String> = emptyList(),
    val hideOnline: Boolean = false,
    val hideLastSeen: Boolean = false,
    val allowFriendRequests: Boolean = true,
    val lastDeviceLabel: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletLedgerEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val entryType: String = "",
    val amountCoins: Long = 0L,
    val balanceAfter: Long = 0L,
    val description: String = "",
    val referenceId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserNotification(
    @DocumentId val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val read: Boolean = false,
    val targetId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VoiceRoom(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val title: String = "",
    val category: String = "Popular",
    val roomType: String = "PUBLIC", // PUBLIC, PRIVATE, PASSWORD, FRIENDS, VIP
    val passwordCode: String = "",
    val language: String = "English",
    val announcement: String = "",
    val themeKey: String = "NEON_VIOLET",
    val maxSeats: Long = 8L,
    val participantCount: Long = 1L,
    val status: String = "LIVE", // LIVE, CLOSED
    val publicListed: Boolean = true,
    val adultOnly: Boolean = false,
    val moderatorUids: List<String> = emptyList(),
    val seatUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VoiceRoomSeat(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val seatIndex: Long = 0L,
    val occupantUid: String = "",
    val occupantName: String = "",
    val occupantAvatar: String = "",
    val occupantVip: String = "NONE",
    val muted: Boolean = false,
    val locked: Boolean = false,
    val speaking: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VoiceRoomMessage(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderVip: String = "NONE",
    val senderChatBubble: String = "DEFAULT",
    val text: String = "",
    val messageType: String = "TEXT", // TEXT, GIFT, SYSTEM
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SpeakerRequest(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterAvatar: String = "🎤",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SocialRequest(
    @DocumentId val id: String = "",
    val fromUserId: String = "",
    val fromName: String = "",
    val fromAvatar: String = "🌟",
    val toUserId: String = "",
    val toName: String = "",
    val toAvatar: String = "🎧",
    val participantUids: List<String> = emptyList(),
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, CANCELLED, ENDED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FollowRelation(
    @DocumentId val id: String = "",
    val followerId: String = "",
    val followingId: String = "",
    val followingName: String = "",
    val followingAvatar: String = "🌟",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val memberUids: List<String> = emptyList(),
    val type: String = "DIRECT", // DIRECT, GROUP
    val title: String = "",
    val announcement: String = "",
    val adminUid: String = "",
    val lastMessage: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DirectMessage(
    @DocumentId val id: String = "",
    val memberUids: List<String> = emptyList(),
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val messageType: String = "TEXT", // TEXT, STICKER, IMAGE, VOICE_NOTE
    val mediaUri: String = "",
    val replyToText: String = "",
    val reaction: String = "",
    val deleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PrivateCall(
    @DocumentId val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val calleeId: String = "",
    val calleeName: String = "",
    val participantUids: List<String> = emptyList(),
    val status: String = "RINGING", // RINGING, ACTIVE, ENDED, DECLINED, MISSED
    val durationSeconds: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GiftTransaction(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val giftId: String = "",
    val giftName: String = "",
    val giftEmoji: String = "🎁",
    val coinCost: Long = 0L,
    val roomId: String = "",
    val participantUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Family(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val emblemEmoji: String = "👑",
    val announcement: String = "",
    val memberCount: Long = 1L,
    val activityScore: Long = 0L,
    val listed: Boolean = true,
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FamilyMember(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val avatarEmoji: String = "🌟",
    val role: String = "MEMBER", // OWNER, CO_OWNER, ADMIN, MODERATOR, MEMBER
    val status: String = "ACTIVE", // PENDING, ACTIVE
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FamilyMessage(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GameRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val gameType: String = "",
    val periodId: String = "",
    val selection: String = "",
    val stakeCoins: Long = 0L,
    val wonCoins: Long = 0L,
    val outcome: String = "WIN", // WIN, LOSS, DRAW
    val detail: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

// --- Static Platform Catalogs ---

data class VirtualGift(
    val id: String,
    val name: String,
    val emoji: String,
    val category: String,
    val coinCost: Long,
    val vipOnly: Boolean = false
)

data class VipPlan(
    val tier: String,
    val title: String,
    val subtitle: String,
    val virtualCoinUnlock: Long,
    val disclosedSubscriptionInfo: String,
    val durationDays: Int,
    val perks: List<String>
)

data class CommunityEvent(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val rewardLabel: String,
    val badgeEmoji: String
)

object SocialVerseCatalog {
    val gifts = listOf(
        VirtualGift("rose", "Rose", "🌹", "Basic", 10),
        VirtualGift("heart", "Heart", "💖", "Romantic", 50),
        VirtualGift("chocolate", "Chocolate", "🍫", "Basic", 100),
        VirtualGift("teddy", "Teddy Bear", "🧸", "Romantic", 200),
        VirtualGift("crown", "Royal Crown", "👑", "Luxury", 500),
        VirtualGift("diamond", "Diamond", "💎", "Premium", 1000, vipOnly = true),
        VirtualGift("car", "Luxury Car", "🏎️", "Special", 5000),
        VirtualGift("jet", "Private Jet", "🛩️", "Special", 10000, vipOnly = true)
    )

    val vipPlans = listOf(
        VipPlan(
            tier = "VIP",
            title = "SocialVerse VIP",
            subtitle = "Golden Aura & Exclusive Room Entrance",
            virtualCoinUnlock = 1500,
            disclosedSubscriptionInfo = "$4.99/mo equivalent entertainment tier (unlocked with 1,500 Virtual Coins)",
            durationDays = 30,
            perks = listOf(
                "Golden VIP Badge on profile & voice stage",
                "Luminous Gold Profile Frame & Username Tint",
                "Neon Violet Exclusive Chat Bubbles",
                "VIP Spotlight Room Entrance Banner",
                "Access to VIP-only Diamond Gift & Themes"
            )
        ),
        VipPlan(
            tier = "SVIP",
            title = "SocialVerse SVIP",
            subtitle = "Supreme Obsidian Crown & Animated Stage FX",
            virtualCoinUnlock = 4500,
            disclosedSubscriptionInfo = "$14.99/mo equivalent top-tier pass (unlocked with 4,500 Virtual Coins)",
            durationDays = 30,
            perks = listOf(
                "Animated Royal SVIP Crest & Crown Aura",
                "All VIP perks + Exclusive Private Jet Gift",
                "Custom Imperial Gold Chat Bubbles & Name Glow",
                "Priority Mic Queue & Premium Room Themes",
                "2x Daily Check-In Bonus Virtual Coins"
            )
        )
    )

    val stickers = listOf("🔥", "🎉", "💜", "👑", "🎧", "💎", "🫶", "🚀", "✨", "🥳", "🌹", "🎮")
    val avatarEmojis = listOf("🌟", "🎧", "🦊", "👑", "⚡", "🦋", "🐉", "💎", "🎤", "🌙", "🔥", "🎮")
    val roomCategories = listOf("All", "Popular", "New", "Music", "Friendship", "Entertainment", "Gaming", "International")
    val roomThemes = listOf("NEON_VIOLET", "CYBER_CYAN", "IMPERIAL_GOLD", "MIDNIGHT_ROSE")
    val profileFrames = listOf("DEFAULT", "NEON_RING", "ROYAL_GOLD", "CYBER_PRISM", "SVIP_CROWN")
    val nameColors = listOf("DEFAULT", "CYAN_GLOW", "GOLD_ELITE", "ROSE_NEON", "VIOLET_STAR")
    val chatBubbles = listOf("DEFAULT", "GLASS_VIOLET", "CYAN_PULSE", "GOLD_LUXURY")

    val communityEvents = listOf(
        CommunityEvent(
            id = "evt_voice_fest",
            title = "Neon Voice Stage Festival",
            subtitle = "Host or join live rooms to earn double XP & climb the Host Leaderboard.",
            category = "Voice Competition",
            rewardLabel = "+200 XP Boost",
            badgeEmoji = "🎤"
        ),
        CommunityEvent(
            id = "evt_color_arena",
            title = "Color Prediction Blitz",
            subtitle = "30s & 60s entertainment rounds open now with virtual coins only.",
            category = "Gaming Arena",
            rewardLabel = "Up to 4.5x Coins",
            badgeEmoji = "🎨"
        ),
        CommunityEvent(
            id = "evt_family_clash",
            title = "Family Glory Week",
            subtitle = "Create or rally your Family clan with gifts and mini-game wins.",
            category = "Family Event",
            rewardLabel = "Clan Emblem",
            badgeEmoji = "🏰"
        )
    )

    fun levelTitle(level: Long): String = when {
        level <= 1L -> "Beginner"
        level == 2L -> "Explorer"
        level == 3L -> "Regular"
        level == 4L -> "Social Star"
        level == 5L -> "Popular Member"
        level == 6L -> "VIP Star"
        level == 7L -> "Elite"
        else -> "Legend"
    }

    fun calculateLevelFromXp(xp: Long): Long {
        return when {
            xp < 200 -> 1L
            xp < 500 -> 2L
            xp < 1000 -> 3L
            xp < 2000 -> 4L
            xp < 3500 -> 5L
            xp < 6000 -> 6L
            xp < 10000 -> 7L
            else -> 8L
        }
    }

    fun nextLevelXpThreshold(level: Long): Long = when (level) {
        1L -> 200L
        2L -> 500L
        3L -> 1000L
        4L -> 2000L
        5L -> 3500L
        6L -> 6000L
        7L -> 10000L
        else -> 15000L
    }
}
