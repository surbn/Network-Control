package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.NetworkMode
import com.example.model.StatusBarIconType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StatusBarIconService : Service() {

    companion object {
        const val CHANNEL_ID = "channel_5g_status_bar"
        const val NOTIFICATION_ID = 5001

        const val ACTION_START_OR_UPDATE = "com.example.action.START_OR_UPDATE"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_SET_MODE = "com.example.action.SET_MODE"

        const val EXTRA_ICON_TYPE = "extra_icon_type"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_CARRIER = "extra_carrier"
        const val EXTRA_PING = "extra_ping"

        fun updateStatusBarIcon(
            context: Context,
            iconType: StatusBarIconType,
            mode: NetworkMode = NetworkMode.AUTO,
            carrier: String = "5G Carrier",
            pingMs: Long = 18L
        ) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                createNotificationChannel(context, notificationManager)
                val notification = buildNotification(context, iconType, mode, carrier, pingMs)
                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }

        fun stopStatusBarIcon(context: Context) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                notificationManager.cancel(NOTIFICATION_ID)
            } catch (_: Exception) {}
        }

        fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "5G Status Bar Monitor"
                val descriptionText = "Displays real-time network mode icon on Android status bar"
                val importance = NotificationManager.IMPORTANCE_LOW
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    setShowBadge(false)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun buildNotification(
            context: Context,
            iconType: StatusBarIconType,
            mode: NetworkMode,
            carrier: String,
            pingMs: Long
        ): android.app.Notification {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val force5gIntent = Intent(context, StatusBarIconService::class.java).apply {
                action = ACTION_SET_MODE
                putExtra(EXTRA_MODE, NetworkMode.NR_ONLY.name)
                putExtra(EXTRA_ICON_TYPE, iconType.name)
            }
            val force5gPendingIntent = PendingIntent.getService(
                context,
                1,
                force5gIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val autoIntent = Intent(context, StatusBarIconService::class.java).apply {
                action = ACTION_SET_MODE
                putExtra(EXTRA_MODE, NetworkMode.AUTO.name)
                putExtra(EXTRA_ICON_TYPE, iconType.name)
            }
            val autoPendingIntent = PendingIntent.getService(
                context,
                2,
                autoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconType.drawableRes)
                .setContentTitle("5G Switch Pro • ${mode.title}")
                .setContentText("$carrier • Latency: ${pingMs}ms")
                .setSubText("5G Engine")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.ic_status_bolt, "⚡ 1-Click 5G", force5gPendingIntent)
                .addAction(R.drawable.ic_status_tower, "🔄 Auto Mode", autoPendingIntent)
                .build()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent.action == ACTION_SET_MODE) {
            val modeName = intent.getStringExtra(EXTRA_MODE) ?: NetworkMode.AUTO.name
            val mode = try { NetworkMode.valueOf(modeName) } catch (_: Exception) { NetworkMode.AUTO }
            CoroutineScope(Dispatchers.IO).launch {
                RootNetworkController.applyMode(mode)
            }
        }

        val iconTypeName = intent.getStringExtra(EXTRA_ICON_TYPE) ?: StatusBarIconType.FIVE_G.name
        val iconType = try { StatusBarIconType.valueOf(iconTypeName) } catch (_: Exception) { StatusBarIconType.FIVE_G }
        val modeName = intent.getStringExtra(EXTRA_MODE) ?: NetworkMode.AUTO.name
        val mode = try { NetworkMode.valueOf(modeName) } catch (_: Exception) { NetworkMode.AUTO }
        val carrier = intent.getStringExtra(EXTRA_CARRIER) ?: "5G SA Active"
        val pingMs = intent.getLongExtra(EXTRA_PING, 18L)

        updateStatusBarIcon(this, iconType, mode, carrier, pingMs)
        return START_NOT_STICKY
    }
}
