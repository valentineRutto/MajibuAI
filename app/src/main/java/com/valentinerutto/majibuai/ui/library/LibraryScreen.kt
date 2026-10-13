package com.valentinerutto.majibuai.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valentinerutto.majibuai.ui.theme.AppBackground
import com.valentinerutto.majibuai.ui.theme.BorderLight
import com.valentinerutto.majibuai.ui.theme.LavenderLight
import com.valentinerutto.majibuai.ui.theme.MintDark
import com.valentinerutto.majibuai.ui.theme.MintPrimary
import com.valentinerutto.majibuai.ui.theme.PeachLight
import com.valentinerutto.majibuai.ui.theme.SurfaceSoft
import com.valentinerutto.majibuai.ui.theme.TextMuted
import com.valentinerutto.majibuai.ui.theme.TextPrimary
import com.valentinerutto.majibuai.ui.theme.TextSecondary
import com.valentinerutto.majibuai.ui.theme.YellowLight

private data class SavedQuestion(
    val title: String,
    val date: String,
    val topic: String,
    val marker: String,
    val markerColor: Color,
)

private val savedQuestions = listOf(
    SavedQuestion("What is Kotlin Flow?", "Apr 25, 2025", "Android", "🌺", PeachLight),
    SavedQuestion("Difference between StateFlow and SharedFlow", "Apr 24, 2025", "Kotlin", "🧡", PeachLight),
    SavedQuestion("How does Jetpack Compose handle state?", "Apr 22, 2025", "Compose", "◉", LavenderLight),
    SavedQuestion("What is the purpose of Hilt?", "Apr 20, 2025", "Android", "✦", YellowLight),
)

@Composable
fun LibraryScreen(
    onAddQuestion: () -> Unit = {},
    onQuestionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf("All") }
    val topics = listOf("All", "Android", "Kotlin", "AI", "Compose")
    val filteredQuestions = savedQuestions.filter { question ->
        val matchesQuery = question.title.contains(query, ignoreCase = true)
        val matchesTopic = selectedTopic == "All" || question.topic == selectedTopic
        matchesQuery && matchesTopic
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp),
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = MintPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
                IconButton(
                    onClick = onAddQuestion,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add saved question",
                        tint = MintPrimary,
                    )
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search saved questions...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted)
                },
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintPrimary,
                    unfocusedBorderColor = BorderLight,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 14.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                topics.forEach { topic ->
                    val selected = selectedTopic == topic
                    FilterChip(
                        selected = selected,
                        onClick = { selectedTopic = topic },
                        label = { Text(topic) },
                        shape = CircleShape,
                        border = if (selected) null else BorderStroke(0.dp, Color.Transparent),
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceSoft,
                            labelColor = TextSecondary,
                        ),
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filteredQuestions) { question ->
                    SavedQuestionCard(question = question) {
                        onQuestionClick(question.title)
                    }
                }
                if (filteredQuestions.isEmpty()) {
                    item {
                        Text(
                            text = "No saved questions found",
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
    }
}

@Composable
private fun SavedQuestionCard(question: SavedQuestion, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = question.marker,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(question.markerColor)
                    .padding(7.dp),
                color = MintDark,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 8.dp),
            ) {
                Text(
                    text = question.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${question.date} · ${question.topic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MintDark,
            )
        }
    }
}
