package com.example

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavScreen
import com.example.ui.PriyaViewModel
import com.example.ui.UIEvent
import com.example.ui.screens.CompanionChatScreen
import com.example.ui.screens.IncomingCallScreen
import com.example.ui.screens.MummyCareScreen
import com.example.ui.screens.QuickActionsScreen
import com.example.ui.screens.RoutinePlannerScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSpaceBg
import com.example.ui.theme.DarkSpaceCard
import com.example.ui.theme.GlowBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoseLove
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: PriyaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val context = LocalContext.current
                val currentScreen by viewModel.currentScreen.collectAsState()
                val isIncomingCallActive by viewModel.isIncomingCallActive.collectAsState()
                val isInCall by viewModel.isInCall.collectAsState()

                // Permission request launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { /* Permissions result handled gracefully */ }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.SEND_SMS
                        )
                    )
                }

                // Handle UI Events from ViewModel
                LaunchedEffect(Unit) {
                    viewModel.uiEvents.collect { event ->
                        when (event) {
                            is UIEvent.ShowToast -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            }
                            is UIEvent.LaunchIntent -> {
                                try {
                                    context.startActivity(event.intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                // Back navigation handling
                if (currentScreen != AppNavScreen.COMPANION_CHAT) {
                    BackHandler {
                        viewModel.setScreen(AppNavScreen.COMPANION_CHAT)
                    }
                }

                // Fullscreen Call Overlay if active
                if (isIncomingCallActive || isInCall) {
                    IncomingCallScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = DarkSpaceBg,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            PriyaBottomNavigationBar(
                                currentScreen = currentScreen,
                                onSelectScreen = { viewModel.setScreen(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                AppNavScreen.COMPANION_CHAT -> CompanionChatScreen(viewModel = viewModel)
                                AppNavScreen.MUMMY_CARE -> MummyCareScreen(viewModel = viewModel)
                                AppNavScreen.ACTIONS_APPS -> QuickActionsScreen(viewModel = viewModel)
                                AppNavScreen.ROUTINE_PLANNER -> RoutinePlannerScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriyaBottomNavigationBar(
    currentScreen: AppNavScreen,
    onSelectScreen: (AppNavScreen) -> Unit
) {
    NavigationBar(
        containerColor = DarkSpaceCard,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppNavScreen.COMPANION_CHAT,
            onClick = { onSelectScreen(AppNavScreen.COMPANION_CHAT) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Priya AI",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Priya AI",
                    fontWeight = if (currentScreen == AppNavScreen.COMPANION_CHAT) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberCyan,
                selectedTextColor = CyberCyan,
                indicatorColor = CyberCyan.copy(alpha = 0.2f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_chat")
        )

        NavigationBarItem(
            selected = currentScreen == AppNavScreen.MUMMY_CARE,
            onClick = { onSelectScreen(AppNavScreen.MUMMY_CARE) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Mummy Care",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "मम्मी केयर",
                    fontWeight = if (currentScreen == AppNavScreen.MUMMY_CARE) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoseLove,
                selectedTextColor = RoseLove,
                indicatorColor = RoseLove.copy(alpha = 0.2f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_mummy")
        )

        NavigationBarItem(
            selected = currentScreen == AppNavScreen.ACTIONS_APPS,
            onClick = { onSelectScreen(AppNavScreen.ACTIONS_APPS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "Apps & Contacts",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "सारे Apps",
                    fontWeight = if (currentScreen == AppNavScreen.ACTIONS_APPS) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberCyan,
                selectedTextColor = CyberCyan,
                indicatorColor = CyberCyan.copy(alpha = 0.2f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_apps")
        )

        NavigationBarItem(
            selected = currentScreen == AppNavScreen.ROUTINE_PLANNER,
            onClick = { onSelectScreen(AppNavScreen.ROUTINE_PLANNER) },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Routine",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "दिनचर्या",
                    fontWeight = if (currentScreen == AppNavScreen.ROUTINE_PLANNER) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberCyan,
                selectedTextColor = CyberCyan,
                indicatorColor = CyberCyan.copy(alpha = 0.2f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_routine")
        )
    }
}
