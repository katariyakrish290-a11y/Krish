package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
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

data class AppLauncherItem(
    val name: String,
    val target: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun QuickActionsScreen(
    viewModel: PriyaViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contacts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var selectedContactForMessage by remember { mutableStateOf<ContactItem?>(null) }
    var messageBody by remember { mutableStateOf("") }

    val appList = listOf(
        AppLauncherItem("WhatsApp", "com.whatsapp", Icons.AutoMirrored.Filled.Chat, Color(0xFF25D366)),
        AppLauncherItem("YouTube", "com.google.android.youtube", Icons.Default.PlayCircle, Color(0xFFFF0000)),
        AppLauncherItem("Camera", "camera", Icons.Default.CameraAlt, CyberCyan),
        AppLauncherItem("Chrome", "com.android.chrome", Icons.Default.Language, Color(0xFF4285F4)),
        AppLauncherItem("Google Maps", "com.google.android.apps.maps", Icons.Default.Map, Color(0xFF34A853)),
        AppLauncherItem("Phone", "dialer", Icons.Default.Phone, SuccessGreen),
        AppLauncherItem("Settings", "settings", Icons.Default.Settings, Color(0xFF9E9E9E)),
        AppLauncherItem("Gallery", "gallery", Icons.Default.PhotoLibrary, Color(0xFFFBBC05)),
        AppLauncherItem("Calculator", "calculator", Icons.Default.Calculate, Color(0xFFFF6D00))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSpaceBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Google Search Bar Section
        item {
            GoogleSearchSection(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { query ->
                    if (query.isNotBlank()) {
                        viewModel.triggerGoogleSearch(query)
                    }
                },
                onVoiceSearch = {
                    viewModel.voiceManager.startListening(
                        onResult = { result ->
                            searchQuery = result
                            viewModel.triggerGoogleSearch(result)
                        }
                    )
                }
            )
        }

        // App Launcher Section (सारे Apps खोलें)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Apps, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "सारे Applications (Jarvis App Launcher)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Touch karke ya aawaz se kholo ('Open WhatsApp')",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSpaceCard),
                border = BorderStroke(1.dp, GlowBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val rows = appList.chunked(3)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            rowItems.forEach { app ->
                                AppTile(
                                    app = app,
                                    onClick = { viewModel.launchAppTarget(app.target) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Contact Management & Messaging Section (संपर्क एवं मैसेज)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = RoseLove, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "संपर्क एवं मैसेज (Contacts & Messaging)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = { showAddContactDialog = true },
                    modifier = Modifier.testTag("add_contact_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseLove.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, RoseLove),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = RoseLove, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ नया संपर्क", color = RoseLove, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(contacts, key = { it.id }) { contact ->
            ContactCard(
                contact = contact,
                onCall = { viewModel.triggerDialIntent(contact.phoneNumber) },
                onMessage = {
                    selectedContactForMessage = contact
                    messageBody = ""
                }
            )
        }
    }

    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onAdd = { name, phone, rel, isMum ->
                viewModel.addContact(name, phone, rel, isMum)
                showAddContactDialog = false
            }
        )
    }

    if (selectedContactForMessage != null) {
        val contact = selectedContactForMessage!!
        SendMessageDialog(
            contactName = contact.name,
            phoneNumber = contact.phoneNumber,
            messageText = messageBody,
            onTextChange = { messageBody = it },
            onDismiss = { selectedContactForMessage = null },
            onSend = {
                if (messageBody.isNotBlank()) {
                    viewModel.triggerSmsIntent(contact.phoneNumber, messageBody)
                    selectedContactForMessage = null
                }
            }
        )
    }
}

@Composable
private fun GoogleSearchSection(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onVoiceSearch: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSpaceCard),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = WarmGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google Search & Jankari (गूगल सर्च)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("google_search_input"),
                    placeholder = { Text("Google par kya khojna hai?", color = TextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold,
                        unfocusedBorderColor = GlowBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSpaceCardElevated,
                        unfocusedContainerColor = DarkSpaceCardElevated
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onVoiceSearch,
                    modifier = Modifier
                        .testTag("google_voice_search_btn")
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(WarmGold.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = WarmGold, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { onSearch(searchQuery) },
                    modifier = Modifier
                        .testTag("google_search_btn")
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(WarmGold)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = DarkSpaceBg, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick search suggestions
            val suggestions = listOf("Today Weather", "Hindi News", "Health Tips", "Biryani Recipe")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(suggestions) { s ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSpaceCardElevated,
                        border = BorderStroke(1.dp, GlowBorder),
                        modifier = Modifier
                            .testTag("quick_search_${s.take(4)}")
                            .clickable {
                                onQueryChange(s)
                                onSearch(s)
                            }
                    ) {
                        Text(
                            text = s,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTile(
    app: AppLauncherItem,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .testTag("app_${app.name.lowercase()}")
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(app.accentColor.copy(alpha = 0.2f))
                .border(1.5.dp, app.accentColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = app.name,
                tint = app.accentColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ContactCard(
    contact: ContactItem,
    onCall: () -> Unit,
    onMessage: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSpaceCard),
        border = BorderStroke(1.dp, if (contact.isMummy) RoseLove.copy(alpha = 0.5f) else GlowBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (contact.isMummy) RoseLove.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (contact.isMummy) RoseLove else CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (contact.isMummy) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RoseLove.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "Mummy ❤️",
                                color = RoseLove,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${contact.relation} • ${contact.phoneNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Quick Message Button
            IconButton(
                onClick = onMessage,
                modifier = Modifier
                    .testTag("sms_btn_${contact.name.take(3)}")
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Message",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick Call Button
            IconButton(
                onClick = onCall,
                modifier = Modifier
                    .testTag("call_btn_${contact.name.take(3)}")
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = SuccessGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SendMessageDialog(
    contactName: String,
    phoneNumber: String,
    messageText: String,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSpaceCard,
        title = {
            Text("$contactName को मैसेज भेजें", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "To: $phoneNumber",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = messageText,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sms_compose_field"),
                    placeholder = { Text("Apna sandesh likhein...", color = TextMuted) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSend,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = DarkSpaceBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("भेजें (Send)", color = DarkSpaceBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, relation: String, isMummy: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("Friend") }
    var isMummy by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSpaceCard,
        title = {
            Text("नया संपर्क जोड़ें", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("नाम (Name)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoseLove,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("फ़ोन नंबर (Phone Number)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoseLove,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("संबंध (Relation: e.g. Mother, Friend)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoseLove,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAdd(name, phone, relation, isMummy)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoseLove)
            ) {
                Text("सुरक्षित करें", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें", color = TextSecondary)
            }
        }
    )
}
