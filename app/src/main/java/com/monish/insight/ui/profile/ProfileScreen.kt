package com.monish.insight.ui.profile

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monish.insight.R
import com.monish.insight.data.model.UserProfile
import com.monish.insight.ui.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    homeViewModel: HomeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { UserPreferences(context) }

    val userProfile by dataStore.userProfileFlow.collectAsState(initial = UserProfile())
    var editingProfile by remember { mutableStateOf(userProfile) }
    var isEditing by remember { mutableStateOf(false) }

    // TTS states
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    val worldArticles by homeViewModel.worldArticles
    val sportsArticles by homeViewModel.sportsArticles

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsReady = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    var showPopup by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("World") }

    fun saveProfile(profile: UserProfile) {
        scope.launch {
            dataStore.saveUserProfile(profile)
            isEditing = false
        }
    }

    fun speakPersonalizedHeadlines(
        scope: CoroutineScope,
        userName: String?,
        category: String,
        articles: List<String>,
        tts: TextToSpeech?
    ) {
        val greeting = buildString {
            append("Good ")
            append(
                if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 12) "morning" else "afternoon"
            )
            if (!userName.isNullOrBlank()) append(", $userName!")
            append(" Here are your top $category headlines.")
        }
        val sentences = listOf(greeting) + articles
        scope.launch(Dispatchers.Main) {
            isSpeaking = true
            for ((i, sentence) in sentences.withIndex()) {
                if (!isSpeaking) break
                tts?.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "utt_$i")
                var done = false
                tts?.setOnUtteranceProgressListener(object :
                    android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { done = true }
                    override fun onError(utteranceId: String?) { done = true }
                })
                while (!done && isSpeaking) {
                    kotlinx.coroutines.delay(100)
                }
            }
            isSpeaking = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Animated Profile Card
            AnimatedProfileCard(
                userProfile = userProfile,
                editingProfile = editingProfile,
                isEditing = isEditing,
                onEditingProfileChange = { editingProfile = it },
                onSave = { saveProfile(editingProfile) },
                onEdit = { isEditing = true },
                onCancel = {
                    editingProfile = userProfile
                    isEditing = false
                }
            )

            Spacer(Modifier.height(32.dp))

            // Theme Toggle Card
            ThemeToggleCard(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )

            Spacer(Modifier.height(32.dp))

            // News Reading Cards
            NewsButtonCard(
                title = "World News",
                icon = R.drawable.ic_default,
                onClick = {
                    selectedCategory = "World"
                    showPopup = true
                },
                enabled = isTtsReady
            )

            Spacer(Modifier.height(16.dp))

            NewsButtonCard(
                title = "Sports News",
                icon = R.drawable.ic_default,
                onClick = {
                    selectedCategory = "Sports"
                    showPopup = true
                },
                enabled = isTtsReady
            )

            Spacer(Modifier.height(40.dp))


        }

        // Anchor Speaking Popup
        AnimatedVisibility(
            visible = showPopup,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)
        ) {
            AnchorSpeakingPopup(
                selectedCategory = selectedCategory,
                isSpeaking = isSpeaking,
                onStart = {
                    if (!isSpeaking) {
                        val headlines = when (selectedCategory) {
                            "World" -> worldArticles
                            "Sports" -> sportsArticles
                            else -> emptyList()
                        }.take(5).mapIndexed { i, article ->
                            "News ${i + 1}: ${article.title ?: "Untitled"}"
                        }
                        speakPersonalizedHeadlines(scope, userProfile.name, selectedCategory, headlines, tts)
                    }
                },
                onStop = {
                    tts?.stop()
                    isSpeaking = false
                },
                onClose = {
                    showPopup = false
                    tts?.stop()
                    isSpeaking = false
                }
            )
        }
    }
}

