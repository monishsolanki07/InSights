package com.monish.insight.ui.home

import android.app.Application
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.scale
import androidx.compose.material3.Icon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.input.ImeAction

import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Search




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    ),
    bookmarksViewModel: BookmarksViewModel
) {
    val indiaArticles by homeViewModel.indiaArticles
    val worldArticles by homeViewModel.worldArticles
    val sportsArticles by homeViewModel.sportsArticles
    val searchArticles by homeViewModel.searchArticles
    val isLoading by homeViewModel.isLoading

    var selectedTab by remember { mutableStateOf(1) }
    val tabs = listOf("India News", "World News", "Sports")

    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search news...") },
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        homeViewModel.clearSearch()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Close Search"
                                        )

                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        homeViewModel.searchNews(searchQuery.trim())
                                    }
                                }
                            )
                        )
                    } else {
                        Text("INSIGHTS")
                    }
                },
                actions = {
                    if (!isSearching) {
                        IconButton(onClick = {
                            isSearching = true
                            searchQuery = ""
                            homeViewModel.clearSearch()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search Icon"
                            )

                        }
                    } else {
                        IconButton(onClick = {
                            isSearching = false
                            searchQuery = ""
                            homeViewModel.clearSearch()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Search"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isSearching) {
                // Show search results
                if (isLoading) {
                    ShimmerArticleList()
                } else {
                    if (searchArticles.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No results for \"$searchQuery\"", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(searchArticles) { article ->
                                ArticleItem(
                                    title = article.title ?: "No Title",
                                    description = article.description ?: "No Description",
                                    imageUrl = article.urlToImage,
                                    onBookmarkClick = {
                                        val brief = article.description
                                            ?.take(150)
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
            } else {
                // Show existing tabbed news
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

                val articlesToShow = when (selectedTab) {
                    0 -> indiaArticles
                    1 -> worldArticles
                    2 -> sportsArticles
                    else -> worldArticles
                }.take(10)

                val context = LocalContext.current
                LaunchedEffect(articlesToShow) {
                    withContext(Dispatchers.IO) {
                        val imageLoader = coil.ImageLoader(context)
                        articlesToShow.mapNotNull { it.urlToImage }.forEach { url ->
                            try {
                                val request = ImageRequest.Builder(context)
                                    .data(url)
                                    .size(800)
                                    .build()
                                imageLoader.enqueue(request)
                            } catch (_: Exception) {}
                        }
                    }
                }

                if (isLoading) {
                    ShimmerArticleList()
                } else {
                    if (articlesToShow.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No news available", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(articlesToShow) { article ->
                                ArticleItem(
                                    title = article.title ?: "No Title",
                                    description = article.description ?: "No Description",
                                    imageUrl = article.urlToImage,
                                    onBookmarkClick = {
                                        val brief = article.description
                                            ?.take(150)
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
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            if (!imageUrl.isNullOrBlank()) {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .size(800)
                        .build(),
                    contentDescription = title,
                    placeholder = painterResource(id = R.drawable.ic_placeholder),
                    error = painterResource(id = R.drawable.ic_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 3,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = description.take(150).plus("..."),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                modifier = Modifier.padding(bottom = 12.dp)
            )

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
                modifier = Modifier
                    .scale(scale),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = MaterialTheme.shapes.small
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

@Composable
fun ShimmerArticleList() {
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            ShimmerArticleItem(alpha = shimmerAlpha)
        }
    }
}


@Composable
fun ShimmerArticleItem(alpha: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = alpha),
                                MaterialTheme.colorScheme.surface.copy(alpha = alpha / 3)
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = alpha)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(
                modifier = Modifier
                    .width(110.dp)
                    .height(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = alpha))
            )
        }
    }
}
