package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.PrivacyViewModel
import com.example.ui.components.PrivacyBlurOverlay
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GuardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ThreatCrimson

class MainActivity : ComponentActivity() {

    private val viewModel: PrivacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: PrivacyViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logs by viewModel.logsList.collectAsStateWithLifecycle()
    val threatCount by viewModel.threatCount.collectAsStateWithLifecycle()
    val ignoredCount by viewModel.ignoredCount.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = DarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (uiState.selectedTab) {
                                0 -> "Privacy Guard"
                                1 -> "Motion Logs & Analytics"
                                else -> "Settings & Privacy"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                    },
                    actions = {
                        if (uiState.isProtectionActive) {
                            Text(
                                text = "ON-DEVICE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = SafeEmerald,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == 0) Icons.Filled.Security else Icons.Outlined.Security,
                                contentDescription = "Guard & Content",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Guard", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyanLight,
                            selectedTextColor = CyberCyanLight,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_item_guard")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == 1) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                                contentDescription = "Dashboard Logs",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Logs", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyanLight,
                            selectedTextColor = CyberCyanLight,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_item_logs")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Settings", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyanLight,
                            selectedTextColor = CyberCyanLight,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
            ) {
                when (uiState.selectedTab) {
                    0 -> GuardScreen(
                        uiState = uiState,
                        detector = viewModel.getDetector(),
                        hasCameraPermission = hasCameraPermission,
                        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        onToggleProtection = { viewModel.toggleProtection() },
                        onTriggerSimulatedPeeker = { side, lowLight ->
                            viewModel.triggerSimulatedPeeker(side, lowLight)
                        },
                        onTriggerSimulatedEnvMotion = { viewModel.triggerSimulatedEnvironmentalMotion() },
                        onNavigateToSettings = { viewModel.setSelectedTab(2) }
                    )
                    1 -> DashboardScreen(
                        logs = logs,
                        threatCount = threatCount,
                        ignoredCount = ignoredCount,
                        searchQuery = uiState.searchQuery,
                        selectedFilterType = uiState.selectedFilterType,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterChange = { viewModel.setFilterType(it) },
                        onDeleteLog = { viewModel.deleteLog(it) },
                        onClearAllLogs = { viewModel.clearAllLogs() }
                    )
                    2 -> SettingsScreen(
                        uiState = uiState,
                        onSetSensitivity = { viewModel.setSensitivity(it) },
                        onToggleBatterySaver = { viewModel.toggleBatterySaver() },
                        onToggleLowLightBoost = { viewModel.toggleLowLightBoost() },
                        onToggleVibration = { viewModel.toggleVibration() }
                    )
                }
            }
        }

        // Full Screen Frosted Privacy Blur Overlay (triggers when a peeker is looking at the screen)
        PrivacyBlurOverlay(
            isTriggered = uiState.isPrivacyBlurTriggered,
            reason = uiState.blurReason,
            peekerAngle = uiState.peekerAngle,
            peekerDistance = uiState.peekerDistance,
            confidence = uiState.confidence,
            isLowLight = uiState.isLowLight,
            onDismiss = { viewModel.dismissBlur() }
        )
    }
}
