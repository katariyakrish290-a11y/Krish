package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayPeriod
import com.example.data.model.MedicineReminder
import com.example.data.model.RoutineItem
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
fun MummyCareScreen(
    viewModel: PriyaViewModel,
    modifier: Modifier = Modifier
) {
    val medicines by viewModel.medicines.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val groceries by viewModel.groceries.collectAsState()
    val contacts by viewModel.contacts.collectAsState()

    val mummyContact = contacts.firstOrNull { it.isMummy } ?: contacts.firstOrNull()

    var showAddMedDialog by remember { mutableStateOf(false) }
    var showAddGroceryDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSpaceBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Mummy Emergency & 1-Tap Call Banner
        item {
            MummyEmergencyCallCard(
                mummyName = mummyContact?.name ?: "मम्मी (Mummy)",
                mummyPhone = mummyContact?.phoneNumber ?: "+919876543210",
                onCall = {
                    val phone = mummyContact?.phoneNumber ?: "+919876543210"
                    viewModel.triggerDialIntent(phone)
                },
                onQuickMessage = { text ->
                    val phone = mummyContact?.phoneNumber ?: "+919876543210"
                    viewModel.triggerSmsIntent(phone, text)
                }
            )
        }

        // Voice Routine Announcer for Mummy
        item {
            VoiceAnnounceBanner(
                onAnnounceRoutine = {
                    val medCount = medicines.filter { !it.taken }.size
                    val speech = "Namaste Mummy ji! Priya bol rahi hoon. Aaj aapki $medCount dawai baaki hai. Subah se lekar raat tak aapka schedule ready hai. Dhyan rakhein aur aaram se din bitayein!"
                    viewModel.voiceManager.speak(speech)
                }
            )
        }

        // Medicine Reminder Section (दवाई की सूची)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "मम्मी की दवाई (Medicine Tracker)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = { showAddMedDialog = true },
                    modifier = Modifier.testTag("add_medicine_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, AlertRed),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ दवाई जोड़ें", color = AlertRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(medicines, key = { it.id }) { med ->
            MedicineItemCard(
                medicine = med,
                onToggle = { viewModel.toggleMedicine(med.id) }
            )
        }

        // Devotional / Bhajan & Spiritual Peace Section
        item {
            SpiritualAartiSection(
                onPlay = { devotionalName ->
                    viewModel.triggerGoogleSearch("$devotionalName sunao video YouTube")
                }
            )
        }

        // Daily Routine Checklist (दैनिक दिनचर्या)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "मम्मी की दैनिक दिनचर्या (Daily Routine)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        items(routines.filter { it.forMummy }, key = { it.id }) { routine ->
            RoutineItemCard(
                item = routine,
                onToggle = { viewModel.toggleRoutine(routine.id) }
            )
        }

        // Kitchen & Grocery Checklist (रसोई की सूची)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = WarmGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "रसोई एवं सामान (Rasoi Grocery List)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = { showAddGroceryDialog = true },
                    modifier = Modifier.testTag("add_grocery_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, WarmGold),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = WarmGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ सामान जोड़ें", color = WarmGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(groceries, key = { it.id }) { item ->
            GroceryItemCard(
                item = item,
                onToggle = { viewModel.toggleGrocery(item.id) }
            )
        }
    }

    if (showAddMedDialog) {
        AddMedicineDialog(
            onDismiss = { showAddMedDialog = false },
            onAdd = { name, dose, time, instructions ->
                viewModel.addMedicine(name, dose, time, instructions)
                showAddMedDialog = false
            }
        )
    }

    if (showAddGroceryDialog) {
        AddGroceryDialog(
            onDismiss = { showAddGroceryDialog = false },
            onAdd = { name, qty ->
                viewModel.addGrocery(name, qty)
                showAddGroceryDialog = false
            }
        )
    }
}

