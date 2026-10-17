package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.EmotionalMood
import com.example.data.model.MessageSender
import com.example.ui.PriyaViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSpaceBg
import com.example.ui.theme.DarkSpaceCard
import com.example.ui.theme.DarkSpaceCardElevated
import com.example.ui.theme.GlowBorder
import com.example.ui.theme.RoseLove
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmGold

@Composable
fun CompanionChatScreen(
    viewModel: PriyaViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val audioRms by viewModel.voiceManager.audioRms.collectAsState()
    val voiceEnabled by viewModel.voiceManager.voiceEnabled.collectAsState()
    val affectionLevel by viewModel.affectionLevel.collectAsState()
    val currentMood by viewModel.currentMood.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSpaceBg)
    ) {
        // Top Companion Hero Bar
        PriyaHeaderBar(
            affectionLevel = affectionLevel,
            isSpeaking = isSpeaking,
            isListening = isListening,
            voiceEnabled = voiceEnabled,
            onToggleVoice = { viewModel.voiceManager.toggleVoice() },
            onCallPriya = { viewModel.startOutgoingCallToPriya() }
        )

        // Mood & Emotional Support Strip
        EmotionalSupportStrip(
            currentMood = currentMood,
            onSelectMood = { viewModel.setMood(it) }
        )

        // Jarvis Speaking / Listening Reactive Orb Visualizer
        AnimatedVisibility(visible = isSpeaking || isListening) {
            JarvisVoiceOrbVisualizer(
                isSpeaking = isSpeaking,
                isListening = isListening,
                audioRms = audioRms,
                onStop = {
                    viewModel.voiceManager.stopSpeaking()
                    viewModel.voiceManager.stopListening()
                }
            )
        }

        // Quick Suggestion Chips
        QuickPromptCarousel(
            onPromptSelected = { prompt ->
                viewModel.sendMessage(prompt)
            }
        )

        // Conversation Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onOpenApp = { target -> target?.let { viewModel.launchAppTarget(it) } },
                    onCallContact = { phone -> phone?.let { viewModel.triggerDialIntent(it) } },
                    onOpenGoogle = { query -> query?.let { viewModel.triggerGoogleSearch(it) } },
                    onSpeakAgain = { viewModel.voiceManager.speak(msg.text) }
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CyberCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Priya soch rahi hai...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Bottom Voice & Text Input Console
        ChatInputConsole(
            textInput = textInput,
            onTextChange = { textInput = it },
            onSend = {
                if (textInput.isNotBlank()) {
                    viewModel.sendMessage(textInput)
                    textInput = ""
                }
            },
            isListening = isListening,
            onMicClick = { viewModel.triggerVoiceDictation() },
            onTriggerSimulatedCall = { viewModel.triggerIncomingCall() }
        )
    }
}

