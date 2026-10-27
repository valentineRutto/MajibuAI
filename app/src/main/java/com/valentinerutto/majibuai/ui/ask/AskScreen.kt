package com.valentinerutto.majibuai.ui.ask

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinerutto.majibuai.R
import com.valentinerutto.majibuai.data.ai.models.ChatRole
import com.valentinerutto.majibuai.data.ai.AiProvider
import com.valentinerutto.majibuai.data.ai.models.OnDeviceAvailability
import com.valentinerutto.majibuai.data.ai.models.toDisplayMessage
import com.valentinerutto.majibuai.ui.MajibuViewModel
import com.valentinerutto.majibuai.ui.theme.AppBackground
import com.valentinerutto.majibuai.ui.theme.BorderLight
import com.valentinerutto.majibuai.ui.theme.LavenderLight
import com.valentinerutto.majibuai.ui.theme.LavenderAccent
import com.valentinerutto.majibuai.ui.theme.MajibuShapes
import com.valentinerutto.majibuai.ui.theme.MintCard
import com.valentinerutto.majibuai.ui.theme.MintDark
import com.valentinerutto.majibuai.ui.theme.MintPrimary
import com.valentinerutto.majibuai.ui.theme.SurfaceSoft
import com.valentinerutto.majibuai.ui.theme.TextMuted
import com.valentinerutto.majibuai.ui.theme.TextPrimary
import com.valentinerutto.majibuai.ui.theme.TextSecondary
import com.valentinerutto.orbmotion.orbs.AnimatedThinkingOrb
import com.valentinerutto.orbmotion.orbs.OrbSize
import com.valentinerutto.orbmotion.orbs.OrbState
import org.koin.androidx.compose.koinViewModel

@Composable
fun AskScreen(
    viewModel: MajibuViewModel = koinViewModel(),
    onSaveAnswer: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val savedQuestions by viewModel.savedQuestions.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lastMessage = uiState.conversation.lastOrNull()

    val onDeviceModelState by
    viewModel.onDeviceModelState.collectAsStateWithLifecycle()

    val isModelBusy = onDeviceModelState.availability in setOf(
        OnDeviceAvailability.DOWNLOADING,
        OnDeviceAvailability.INITIALIZING
    )



    LaunchedEffect(
        uiState.conversation.size,
        lastMessage?.content,
        onDeviceModelState.availability,
        uiState.error
    ) {
        if (listState.layoutInfo.totalItemsCount > 0) {
            listState.animateScrollToItem(
                listState.layoutInfo.totalItemsCount - 1
            )
        }
    }

    if (uiState.showModelDownloadDialog) {

        AlertDialog(
            onDismissRequest = viewModel::dismissModelDownloadDialog,
            title = {
                Text("Download on-device AI?")
            },
            text = {
                Text(
                    "MajibuAI needs to download Gemma 3 1B " +
                            "(approximately 600MBs). " +
                            "After downloading, you can ask questions offline."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmModelDownload
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::dismissModelDownloadDialog
                ) {
                    Text("Cancel")
                }
            }
        )
    }




    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        AskTopBar(
            selectedProvider = uiState.selectedProvider,
            onProviderSelected = viewModel::selectProvider,
        )
        if (onDeviceModelState.availability == OnDeviceAvailability.DOWNLOADING) {
            OnDeviceDownloadProgress(
                downloaded = onDeviceModelState.bytesDownloaded,
                total = onDeviceModelState.bytesToDownload,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // LiteRT-LM initialization progress
            if (
                onDeviceModelState.availability ==
                OnDeviceAvailability.INITIALIZING
            ) {
                item(key = "model_initializing") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        AnimatedThinkingOrb(
                            modifier = Modifier.size(24.dp),
                            state = OrbState.SHAPING,
                            size = OrbSize.Custom(64.dp).floatValue,
                            speed = 1f,
                            color = MintDark,
                        )

                        Text("Initializing Gemma 3...")
                    }
                }
            }

            uiState.conversation.forEachIndexed { index, message ->

                item(key = message.id) {

                    when (message.role) {

                        ChatRole.USER -> UserMessage(message.content)

                        ChatRole.ASSISTANT -> {
                            val question = uiState.conversation
                                .take(index)
                                .lastOrNull { it.role == ChatRole.USER }
                                ?.content
                                .orEmpty()

                            val isStreaming =
                                uiState.isLoading &&
                                        message.id == lastMessage?.id

                            AssistantMessage(
                                answer = message.content,
                                showSaveAction = question.isNotBlank() && message.content.isNotBlank() && !isStreaming,
                                isSaved = savedQuestions.any {
                                    it.question == question && it.answer == message.content
                                },
                                onSave = {
                                    if (question.isNotBlank()) {
                                        viewModel.saveAnswer(question, message.content)
                                        onSaveAnswer(message.content)
                                    }
                                },
                            )
                        }
                    }
                }
            }



            if (uiState.isLoading && lastMessage?.content.isNullOrEmpty()) {
                item(key = "loading") { LoadingMessage() }
            }
            uiState.error?.let { error ->
                item(key = "error") { ErrorMessage(error.toDisplayMessage()) }
            }
        }

        if (uiState.isLoading) {
            TextButton(
                onClick = viewModel::stopGenerating,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Stop generating")
            }
        }

        MessageComposer(
            value = uiState.question,
            enabled = !uiState.isLoading,
            onValueChange = viewModel::onQuestionChange,
            onSend = viewModel::ask,
        )
    }
}

