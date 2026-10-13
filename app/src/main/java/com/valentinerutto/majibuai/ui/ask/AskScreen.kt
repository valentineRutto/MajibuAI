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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.valentinerutto.majibuai.R
import com.valentinerutto.majibuai.ui.theme.AppBackground
import com.valentinerutto.majibuai.ui.theme.BorderLight
import com.valentinerutto.majibuai.ui.theme.CodeBackground
import com.valentinerutto.majibuai.ui.theme.CodeText
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

private data class ChatMessage(
    val text: String,
    val fromAssistant: Boolean,
    val showCodeSample: Boolean = false,
)

@Composable
fun AskScreen(
    onBackClick: () -> Unit = {},
    onSaveAnswer: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {

    val messages = remember {
        mutableStateListOf(
            ChatMessage("What is Kotlin Flow?", fromAssistant = false),
            ChatMessage(
                text = "Kotlin Flow is a coroutine-based API for handling asynchronous streams of data.\n\nIt lets you work with data that arrives over time, such as from a network, database, or user input. Flow is cancelable, lifecycle-aware, and works great with Jetpack Compose.",
                fromAssistant = true,
                showCodeSample = true,
            ),
        )
    }
    var draft by remember { mutableStateOf("") }
    var isAnswerSaved by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        AskTopBar(onBackClick = onBackClick)

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
            itemsIndexed(messages) { index, message ->
                if (message.fromAssistant) {
                    AssistantMessage(
                        message = message,
                        showSaveAction = index == messages.lastIndex,
                        isSaved = isAnswerSaved,
                        onSave = {
                            isAnswerSaved = !isAnswerSaved
                            if (isAnswerSaved) onSaveAnswer(message.text)
                        },
                    )
                } else {
                    UserMessage(message.text)
                }
            }
        }

        MessageComposer(
            value = draft,
            onValueChange = { draft = it },
            onSend = {
                val question = draft.trim()
                if (question.isNotEmpty()) {
                    messages.add(ChatMessage(question, fromAssistant = false))
                    messages.add(
                        ChatMessage(
                            text = "I can help you explore $question. Ask a follow-up or tell me which part you would like explained.",
                            fromAssistant = true,
                        ),
                    )
                    draft = ""
                    isAnswerSaved = false
                }
            },
        )
    }
}

@Composable
private fun AskTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
        Text(
            text = "Ask",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp),
        )

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
    message: ChatMessage,
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
                    text = message.text,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 21.sp,
                )
            }
            if (message.showCodeSample) {
                Spacer(Modifier.height(12.dp))
                CodeExampleCard()
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
private fun CodeExampleCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(10.dp),
    ) {
        Text(
            text = "Quick example:",
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "flow {\n    emit(\"Hello\")\n    delay(1000)\n    emit(\"World\")\n}",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .background(CodeBackground)
                .padding(10.dp),
            color = CodeText,
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun MessageComposer(
    value: String,
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
                )
            }
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MintPrimary),
                enabled = value.isNotBlank(),
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