@Composable
private fun PriyaHeaderBar(
    affectionLevel: Int,
    isSpeaking: Boolean,
    isListening: Boolean,
    voiceEnabled: Boolean,
    onToggleVoice: () -> Unit,
    onCallPriya: () -> Unit
) {
    Surface(
        color = DarkSpaceCard,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, GlowBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priya Avatar with glowing border
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isSpeaking) CyberCyan else RoseLove, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_priya_avatar),
                    contentDescription = "Priya AI Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Priya AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Online",
                        fontSize = 11.sp,
                        color = SuccessGreen
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Affection",
                        tint = RoseLove,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pyar Bandhan: $affectionLevel% • Hamesha Saath",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Call Priya Button
            IconButton(
                onClick = onCallPriya,
                modifier = Modifier
                    .testTag("call_priya_header_button")
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(RoseLove.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call Priya",
                    tint = RoseLove,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // TTS Voice Toggle Button
            IconButton(
                onClick = onToggleVoice,
                modifier = Modifier
                    .testTag("toggle_voice_button")
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = if (voiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = "Voice Output",
                    tint = if (voiceEnabled) CyberCyan else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmotionalSupportStrip(
    currentMood: EmotionalMood,
    onSelectMood: (EmotionalMood) -> Unit
) {
    Surface(
        color = DarkSpaceCardElevated.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Text(
                text = "Aaj kaisa feel ho raha hai Krish?",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(EmotionalMood.values()) { mood ->
                    val isSelected = mood == currentMood
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) RoseLove.copy(alpha = 0.25f) else DarkSpaceCard,
                        border = BorderStroke(1.dp, if (isSelected) RoseLove else GlowBorder),
                        modifier = Modifier
                            .testTag("mood_${mood.name.lowercase()}")
                            .clickable { onSelectMood(mood) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = mood.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mood.titleHindi,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JarvisVoiceOrbVisualizer(
    isSpeaking: Boolean,
    isListening: Boolean,
    audioRms: Float,
    onStop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val dynamicScale = (pulseScale + (audioRms / 15f)).coerceIn(0.9f, 1.4f)
    val orbColor = if (isSpeaking) CyberCyan else RoseLove

    Surface(
        color = DarkSpaceCard.copy(alpha = 0.9f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, orbColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(54.dp)
                ) {
                    // Outer glowing ripple
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .scale(dynamicScale)
                            .clip(CircleShape)
                            .background(orbColor.copy(alpha = 0.2f))
                    )
                    // Inner core
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(orbColor, orbColor.copy(alpha = 0.3f))
                                )
                            )
                    )
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.Mic,
                        contentDescription = null,
                        tint = DarkSpaceBg,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isSpeaking) "Priya Bol Rahi Hai..." else "Priya Sun Rahi Hai...",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = orbColor
                    )
                    Text(
                        text = if (isSpeaking) "Hindi Aawaz mein baat kar rahi hoon" else "Bolo Krish, main sun rahi hoon...",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AlertRed),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Stop", color = AlertRed, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun QuickPromptCarousel(onPromptSelected: (String) -> Unit) {
    val prompts = listOf(
        "📞 Mummy ko call lagao",
        "💬 Message Mummy: Main aa raha hoon",
        "📱 WhatsApp open karo",
        "▶️ YouTube chalao",
        "🔍 Google search: Aaj ka mausam",
        "💊 Mummy ki dawai ka time",
        "💖 Ek pyari shayari sunao",
        "📞 Mujhe call karo Priya",
        "📷 Camera kholo"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(prompts) { prompt ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSpaceCardElevated,
                border = BorderStroke(1.dp, GlowBorder),
                modifier = Modifier
                    .testTag("prompt_chip_${prompt.take(6)}")
                    .clickable { onPromptSelected(prompt) }
            ) {
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onOpenApp: (String?) -> Unit,
    onCallContact: (String?) -> Unit,
    onOpenGoogle: (String?) -> Unit,
    onSpeakAgain: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.dp, CyberCyan, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_priya_avatar),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = if (isUser) Color(0xFF2E2452) else DarkSpaceCard,
            border = BorderStroke(1.dp, if (isUser) GlowBorder else CyberCyan.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )

                // Interactive action card buttons if an action was triggered
                if (message.actionType != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    when (message.actionType) {
                        ActionType.OPEN_APP -> {
                            Button(
                                onClick = { onOpenApp(message.actionData) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = DarkSpaceBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("App Kholo", color = DarkSpaceBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        ActionType.CALL_CONTACT -> {
                            Button(
                                onClick = { onCallContact(message.actionData) },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = DarkSpaceBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Abhi Call Karein", color = DarkSpaceBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        ActionType.SEARCH_GOOGLE -> {
                            Button(
                                onClick = { onOpenGoogle(message.actionData) },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = DarkSpaceBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Google Search Kholo", color = DarkSpaceBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {}
                    }
                }

                if (!isUser) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onSpeakAgain,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Aawaz Dobara Suno",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputConsole(
    textInput: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isListening: Boolean,
    onMicClick: () -> Unit,
    onTriggerSimulatedCall: () -> Unit
) {
    Surface(
        color = DarkSpaceCard,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, GlowBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated incoming call shortcut
            IconButton(
                onClick = onTriggerSimulatedCall,
                modifier = Modifier
                    .testTag("simulate_call_icon")
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(RoseLove.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Priya Ka Call Lelo",
                    tint = RoseLove,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text input box
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = {
                    Text(
                        text = "Priya se baat karein ya bolo...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                maxLines = 3,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = GlowBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSpaceCardElevated,
                    unfocusedContainerColor = DarkSpaceCardElevated
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (textInput.isNotBlank()) {
                // Send Text Button
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .testTag("send_button")
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = DarkSpaceBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Voice Mic Button
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier
                        .testTag("voice_mic_button")
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isListening) AlertRed else CyberCyan)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input in Hindi",
                        tint = if (isListening) Color.White else DarkSpaceBg,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