@Composable
fun AnimatedProfileCard(
    userProfile: UserProfile,
    editingProfile: UserProfile,
    isEditing: Boolean,
    onEditingProfileChange: (UserProfile) -> Unit,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isEditing) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer {
                shadowElevation = 12.dp.toPx()
                shape = RoundedCornerShape(28.dp)
                clip = true
            },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            // Glassmorphic glare effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = androidx.compose.ui.geometry.Offset(0.3f, 0.2f),
                            radius = 800f
                        )
                    )
            )

            // Curved shine overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .graphicsLayer {
                        rotationZ = -15f
                        translationX = -50f
                        translationY = -80f
                    }
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with ring animation
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Animated ring
                    val infiniteTransition = rememberInfiniteTransition(label = "ring")
                    val ringScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "ringScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(ringScale)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ic_default),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                AnimatedContent(
                    targetState = isEditing,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "profile_content"
                ) { editing ->
                    if (editing) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedTextField(
                                value = editingProfile.name,
                                onValueChange = { onEditingProfileChange(editingProfile.copy(name = it)) },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = editingProfile.email,
                                onValueChange = { onEditingProfileChange(editingProfile.copy(email = it)) },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = editingProfile.location,
                                onValueChange = { onEditingProfileChange(editingProfile.copy(location = it)) },
                                label = { Text("Location") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = editingProfile.bio,
                                onValueChange = { onEditingProfileChange(editingProfile.copy(bio = it)) },
                                label = { Text("Bio") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = onCancel,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = onSave,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = userProfile.name.ifBlank { "Guest User" },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))

                            ProfileInfoRow(
                                label = "Email",
                                value = userProfile.email.ifBlank { "Not set" }
                            )

                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(0.3f),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Spacer(Modifier.height(12.dp))

                            ProfileInfoRow(
                                label = "Location",
                                value = userProfile.location.ifBlank { "Not set" }
                            )

                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(0.3f),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Spacer(Modifier.height(12.dp))

                            ProfileInfoRow(
                                label = "About",
                                value = userProfile.bio.ifBlank { "No bio available" }
                            )

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = onEdit,
                                modifier = Modifier.fillMaxWidth(0.8f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Edit Profile")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Composable
fun ThemeToggleCard(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = RoundedCornerShape(24.dp)
                clip = true
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            // Subtle gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Dark Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Toggle theme appearance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeToggle
                )
            }
        }
    }
}

@Composable
fun NewsButtonCard(
    title: String,
    icon: Int,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = 10.dp.toPx()
                shape = RoundedCornerShape(24.dp)
                clip = true
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box {
            // Animated gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Curved highlight
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        rotationZ = 45f
                        translationX = 200f
                        translationY = -50f
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Listen to latest headlines",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ExclusiveAnchorCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_anchor),
                contentDescription = "Exclusive Anchor",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Exclusive Anchor",
                style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
            )
        }
    }
}


@Composable
fun AnchorSpeakingPopup(
    selectedCategory: String,
    isSpeaking: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f)
                .graphicsLayer {
                    shadowElevation = 24.dp.toPx()
                    shape = RoundedCornerShape(36.dp)
                    clip = true
                },
            shape = RoundedCornerShape(36.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box {
                // Dynamic gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )

                // Radial glow effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = if (isSpeaking) 0.15f else 0.08f),
                                    Color.Transparent
                                ),
                                radius = 800f
                            )
                        )
                )

                // Curved light streaks
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            rotationZ = -25f
                            translationX = 150f
                            translationY = -150f
                        }
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Live indicator with animation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val alpha by rememberInfiniteTransition(label = "live").animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "liveAlpha"
                            )

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red.copy(alpha = alpha), CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "LIVE BROADCAST",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Daily Insights",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            selectedCategory,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Animated Anchor with pulse effect
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = if (isSpeaking) 1f else 0.95f,
                            targetValue = if (isSpeaking) 1.1f else 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale"
                        )

                        // Speaking indicator rings
                        if (isSpeaking) {
                            repeat(3) { index ->
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.5f,
                                    targetValue = 0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1500, delayMillis = index * 200),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "ring_$index"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(200.dp + (index * 30).dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.3f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        Image(
                            painter = painterResource(id = R.drawable.ic_anchor),
                            contentDescription = "News Anchor",
                            modifier = Modifier
                                .size(180.dp)
                                .scale(pulseScale)
                        )
                    }

                    // Status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isSpeaking) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        Color.Red,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "ON AIR",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        } else {
                            Text(
                                "Ready to broadcast",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Control buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onClose,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Close", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = if (isSpeaking) onStop else onStart,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSpeaking)
                                    Color(0xFFE53E3E)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                if (isSpeaking) "Stop" else "Start",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}