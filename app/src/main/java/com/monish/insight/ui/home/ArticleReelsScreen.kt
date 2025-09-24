package com.monish.insight.ui.home

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.*
import com.monish.insight.data.model.Article
import com.monish.insight.ui.bookmarks.BookmarksViewModel
import kotlinx.coroutines.delay

// ✅ Infinite circular pager for articles
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ArticleReelsScreen(homeViewModel: HomeViewModel = viewModel(),
                       bookmarksViewModel: BookmarksViewModel) {
    val context = LocalContext.current

    val world by homeViewModel.worldArticles
    val sports by homeViewModel.sportsArticles
    val isLoading by homeViewModel.isLoading

    val allArticles = remember(world, sports) { (world.take(5) + sports.take(5)) }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (allArticles.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No fresh insights today", color = Color.Gray)
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = 0)
    var completedCycle by remember { mutableStateOf(false) }

    VerticalPager(
        count = Int.MAX_VALUE, // infinite
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val index = page % allArticles.size
        ArticlePage(allArticles[index])

        // Show toast + animation only once per cycle
        LaunchedEffect(page) {
            if (page != 0 && index == allArticles.size - 1 && !completedCycle) {
                completedCycle = true
                Toast.makeText(
                    context,
                    "🎉 All articles read! Keep your streak alive!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (index == 0) completedCycle = false
        }
    }
}

@Composable
fun ArticlePage(article: Article) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Heading
        Text(
            text = article.title ?: "Untitled",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        Spacer(Modifier.height(16.dp))

        // Image inside Card
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(article.urlToImage)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(16.dp))

        // Description inside Card
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = article.description ?: "Details not provided",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
fun CompletionAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -600f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(1500))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🎉 You’ve completed today’s insights!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Maintain your streak — come back tomorrow for fresh perspectives.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
}
