package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val cellular by viewModel.cellularState.collectAsStateWithLifecycle()
    val wifi by viewModel.wifiState.collectAsStateWithLifecycle()
    val ping by viewModel.pingState.collectAsStateWithLifecycle()
    val cellTowers by viewModel.cellTowers.collectAsStateWithLifecycle()
    val diagnostic by viewModel.diagnosticState.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val isApplying by viewModel.isApplying.collectAsStateWithLifecycle()
    val applyResult by viewModel.applyResult.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val statusBarIconEnabled by viewModel.statusBarIconEnabled.collectAsStateWithLifecycle()
    val selectedStatusBarIcon by viewModel.selectedStatusBarIcon.collectAsStateWithLifecycle()

    // Request notification and location permissions for status bar & tower GPS accuracy
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true) {
            viewModel.syncStatusBar()
        }
        viewModel.refreshAll()
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Handle back button on sub-screens to return to Dashboard
    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        viewModel.selectTab(AppTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = IosDeepBlack,
        bottomBar = {
            IosBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    AppTab.DASHBOARD -> DashboardScreen(
                        cellular = cellular,
                        ping = ping,
                        diagnostic = diagnostic,
                        selectedMode = selectedMode,
                        isApplying = isApplying,
                        applyResult = applyResult,
                        statusBarIconEnabled = statusBarIconEnabled,
                        selectedStatusBarIcon = selectedStatusBarIcon,
                        onSelectMode = { viewModel.selectMode(it) },
                        onApplyModeOneClick = { viewModel.applySelectedModeOneClick(context) },
                        onApplyMode = { viewModel.applySelectedMode() },
                        onToggleStatusBarIcon = { viewModel.setStatusBarIconEnabled(it) },
                        onSelectStatusBarIcon = { viewModel.setStatusBarIcon(it) },
                        onRefreshTelemetry = { viewModel.refreshAll() }
                    )

                    AppTab.MAP -> TowerMapScreen(
                        cellular = cellular,
                        towers = cellTowers,
                        onRefreshTowers = { viewModel.refreshAll() }
                    )

                    AppTab.TELEMETRY -> TelemetryScreen(
                        cellular = cellular,
                        wifi = wifi
                    )

                    AppTab.SPEED_PING -> SpeedPingScreen(
                        ping = ping,
                        onStartPing = { host, name -> viewModel.startPing(host, name) },
                        onStopPing = { viewModel.stopPing() }
                    )

                    AppTab.TOOLS -> ToolsSettingsScreen(
                        diagnostic = diagnostic,
                        hapticEnabled = hapticEnabled,
                        statusBarIconEnabled = statusBarIconEnabled,
                        selectedStatusBarIcon = selectedStatusBarIcon,
                        onRefreshDiagnostics = { viewModel.refreshAll() },
                        onToggleHaptic = { viewModel.setHapticEnabled(it) },
                        onToggleStatusBarIcon = { viewModel.setStatusBarIconEnabled(it) },
                        onSelectStatusBarIcon = { viewModel.setStatusBarIcon(it) }
                    )
                }
            }
        }
    }
}

/**
 * Translucent Apple iOS Tab Bar
 */
@Composable
fun IosBottomNavigationBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color(0xEB121319),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabNavItem(
                tab = AppTab.DASHBOARD,
                icon = Icons.Rounded.GridView,
                isSelected = currentTab == AppTab.DASHBOARD,
                onClick = { onTabSelected(AppTab.DASHBOARD) }
            )
            TabNavItem(
                tab = AppTab.MAP,
                icon = Icons.Rounded.Map,
                isSelected = currentTab == AppTab.MAP,
                onClick = { onTabSelected(AppTab.MAP) }
            )
            TabNavItem(
                tab = AppTab.TELEMETRY,
                icon = Icons.Rounded.CellTower,
                isSelected = currentTab == AppTab.TELEMETRY,
                onClick = { onTabSelected(AppTab.TELEMETRY) }
            )
            TabNavItem(
                tab = AppTab.SPEED_PING,
                icon = Icons.Rounded.Speed,
                isSelected = currentTab == AppTab.SPEED_PING,
                onClick = { onTabSelected(AppTab.SPEED_PING) }
            )
            TabNavItem(
                tab = AppTab.TOOLS,
                icon = Icons.Rounded.Tune,
                isSelected = currentTab == AppTab.TOOLS,
                onClick = { onTabSelected(AppTab.TOOLS) }
            )
        }
    }
}

@Composable
private fun TabNavItem(
    tab: AppTab,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("tab_${tab.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tab.title,
            tint = if (isSelected) IosCyan else IosGray1,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = tab.title,
            color = if (isSelected) IosCyan else IosGray1,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
