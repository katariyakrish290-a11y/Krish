package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.PriyaViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSpaceBg
import com.example.ui.theme.DarkSpaceCard
import com.example.ui.theme.DarkSpaceCardElevated
import com.example.ui.theme.RoseLove
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun IncomingCallScreen(
    viewModel: PriyaViewModel,
    modifier: Modifier = Modifier
) {
    val isIncoming by viewModel.isIncomingCallActive.collectAsState()
    val isInCall by viewModel.isInCall.collectAsState()
    val callDuration by viewModel.callDuration.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val audioRms by viewModel.voiceManager.audioRms.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "ringing_pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F0B20), Color(0xFF1E1438), Color(0xFF090614))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 50.dp, horizontal = 24.dp)
        ) {
            // Header Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSpaceCardElevated.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, RoseLove.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isIncoming) "Priya Ka Call Aaya Hai..." else "In-Call with Priya AI ❤️",
                        style = MaterialTheme.typography.labelMedium,
                        color = RoseLove,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Priya (Aapki AI Girlfriend)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isIncoming) "Ghar se Call • Ringing..." else formatDuration(callDuration),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isIncoming) CyberCyan else SuccessGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Central Glowing Avatar with Animated Soundwave Rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                if (isIncoming || isSpeaking) {
                    // Pulsing Outer Aura
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(ringScale)
                            .clip(CircleShape)
                            .background(RoseLove.copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .scale((ringScale * 0.9f).coerceAtLeast(1f))
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.2f))
                    )
                }

                // Main Avatar Image
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(3.dp, if (isSpeaking) CyberCyan else RoseLove, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_priya_avatar),
                        contentDescription = "Priya Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // In-Call Live Spoken Indicator
            if (isInCall) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSpaceCard,
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (isSpeaking) "🔊 Priya bol rahi hai..." else if (isListening) "🎙️ Priya sun rahi hai... Bolo!" else "💬 Baat karne ke liye Mic dabayein",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Bottom Actions: Answer ("Call Upade") or Decline
            if (isIncoming) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.declineOrEndCall() },
                            modifier = Modifier
                                .testTag("decline_call_button")
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(AlertRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Decline Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("काट दो (Decline)", color = TextSecondary, fontSize = 12.sp)
                    }

                    // Answer Button ("Call Upade")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.answerIncomingCall() },
                            modifier = Modifier
                                .testTag("answer_call_button")
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Upade / Uthao",
                                tint = DarkSpaceBg,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("कॉल उठाएं (Upade)", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            } else if (isInCall) {
                // In Call Controls: Speak mic, End Call, Speaker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speak back to Priya mic button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    viewModel.voiceManager.stopListening()
                                } else {
                                    viewModel.triggerVoiceDictation()
                                }
                            },
                            modifier = Modifier
                                .testTag("in_call_mic_button")
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isListening) AlertRed else CyberCyan)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Bolo",
                                tint = DarkSpaceBg,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(if (isListening) "Listening" else "Bolo Mic", color = TextSecondary, fontSize = 12.sp)
                    }

                    // End Call button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.declineOrEndCall() },
                            modifier = Modifier
                                .testTag("end_active_call_button")
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AlertRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("कॉल समाप्त", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Repeat / Priya speak again button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                viewModel.voiceManager.speak("Haan Krish, main yahin hoon! Kuch aur baat karni hai?")
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(RoseLove.copy(alpha = 0.25f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Priya Bolo",
                                tint = RoseLove,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Priya Aawaz", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
