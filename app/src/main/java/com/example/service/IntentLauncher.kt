package com.example.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object IntentLauncher {

    /**
     * Launches Android's hidden RadioInfo / Phone Information screen (*#*#4636#*#*).
     * This provides the universal "Set Preferred Network Type" dropdown (NR ONLY, LTE ONLY, NR/LTE/GSM).
     */
    fun launchRadioInfoTesting(context: Context): Boolean {
        val candidateIntents = listOf(
            Intent().apply {
                component = ComponentName("com.android.settings", "com.android.settings.RadioInfo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            Intent().apply {
                component = ComponentName("com.android.settings", "com.android.settings.TestingSettings")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.android.phone", "com.android.phone.settings.RadioInfo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            Intent("android.intent.action.MAIN").apply {
                component = ComponentName("com.android.settings", "com.android.settings.Settings\$RadioInfoActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )

        for (intent in candidateIntents) {
            try {
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                // Try next candidate
            }
        }

        // Fallback: direct to Mobile Network settings
        return launchMobileNetworkSettings(context)
    }

    fun launchMobileNetworkSettings(context: Context): Boolean {
        return try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                true
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open Network Settings: ${ex.message}", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun launchApnSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APN_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "APN settings unavailable on this device", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun launchDeveloperOptions(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Please enable Developer Options in Android Settings", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun launchShizukuApp(context: Context): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(marketIntent)
                true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Shizuku app not found: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
