package com.example.data

import android.content.Context
import android.os.Build
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SocialVerseRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun <T> Flow<T>.guardFirestoreErrors(
        op: OperationType,
        path: String,
        uid: String
    ): Flow<T> = this.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
        throw error
    }

    // --- User Profile & Onboarding ---

    fun observeCurrentUserProfile(uid: String): Flow<UserPublicProfile?> = flow {
        require(uid.isNotBlank())
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) snap.toObject(UserPublicProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    else null
                }
                .guardFirestoreErrors(OperationType.GET, path, uid)
        )
    }

    fun observeCurrentUserPrivate(uid: String): Flow<UserPrivateAccount?> = flow {
        require(uid.isNotBlank())
        val path = "users_private/$uid"
        emitAll(
            db.collection("users_private").document(uid)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) snap.toObject(UserPrivateAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    else null
                }
                .guardFirestoreErrors(OperationType.GET, path, uid)
        )
    }

    fun observeDiscoverableUsers(uid: String): Flow<List<UserPublicProfile>> = flow {
        require(uid.isNotBlank())
        val path = "users"
        emitAll(
            db.collection("users")
                .whereEqualTo("discoverable", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(UserPublicProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun createInitialProfile(
        fullName: String,
        username: String,
        dobIso: String,
        adultEligible: Boolean,
        country: String,
        language: String,
        avatarEmoji: String
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val userEmail = auth.currentUser?.email ?: ""
            val cleanUsername = username.trim().lowercase().replace(Regex("[^a-z0-9_.]"), "").take(28).ifBlank { "user_${uid.take(6)}" }
            val cleanFullName = fullName.trim().take(50).ifBlank { "SocialVerse Star" }
            val displayName = cleanFullName.take(36)
            val deviceLabel = "${Build.MANUFACTURER} ${Build.MODEL}".take(70)

            val publicPayload = mapOf(
                "userId" to uid,
                "username" to cleanUsername,
                "displayName" to displayName,
                "avatarEmoji" to avatarEmoji.ifBlank { "🌟" },
                "avatarColorHex" to "#8B5CF6",
                "bio" to "Exploring SocialVerse voice stages & games!",
                "country" to country.ifBlank { "India" }.take(36),
                "language" to language.ifBlank { "English" }.take(28),
                "level" to 1L,
                "xp" to 100L,
                "vipTier" to "NONE",
                "profileFrame" to "DEFAULT",
                "nameColor" to "DEFAULT",
                "chatBubbleStyle" to "DEFAULT",
                "familyId" to "",
                "familyName" to "",
                "cpPartnerId" to "",
                "cpPartnerName" to "",
                "followersCount" to 0L,
                "friendsCount" to 0L,
                "popularityScore" to 10L,
                "giftsReceivedValue" to 0L,
                "discoverable" to true,
                "adultEligible" to adultEligible,
                "onlineVisible" to true,
                "onlineStatus" to "ONLINE",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val privatePayload = mapOf(
                "userId" to uid,
                "email" to userEmail.take(110),
                "fullName" to cleanFullName,
                "dobIso" to dobIso,
                "coinBalance" to 1000L,
                "vipTier" to "NONE",
                "vipExpiryMillis" to 0L,
                "lastDailyClaimDay" to "",
                "blockedUids" to emptyList<String>(),
                "hideOnline" to false,
                "hideLastSeen" to false,
                "allowFriendRequests" to true,
                "lastDeviceLabel" to deviceLabel,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val ledgerRef = db.collection("users_private").document(uid)
                .collection("wallet_ledger").document()
            val ledgerPayload = mapOf(
                "userId" to uid,
                "entryType" to "WELCOME_BONUS",
                "amountCoins" to 1000L,
                "balanceAfter" to 1000L,
                "description" to "Welcome to SocialVerse! Starter entertainment coins",
                "referenceId" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            )

            path = "users/$uid"
            db.runBatch { batch ->
                batch.set(db.collection("users").document(uid), publicPayload)
                batch.set(db.collection("users_private").document(uid), privatePayload)
                batch.set(ledgerRef, ledgerPayload)
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileCustomization(
        displayName: String,
        bio: String,
        avatarEmoji: String,
        country: String,
        language: String,
        profileFrame: String,
        nameColor: String,
        chatBubbleStyle: String
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            db.collection("users").document(uid).update(
                mapOf(
                    "displayName" to displayName.trim().take(38).ifBlank { "Star" },
                    "bio" to bio.trim().take(180),
                    "avatarEmoji" to avatarEmoji.ifBlank { "🌟" },
                    "country" to country.trim().take(36).ifBlank { "India" },
                    "language" to language.trim().take(28).ifBlank { "English" },
                    "profileFrame" to profileFrame,
                    "nameColor" to nameColor,
                    "chatBubbleStyle" to chatBubbleStyle,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updatePrivacySettings(
        hideOnline: Boolean,
        hideLastSeen: Boolean,
        allowFriendRequests: Boolean,
        discoverable: Boolean
    ): Result<Unit> {
        var path = "users_private"
        return try {
            val uid = requireUserId()
            path = "users_private/$uid"
            db.runBatch { batch ->
                batch.update(
                    db.collection("users_private").document(uid),
                    mapOf(
                        "hideOnline" to hideOnline,
                        "hideLastSeen" to hideLastSeen,
                        "allowFriendRequests" to allowFriendRequests,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    db.collection("users").document(uid),
                    mapOf(
                        "onlineVisible" to !hideOnline,
                        "onlineStatus" to if (hideOnline) "OFFLINE" else "ONLINE",
                        "discoverable" to discoverable,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleBlockUser(targetUid: String, block: Boolean): Result<Unit> {
        var path = "users_private"
        return try {
            val uid = requireUserId()
            require(targetUid.isNotBlank() && targetUid != uid)
            path = "users_private/$uid"
            db.collection("users_private").document(uid).update(
                mapOf(
                    "blockedUids" to if (block) FieldValue.arrayUnion(targetUid) else FieldValue.arrayRemove(targetUid),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteAccountData(): Result<Unit> {
        var path = "users_private"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            db.runBatch { batch ->
                batch.delete(db.collection("users").document(uid))
                batch.delete(db.collection("users_private").document(uid))
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- Wallet, Rewards, XP & VIP ---

    fun observeWalletLedger(uid: String): Flow<List<WalletLedgerEntry>> = flow {
        val path = "users_private/$uid/wallet_ledger"
        emitAll(
            db.collection("users_private").document(uid).collection("wallet_ledger")
                .snapshots()
                .map { snap ->
                    snap.toObjects(WalletLedgerEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun claimDailyReward(isSvip: Boolean): Result<Long> {
        var path = "users_private"
        return try {
            val uid = requireUserId()
            path = "users_private/$uid"
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val rewardAmount = if (isSvip) 300L else 150L
            val xpGain = 50L

            db.runTransaction { tx ->
                val privRef = db.collection("users_private").document(uid)
                val pubRef = db.collection("users").document(uid)
                val privSnap = tx.get(privRef)
                val pubSnap = tx.get(pubRef)
                val lastClaim = privSnap.getString("lastDailyClaimDay") ?: ""
                if (lastClaim == todayKey) {
                    throw IllegalStateException("Daily reward already claimed today! Come back tomorrow.")
                }
                val currentBalance = privSnap.getLong("coinBalance") ?: 0L
                val newBalance = currentBalance + rewardAmount
                val currentXp = pubSnap.getLong("xp") ?: 0L
                val newXp = currentXp + xpGain
                val newLevel = SocialVerseCatalog.calculateLevelFromXp(newXp)

                tx.update(privRef, mapOf(
                    "coinBalance" to newBalance,
                    "lastDailyClaimDay" to todayKey,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                tx.update(pubRef, mapOf(
                    "xp" to newXp,
                    "level" to newLevel,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                val ledgerRef = privRef.collection("wallet_ledger").document()
                tx.set(ledgerRef, mapOf(
                    "userId" to uid,
                    "entryType" to "DAILY_REWARD",
                    "amountCoins" to rewardAmount,
                    "balanceAfter" to newBalance,
                    "description" to "Daily check-in reward (+$xpGain XP)",
                    "referenceId" to todayKey,
                    "createdAt" to FieldValue.serverTimestamp()
                ))
            }.await()
            Result.success(rewardAmount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun claimPromoVirtualCoins(amount: Long, label: String): Result<Unit> {
        var path = "users_private"
        return try {
            val uid = requireUserId()
            require(amount in 1L..10000L)
            path = "users_private/$uid"
            db.runTransaction { tx ->
                val privRef = db.collection("users_private").document(uid)
                val privSnap = tx.get(privRef)
                val currentBalance = privSnap.getLong("coinBalance") ?: 0L
                val newBalance = currentBalance + amount
                tx.update(privRef, mapOf(
                    "coinBalance" to newBalance,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                val ledgerRef = privRef.collection("wallet_ledger").document()
                tx.set(ledgerRef, mapOf(
                    "userId" to uid,
                    "entryType" to "PROMO_CREDIT",
                    "amountCoins" to amount,
                    "balanceAfter" to newBalance,
                    "description" to label.take(140),
                    "referenceId" to "promo_${System.currentTimeMillis()}",
                    "createdAt" to FieldValue.serverTimestamp()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun activateVipPlan(plan: VipPlan): Result<Unit> {
        var path = "users_private"
        return try {
            val uid = requireUserId()
            path = "users_private/$uid"
            val expiry = System.currentTimeMillis() + plan.durationDays * 86_400_000L
            db.runTransaction { tx ->
                val privRef = db.collection("users_private").document(uid)
                val pubRef = db.collection("users").document(uid)
                val privSnap = tx.get(privRef)
                val pubSnap = tx.get(pubRef)
                val bal = privSnap.getLong("coinBalance") ?: 0L
                if (bal < plan.virtualCoinUnlock) {
                    throw IllegalStateException("Need ${plan.virtualCoinUnlock} virtual coins to unlock ${plan.title}.")
                }
                val newBal = bal - plan.virtualCoinUnlock
                val newXp = (pubSnap.getLong("xp") ?: 0L) + 250L
                val newLevel = SocialVerseCatalog.calculateLevelFromXp(newXp)
                tx.update(privRef, mapOf(
                    "coinBalance" to newBal,
                    "vipTier" to plan.tier,
                    "vipExpiryMillis" to expiry,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                tx.update(pubRef, mapOf(
                    "vipTier" to plan.tier,
                    "profileFrame" to if (plan.tier == "SVIP") "SVIP_CROWN" else "ROYAL_GOLD",
                    "xp" to newXp,
                    "level" to newLevel,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                val ledgerRef = privRef.collection("wallet_ledger").document()
                tx.set(ledgerRef, mapOf(
                    "userId" to uid,
                    "entryType" to "VIP_UNLOCK",
                    "amountCoins" to -plan.virtualCoinUnlock,
                    "balanceAfter" to newBal,
                    "description" to "Activated ${plan.title} (${plan.durationDays} days)",
                    "referenceId" to plan.tier,
                    "createdAt" to FieldValue.serverTimestamp()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Notifications ---

    fun observeNotifications(uid: String): Flow<List<UserNotification>> = flow {
        val path = "users_private/$uid/notifications"
        emitAll(
            db.collection("users_private").document(uid).collection("notifications")
                .snapshots()
                .map { snap ->
                    snap.toObjects(UserNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun markNotificationRead(notifId: String): Result<Unit> {
        var path = "users_private"
        return try {
            val uid = requireUserId()
            path = "users_private/$uid/notifications/$notifId"
            db.collection("users_private").document(uid)
                .collection("notifications").document(notifId)
                .update(mapOf("read" to true))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    private suspend fun pushNotification(
        recipientId: String,
        senderName: String,
        type: String,
        title: String,
        body: String,
        targetId: String = ""
    ) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val docRef = db.collection("users_private").document(recipientId)
                .collection("notifications").document()
            docRef.set(
                mapOf(
                    "recipientId" to recipientId,
                    "senderId" to uid,
                    "senderName" to senderName.take(45).ifBlank { "SocialVerse" },
                    "type" to type,
                    "title" to title.take(75),
                    "body" to body.take(190),
                    "read" to false,
                    "targetId" to targetId.take(120),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
            // Non-blocking notification failure should not break primary action
        }
    }

    // --- Live Voice Chat Rooms ---

    fun observeLiveRooms(uid: String): Flow<List<VoiceRoom>> = flow {
        val path = "voice_rooms"
        emitAll(
            db.collection("voice_rooms")
                .whereEqualTo("publicListed", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(VoiceRoom::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .filter { it.status == "LIVE" }
                        .sortedByDescending { it.participantCount }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeRoom(roomId: String, uid: String): Flow<VoiceRoom?> = flow {
        val path = "voice_rooms/$roomId"
        emitAll(
            db.collection("voice_rooms").document(roomId)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) snap.toObject(VoiceRoom::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    else null
                }
                .guardFirestoreErrors(OperationType.GET, path, uid)
        )
    }

    fun observeRoomSeats(roomId: String, uid: String): Flow<List<VoiceRoomSeat>> = flow {
        val path = "voice_rooms/$roomId/seats"
        emitAll(
            db.collection("voice_rooms").document(roomId).collection("seats")
                .whereEqualTo("roomId", roomId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(VoiceRoomSeat::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.seatIndex }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeRoomMessages(roomId: String, uid: String): Flow<List<VoiceRoomMessage>> = flow {
        val path = "voice_rooms/$roomId/messages"
        emitAll(
            db.collection("voice_rooms").document(roomId).collection("messages")
                .whereEqualTo("roomId", roomId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(VoiceRoomMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeSpeakerRequests(roomId: String, uid: String): Flow<List<SpeakerRequest>> = flow {
        val path = "voice_rooms/$roomId/speaker_requests"
        emitAll(
            db.collection("voice_rooms").document(roomId).collection("speaker_requests")
                .whereEqualTo("roomId", roomId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(SpeakerRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .filter { it.status == "PENDING" }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun createVoiceRoom(
        hostProfile: UserPublicProfile,
        title: String,
        category: String,
        roomType: String,
        passwordCode: String,
        language: String,
        announcement: String,
        themeKey: String,
        maxSeats: Int,
        adultOnly: Boolean
    ): Result<String> {
        var path = "voice_rooms"
        return try {
            val uid = requireUserId()
            val roomRef = db.collection("voice_rooms").document()
            val roomId = roomRef.id
            path = roomRef.path
            val clampedSeats = maxSeats.coerceIn(4, 12)

            val roomPayload = mapOf(
                "hostId" to uid,
                "hostName" to hostProfile.displayName.take(38).ifBlank { "Host" },
                "title" to title.trim().take(75).ifBlank { "${hostProfile.displayName}'s Lounge" },
                "category" to category.ifBlank { "Popular" },
                "roomType" to roomType,
                "passwordCode" to passwordCode.trim().take(30),
                "language" to language.ifBlank { "English" },
                "announcement" to announcement.trim().take(220).ifBlank { "Be respectful & have fun!" },
                "themeKey" to themeKey,
                "maxSeats" to clampedSeats.toLong(),
                "participantCount" to 1L,
                "status" to "LIVE",
                "publicListed" to true,
                "adultOnly" to adultOnly,
                "moderatorUids" to emptyList<String>(),
                "seatUids" to listOf(uid),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            db.runBatch { batch ->
                batch.set(roomRef, roomPayload)
                for (i in 0 until clampedSeats) {
                    val seatRef = roomRef.collection("seats").document("seat_$i")
                    val isHostSeat = i == 0
                    batch.set(
                        seatRef,
                        mapOf(
                            "roomId" to roomId,
                            "seatIndex" to i.toLong(),
                            "occupantUid" to if (isHostSeat) uid else "",
                            "occupantName" to if (isHostSeat) hostProfile.displayName.take(36) else "",
                            "occupantAvatar" to if (isHostSeat) hostProfile.avatarEmoji else "",
                            "occupantVip" to if (isHostSeat) hostProfile.vipTier else "NONE",
                            "muted" to false,
                            "locked" to false,
                            "speaking" to false,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                val welcomeMsgRef = roomRef.collection("messages").document()
                batch.set(
                    welcomeMsgRef,
                    mapOf(
                        "roomId" to roomId,
                        "senderId" to uid,
                        "senderName" to hostProfile.displayName.take(36),
                        "senderVip" to hostProfile.vipTier,
                        "senderChatBubble" to hostProfile.chatBubbleStyle,
                        "text" to "🎙️ ${hostProfile.displayName} opened the live voice stage!",
                        "messageType" to "SYSTEM",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            // Award host XP
            awardXp(uid, 40L)
            Result.success(roomId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun joinVoiceRoom(roomId: String, profile: UserPublicProfile): Result<Unit> {
        var path = "voice_rooms/$roomId"
        return try {
            val uid = requireUserId()
            val roomRef = db.collection("voice_rooms").document(roomId)
            roomRef.update(
                mapOf(
                    "participantCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val entranceText = if (profile.vipTier != "NONE") {
                "✨ ${profile.vipTier} ${profile.displayName} made a grand entrance!"
            } else {
                "👋 ${profile.displayName} joined the room"
            }
            roomRef.collection("messages").document().set(
                mapOf(
                    "roomId" to roomId,
                    "senderId" to uid,
                    "senderName" to profile.displayName.take(36),
                    "senderVip" to profile.vipTier,
                    "senderChatBubble" to profile.chatBubbleStyle,
                    "text" to entranceText,
                    "messageType" to "SYSTEM",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            awardXp(uid, 15L)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun leaveVoiceRoom(roomId: String, seatId: String?): Result<Unit> {
        var path = "voice_rooms/$roomId"
        return try {
            val uid = requireUserId()
            val roomRef = db.collection("voice_rooms").document(roomId)
            db.runTransaction { tx ->
                val roomSnap = tx.get(roomRef)
                if (roomSnap.exists() && roomSnap.getString("status") != "CLOSED") {
                    val currentCount = roomSnap.getLong("participantCount") ?: 1L
                    tx.update(
                        roomRef,
                        mapOf(
                            "participantCount" to (currentCount - 1L).coerceAtLeast(0L),
                            "seatUids" to FieldValue.arrayRemove(uid),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                if (!seatId.isNullOrBlank()) {
                    tx.update(
                        roomRef.collection("seats").document(seatId),
                        mapOf(
                            "occupantUid" to "",
                            "occupantName" to "",
                            "occupantAvatar" to "",
                            "occupantVip" to "NONE",
                            "muted" to false,
                            "speaking" to false,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun takeMicSeat(roomId: String, seatId: String, profile: UserPublicProfile): Result<Unit> {
        var path = "voice_rooms/$roomId/seats/$seatId"
        return try {
            val uid = requireUserId()
            db.runBatch { batch ->
                batch.update(
                    db.collection("voice_rooms").document(roomId).collection("seats").document(seatId),
                    mapOf(
                        "occupantUid" to uid,
                        "occupantName" to profile.displayName.take(36),
                        "occupantAvatar" to profile.avatarEmoji,
                        "occupantVip" to profile.vipTier,
                        "muted" to false,
                        "speaking" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    db.collection("voice_rooms").document(roomId),
                    mapOf(
                        "seatUids" to FieldValue.arrayUnion(uid),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun stepDownFromSeat(roomId: String, seatId: String, occupantUid: String): Result<Unit> {
        var path = "voice_rooms/$roomId/seats/$seatId"
        return try {
            requireUserId()
            db.runBatch { batch ->
                batch.update(
                    db.collection("voice_rooms").document(roomId).collection("seats").document(seatId),
                    mapOf(
                        "occupantUid" to "",
                        "occupantName" to "",
                        "occupantAvatar" to "",
                        "occupantVip" to "NONE",
                        "muted" to false,
                        "speaking" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (occupantUid.isNotBlank()) {
                    batch.update(
                        db.collection("voice_rooms").document(roomId),
                        mapOf(
                            "seatUids" to FieldValue.arrayRemove(occupantUid),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleSeatMute(roomId: String, seatId: String, muted: Boolean): Result<Unit> {
        var path = "voice_rooms/$roomId/seats/$seatId"
        return try {
            requireUserId()
            db.collection("voice_rooms").document(roomId).collection("seats").document(seatId)
                .update(
                    mapOf(
                        "muted" to muted,
                        "speaking" to if (muted) false else false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleSeatLock(roomId: String, seatId: String, locked: Boolean): Result<Unit> {
        var path = "voice_rooms/$roomId/seats/$seatId"
        return try {
            requireUserId()
            db.collection("voice_rooms").document(roomId).collection("seats").document(seatId)
                .update(
                    mapOf(
                        "locked" to locked,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun requestMicSeat(roomId: String, profile: UserPublicProfile): Result<Unit> {
        var path = "voice_rooms/$roomId/speaker_requests"
        return try {
            val uid = requireUserId()
            path = "voice_rooms/$roomId/speaker_requests/$uid"
            db.collection("voice_rooms").document(roomId)
                .collection("speaker_requests").document(uid)
                .set(
                    mapOf(
                        "roomId" to roomId,
                        "requesterId" to uid,
                        "requesterName" to profile.displayName.take(36),
                        "requesterAvatar" to profile.avatarEmoji,
                        "status" to "PENDING",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun respondSpeakerRequest(
        roomId: String,
        requesterId: String,
        approve: Boolean,
        targetSeat: VoiceRoomSeat?,
        requesterName: String,
        requesterAvatar: String
    ): Result<Unit> {
        var path = "voice_rooms/$roomId/speaker_requests/$requesterId"
        return try {
            requireUserId()
            db.runBatch { batch ->
                batch.update(
                    db.collection("voice_rooms").document(roomId)
                        .collection("speaker_requests").document(requesterId),
                    mapOf(
                        "status" to if (approve) "APPROVED" else "REJECTED",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (approve && targetSeat != null) {
                    batch.update(
                        db.collection("voice_rooms").document(roomId)
                            .collection("seats").document(targetSeat.id),
                        mapOf(
                            "occupantUid" to requesterId,
                            "occupantName" to requesterName.take(36),
                            "occupantAvatar" to requesterAvatar,
                            "occupantVip" to "NONE",
                            "muted" to false,
                            "speaking" to false,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun sendRoomMessage(roomId: String, profile: UserPublicProfile, text: String): Result<Unit> {
        var path = "voice_rooms/$roomId/messages"
        return try {
            val uid = requireUserId()
            val clean = text.trim().take(380)
            require(clean.isNotEmpty())
            val msgRef = db.collection("voice_rooms").document(roomId).collection("messages").document()
            path = msgRef.path
            msgRef.set(
                mapOf(
                    "roomId" to roomId,
                    "senderId" to uid,
                    "senderName" to profile.displayName.take(36),
                    "senderVip" to profile.vipTier,
                    "senderChatBubble" to profile.chatBubbleStyle,
                    "text" to clean,
                    "messageType" to "TEXT",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateRoomHostSettings(
        roomId: String,
        title: String,
        announcement: String,
        themeKey: String,
        roomType: String,
        passwordCode: String
    ): Result<Unit> {
        var path = "voice_rooms/$roomId"
        return try {
            requireUserId()
            val safeTitle = title.trim().take(75).let { if (it.length < 2) "Live Voice Lounge" else it }
            db.collection("voice_rooms").document(roomId).update(
                mapOf(
                    "title" to safeTitle,
                    "announcement" to announcement.trim().take(220),
                    "themeKey" to themeKey,
                    "roomType" to roomType,
                    "passwordCode" to passwordCode.trim().take(30),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun closeVoiceRoom(roomId: String): Result<Unit> {
        var path = "voice_rooms/$roomId"
        return try {
            requireUserId()
            db.collection("voice_rooms").document(roomId).update(
                mapOf(
                    "status" to "CLOSED",
                    "publicListed" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Virtual Gifts ---

    fun observeGiftTransactions(uid: String): Flow<List<GiftTransaction>> = flow {
        val path = "gift_transactions"
        emitAll(
            db.collection("gift_transactions")
                .whereArrayContains("participantUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(GiftTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun sendVirtualGift(
        senderProfile: UserPublicProfile,
        recipientId: String,
        recipientName: String,
        gift: VirtualGift,
        roomId: String?
    ): Result<Unit> {
        var path = "gift_transactions"
        return try {
            val uid = requireUserId()
            if (gift.vipOnly && senderProfile.vipTier == "NONE") {
                throw IllegalStateException("${gift.name} is exclusive to VIP/SVIP members.")
            }
            val txRef = db.collection("gift_transactions").document()
            path = txRef.path

            db.runTransaction { tx ->
                val privRef = db.collection("users_private").document(uid)
                val pubRef = db.collection("users").document(uid)
                val privSnap = tx.get(privRef)
                val pubSnap = tx.get(pubRef)
                val bal = privSnap.getLong("coinBalance") ?: 0L
                if (bal < gift.coinCost) {
                    throw IllegalStateException("Insufficient virtual coins! Need ${gift.coinCost} coins.")
                }
                val newBal = bal - gift.coinCost
                val xpGain = (gift.coinCost / 5L).coerceAtLeast(15L)
                val newXp = (pubSnap.getLong("xp") ?: 0L) + xpGain
                val newLevel = SocialVerseCatalog.calculateLevelFromXp(newXp)

                tx.update(privRef, mapOf(
                    "coinBalance" to newBal,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                tx.update(pubRef, mapOf(
                    "xp" to newXp,
                    "level" to newLevel,
                    "popularityScore" to FieldValue.increment(gift.coinCost / 10L + 5L),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))

                val ledgerRef = privRef.collection("wallet_ledger").document()
                tx.set(ledgerRef, mapOf(
                    "userId" to uid,
                    "entryType" to "GIFT_SENT",
                    "amountCoins" to -gift.coinCost,
                    "balanceAfter" to newBal,
                    "description" to "Sent ${gift.emoji} ${gift.name} to $recipientName",
                    "referenceId" to txRef.id,
                    "createdAt" to FieldValue.serverTimestamp()
                ))

                val giftPayload = mutableMapOf<String, Any>(
                    "senderId" to uid,
                    "senderName" to senderProfile.displayName.take(36),
                    "recipientId" to recipientId,
                    "recipientName" to recipientName.take(36),
                    "giftId" to gift.id,
                    "giftName" to gift.name,
                    "giftEmoji" to gift.emoji,
                    "coinCost" to gift.coinCost,
                    "participantUids" to listOf(uid, recipientId).distinct(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
                if (!roomId.isNullOrBlank()) {
                    giftPayload["roomId"] = roomId
                }
                tx.set(txRef, giftPayload)
            }.await()

            if (!roomId.isNullOrBlank()) {
                db.collection("voice_rooms").document(roomId).collection("messages").document().set(
                    mapOf(
                        "roomId" to roomId,
                        "senderId" to uid,
                        "senderName" to senderProfile.displayName.take(36),
                        "senderVip" to senderProfile.vipTier,
                        "senderChatBubble" to senderProfile.chatBubbleStyle,
                        "text" to "${gift.emoji} sent ${gift.name} (${gift.coinCost} coins) to $recipientName!",
                        "messageType" to "GIFT",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            if (recipientId != uid) {
                pushNotification(
                    recipientId = recipientId,
                    senderName = senderProfile.displayName,
                    type = "GIFT",
                    title = "Virtual Gift Received ${gift.emoji}",
                    body = "${senderProfile.displayName} sent you ${gift.name} (${gift.coinCost} coins)!",
                    targetId = roomId ?: txRef.id
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- Friends, CP Partner & Follows ---

    fun observeFriendRequests(uid: String): Flow<List<SocialRequest>> = flow {
        val path = "friend_requests"
        emitAll(
            db.collection("friend_requests")
                .whereArrayContains("participantUids", uid)
                .snapshots()
                .map { snap -> snap.toObjects(SocialRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeCpRequests(uid: String): Flow<List<SocialRequest>> = flow {
        val path = "cp_requests"
        emitAll(
            db.collection("cp_requests")
                .whereArrayContains("participantUids", uid)
                .snapshots()
                .map { snap -> snap.toObjects(SocialRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeFollows(uid: String): Flow<List<FollowRelation>> = flow {
        val path = "follows"
        emitAll(
            db.collection("follows")
                .whereEqualTo("followerId", uid)
                .snapshots()
                .map { snap -> snap.toObjects(FollowRelation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun sendSocialRequest(
        collectionName: String, // "friend_requests" or "cp_requests"
        myProfile: UserPublicProfile,
        targetUser: UserPublicProfile
    ): Result<Unit> {
        var path = collectionName
        return try {
            val uid = requireUserId()
            require(targetUser.userId.isNotBlank() && targetUser.userId != uid)
            val pairId = listOf(uid, targetUser.userId).sorted().joinToString("_")
            val docRef = db.collection(collectionName).document(pairId)
            path = docRef.path
            docRef.set(
                mapOf(
                    "fromUserId" to uid,
                    "fromName" to myProfile.displayName.take(36),
                    "fromAvatar" to myProfile.avatarEmoji,
                    "toUserId" to targetUser.userId,
                    "toName" to targetUser.displayName.take(36),
                    "toAvatar" to targetUser.avatarEmoji,
                    "participantUids" to listOf(uid, targetUser.userId),
                    "status" to "PENDING",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val isCp = collectionName == "cp_requests"
            pushNotification(
                recipientId = targetUser.userId,
                senderName = myProfile.displayName,
                type = if (isCp) "CP_REQUEST" else "FRIEND_REQUEST",
                title = if (isCp) "New CP / Partner Request" else "New Friend Request",
                body = "${myProfile.displayName} sent you a ${if (isCp) "virtual CP partner" else "friend"} request!",
                targetId = pairId
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun respondSocialRequest(
        collectionName: String,
        request: SocialRequest,
        newStatus: String,
        myProfile: UserPublicProfile
    ): Result<Unit> {
        var path = "$collectionName/${request.id}"
        return try {
            val uid = requireUserId()
            db.collection(collectionName).document(request.id).update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (newStatus == "ACCEPTED") {
                if (collectionName == "cp_requests") {
                    val partnerId = if (request.fromUserId == uid) request.toUserId else request.fromUserId
                    val partnerName = if (request.fromUserId == uid) request.toName else request.fromName
                    db.collection("users").document(uid).update(
                        mapOf(
                            "cpPartnerId" to partnerId,
                            "cpPartnerName" to partnerName,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } else {
                    db.collection("users").document(uid).update(
                        mapOf(
                            "friendsCount" to FieldValue.increment(1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    pushNotification(
                        recipientId = request.fromUserId,
                        senderName = myProfile.displayName,
                        type = "FRIEND_ACCEPTED",
                        title = "Friend Request Accepted",
                        body = "${myProfile.displayName} accepted your friend request!",
                        targetId = request.id
                    )
                }
            } else if (newStatus == "ENDED" && collectionName == "cp_requests") {
                db.collection("users").document(uid).update(
                    mapOf(
                        "cpPartnerId" to "",
                        "cpPartnerName" to "",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleFollow(targetUser: UserPublicProfile, currentlyFollowing: Boolean): Result<Unit> {
        var path = "follows"
        return try {
            val uid = requireUserId()
            val followId = "${uid}_${targetUser.userId}"
            path = "follows/$followId"
            if (currentlyFollowing) {
                db.collection("follows").document(followId).delete().await()
            } else {
                db.collection("follows").document(followId).set(
                    mapOf(
                        "followerId" to uid,
                        "followingId" to targetUser.userId,
                        "followingName" to targetUser.displayName.take(36),
                        "followingAvatar" to targetUser.avatarEmoji,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Direct & Group Conversations ---

    fun observeConversations(uid: String): Flow<List<Conversation>> = flow {
        val path = "conversations"
        emitAll(
            db.collection("conversations")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt?.seconds ?: 0L }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeDirectMessages(conversationId: String, uid: String): Flow<List<DirectMessage>> = flow {
        val path = "conversations/$conversationId/messages"
        emitAll(
            db.collection("conversations").document(conversationId).collection("messages")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(DirectMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun openOrCreateDirectConversation(myProfile: UserPublicProfile, other: UserPublicProfile): Result<String> {
        var path = "conversations"
        return try {
            val uid = requireUserId()
            val convId = listOf(uid, other.userId).sorted().joinToString("_")
            path = "conversations/$convId"
            val docRef = db.collection("conversations").document(convId)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                docRef.set(
                    mapOf(
                        "memberUids" to listOf(uid, other.userId),
                        "type" to "DIRECT",
                        "title" to "${myProfile.displayName} & ${other.displayName}".take(58),
                        "announcement" to "",
                        "adminUid" to uid,
                        "lastMessage" to "Say hello!",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(convId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun createGroupConversation(
        title: String,
        announcement: String,
        selectedMemberUids: List<String>
    ): Result<String> {
        var path = "conversations"
        return try {
            val uid = requireUserId()
            val members = (listOf(uid) + selectedMemberUids).distinct().take(50)
            require(members.size >= 2) { "Select at least 1 friend to form a group chat." }
            val docRef = db.collection("conversations").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "memberUids" to members,
                    "type" to "GROUP",
                    "title" to title.trim().take(58).ifBlank { "SocialVerse Squad" },
                    "announcement" to announcement.trim().take(180),
                    "adminUid" to uid,
                    "lastMessage" to "Group created",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun sendDirectMessage(
        conversation: Conversation,
        senderProfile: UserPublicProfile,
        text: String,
        messageType: String = "TEXT",
        mediaUri: String? = null,
        replyToText: String? = null
    ): Result<Unit> {
        var path = "conversations/${conversation.id}/messages"
        return try {
            val uid = requireUserId()
            val msgRef = db.collection("conversations").document(conversation.id).collection("messages").document()
            path = msgRef.path
            val cleanText = text.trim().take(900).ifBlank { if (messageType == "IMAGE") "📷 Photo" else "🎙️ Voice Clip" }
            val payload = mutableMapOf<String, Any>(
                "memberUids" to conversation.memberUids,
                "senderId" to uid,
                "senderName" to senderProfile.displayName.take(36),
                "text" to cleanText,
                "messageType" to messageType,
                "deleted" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            if (!mediaUri.isNullOrBlank()) payload["mediaUri"] = mediaUri.take(480)
            if (!replyToText.isNullOrBlank()) payload["replyToText"] = replyToText.take(180)

            db.runBatch { batch ->
                batch.set(msgRef, payload)
                batch.update(
                    db.collection("conversations").document(conversation.id),
                    mapOf(
                        "lastMessage" to cleanText.take(180),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            conversation.memberUids.filter { it != uid }.forEach { otherUid ->
                pushNotification(
                    recipientId = otherUid,
                    senderName = senderProfile.displayName,
                    type = "MESSAGE",
                    title = "Message from ${senderProfile.displayName}",
                    body = cleanText.take(120),
                    targetId = conversation.id
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun reactToDirectMessage(conversationId: String, messageId: String, emoji: String): Result<Unit> {
        var path = "conversations/$conversationId/messages/$messageId"
        return try {
            requireUserId()
            db.collection("conversations").document(conversationId)
                .collection("messages").document(messageId)
                .update(mapOf("reaction" to emoji.take(12)))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteDirectMessage(conversationId: String, messageId: String): Result<Unit> {
        var path = "conversations/$conversationId/messages/$messageId"
        return try {
            requireUserId()
            db.collection("conversations").document(conversationId)
                .collection("messages").document(messageId)
                .update(mapOf("deleted" to true, "text" to "Message deleted"))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Private Voice Calls ---

    fun observePrivateCalls(uid: String): Flow<List<PrivateCall>> = flow {
        val path = "private_calls"
        emitAll(
            db.collection("private_calls")
                .whereArrayContains("participantUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PrivateCall::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun startPrivateCall(myProfile: UserPublicProfile, callee: UserPublicProfile): Result<String> {
        var path = "private_calls"
        return try {
            val uid = requireUserId()
            val callRef = db.collection("private_calls").document()
            path = callRef.path
            callRef.set(
                mapOf(
                    "callerId" to uid,
                    "callerName" to myProfile.displayName.take(36),
                    "calleeId" to callee.userId,
                    "calleeName" to callee.displayName.take(36),
                    "participantUids" to listOf(uid, callee.userId),
                    "status" to "RINGING",
                    "durationSeconds" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            pushNotification(
                recipientId = callee.userId,
                senderName = myProfile.displayName,
                type = "CALL",
                title = "Incoming Private Voice Call",
                body = "${myProfile.displayName} is calling you on SocialVerse",
                targetId = callRef.id
            )
            Result.success(callRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updatePrivateCall(callId: String, status: String, durationSeconds: Long): Result<Unit> {
        var path = "private_calls/$callId"
        return try {
            requireUserId()
            db.collection("private_calls").document(callId).update(
                mapOf(
                    "status" to status,
                    "durationSeconds" to durationSeconds.coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Families ---

    fun observeFamilies(uid: String): Flow<List<Family>> = flow {
        val path = "families"
        emitAll(
            db.collection("families")
                .whereEqualTo("listed", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Family::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.activityScore }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeFamilyMembers(familyId: String, uid: String): Flow<List<FamilyMember>> = flow {
        val path = "families/$familyId/members"
        emitAll(
            db.collection("families").document(familyId).collection("members")
                .whereEqualTo("familyId", familyId)
                .snapshots()
                .map { snap -> snap.toObjects(FamilyMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeFamilyMessages(familyId: String, uid: String): Flow<List<FamilyMessage>> = flow {
        val path = "families/$familyId/messages"
        emitAll(
            db.collection("families").document(familyId).collection("messages")
                .whereEqualTo("familyId", familyId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FamilyMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun createFamily(
        ownerProfile: UserPublicProfile,
        name: String,
        description: String,
        emblemEmoji: String
    ): Result<String> {
        var path = "families"
        return try {
            val uid = requireUserId()
            val famRef = db.collection("families").document()
            val famId = famRef.id
            path = famRef.path
            val cleanName = name.trim().take(46).ifBlank { "${ownerProfile.displayName}'s Clan" }

            db.runBatch { batch ->
                batch.set(
                    famRef,
                    mapOf(
                        "ownerId" to uid,
                        "name" to cleanName,
                        "description" to description.trim().take(220).ifBlank { "United SocialVerse family!" },
                        "emblemEmoji" to emblemEmoji.ifBlank { "👑" },
                        "announcement" to "Welcome to $cleanName!",
                        "memberCount" to 1L,
                        "activityScore" to 100L,
                        "listed" to true,
                        "memberUids" to listOf(uid),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.set(
                    famRef.collection("members").document(uid),
                    mapOf(
                        "familyId" to famId,
                        "userId" to uid,
                        "displayName" to ownerProfile.displayName.take(36),
                        "avatarEmoji" to ownerProfile.avatarEmoji,
                        "role" to "OWNER",
                        "status" to "ACTIVE",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    db.collection("users").document(uid),
                    mapOf(
                        "familyId" to famId,
                        "familyName" to cleanName,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(famId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun joinFamily(family: Family, profile: UserPublicProfile): Result<Unit> {
        var path = "families/${family.id}/members"
        return try {
            val uid = requireUserId()
            val famRef = db.collection("families").document(family.id)
            path = "${famRef.path}/members/$uid"
            db.runBatch { batch ->
                batch.set(
                    famRef.collection("members").document(uid),
                    mapOf(
                        "familyId" to family.id,
                        "userId" to uid,
                        "displayName" to profile.displayName.take(36),
                        "avatarEmoji" to profile.avatarEmoji,
                        "role" to "MEMBER",
                        "status" to "ACTIVE",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    famRef,
                    mapOf(
                        "memberCount" to FieldValue.increment(1L),
                        "activityScore" to FieldValue.increment(25L),
                        "memberUids" to FieldValue.arrayUnion(uid),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    db.collection("users").document(uid),
                    mapOf(
                        "familyId" to family.id,
                        "familyName" to family.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun leaveFamily(familyId: String): Result<Unit> {
        var path = "families/$familyId"
        return try {
            val uid = requireUserId()
            val famRef = db.collection("families").document(familyId)
            db.runTransaction { tx ->
                val famSnap = tx.get(famRef)
                val memberCount = famSnap.getLong("memberCount") ?: 1L
                val ownerId = famSnap.getString("ownerId") ?: ""
                tx.delete(famRef.collection("members").document(uid))
                if (famSnap.exists()) {
                    if (memberCount <= 1L && ownerId == uid) {
                        tx.delete(famRef)
                    } else {
                        tx.update(
                            famRef,
                            mapOf(
                                "memberCount" to (memberCount - 1L).coerceAtLeast(1L),
                                "memberUids" to FieldValue.arrayRemove(uid),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                }
                tx.update(
                    db.collection("users").document(uid),
                    mapOf(
                        "familyId" to "",
                        "familyName" to "",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun sendFamilyMessage(familyId: String, profile: UserPublicProfile, text: String): Result<Unit> {
        var path = "families/$familyId/messages"
        return try {
            val uid = requireUserId()
            val msgRef = db.collection("families").document(familyId).collection("messages").document()
            path = msgRef.path
            msgRef.set(
                mapOf(
                    "familyId" to familyId,
                    "senderId" to uid,
                    "senderName" to profile.displayName.take(36),
                    "text" to text.trim().take(380),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- Mini-Games & Virtual Coin Scoring ---

    fun observeGameRecords(uid: String): Flow<List<GameRecord>> = flow {
        val path = "game_records"
        emitAll(
            db.collection("game_records")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(GameRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .guardFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun playMiniGame(
        gameType: String,
        periodId: String,
        selection: String,
        stakeCoins: Long,
        wonCoins: Long,
        outcome: String,
        detail: String
    ): Result<Long> {
        var path = "game_records"
        return try {
            val uid = requireUserId()
            val recordRef = db.collection("game_records").document()
            path = recordRef.path
            var updatedBalance = 0L

            db.runTransaction { tx ->
                val privRef = db.collection("users_private").document(uid)
                val pubRef = db.collection("users").document(uid)
                val privSnap = tx.get(privRef)
                val pubSnap = tx.get(pubRef)
                val currentBal = privSnap.getLong("coinBalance") ?: 0L
                if (stakeCoins > 0 && currentBal < stakeCoins) {
                    throw IllegalStateException("Need $stakeCoins virtual coins to play.")
                }
                val netDelta = wonCoins - stakeCoins
                updatedBalance = (currentBal + netDelta).coerceAtLeast(0L)
                val xpGain = if (outcome == "WIN") 35L else 15L
                val newXp = (pubSnap.getLong("xp") ?: 0L) + xpGain
                val newLevel = SocialVerseCatalog.calculateLevelFromXp(newXp)

                tx.update(privRef, mapOf(
                    "coinBalance" to updatedBalance,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                tx.update(pubRef, mapOf(
                    "xp" to newXp,
                    "level" to newLevel,
                    "popularityScore" to FieldValue.increment(if (outcome == "WIN") 10L else 2L),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                tx.set(recordRef, mapOf(
                    "userId" to uid,
                    "gameType" to gameType,
                    "periodId" to periodId.take(60),
                    "selection" to selection.take(60),
                    "stakeCoins" to stakeCoins,
                    "wonCoins" to wonCoins,
                    "outcome" to outcome,
                    "detail" to detail.take(190),
                    "createdAt" to FieldValue.serverTimestamp()
                ))
                if (netDelta != 0L) {
                    val ledgerRef = privRef.collection("wallet_ledger").document()
                    tx.set(ledgerRef, mapOf(
                        "userId" to uid,
                        "entryType" to if (netDelta > 0) "GAME_WIN" else "GAME_ENTRY",
                        "amountCoins" to netDelta,
                        "balanceAfter" to updatedBalance,
                        "description" to "$gameType: $detail".take(150),
                        "referenceId" to recordRef.id,
                        "createdAt" to FieldValue.serverTimestamp()
                    ))
                }
            }.await()
            Result.success(updatedBalance)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Safety Reports & XP ---

    suspend fun submitSafetyReport(
        targetId: String,
        targetType: String,
        reason: String,
        details: String
    ): Result<Unit> {
        var path = "reports"
        return try {
            val uid = requireUserId()
            val ref = db.collection("reports").document()
            path = ref.path
            ref.set(
                mapOf(
                    "reporterId" to uid,
                    "targetId" to targetId,
                    "targetType" to targetType,
                    "reason" to reason.take(58),
                    "details" to details.trim().take(280),
                    "status" to "SUBMITTED",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    private suspend fun awardXp(uid: String, deltaXp: Long) {
        try {
            db.runTransaction { tx ->
                val pubRef = db.collection("users").document(uid)
                val snap = tx.get(pubRef)
                val newXp = (snap.getLong("xp") ?: 0L) + deltaXp
                val newLevel = SocialVerseCatalog.calculateLevelFromXp(newXp)
                tx.update(pubRef, mapOf(
                    "xp" to newXp,
                    "level" to newLevel,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
            }.await()
        } catch (_: Exception) {
        }
    }
}
