package com.example.data.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.AppNotificationType

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationTargetPayload(
    val type: AppNotificationType,
    val targetId: String,
)

object SurabayaNotificationHelper {
    private const val TAG = "SurabayaNotifHelper"

    const val CHANNEL_ID_URGENT = "surabaya_urgent_channel"
    const val CHANNEL_ID_GENERAL = "surabaya_general_channel"

    const val EXTRA_NOTIF_TYPE = "extra_notif_type"
    const val EXTRA_NOTIF_TARGET_ID = "extra_notif_target_id"

    private val _pendingNotificationPayload = MutableStateFlow<NotificationTargetPayload?>(null)
    val pendingNotificationPayload: StateFlow<NotificationTargetPayload?> = _pendingNotificationPayload.asStateFlow()

    fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val typeStr = intent.getStringExtra(EXTRA_NOTIF_TYPE) ?: return
        val targetId = intent.getStringExtra(EXTRA_NOTIF_TARGET_ID).orEmpty()
        val type = try {
            AppNotificationType.valueOf(typeStr)
        } catch (_: Exception) {
            null
        }
        if (type != null) {
            _pendingNotificationPayload.value = NotificationTargetPayload(type, targetId)
            Log.d(TAG, "Parsed incoming notification intent: type=$type, targetId=$targetId")
        }
    }

    fun consumePendingPayload() {
        _pendingNotificationPayload.value = null
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // 1. Saluran Utama Notifikasi Penting (Heads-Up Banner di atas layar)
            val urgentChannel = NotificationChannel(
                CHANNEL_ID_URGENT,
                "Notifikasi Penting Surabaya",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Pesan chat warga, agenda event baru, dan peringatan darurat kota Surabaya"
                enableLights(true)
                lightColor = 0xFF006B5E.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setSound(defaultSoundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // 2. Saluran Informasi Umum
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "Kabar & Informasi Warga",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Update destinasi, kuliner, dan informasi komunitas warga Surabaya"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(urgentChannel, generalChannel))
            Log.d(TAG, "Notification channels initialized successfully.")
        }
    }

    fun canPostNotifications(context: Context): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun showHeadsUpNotification(
        context: Context,
        title: String,
        message: String,
        type: AppNotificationType,
        targetId: String = "",
        notificationId: Int = (System.currentTimeMillis() % 100_000).toInt(),
    ) {
        try {
            createNotificationChannels(context)

            if (!canPostNotifications(context)) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted or notifications disabled by user.")
                return
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_NOTIF_TYPE, type.name)
                putExtra(EXTRA_NOTIF_TARGET_ID, targetId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val channelId = if (type == AppNotificationType.CHAT || type == AppNotificationType.EVENT || type == AppNotificationType.INCIDENT) {
                CHANNEL_ID_URGENT
            } else {
                CHANNEL_ID_GENERAL
            }

            val category = when (type) {
                AppNotificationType.CHAT -> NotificationCompat.CATEGORY_MESSAGE
                AppNotificationType.EVENT -> NotificationCompat.CATEGORY_EVENT
                AppNotificationType.INCIDENT -> NotificationCompat.CATEGORY_ALARM
                AppNotificationType.SYSTEM -> NotificationCompat.CATEGORY_STATUS
            }

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(category)
                .setColor(0xFF006B5E.toInt())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri)
                .setVibrate(longArrayOf(0, 250, 150, 250))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
            Log.d(TAG, "Heads-up notification posted: $title")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException while posting notification: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show heads-up notification", e)
        }
    }
}