@Composable
private fun MummyEmergencyCallCard(
    mummyName: String,
    mummyPhone: String,
    onCall: () -> Unit,
    onQuickMessage: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSpaceCard),
        border = BorderStroke(1.5.dp, RoseLove.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = RoseLove,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mummyName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Phone: $mummyPhone",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Huge accessible 1-tap call button
                Button(
                    onClick = onCall,
                    modifier = Modifier
                        .testTag("one_tap_call_mummy")
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = DarkSpaceBg, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("कॉल करें", color = DarkSpaceBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "क्विक मैसेज (Quick Message to Mummy):",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val quickTexts = listOf(
                "घर आ रहा हूँ (Coming home)",
                "दवाई खा ली मम्मी? (Took medicine?)",
                "खाना खा लिया? (Had food?)",
                "5 मिनट में कॉल करता हूँ"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickTexts) { text ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSpaceCardElevated,
                        border = BorderStroke(1.dp, GlowBorder),
                        modifier = Modifier
                            .testTag("mummy_msg_${text.take(4)}")
                            .clickable { onQuickMessage(text) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = text, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceAnnounceBanner(onAnnounceRoutine: () -> Unit) {
    Surface(
        color = Brush.horizontalGradient(listOf(DarkSpaceCard, DarkSpaceCardElevated)).let { DarkSpaceCard },
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "मम्मी को दिनचर्या सुनाएं",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Priya aawaz mein pura schedule bolegi",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = onAnnounceRoutine,
                modifier = Modifier.testTag("announce_routine_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("सुनाओ", color = DarkSpaceBg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MedicineItemCard(
    medicine: MedicineReminder,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (medicine.taken) DarkSpaceCardElevated.copy(alpha = 0.4f) else DarkSpaceCard
        ),
        border = BorderStroke(1.dp, if (medicine.taken) SuccessGreen.copy(alpha = 0.4f) else AlertRed.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = medicine.taken,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SuccessGreen,
                    uncheckedColor = AlertRed
                ),
                modifier = Modifier.testTag("med_check_${medicine.id.take(4)}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.medicineName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (medicine.taken) TextMuted else TextPrimary,
                    textDecoration = if (medicine.taken) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = "${medicine.dosage} • ${medicine.time} • ${medicine.instructions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (medicine.taken) SuccessGreen.copy(alpha = 0.2f) else AlertRed.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (medicine.taken) "ले ली ✓" else "बाकी है",
                    color = if (medicine.taken) SuccessGreen else AlertRed,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SpiritualAartiSection(onPlay: (String) -> Unit) {
    Surface(
        color = DarkSpaceCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Headphones, contentDescription = null, tint = WarmGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "भक्ति, आरती एवं भजन (Devotional Peace)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            val bhajanList = listOf(
                "हनुमान चालीसा (Hanuman Chalisa)",
                "गायत्री मंत्र (Gayatri Mantra)",
                "शिव तांडव व आरती (Shiv Aarti)",
                "श्री कृष्ण भजन (Krishna Bhajan)"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bhajanList) { item ->
                    Button(
                        onClick = { onPlay(item) },
                        modifier = Modifier.testTag("bhajan_${item.take(4)}"),
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(item, color = WarmGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineItemCard(
    item: RoutineItem,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSpaceCard),
        border = BorderStroke(1.dp, if (item.isCompleted) SuccessGreen.copy(alpha = 0.3f) else GlowBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (item.isCompleted) SuccessGreen else TextMuted
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isCompleted) TextMuted else TextPrimary,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = "${item.time} • ${item.period.hindiLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun GroceryItemCard(
    item: com.example.data.model.GroceryItem,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSpaceCard),
        border = BorderStroke(1.dp, GlowBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isBought,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SuccessGreen)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (item.isBought) TextMuted else TextPrimary,
                textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.quantity,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, dose: String, time: String, instructions: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("1 Tablet") }
    var time by remember { mutableStateOf("08:00 AM") }
    var instructions by remember { mutableStateOf("खाने के बाद") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSpaceCard,
        title = {
            Text("मम्मी की नई दवाई जोड़ें", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("दवाई का नाम (e.g. Paracetamol)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = { Text("खुराक (e.g. 1 Tablet, 5ml)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("समय (e.g. 09:00 AM / Raat)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("निर्देश (e.g. खाने के बाद / खाली पेट)") },
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
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, dose, time, instructions)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("सुरक्षित करें", color = DarkSpaceBg, fontWeight = FontWeight.Bold)
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
private fun AddGroceryDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, qty: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1 kg") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSpaceCard,
        title = {
            Text("रसोई का सामान जोड़ें", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("सामान का नाम (e.g. दूध, आटा)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("मात्रा (e.g. 1 Litre, 2 kg)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, qty)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
            ) {
                Text("जोड़ें", color = DarkSpaceBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें", color = TextSecondary)
            }
        }
    )
}
