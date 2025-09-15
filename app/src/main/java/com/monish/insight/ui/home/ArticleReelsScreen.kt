package com.monish.insight.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.*
import com.monish.insight.data.model.Article
import com.monish.insight.ui.home.HomeViewModel
import androidx.compose.ui.platform.LocalContext



@OptIn(ExperimentalPagerApi::class)
@Composable
fun ArticleReelsScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val world by homeViewModel.worldArticles
    val india by homeViewModel.indiaArticles
    val sports by homeViewModel.sportsArticles
    val isLoading by homeViewModel.isLoading

    // Collect 5 from each → total 15
    val allArticles = remember(world, india, sports) {
        (world.take(5) + india.take(5) + sports.take(5))
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (allArticles.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No news available")
        }
        return
    }

    val pagerState = rememberPagerState()

    VerticalPager(
        count = allArticles.size + 1, // last page = completion screen
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        if (page < allArticles.size) {
            val article = allArticles[page]
            ArticlePage(article)
        } else {
            CompletionPage()
        }
    }
}

@Composable
fun ArticlePage(article: Article) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title
        Text(
            text = article.title ?: "No Title",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        // Image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(article.urlToImage)
                .crossfade(true)
                .build(),
            contentDescription = article.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Full Description
        Text(
            text = article.description ?: "No description available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CompletionPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🎉 You’ve completed today’s insights!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Keep your streak alive by checking in tomorrow.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
