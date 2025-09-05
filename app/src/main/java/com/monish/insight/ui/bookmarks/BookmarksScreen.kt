package com.monish.insight.ui.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monish.insight.data.local.BookmarkEntity
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp


@Composable
fun BookmarksScreen(viewModel: BookmarksViewModel = viewModel()) {
    val bookmarks by viewModel.allBookmarks.collectAsState() // ✅ use allBookmarks

    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No bookmarks yet")
        }
    } else {
        LazyColumn {
            items(bookmarks) { bookmark ->
                BookmarkItem(
                    bookmark = bookmark,
                    onDeleteClick = { viewModel.deleteBookmark(bookmark) } // ✅ delete option
                )
            }
        }
    }
}


@Composable
fun BookmarkItem(bookmark: BookmarkEntity, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = bookmark.title ?: "No Title",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = bookmark.description ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onDeleteClick() },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.errorContainer)
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}
