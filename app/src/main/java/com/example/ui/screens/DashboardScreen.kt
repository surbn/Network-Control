package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CellularTelemetry
import com.example.model.DiagnosticReport
import com.example.model.NetworkMode
import com.example.model.PingMetrics
import com.example.model.StatusBarIconType
import com.example.service.IntentLauncher
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    cellular: CellularTelemetry,
    ping: PingMetrics,
    diagnostic: DiagnosticReport,
    selectedMode: NetworkMode,
    isApplying: Boolean,
    applyResult: String,
    statusBarIconEnabled: Boolean,
    selectedStatusBarIcon: StatusBarIconType,
    onSelectMode: (NetworkMode) -> Unit,
    onApplyModeOneClick: () -> Unit,
    onApplyMode: () -> Unit,
    onToggleStatusBarIcon: (Boolean) -> Unit,
    onSelectStatusBarIcon: (StatusBarIconType) -> Unit,
    onRefreshTelemetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var voNrActive by remember { mutableStateOf(true) }
    var gamingLatencyMode by remember { mutableStateOf(false) }
    var cellularDataActive by remember { mutableStateOf(true) }

    fun triggerHaptic() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(25)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Island Banner
        IosDynamicIsland(
            carrier = cellular.carrierName,
            networkType = cellular.networkType,
            pingMs = ping.currentMs,
            is5G = cellular.is5G,
            onRadioInfoClick = { IntentLauncher.launchRadioInfoTesting(context) }
        )

        // Apple Style Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Control Center",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Text(
                    text = "5G & LTE TELEPHONY ENGINE",
                    fontSize = 11.sp,
                    color = IosCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        triggerHaptic()
                        onRefreshTelemetry()
                    },
                color = IosBlue.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, IosBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = IosBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Refresh",
                        color = IosBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Live Network Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp)),
            color = Color(0xFF14161F)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-20).dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (cellular.is5G) IosCyan.copy(alpha = 0.25f) else IosBlue.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                if (cellular.is5G) IosCyan else IosBlue,
                                                if (cellular.is5G) IosPurple else Color(0xFF5E5CE6)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CellTower,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = cellular.carrierName,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SIM 1 • ${cellular.operatorCode}",
                                    color = IosGray1,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (cellular.is5G) IosGreen.copy(alpha = 0.16f) else IosBlue.copy(alpha = 0.16f),
                            border = BorderStroke(
                                1.dp,
                                if (cellular.is5G) IosGreen.copy(alpha = 0.4f) else IosBlue.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = if (cellular.is5G) "5G SA ULTRA" else "4G LTE+",
                                color = if (cellular.is5G) IosGreen else IosCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PRIMARY BAND", color = IosGray1, fontSize = 10.sp, letterSpacing = 0.8.sp)
                            Text(cellular.primaryBand, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("SIGNAL STRENGTH", color = IosGray1, fontSize = 10.sp, letterSpacing = 0.8.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${cellular.signalDbm} dBm", color = IosCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                MiniSignalBars(level = cellular.signalLevel)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Carrier Aggregation: ${cellular.caStatus}",
                        color = IosGray1,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // STATUS BAR ICON CHANGER (USER REQUEST)
        IosGroupedCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(IosPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = selectedStatusBarIcon.drawableRes),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Status Bar Icon",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (statusBarIconEnabled) "Showing: ${selectedStatusBarIcon.title}" else "Disabled",
                            color = if (statusBarIconEnabled) IosCyan else IosGray1,
                            fontSize = 12.sp
                        )
                    }
                }

                IosSwitch(
                    checked = statusBarIconEnabled,
                    onCheckedChange = {
                        triggerHaptic()
                        onToggleStatusBarIcon(it)
                    }
                )
            }

            if (statusBarIconEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "SELECT STATUS BAR ICON STYLE",
                    color = IosGray1,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusBarIconType.entries.forEach { iconItem ->
                        val isSelected = iconItem == selectedStatusBarIcon
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) IosCyan else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    triggerHaptic()
                                    onSelectStatusBarIcon(iconItem)
                                }
                                .testTag("status_icon_${iconItem.id}"),
                            color = if (isSelected) IosCyan.copy(alpha = 0.18f) else Color(0xFF1E212D)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) IosCyan else Color(0xFF2C2F3E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = iconItem.drawableRes),
                                        contentDescription = iconItem.title,
                                        tint = if (isSelected) Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = iconItem.title,
                                    color = if (isSelected) IosCyan else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // iOS Segmented Mode Selector & AUTOMATIC 1-CLICK CHANGE (USER REQUEST)
        IosGroupedCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NETWORK MODE SELECTOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosGray1,
                    letterSpacing = 1.2.sp
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = IosCyan.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "1-CLICK READY",
                        color = IosCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            IosSegmentedControl(
                items = NetworkMode.entries,
                selectedItem = selectedMode,
                onItemSelected = {
                    triggerHaptic()
                    onSelectMode(it)
                },
                label = { it.shortName },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E212B),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (selectedMode) {
                                NetworkMode.NR_ONLY -> Icons.Rounded.FlashOn
                                NetworkMode.AUTO -> Icons.Rounded.Autorenew
                                NetworkMode.LTE_ONLY -> Icons.Rounded.SignalCellularAlt
                                NetworkMode.LEGACY -> Icons.Rounded.BatterySaver
                            },
                            contentDescription = null,
                            tint = when (selectedMode) {
                                NetworkMode.NR_ONLY -> IosCyan
                                NetworkMode.AUTO -> IosGreen
                                NetworkMode.LTE_ONLY -> IosBlue
                                NetworkMode.LEGACY -> IosOrange
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedMode.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = selectedMode.badge,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = selectedMode.desc,
                        color = IosGray1,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Radio Info Target: \"${selectedMode.spinnerMatchKeyword}\"",
                        color = IosCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PRIMARY AUTOMATIC 1-CLICK CHANGE BUTTON
            Button(
                onClick = {
                    triggerHaptic()
                    onApplyModeOneClick()
                },
                enabled = !isApplying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == NetworkMode.NR_ONLY) IosCyan else IosBlue
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("one_click_auto_switch_btn")
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto Switching...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.FlashOn,
                        contentDescription = null,
                        tint = if (selectedMode == NetworkMode.NR_ONLY) Color.Black else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ Automatic 1-Click Switch (${selectedMode.shortName})",
                        color = if (selectedMode == NetworkMode.NR_ONLY) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        triggerHaptic()
                        onApplyMode()
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Rounded.Terminal, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Direct Shell", color = Color.White, fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = {
                        triggerHaptic()
                        IntentLauncher.launchRadioInfoTesting(context)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF2C2D38)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("radio_info_manual_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Dialpad,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Radio Info (*#4636)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (applyResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedVisibility(visible = true) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0F1218),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = applyResult,
                            color = if (applyResult.contains("✓")) IosGreen else IosGray1,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // iOS Control Center 4-Tile Grid
        Text(
            text = "QUICK TOGGLES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IosControlTile(
                title = "5G SA Force",
                status = if (selectedMode == NetworkMode.NR_ONLY) "Active" else "Off",
                icon = Icons.Rounded.FlashOn,
                isActive = (selectedMode == NetworkMode.NR_ONLY),
                activeColor = IosCyan,
                onClick = {
                    triggerHaptic()
                    if (selectedMode == NetworkMode.NR_ONLY) onSelectMode(NetworkMode.AUTO)
                    else onSelectMode(NetworkMode.NR_ONLY)
                },
                modifier = Modifier.weight(1f)
            )

            IosControlTile(
                title = "VoNR Voice",
                status = if (voNrActive) "Enabled" else "Disabled",
                icon = Icons.Rounded.Voicemail,
                isActive = voNrActive,
                activeColor = IosGreen,
                onClick = {
                    triggerHaptic()
                    voNrActive = !voNrActive
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IosControlTile(
                title = "Cellular Data",
                status = if (cellularDataActive) "Active" else "Standby",
                icon = Icons.Rounded.SignalCellularAlt,
                isActive = cellularDataActive,
                activeColor = IosBlue,
                onClick = {
                    triggerHaptic()
                    cellularDataActive = !cellularDataActive
                    IntentLauncher.launchMobileNetworkSettings(context)
                },
                modifier = Modifier.weight(1f)
            )

            IosControlTile(
                title = "Gaming Mode",
                status = if (gamingLatencyMode) "Ultra Low Latency" else "Standard",
                icon = Icons.Rounded.SportsEsports,
                isActive = gamingLatencyMode,
                activeColor = IosPurple,
                onClick = {
                    triggerHaptic()
                    gamingLatencyMode = !gamingLatencyMode
                    if (gamingLatencyMode) onSelectMode(NetworkMode.NR_ONLY)
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Diagnostics & Privileged Engine Status
        IosGroupedCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(IosGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = IosGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "System Diagnostics",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = if (diagnostic.isRootAvailable || diagnostic.isShizukuBound) IosGreen.copy(alpha = 0.15f) else Color(0xFF2A2B35),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (diagnostic.isRootAvailable) "ROOT ACTIVE" else if (diagnostic.isShizukuBound) "SHIZUKU BOUND" else "SAFE NON-ROOT",
                        color = if (diagnostic.isRootAvailable) IosGreen else if (diagnostic.isShizukuBound) IosCyan else IosGray1,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${diagnostic.manufacturer} ${diagnostic.model} • Android ${diagnostic.androidRelease} (API ${diagnostic.androidSdk})",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = diagnostic.diagnosticSummary,
                color = IosGray1,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
