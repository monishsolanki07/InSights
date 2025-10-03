package com.monish.insight.ui.reels

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import com.monish.insight.data.local.BookmarkEntity
import com.monish.insight.data.model.NewArticle
import com.monish.insight.ui.bookmarks.BookmarksViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReelScreen(
    reelsViewModel: ReelsViewModel = viewModel(),
    bookmarksViewModel: BookmarksViewModel = viewModel()
) {
    val articles by reelsViewModel.articles.collectAsState()
    val isLoading by reelsViewModel.isLoading.collectAsState()
    val bookmarkedUrls by bookmarksViewModel.bookmarkedUrls.collectAsState(initial = emptySet())

    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedArticle by remember { mutableStateOf<NewArticle?>(null) }

    // *** Add here: optimized image prefetching ***
    val context = LocalContext.current
    val lastPrefetchedUrls = remember { mutableStateListOf<String>() }

    LaunchedEffect(articles) {
        val newUrls = articles.mapNotNull { it.image_url }
        if (newUrls != lastPrefetchedUrls) {
            withContext(Dispatchers.IO) {
                val imageLoader = coil.ImageLoader(context)
                newUrls.forEach { url ->
                    try {
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .size(800)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build()
                        imageLoader.enqueue(request)
                    } catch (_: Exception) {
                        // Ignore failures
                    }
                }
            }
            lastPrefetchedUrls.clear()
            lastPrefetchedUrls.addAll(newUrls)
        }
    }


    if (articles.isEmpty() && isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (articles.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No articles found")
        }
        return
    }

    VerticalPager(
        count = Int.MAX_VALUE,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val index = page % articles.size
        val article = articles[index]
        ReelPage(
            article = article,
            isBookmarked = bookmarkedUrls.contains(article.link),
            onReadMore = {
                selectedArticle = it
                coroutineScope.launch { sheetState.show() }
            },
            onBookmark = { art ->
                val brief = (art.description ?: art.content).take(150).plus("...")
                val bookmark = BookmarkEntity(
                    title = art.title,
                    description = brief,
                    url = art.link,
                    urlToImage = art.image_url,
                    publishedAt = art.pubDate
                )
                bookmarksViewModel.toggleBookmark(bookmark)
            }
        )
    }

    if (selectedArticle != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedArticle = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 700.dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedArticle?.title ?: "",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                selectedArticle?.image_url?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = selectedArticle?.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                val contentToShow = remember(selectedArticle) {
                    val cont = selectedArticle?.content
                    if (cont == null || cont.contains("ONLY AVAILABLE IN PAID PLANS", ignoreCase = true)) {
                        selectedArticle?.description ?: "No content"
                    } else {
                        cont
                    }
                }

                Text(
                    text = contentToShow,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(12.dp))
                val context = LocalContext.current
                val url = selectedArticle?.link ?: ""

                if (url.isNotBlank()) {
                    ClickableText(
                        text = AnnotatedString("Source: $url"),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                } else {
                    Text(
                        text = "Source: unknown",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun ReelPage(
    article: NewArticle,
    isBookmarked: Boolean,
    onReadMore: (NewArticle) -> Unit,
    onBookmark: (NewArticle) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        article.image_url?.takeIf { it.isNotBlank() }?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = article.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = "INSIGHTS",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (article.description ?: article.content).take(150).plus("..."),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onReadMore(article) }) {
                        Text("Read More")
                    }
                    IconButton(onClick = { onBookmark(article) }) {
                        if (isBookmarked) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = "Bookmarked",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark"
                            )
                        }
                    }
                }
            }
        }
    }
}
