package com.monish.insight.ui.home

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.*
import com.monish.insight.data.local.BookmarkEntity
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
        ArticlePage(
            article = allArticles[index],
            bookmarksViewModel = bookmarksViewModel // ✅ pass it here
        )

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
fun ArticlePage(
    article: Article,
    bookmarksViewModel: BookmarksViewModel
) {
    val context = LocalContext.current
    var isBookmarked by remember { mutableStateOf(false) }

    // Check if already bookmarked
    LaunchedEffect(article.url) {
        isBookmarked = bookmarksViewModel.allBookmarks.value.any { it.url == article.url }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(20.dp))

        // App Header - INSIGHTS
        Text(
            text = "INSIGHTS",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Title Card - Clean and prominent
        Card(
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(3.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = article.title ?: "Untitled",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Start,
                maxLines = 3
            )
        }

        Spacer(Modifier.height(20.dp))

        // Image Card - Hero image
        Card(
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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

        Spacer(Modifier.height(20.dp))

        // Content section with bookmark - Properly separated
        Card(
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header row with bookmark button - separated from text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Article Details",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Bookmark button - now properly separated
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBookmarked)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val brief = article.description?.take(150)?.plus("...") ?: "No description available"
                                val bookmark = BookmarkEntity(
                                    title = article.title,
                                    description = brief,
                                    url = article.url,
                                    urlToImage = article.urlToImage,
                                    publishedAt = article.publishedAt
                                )
                                bookmarksViewModel.toggleBookmark(bookmark)
                                isBookmarked = !isBookmarked
                            },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked)
                                    MaterialTheme.colorScheme.secondary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Divider for visual separation
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    thickness = 2.dp
                )

                Spacer(Modifier.height(20.dp))

                // Description text - now clean and separated
                Text(
                    text = article.description ?: "Details not provided",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Publication date if available
                article.publishedAt?.let { publishedAt ->
                    Text(
                        text = "📅 Published: ${publishedAt.take(10)}", // Just the date part
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(0.8f)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
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
                "🎉 You've completed today's insights!",
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