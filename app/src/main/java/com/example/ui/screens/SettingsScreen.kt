package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StatusBarIconType
import com.example.service.IntentLauncher
import com.example.ui.components.IosGroupedCard
import com.example.ui.components.IosListRow
import com.example.ui.components.IosSwitch
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    hapticEnabled: Boolean = true,
    statusBarIconEnabled: Boolean = true,
    selectedStatusBarIcon: StatusBarIconType = StatusBarIconType.FIVE_G,
    onToggleHaptic: (Boolean) -> Unit = {},
    onToggleStatusBarIcon: (Boolean) -> Unit = {},
    onSelectStatusBarIcon: (StatusBarIconType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var autoFallback by remember { mutableStateOf(true) }
    var highAccuracyMode by remember { mutableStateOf(true) }
    var selectedSimSlot by remember { mutableIntStateOf(1) }

    fun triggerHaptic() {
        if (!hapticEnabled) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            Text(
                text = "PREFERENCES & HARDWARE CONTROLS",
                fontSize = 11.sp,
                color = IosCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        // STATUS BAR ICON OPTIONS (USER REQUEST)
        Text(
            text = "STATUS BAR CUSTOMIZATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "Status Bar Icon",
                subtitle = "Display persistent 5G/4G/Tower icon in top status bar",
                icon = Icons.Rounded.NotificationsActive,
                iconBgColor = IosPurple,
                trailingContent = {
                    IosSwitch(
                        checked = statusBarIconEnabled,
                        onCheckedChange = {
                            triggerHaptic()
                            onToggleStatusBarIcon(it)
                        }
                    )
                }
            )

            if (statusBarIconEnabled) {
                HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ACTIVE ICON STYLE",
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
                                .testTag("settings_status_icon_${iconItem.id}"),
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

        // 1-CLICK AUTOMATION SETUP (USER REQUEST)
        Text(
            text = "1-CLICK AUTOMATION (RADIO INFO)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "Hands-Free Auto Pilot",
                subtitle = "Enables automatic selection of NR only / LTE only inside Radio Info (*#*#4636#*#*)",
                icon = Icons.Rounded.AutoMode,
                iconBgColor = IosCyan,
                trailingBadge = "Accessibility",
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    } catch (_: Exception) {}
                }
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "Launch Radio Info Directly",
                subtitle = "Manual verification shortcut (*#*#4636#*#*)",
                icon = Icons.Rounded.Dialpad,
                iconBgColor = IosBlue,
                onClick = { IntentLauncher.launchRadioInfoTesting(context) }
            )
        }

        // SIM Management
        Text(
            text = "SIM & HARDWARE ALLOCATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "Primary Data SIM",
                subtitle = "Active subscription targeted for 5G switching",
                icon = Icons.Rounded.SimCard,
                iconBgColor = IosBlue,
                trailingText = "SIM $selectedSimSlot",
                onClick = {
                    triggerHaptic()
                    selectedSimSlot = if (selectedSimSlot == 1) 2 else 1
                }
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "VoNR Auto-Recovery",
                subtitle = "Revert to AUTO if carrier drops emergency VoNR calls",
                icon = Icons.Rounded.PhoneCallback,
                iconBgColor = IosGreen,
                trailingContent = {
                    IosSwitch(
                        checked = autoFallback,
                        onCheckedChange = {
                            triggerHaptic()
                            autoFallback = it
                        }
                    )
                }
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "Carrier APN & Roaming",
                subtitle = "Open Android carrier roaming profile",
                icon = Icons.Rounded.Public,
                iconBgColor = IosOrange,
                onClick = { IntentLauncher.launchMobileNetworkSettings(context) }
            )
        }

        // Tactile & Interface
        Text(
            text = "TACTILE & DESIGN INTERACTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "iOS Taptic Engine Feedback",
                subtitle = "Tactile click when toggling switches & mode buttons",
                icon = Icons.Rounded.Vibration,
                iconBgColor = IosPurple,
                trailingContent = {
                    IosSwitch(
                        checked = hapticEnabled,
                        onCheckedChange = {
                            onToggleHaptic(it)
                            triggerHaptic()
                        }
                    )
                }
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "High Accuracy Telemetry",
                subtitle = "Continuous background polling of signal dBm and ASU",
                icon = Icons.Rounded.GpsFixed,
                iconBgColor = IosCyan,
                trailingContent = {
                    IosSwitch(
                        checked = highAccuracyMode,
                        onCheckedChange = {
                            triggerHaptic()
                            highAccuracyMode = it
                        }
                    )
                }
            )
        }

        // Quick Settings Tile guide
        Text(
            text = "NOTIFICATION QUICK SETTINGS TILE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "Add '5G Switch Pro' Tile",
                subtitle = "Swipe down twice from top > tap Edit > drag 5G Switch Pro tile for instant 1-tap switching anytime.",
                icon = Icons.Rounded.Widgets,
                iconBgColor = IosPink,
                showChevron = false
            )
        }

        // About & Safety Disclaimer
        Text(
            text = "ABOUT & SAFETY GUARANTEE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "5G Switch Pro",
                subtitle = "Ultra-premium iOS Cupertino Design v1.1.0",
                icon = Icons.Rounded.VerifiedUser,
                iconBgColor = IosBlue,
                trailingBadge = "v1.1 Pro",
                showChevron = false
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "Fail-Safe Architecture",
                subtitle = "Designed with zero-risk verification. Unsupported modems are preserved without hazardous blind writes.",
                icon = Icons.Rounded.Shield,
                iconBgColor = IosGreen,
                showChevron = false
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
