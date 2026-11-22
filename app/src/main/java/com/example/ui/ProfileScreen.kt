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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Family
import com.example.data.FamilyMember
import com.example.data.FamilyMessage
import com.example.data.GiftTransaction
import com.example.data.SocialRequest
import com.example.data.SocialVerseCatalog
import com.example.data.UserPrivateAccount
import com.example.data.UserPublicProfile
import com.example.data.VipPlan
import com.example.data.WalletLedgerEntry
import com.example.ui.components.AvatarWithFrame
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.components.VipAndLevelBadges
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

enum class ProfileSubSection {
    MAIN, WALLET, VIP, CP, FAMILY, CUSTOMIZE, PRIVACY
}

@Composable
fun ProfileScreen(
    initialSubSection: ProfileSubSection = ProfileSubSection.MAIN,
    myProfile: UserPublicProfile,
    myPrivate: UserPrivateAccount?,
    walletLedger: List<WalletLedgerEntry>,
    giftTransactions: List<GiftTransaction>,
    cpRequests: List<SocialRequest>,
    families: List<Family>,
    activeFamilyMembers: List<FamilyMember>,
    activeFamilyMessages: List<FamilyMessage>,
    onSelectFamily: (String?) -> Unit,
    onClaimDaily: () -> Unit,
    onClaimPromoCoins: (Long, String) -> Unit,
    onActivateVip: (VipPlan) -> Unit,
    onRespondCp: (SocialRequest, String) -> Unit,
    onCreateFamily: (String, String, String) -> Unit,
    onJoinFamily: (Family) -> Unit,
    onLeaveFamily: (String) -> Unit,
    onSendFamilyChat: (String, String) -> Unit,
    onUpdateCustomization: (String, String, String, String, String, String, String, String) -> Unit,
    onUpdatePrivacy: (Boolean, Boolean, Boolean, Boolean) -> Unit,
    onUnblockUser: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit
) {
    var subSection by remember(initialSubSection) { mutableStateOf(initialSubSection) }

    if (subSection != ProfileSubSection.MAIN) {
        BackHandler { subSection = ProfileSubSection.MAIN }
    }

    when (subSection) {
        ProfileSubSection.WALLET -> WalletSection(
            coinBalance = myPrivate?.coinBalance ?: 0L,
            ledger = walletLedger,
            giftTransactions = giftTransactions,
            onBack = { subSection = ProfileSubSection.MAIN },
            onClaimDaily = onClaimDaily,
            onClaimPromoCoins = onClaimPromoCoins
        )
        ProfileSubSection.VIP -> VipSection(
            myProfile = myProfile,
            myPrivate = myPrivate,
            onBack = { subSection = ProfileSubSection.MAIN },
            onActivateVip = onActivateVip
        )
        ProfileSubSection.CP -> CpPartnerSection(
            myProfile = myProfile,
            cpRequests = cpRequests,
            onBack = { subSection = ProfileSubSection.MAIN },
            onRespondCp = onRespondCp
        )
        ProfileSubSection.FAMILY -> FamilySection(
            myProfile = myProfile,
            families = families,
            members = activeFamilyMembers,
            messages = activeFamilyMessages,
            onSelectFamily = onSelectFamily,
            onCreateFamily = onCreateFamily,
            onJoinFamily = onJoinFamily,
            onLeaveFamily = onLeaveFamily,
            onSendFamilyChat = onSendFamilyChat,
            onBack = { subSection = ProfileSubSection.MAIN }
        )
        ProfileSubSection.CUSTOMIZE -> CustomizeProfileSection(
            myProfile = myProfile,
            onSave = { dn, bio, av, ctry, lang, fr, nc, cb ->
                onUpdateCustomization(dn, bio, av, ctry, lang, fr, nc, cb)
                subSection = ProfileSubSection.MAIN
            },
            onBack = { subSection = ProfileSubSection.MAIN }
        )
        ProfileSubSection.PRIVACY -> PrivacyAndAccountSection(
            myProfile = myProfile,
            myPrivate = myPrivate,
            onSavePrivacy = onUpdatePrivacy,
            onUnblockUser = onUnblockUser,
            onDeleteAccount = onDeleteAccount,
            onSignOut = onSignOut,
            onBack = { subSection = ProfileSubSection.MAIN }
        )
        ProfileSubSection.MAIN -> {
            val nextXp = SocialVerseCatalog.nextLevelXpThreshold(myProfile.level)
            val progress = (myProfile.xp.toFloat() / nextXp.toFloat()).coerceIn(0f, 1f)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    GlassCard(borderColor = ElectricViolet.copy(alpha = 0.6f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarWithFrame(
                                emoji = myProfile.avatarEmoji,
                                vipTier = myProfile.vipTier,
                                profileFrame = myProfile.profileFrame,
                                size = 70.dp,
                                isOnline = myProfile.onlineVisible
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(myProfile.displayName, style = MaterialTheme.typography.headlineSmall)
                                Text("@${myProfile.username} • ID: ${myProfile.userId.take(8)}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Spacer(Modifier.height(6.dp))
                                VipAndLevelBadges(myProfile)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        if (myProfile.bio.isNotBlank()) {
                            Text(myProfile.bio, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Spacer(Modifier.height(10.dp))
                        }
                        // XP Progress Bar
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Level ${myProfile.level} (${SocialVerseCatalog.levelTitle(myProfile.level)})", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                            Text("${myProfile.xp} / $nextXp XP", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = NeonCyan,
                            trackColor = ObsidianElevated
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatBlock("${myPrivate?.coinBalance ?: 0L}", "Coins", ImperialGold)
                            StatBlock("${myProfile.friendsCount}", "Friends", NeonCyan)
                            StatBlock("${myProfile.popularityScore}", "Popularity", Color.White)
                        }
                    }
                }

                item {
                    ProfileMenuTile(Icons.Filled.AccountBalanceWallet, "Virtual Coin Wallet", "Balance, daily bonus & immutable ledger", ImperialGold) {
                        subSection = ProfileSubSection.WALLET
                    }
                }
                item {
                    ProfileMenuTile(Icons.Filled.WorkspacePremium, "VIP & SVIP Membership", "Frames, entrance effects & exclusive gifts", ElectricViolet) {
                        subSection = ProfileSubSection.VIP
                    }
                }
                item {
                    ProfileMenuTile(Icons.Filled.Groups, "Family Community", myProfile.familyName.ifBlank { "Create or join a Family clan" }, NeonCyan) {
                        subSection = ProfileSubSection.FAMILY
                    }
                }
                item {
                    ProfileMenuTile(Icons.Filled.Favorite, "CP / Partner System", myProfile.cpPartnerName.ifBlank { "Voluntary virtual social partner" }, RoseMagenta) {
                        subSection = ProfileSubSection.CP
                    }
                }
                item {
                    ProfileMenuTile(Icons.Filled.Edit, "Customize Profile & Look", "Frames, name color, chat bubbles & avatar", NeonCyan) {
                        subSection = ProfileSubSection.CUSTOMIZE
                    }
                }
                item {
                    ProfileMenuTile(Icons.Filled.Security, "Privacy, Sessions & Safety", "Online visibility, blocked users & sign out", TextSecondary) {
                        subSection = ProfileSubSection.PRIVACY
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
    }
}

@Composable
private fun ProfileMenuTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    GlassCard(onClick = onClick, contentPadding = PaddingValues(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Text("›", fontSize = 22.sp, color = TextMuted)
        }
    }
}

@Composable
fun WalletSection(
    coinBalance: Long,
    ledger: List<WalletLedgerEntry>,
    giftTransactions: List<GiftTransaction>,
    onBack: () -> Unit,
    onClaimDaily: () -> Unit,
    onClaimPromoCoins: (Long, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("Virtual Coin Wallet", style = MaterialTheme.typography.headlineSmall)
            }
        }
        item {
            GlassCard(borderColor = ImperialGold) {
                Text("Entertainment Coin Balance", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text("%,d Coins".format(coinBalance), style = MaterialTheme.typography.displaySmall, color = ImperialGold)
                Text("Virtual coins have zero cash redemption or withdrawal value.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GradientButton(text = "Daily Bonus", onClick = onClaimDaily, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { onClaimPromoCoins(500L, "SocialVerse Event Promo Pack") }, modifier = Modifier.weight(1f).height(48.dp)) {
                        Text("+500 Promo")
                    }
                }
            }
        }
        item { Text("Gift History (${giftTransactions.size})", style = MaterialTheme.typography.titleMedium) }
        items(giftTransactions.take(5), key = { "gt_${it.id}" }) { g ->
            GlassCard(contentPadding = PaddingValues(12.dp)) {
                Text("${g.giftEmoji} ${g.senderName} → ${g.recipientName} (${g.giftName})", style = MaterialTheme.typography.titleSmall)
                Text("${g.coinCost} Virtual Coins", style = MaterialTheme.typography.labelSmall, color = ImperialGold)
            }
        }
        item { Text("Immutable Transaction Ledger", style = MaterialTheme.typography.titleMedium) }
        items(ledger, key = { it.id }) { entry ->
            GlassCard(contentPadding = PaddingValues(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.description, style = MaterialTheme.typography.titleSmall)
                        Text(entry.entryType, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }
                    Text(
                        text = if (entry.amountCoins >= 0) "+${entry.amountCoins}c" else "${entry.amountCoins}c",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.amountCoins >= 0) EmeraldWin else RoseMagenta,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VipSection(
    myProfile: UserPublicProfile,
    myPrivate: UserPrivateAccount?,
    onBack: () -> Unit,
    onActivateVip: (VipPlan) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("VIP & SVIP Club", style = MaterialTheme.typography.headlineSmall)
            }
        }
        item {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(20.dp))) {
                Image(
                    painter = painterResource(id = R.drawable.img_vip_banner),
                    contentDescription = "VIP Crest",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(ObsidianBg.copy(alpha = 0.45f)))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text("Current Status: ${myProfile.vipTier}", style = MaterialTheme.typography.titleLarge, color = ImperialGold)
                    Text("Available Coins: %,d".format(myPrivate?.coinBalance ?: 0L), style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }
        }
        items(SocialVerseCatalog.vipPlans, key = { it.tier }) { plan ->
            GlassCard(borderColor = if (plan.tier == "SVIP") ImperialGold else ElectricViolet) {
                Text(plan.title, style = MaterialTheme.typography.titleLarge, color = if (plan.tier == "SVIP") ImperialGold else NeonCyan)
                Text(plan.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text(plan.disclosedSubscriptionInfo, style = MaterialTheme.typography.labelMedium, color = ImperialGold)
                Spacer(Modifier.height(8.dp))
                plan.perks.forEach { perk ->
                    Text("• $perk", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Spacer(Modifier.height(10.dp))
                GradientButton(
                    text = if (myProfile.vipTier == plan.tier) "Active (${plan.durationDays}d Pass)" else "Unlock (${plan.virtualCoinUnlock} Coins)",
                    enabled = myProfile.vipTier != plan.tier,
                    onClick = { onActivateVip(plan) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CpPartnerSection(
    myProfile: UserPublicProfile,
    cpRequests: List<SocialRequest>,
    onBack: () -> Unit,
    onRespondCp: (SocialRequest, String) -> Unit
) {
    val activeCp = cpRequests.firstOrNull { it.status == "ACCEPTED" }
    val pendingCp = cpRequests.filter { it.status == "PENDING" && it.toUserId == myProfile.userId }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("CP / Virtual Partner", style = MaterialTheme.typography.headlineSmall)
            }
            Text(
                "CP Partner is a voluntary virtual friendship badge for fun & gifting milestones — not a real-world relationship claim.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        item {
            if (activeCp != null) {
                val partnerName = if (activeCp.fromUserId == myProfile.userId) activeCp.toName else activeCp.fromName
                GlassCard(borderColor = RoseMagenta) {
                    Text("💞 Active Virtual Partner: $partnerName", style = MaterialTheme.typography.titleLarge, color = RoseMagenta)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onRespondCp(activeCp, "ENDED") },
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianElevated)
                    ) { Text("End Virtual Partnership", color = RoseMagenta) }
                }
            } else {
                GlassCard {
                    Text("No active CP Partner yet. Open any user profile on Home to send a CP request!", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }
        items(pendingCp, key = { it.id }) { req ->
            GlassCard(borderColor = RoseMagenta) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${req.fromAvatar} ${req.fromName} sent a CP request", style = MaterialTheme.typography.titleSmall)
                    Row {
                        IconButton(onClick = { onRespondCp(req, "ACCEPTED") }) { Icon(Icons.Filled.Check, contentDescription = "Accept", tint = EmeraldWin) }
                        IconButton(onClick = { onRespondCp(req, "REJECTED") }) { Icon(Icons.Filled.Close, contentDescription = "Reject", tint = RoseMagenta) }
                    }
                }
            }
        }
    }
}

@Composable
fun FamilySection(
    myProfile: UserPublicProfile,
    families: List<Family>,
    members: List<FamilyMember>,
    messages: List<FamilyMessage>,
    onSelectFamily: (String?) -> Unit,
    onCreateFamily: (String, String, String) -> Unit,
    onJoinFamily: (Family) -> Unit,
    onLeaveFamily: (String) -> Unit,
    onSendFamilyChat: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val myFamily = families.firstOrNull { it.id == myProfile.familyId }
    var showCreateDialog by remember { mutableStateOf(false) }
    var famChatInput by remember { mutableStateOf("") }

    LaunchedEffect(myFamily?.id) {
        onSelectFamily(myFamily?.id)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("Family Clans", style = MaterialTheme.typography.headlineSmall)
            }
            if (myFamily == null) {
                Button(onClick = { showCreateDialog = true }) { Text("+ Create") }
            } else {
                TextButton(onClick = { onLeaveFamily(myFamily.id) }) { Text("Leave", color = RoseMagenta) }
            }
        }

        if (myFamily != null) {
            GlassCard(borderColor = NeonCyan) {
                Text("${myFamily.emblemEmoji} ${myFamily.name}", style = MaterialTheme.typography.titleLarge, color = NeonCyan)
                Text("${myFamily.memberCount} members • Activity ${myFamily.activityScore}", style = MaterialTheme.typography.bodySmall, color = ImperialGold)
                Text(myFamily.announcement, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(members, key = { it.id }) { m ->
                    Surface(shape = RoundedCornerShape(10.dp), color = ObsidianElevated) {
                        Text("${m.avatarEmoji} ${m.displayName} (${m.role})", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            GlassCard(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(messages, key = { it.id }) { msg ->
                        Text("${msg.senderName}: ${msg.text}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = famChatInput,
                        onValueChange = { famChatInput = it },
                        placeholder = { Text("Family chat...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        onSendFamilyChat(myFamily.id, famChatInput)
                        famChatInput = ""
                    }) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = NeonCyan) }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(families, key = { it.id }) { fam ->
                    GlassCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${fam.emblemEmoji} ${fam.name}", style = MaterialTheme.typography.titleMedium)
                                Text(fam.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Button(onClick = { onJoinFamily(fam) }) { Text("Join") }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var emblem by remember { mutableStateOf("👑") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("Create Family Clan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Family Name") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("👑", "🔥", "⚡", "🐉", "💎", "🦅")) { e ->
                            FilterChip(selected = emblem == e, onClick = { emblem = e }, label = { Text(e) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(enabled = name.isNotBlank(), onClick = {
                    onCreateFamily(name, desc, emblem)
                    showCreateDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun CustomizeProfileSection(
    myProfile: UserPublicProfile,
    onSave: (String, String, String, String, String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var displayName by remember { mutableStateOf(myProfile.displayName) }
    var bio by remember { mutableStateOf(myProfile.bio) }
    var avatar by remember { mutableStateOf(myProfile.avatarEmoji) }
    var country by remember { mutableStateOf(myProfile.country) }
    var language by remember { mutableStateOf(myProfile.language) }
    var frame by remember { mutableStateOf(myProfile.profileFrame) }
    var nameColor by remember { mutableStateOf(myProfile.nameColor) }
    var bubble by remember { mutableStateOf(myProfile.chatBubbleStyle) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("Customize Profile", style = MaterialTheme.typography.headlineSmall)
            }
        }
        item { OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Display Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = language, onValueChange = { language = it }, label = { Text("Language") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Text("Avatar", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SocialVerseCatalog.avatarEmojis) { e -> FilterChip(selected = avatar == e, onClick = { avatar = e }, label = { Text(e) }) }
            }
        }
        item {
            Text("Profile Frame", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SocialVerseCatalog.profileFrames) { f -> FilterChip(selected = frame == f, onClick = { frame = f }, label = { Text(f) }) }
            }
        }
        item {
            Text("Chat Bubble Style", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SocialVerseCatalog.chatBubbles) { b -> FilterChip(selected = bubble == b, onClick = { bubble = b }, label = { Text(b) }) }
            }
        }
        item {
            GradientButton(
                text = "Save Customization",
                onClick = { onSave(displayName, bio, avatar, country, language, frame, nameColor, bubble) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PrivacyAndAccountSection(
    myProfile: UserPublicProfile,
    myPrivate: UserPrivateAccount?,
    onSavePrivacy: (Boolean, Boolean, Boolean, Boolean) -> Unit,
    onUnblockUser: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    var hideOnline by remember { mutableStateOf(myPrivate?.hideOnline ?: false) }
    var hideLastSeen by remember { mutableStateOf(myPrivate?.hideLastSeen ?: false) }
    var allowFriends by remember { mutableStateOf(myPrivate?.allowFriendRequests ?: true) }
    var discoverable by remember { mutableStateOf(myProfile.discoverable) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("Privacy & Account", style = MaterialTheme.typography.headlineSmall)
            }
        }
        item {
            GlassCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Hide Online Status")
                    Switch(checked = hideOnline, onCheckedChange = { hideOnline = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Hide Last Seen")
                    Switch(checked = hideLastSeen, onCheckedChange = { hideLastSeen = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Allow Friend Requests")
                    Switch(checked = allowFriends, onCheckedChange = { allowFriends = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Show in Discovery")
                    Switch(checked = discoverable, onCheckedChange = { discoverable = it })
                }
                Spacer(Modifier.height(8.dp))
                GradientButton(
                    text = "Save Privacy Settings",
                    onClick = { onSavePrivacy(hideOnline, hideLastSeen, allowFriends, discoverable) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            GlassCard {
                Text("Active Device Session", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                Text(myPrivate?.lastDeviceLabel?.ifBlank { "Android Device" } ?: "Android Device", style = MaterialTheme.typography.bodySmall)
                Text("Blocked Users: ${myPrivate?.blockedUids?.size ?: 0}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                myPrivate?.blockedUids?.forEach { buid ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(buid.take(14), style = MaterialTheme.typography.labelSmall)
                        TextButton(onClick = { onUnblockUser(buid) }) { Text("Unblock") }
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
        item {
            Button(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = RoseMagenta.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, RoseMagenta),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = RoseMagenta)
                Spacer(Modifier.width(8.dp))
                Text("Delete Account Data", color = RoseMagenta)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = ObsidianSurface,
            title = { Text("Delete SocialVerse Profile?") },
            text = { Text("This permanently deletes your SocialVerse profile and private wallet record.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDeleteAccount() },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseMagenta)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
