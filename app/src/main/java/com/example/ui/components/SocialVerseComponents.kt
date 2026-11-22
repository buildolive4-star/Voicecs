package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SocialVerseCatalog
import com.example.data.UserPublicProfile
import com.example.ui.theme.CyanDeep
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.RoseMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletDeep

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassBorder,
    containerColor: Color = ObsidianSurface.copy(alpha = 0.86f),
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    gradient: List<Color> = listOf(ElectricViolet, CyanDeep)
) {
    val brush = if (enabled) {
        Brush.horizontalGradient(gradient)
    } else {
        Brush.horizontalGradient(listOf(ObsidianElevated, ObsidianElevated))
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(brush),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TextMuted
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun AvatarWithFrame(
    emoji: String,
    vipTier: String,
    profileFrame: String,
    size: Dp = 52.dp,
    isOnline: Boolean = false
) {
    val ringColors = when {
        vipTier == "SVIP" || profileFrame == "SVIP_CROWN" -> listOf(ImperialGold, RoseMagenta, ElectricViolet)
        vipTier == "VIP" || profileFrame == "ROYAL_GOLD" -> listOf(ImperialGold, GoldDeep)
        profileFrame == "CYBER_PRISM" -> listOf(NeonCyan, ElectricViolet)
        profileFrame == "NEON_RING" -> listOf(ElectricViolet, NeonCyan)
        else -> listOf(GlassBorder, GlassBorder)
    }
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(ObsidianElevated)
                .border(2.5.dp, Brush.sweepGradient(ringColors), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji.ifBlank { "🌟" },
                fontSize = (size.value * 0.44f).sp
            )
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .border(2.dp, ObsidianSurface, CircleShape)
            )
        }
    }
}

@Composable
fun CoinBalancePill(
    coins: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .testTag("coin_balance_pill"),
        shape = RoundedCornerShape(50),
        color = ObsidianElevated,
        border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.MonetizationOn,
                contentDescription = "Virtual Coins",
                tint = ImperialGold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "%,d".format(coins),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ImperialGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
fun VipAndLevelBadges(profile: UserPublicProfile) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = ElectricViolet.copy(alpha = 0.22f),
            border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.5f))
        ) {
            Text(
                text = "Lv.${profile.level} ${SocialVerseCatalog.levelTitle(profile.level)}",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        if (profile.vipTier != "NONE") {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (profile.vipTier == "SVIP") RoseMagenta.copy(alpha = 0.25f) else ImperialGold.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, ImperialGold)
            ) {
                Text(
                    text = "👑 ${profile.vipTier}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ImperialGold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        if (profile.cpPartnerName.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RoseMagenta.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, RoseMagenta.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "💞 CP: ${profile.cpPartnerName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFDA4AF),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(text = actionLabel, color = NeonCyan, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = VioletDeep.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ElectricViolet.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(14.dp))
                GradientButton(text = actionLabel, onClick = onAction)
            }
        }
    }
}

data class GiftBurstEvent(
    val id: Long = System.currentTimeMillis(),
    val emoji: String,
    val giftName: String,
    val senderName: String,
    val recipientName: String,
    val coinCost: Long
)

@Composable
fun AnimatedGiftBurstOverlay(
    event: GiftBurstEvent?,
    onFinished: () -> Unit
) {
    val scale = remember { Animatable(0.5f) }
    LaunchedEffect(event?.id) {
        if (event != null) {
            scale.snapTo(0.5f)
            scale.animateTo(1.18f, tween(360, easing = FastOutSlowInEasing))
            scale.animateTo(1.0f, tween(200))
            kotlinx.coroutines.delay(2200L)
            onFinished()
        }
    }
    AnimatedVisibility(
        visible = event != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        if (event != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(onClick = onFinished),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .padding(32.dp)
                        .scale(scale.value),
                    borderColor = ImperialGold,
                    containerColor = ObsidianElevated.copy(alpha = 0.96f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = event.emoji, fontSize = 68.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${event.giftName}!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ImperialGold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${event.senderName} → ${event.recipientName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${event.coinCost} Virtual Coins",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SafetyReportDialog(
    targetTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String) -> Unit
) {
    val reasons = listOf("Harassment or Abuse", "Spam or Scam", "Inappropriate Content", "Impersonation", "Voice Misconduct")
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        icon = { Icon(Icons.Filled.Flag, contentDescription = null, tint = RoseMagenta) },
        title = { Text("Report $targetTitle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select a reason for safety review:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                reasons.forEach { r ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = r },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedReason == r) ElectricViolet.copy(alpha = 0.25f) else ObsidianElevated,
                        border = BorderStroke(1.dp, if (selectedReason == r) ElectricViolet else GlassBorder)
                    ) {
                        Text(
                            text = r,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Additional details (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedReason, details) },
                colors = ButtonDefaults.buttonColors(containerColor = RoseMagenta)
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
