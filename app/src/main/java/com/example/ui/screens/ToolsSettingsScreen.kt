package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.DiagnosticReport
import com.example.model.StatusBarIconType
import com.example.ui.components.IosSegmentedControl

enum class ToolsSubTab(val title: String) {
    TOOLS("Phone Tools & Codes"),
    SETTINGS("Settings & Status Bar")
}

@Composable
fun ToolsSettingsScreen(
    diagnostic: DiagnosticReport = DiagnosticReport(),
    hapticEnabled: Boolean = true,
    statusBarIconEnabled: Boolean = true,
    selectedStatusBarIcon: StatusBarIconType = StatusBarIconType.FIVE_G,
    onRefreshDiagnostics: () -> Unit = {},
    onToggleHaptic: (Boolean) -> Unit = {},
    onToggleStatusBarIcon: (Boolean) -> Unit = {},
    onSelectStatusBarIcon: (StatusBarIconType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember { mutableStateOf(ToolsSubTab.TOOLS) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Sub-tab Segmented Control
        Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
            IosSegmentedControl(
                items = ToolsSubTab.entries,
                selectedItem = currentSubTab,
                onItemSelected = { currentSubTab = it },
                label = { it.title },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Crossfade(targetState = currentSubTab, label = "tools_settings_crossfade") { subTab ->
            when (subTab) {
                ToolsSubTab.TOOLS -> ToolsScreen(
                    diagnostic = diagnostic,
                    onRefreshDiagnostics = onRefreshDiagnostics
                )
                ToolsSubTab.SETTINGS -> SettingsScreen(
                    hapticEnabled = hapticEnabled,
                    statusBarIconEnabled = statusBarIconEnabled,
                    selectedStatusBarIcon = selectedStatusBarIcon,
                    onToggleHaptic = onToggleHaptic,
                    onToggleStatusBarIcon = onToggleStatusBarIcon,
                    onSelectStatusBarIcon = onSelectStatusBarIcon
                )
            }
        }
    }
}
