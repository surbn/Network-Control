package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiagnosticReport
import com.example.service.IntentLauncher
import com.example.service.RootNetworkController
import com.example.ui.components.IosGroupedCard
import com.example.ui.components.IosListRow
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ToolsScreen(
    diagnostic: DiagnosticReport,
    onRefreshDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var terminalOutput by remember { mutableStateOf("Ready for shell diagnosis.") }
    var isExecuting by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard: $text", Toast.LENGTH_SHORT).show()
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
                text = "Phone Tools",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            Text(
                text = "SECRET CODES & PRIVILEGED DIAGNOSTICS",
                fontSize = 11.sp,
                color = IosCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        // Secret Code *#*#4636#*#* Hero Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, IosBlue.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            color = Color(0xFF131724)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(IosBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Dialpad, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Radio Info (*#*#4636#*#*)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Universal non-root 5G NR force shortcut", color = IosCyan, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Opens Android's hidden engineering menu. Scroll to 'Set Preferred Network Type' and select 'NR only' or 'LTE only'. Works on almost all Android devices without root!",
                    color = IosGray1,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { IntentLauncher.launchRadioInfoTesting(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("launch_radio_info_btn")
                ) {
                    Icon(Icons.Rounded.OpenInNew, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Launch Hidden Radio Info Menu", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Android System Quick Shortcuts
        Text(
            text = "SYSTEM SETTINGS SHORTCUTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "Mobile Network Settings",
                subtitle = "SIM preferences, data roaming & APN",
                icon = Icons.Rounded.SettingsCell,
                iconBgColor = IosGreen,
                onClick = { IntentLauncher.launchMobileNetworkSettings(context) }
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "APN Configurations",
                subtitle = "Access Point Names for 5G SA MMS/Data",
                icon = Icons.Rounded.VpnKey,
                iconBgColor = IosOrange,
                onClick = { IntentLauncher.launchApnSettings(context) }
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "Developer Options",
                subtitle = "Enable Wireless Debugging for Shizuku",
                icon = Icons.Rounded.DeveloperMode,
                iconBgColor = IosPurple,
                onClick = { IntentLauncher.launchDeveloperOptions(context) }
            )
        }

        // Shizuku Manager Integration Card
        Text(
            text = "SHIZUKU PRIVILEGED MANAGER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "Shizuku Status",
                subtitle = if (diagnostic.isShizukuBound) "Privileged Binder Active" else "Not Connected",
                icon = Icons.Rounded.AccountTree,
                iconBgColor = if (diagnostic.isShizukuBound) IosGreen else Color(0xFF383A4C),
                trailingBadge = if (diagnostic.isShizukuBound) "v${diagnostic.shizukuVersion}" else "Standby",
                showChevron = false
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "Open Shizuku App",
                subtitle = "Configure ADB Wireless authorization",
                icon = Icons.Rounded.Launch,
                iconBgColor = IosCyan,
                onClick = { IntentLauncher.launchShizukuApp(context) }
            )
        }

        // SuperUser Console & Diagnostic Test
        Text(
            text = "PRIVILEGED DIAGNOSTIC CONSOLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isExecuting = true
                            terminalOutput = RootNetworkController.runShellTest("id")
                            isExecuting = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test 'id'", fontSize = 11.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isExecuting = true
                            terminalOutput = RootNetworkController.runShellTest("getprop ro.product.model")
                            isExecuting = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Model Info", fontSize = 11.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isExecuting = true
                            terminalOutput = RootNetworkController.runShellTest("getprop gsm.network.type")
                            isExecuting = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("GSM Type", fontSize = 11.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dark Terminal Window
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090A0E))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DIAGNOSTIC SHELL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosGray1,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy",
                            tint = IosGray1,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { copyToClipboard("Shell Output", terminalOutput) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = terminalOutput,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = IosGreen,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
