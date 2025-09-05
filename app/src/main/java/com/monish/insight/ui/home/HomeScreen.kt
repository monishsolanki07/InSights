package com.monish.insight.ui.home

import android.app.Application
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monish.insight.data.local.BookmarkEntity
import com.monish.insight.ui.bookmarks.BookmarksViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    ),
    bookmarksViewModel: BookmarksViewModel = viewModel()
) {
    val newsArticles by homeViewModel.articles
    val isLoading by homeViewModel.isLoading

    // Debug logs for image URLs
    LaunchedEffect(newsArticles) {
        newsArticles.forEach {
            Log.d("HomeScreen", "Image URL: ${it.urlToImage}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "InSight",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    )
    { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Loading news...", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                newsArticles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No news available", style = MaterialTheme.typography.titleMedium)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(newsArticles.filter { !it.urlToImage.isNullOrBlank() }) { article ->
                            ArticleItem(
                                title = article.title ?: "",
                                description = article.description ?: "",
                                imageUrl = article.urlToImage,
                                onBookmarkClick = {
                                    val bookmark = BookmarkEntity(
                                        title = article.title,
                                        description = article.description,
                                        url = article.url,
                                        urlToImage = article.urlToImage,
                                        publishedAt = article.publishedAt
                                    )
                                    bookmarksViewModel.toggleBookmark(bookmark)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ArticleItem(
    title: String,
    description: String,
    imageUrl: String?,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ✅ Show article image if available
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Bookmark button with animation
            var clicked by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(targetValue = if (clicked) 1.2f else 1f)
            val buttonColor by animateColorAsState(
                targetValue = if (clicked) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer
            )

            Button(
                onClick = {
                    onBookmarkClick()
                    clicked = true
                },
                modifier = Modifier.scale(scale),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text("Bookmark")
            }

            LaunchedEffect(clicked) {
                if (clicked) {
                    kotlinx.coroutines.delay(300)
                    clicked = false
                }
            }
        }
    }
}
