package com.monish.insight.ui.home

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.monish.insight.R
import com.monish.insight.data.local.BookmarkEntity
import com.monish.insight.ui.bookmarks.BookmarksViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.res.painterResource



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
    val indiaArticles by homeViewModel.indiaArticles
    val worldArticles by homeViewModel.worldArticles
    val isLoading by homeViewModel.isLoading

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("India News", "World News")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("InSight") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loading / Content
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    val articlesToShow = if (selectedTab == 0) indiaArticles else worldArticles
                    val limitedArticles = articlesToShow.take(10) // limit to 10

                    // Prefetch images off main thread
                    val context = LocalContext.current
                    LaunchedEffect(limitedArticles) {
                        withContext(Dispatchers.IO) {
                            val imageLoader = coil.ImageLoader(context)
                            limitedArticles.mapNotNull { it.urlToImage }.forEach { url ->
                                try {
                                    val request = ImageRequest.Builder(context)
                                        .data(url)
                                        .size(800) // scale image to reduce memory
                                        .build()
                                    imageLoader.enqueue(request)
                                } catch (_: Exception) {
                                    // ignore prefetch errors
                                }
                            }
                        }
                    }

                    // No articles
                    if (limitedArticles.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No news available")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(limitedArticles) { article ->
                                ArticleItem(
                                    title = article.title ?: "No Title",
                                    description = article.description ?: "No Description",
                                    imageUrl = article.urlToImage,
                                    onBookmarkClick = {
                                        val brief = article.description
                                            ?.take(150)  // keep first 150 chars
                                            ?.plus("...")
                                            ?: "No description available"

                                        val bookmark = BookmarkEntity(
                                            title = article.title,
                                            description = brief,
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

            // Image (with Coil placeholder + scaled decoding)
            if (!imageUrl.isNullOrBlank()) {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .size(800) // reduce decode size
                        .build(),
                    contentDescription = title,
                    placeholder = painterResource(id = R.drawable.ic_placeholder),
                    error = painterResource(id = R.drawable.ic_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 3)
            Spacer(modifier = Modifier.height(6.dp))
            val briefDescription = description
                ?.take(150)  // first 150 chars
                ?.plus("...")
                ?: "No description available"

            Text(text = briefDescription, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            var clicked by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(targetValue = if (clicked) 1.12f else 1f)
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
