package com.monish.insight.ui.profile

import android.app.Activity
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monish.insight.R
import com.monish.insight.ui.home.HomeViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    homeViewModel: HomeViewModel
) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // Observe articles from HomeViewModel
    val newsArticles = homeViewModel.articles.value

    // TTS initialization
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
    var isSpeaking by remember { mutableStateOf(false) }
    var pausedText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // Theme toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { enabled -> onThemeToggle(enabled) }
            )
        }

        Spacer(Modifier.height(32.dp))

        // Button to show TTS popup
        Button(
            onClick = { showPopup = true },
            enabled = isTtsReady
        ) {
            Text(if (isTtsReady) "Read Top 5 Headlines" else "Initializing...")
        }

        // Popup for TTS controls
        if (showPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.5f)
                        .clip(RoundedCornerShape(16.dp)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Speaker image
                        Image(
                            painter = painterResource(id = R.drawable.ic_speaker),
                            contentDescription = "Speaker",
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        // Play / Pause buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(onClick = {
                                if (!isSpeaking) {
                                    val topHeadlines = newsArticles
                                        .take(5)
                                        .mapIndexed { index, article ->
                                            "News ${index + 1}: ${article.title ?: "Untitled"}"
                                        }
                                    val textToRead = buildString {
                                        append("Let's hear today's insights. ")
                                        append(topHeadlines.joinToString(". "))
                                    }
                                    tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "ttsId")
                                    isSpeaking = true
                                }
                            }) {
                                Text("Play")
                            }

                            Button(onClick = {
                                tts?.stop()
                                isSpeaking = false
                            }) {
                                Text("Pause/Stop")
                            }

                            Button(onClick = {
                                showPopup = false
                                tts?.stop()
                                isSpeaking = false
                            }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}
