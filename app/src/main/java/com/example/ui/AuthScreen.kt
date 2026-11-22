package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.SocialVerseCatalog
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
import com.example.ui.theme.RoseMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    scope.launch {
        try {
            val cred = credentialManager.getCredential(context, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()
    scope.launch {
        try {
            val activity = context as? Activity ?: throw IllegalStateException("Activity context required")
            val cred = credentialManager.getCredential(activity, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOutUser(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isAutoChecking by remember { mutableStateOf(true) }
    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var slideIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isAutoChecking = false
                onAuthSuccess()
            },
            onUnauthenticated = {
                isAutoChecking = false
            },
            scope = scope
        )
    }

    val onboardingSlides = listOf(
        Triple(
            "Live Voice Stages",
            "Host 8-seat rooms, sing, chill, and send animated virtual gifts in real time.",
            Icons.Filled.GraphicEq
        ),
        Triple(
            "Social Gaming Arena",
            "Play Color Prediction, Ludo, Spin Wheel & Trivia with entertainment-only virtual coins.",
            Icons.Filled.SportsEsports
        ),
        Triple(
            "VIP, Families & Safety",
            "Build loyal Family clans, unlock SVIP frames, and connect safely with age & moderation guards.",
            Icons.Filled.Security
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ObsidianDeep, ObsidianBg, Color(0xFF170F34))
                )
            )
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "SocialVerse Stage",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, ObsidianBg.copy(alpha = 0.88f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "SOCIALVERSE",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Text(
                            text = "Voice Chat • Virtual Gifts • Mini-Games",
                            style = MaterialTheme.typography.titleSmall,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Interactive Onboarding Carousel
                val slide = onboardingSlides[slideIndex]
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ElectricViolet.copy(alpha = 0.45f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(ElectricViolet, CyanDeep))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(slide.third, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(slide.first, style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Spacer(Modifier.height(4.dp))
                            Text(slide.second, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        onboardingSlides.indices.forEach { i ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .height(8.dp)
                                    .width(if (i == slideIndex) 26.dp else 10.dp)
                                    .clip(CircleShape)
                                    .background(if (i == slideIndex) NeonCyan else ObsidianElevated)
                                    .clickable { slideIndex = i }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("What awaits inside", style = MaterialTheme.typography.titleMedium, color = ImperialGold)
                    Spacer(Modifier.height(8.dp))
                    val highlights = listOf(
                        "8-seat live voice stages & private 1-on-1 voice calls",
                        "Animated virtual gifts, VIP / SVIP frames & Family clans",
                        "Color Prediction (virtual coins only), Ludo, Spin Wheel & Quiz"
                    )
                    highlights.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldWin,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(item, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = RoseMagenta,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                if (isAutoChecking) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(Modifier.height(8.dp))
                    Text("Checking session...", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                } else {
                    Button(
                        onClick = {
                            errorMessage = null
                            isSigningIn = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isSigningIn = false
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isSigningIn = false
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isSigningIn = false
                                }
                            )
                        },
                        enabled = !isSigningIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricViolet,
                            contentColor = Color.White
                        )
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Signing in...", style = MaterialTheme.typography.titleMedium)
                        } else {
                            Text("Continue with Google", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Virtual coins are entertainment-only credits with no cash value or withdrawals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(
    defaultDisplayName: String,
    defaultEmail: String,
    onCreateProfile: (
        fullName: String,
        username: String,
        dobIso: String,
        adultEligible: Boolean,
        country: String,
        language: String,
        avatarEmoji: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf(defaultDisplayName.ifBlank { "" }) }
    var username by remember {
        mutableStateOf(
            defaultEmail.substringBefore("@")
                .lowercase()
                .replace(Regex("[^a-z0-9_]"), "")
                .take(18)
        )
    }
    var birthYear by remember { mutableStateOf("2000") }
    var birthMonth by remember { mutableStateOf("05") }
    var birthDay by remember { mutableStateOf("15") }
    var country by remember { mutableStateOf("India") }
    var language by remember { mutableStateOf("English") }
    var selectedEmoji by remember { mutableStateOf(SocialVerseCatalog.avatarEmojis.first()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ObsidianDeep, ObsidianBg)))
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Create Your SocialVerse Identity", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Pick your stage avatar, handle, and birth year for age-appropriate room safety.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Text("Choose Avatar", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SocialVerseCatalog.avatarEmojis) { emoji ->
                    Surface(
                        modifier = Modifier
                            .size(54.dp)
                            .clickable { selectedEmoji = emoji },
                        shape = CircleShape,
                        color = if (selectedEmoji == emoji) ElectricViolet.copy(alpha = 0.3f) else ObsidianElevated,
                        border = BorderStroke(
                            2.dp,
                            if (selectedEmoji == emoji) NeonCyan else GlassBorder
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 26.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("setup_fullname_input")
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username (letters, numbers, _)") },
                prefix = { Text("@") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("setup_username_input")
            )

            Text("Date of Birth (Age Safety Check)", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { if (it.length <= 4) birthYear = it.filter(Char::isDigit) },
                    label = { Text("Year") },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = birthMonth,
                    onValueChange = { if (it.length <= 2) birthMonth = it.filter(Char::isDigit) },
                    label = { Text("Month") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = birthDay,
                    onValueChange = { if (it.length <= 2) birthDay = it.filter(Char::isDigit) },
                    label = { Text("Day") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            if (validationError != null) {
                Text(validationError ?: "", color = RoseMagenta, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(6.dp))
            GradientButton(
                text = "Enter SocialVerse (+1,000 Coins)",
                onClick = {
                    val y = birthYear.toIntOrNull() ?: 0
                    val m = birthMonth.toIntOrNull() ?: 0
                    val d = birthDay.toIntOrNull() ?: 0
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val age = currentYear - y
                    when {
                        fullName.trim().length < 2 -> validationError = "Please enter your full name."
                        username.trim().length < 3 -> validationError = "Username must be at least 3 characters."
                        y !in 1920..currentYear || m !in 1..12 || d !in 1..31 ->
                            validationError = "Please enter a valid date of birth."
                        age < 13 -> validationError = "Minimum age requirement is 13 years."
                        else -> {
                            validationError = null
                            val dobIso = "%04d-%02d-%02d".format(y, m, d)
                            onCreateProfile(
                                fullName.trim(),
                                username.trim(),
                                dobIso,
                                age >= 18,
                                country.trim().ifBlank { "India" },
                                language.trim().ifBlank { "English" },
                                selectedEmoji
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("complete_setup_button")
            )
        }
    }
}