@Composable
private fun AskTopBar(
    selectedProvider: AiProvider,
    onProviderSelected: (AiProvider) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_mascot),
                contentDescription = "MajibuAI logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
            )
            Text(
                text = "Ask",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 10.dp),
            )
        }

        Box {
            androidx.compose.material3.TextButton(onClick = { menuExpanded = true }) {
                Text(
                    text = selectedProvider.label,
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Choose AI provider",
                    tint = TextPrimary,
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                AiProvider.entries.forEach { provider ->
                    DropdownMenuItem(
                        text = { Text(provider.label) },
                        onClick = {
                            onProviderSelected(provider)
                            menuExpanded = false
                        },
                    )
                }
            }
       }
    }
}


@Composable
private fun UserMessage(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            color = SurfaceSoft,
          shape = MajibuShapes.extraLarge
        ) {
            Text(
                text = text,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Avatar(emoji = "👩🏻‍🎓", background = LavenderLight)
    }
}

@Composable
private fun AssistantMessage(
    answer: String,
    showSaveAction: Boolean,
    isSaved: Boolean,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        AssistantAvatar()
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(shape = MajibuShapes.extraLarge)
                .background(MintCard)
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MintPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = answer,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 21.sp,
                )
            }
            if (showSaveAction) {
                Spacer(Modifier.height(12.dp))
                AssistChip(
                    onClick = onSave,
                    label = { Text(if (isSaved) "Saved" else "Save for Later") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = LavenderLight,
                        labelColor = LavenderAccent,
                        leadingIconContentColor = LavenderAccent,
                    ),
                    border = null,
                )
            }
        }
    }
}

@Composable
private fun LoadingMessage() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistantAvatar()
        Spacer(Modifier.width(10.dp))
        Row(
            modifier = Modifier
                .clip(shape = MajibuShapes.extraLarge)
                .background(MintCard)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            AnimatedThinkingOrb(
                modifier = Modifier.size(24.dp),
                state = OrbState.SHAPING,
                size = OrbSize.Custom(64.dp).floatValue,
                speed = 1f,
                color = MintDark,
            )

            Spacer(Modifier.width(10.dp))

            Text("Thinking...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Text(
        text = error,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 44.dp, end = 8.dp),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun MessageComposer(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(shape = MajibuShapes.large)
                .background(Color.White)
                .border(1.dp, BorderLight, CircleShape)
                .padding(start = 16.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text("Ask another question...", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    singleLine = true,
                    enabled = enabled,
                )
            }
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MintPrimary),
                enabled = enabled && value.isNotBlank(),
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send question", tint = Color.White)
            }
        }
    }
}

@Composable
private fun Avatar(emoji: String, background: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AssistantAvatar() {
    Image(
        painter = painterResource(R.drawable.majibu_robot),
        contentDescription = "MajibuAI assistant",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape),
    )
}

@Composable
private fun OnDeviceDownloadProgress(
    downloaded: Long,
    total: Long?,
    modifier: Modifier = Modifier,
) {
    val progress = total
        ?.takeIf { it > 0 }
        ?.let {
            (downloaded.toFloat() / it.toFloat())
                .coerceIn(0f, 1f)
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Downloading Gemma 3 1B...",
            style = MaterialTheme.typography.bodyMedium
        )

        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
