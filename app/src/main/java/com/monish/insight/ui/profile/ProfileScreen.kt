package com.monish.insight.ui.profile

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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

    // ---------- User Name Persistence ----------
    val dataStore = remember { UserPreferences(context) }
    var userName by remember { mutableStateOf<String?>(null) }
    var nameInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userName = dataStore.getUserName().first()
    }

    fun saveName(name: String) {
        scope.launch {
            dataStore.saveUserName(name)
            userName = name
        }
    }

    // ---------- TTS ----------
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

    fun speakHeadlines(
        scope: CoroutineScope,
        category: String,
        articles: List<String>
    ) {
        val intro = "Good day, I’m David from Insights. Here are today’s top headlines for $category."
        val sentences = listOf(intro) + articles

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

    // ---------- UI ----------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar + Name
        Image(
            painter = painterResource(id = R.drawable.ic_default),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        if (userName.isNullOrEmpty()) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Enter your name") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (nameInput.isNotBlank()) saveName(nameInput)
            }) {
                Text("Save Name")
            }
        } else {
            Text(
                text = "Hello, $userName 👋",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(Modifier.height(32.dp))

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

        // World News Button
        Button(
            onClick = { selectedCategory = "World"; showPopup = true },
            enabled = isTtsReady,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Read World News")
        }
        Spacer(Modifier.height(12.dp))

        // Sports News Button
        Button(
            onClick = { selectedCategory = "Sports"; showPopup = true },
            enabled = isTtsReady,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Read Sports News")
        }
    }

    // ---- Popup (same as before) ----
    if (showPopup) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.6f)
                    .clip(RoundedCornerShape(20.dp)),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_anchor),
                        contentDescription = "Anchor",
                        modifier = Modifier.size(160.dp)
                    )

                    Text(
                        "Daily Insights - $selectedCategory",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Button(onClick = {
                            if (!isSpeaking) {
                                val headlines = when (selectedCategory) {
                                    "World" -> worldArticles
                                    "Sports" -> sportsArticles
                                    else -> emptyList()
                                }.take(5).mapIndexed { i, article ->
                                    "News ${i + 1}: ${article.title ?: "Untitled"}"
                                }
                                speakHeadlines(scope, selectedCategory, headlines)
                            }
                        }) {
                            Text("Start")
                        }

                        Button(onClick = {
                            tts?.stop()
                            isSpeaking = false
                        }) {
                            Text("Stop")
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
