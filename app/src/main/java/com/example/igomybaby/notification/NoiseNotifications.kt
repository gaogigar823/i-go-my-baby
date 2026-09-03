package com.example.igomybaby.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat

object NoiseNotifications {
    const val SERVICE_CHANNEL_ID = "decibel_service"
    const val ALERT_CHANNEL_ID = "decibel_alert_v2"

    private const val LEGACY_ALERT_CHANNEL_ID = "decibel_alert"
    private const val ALERT_NOTIFICATION_ID = 2001
    private const val PREVIEW_NOTIFICATION_ID = 3001
    private val vibrationPattern = longArrayOf(0, 400, 200, 400)

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.deleteNotificationChannel(LEGACY_ALERT_CHANNEL_ID)
        manager.createNotificationChannel(
            NotificationChannel(
                SERVICE_CHANNEL_ID,
                "소음 감지 서비스",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) },
        )

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        manager.createNotificationChannel(
            NotificationChannel(
                ALERT_CHANNEL_ID,
                "소음 감지 알림",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                enableVibration(true)
                vibrationPattern = NoiseNotifications.vibrationPattern
                setSound(sound, audioAttributes)
                setBypassDnd(true)
            },
        )
    }

    fun showAlert(context: Context, db: Int, threshold: Int) {
        notify(
            context = context,
            id = ALERT_NOTIFICATION_ID,
            title = "큰 소음 감지",
            message = "$db dB · 임계값 ${threshold}dB 초과",
        )
    }

    fun showPreview(context: Context) {
        notify(
            context = context,
            id = PREVIEW_NOTIFICATION_ID,
            title = "알림 미리보기",
            message = "연결된 워치가 진동하는지 확인해보세요",
        )
    }

    // 진동·사운드는 minSdk 26(채널 도입) 이후 채널이 제어하므로 빌더에 지정하지 않음.
    // 채널 생성은 앱 시작(MainActivity)과 서비스 시작(onStartCommand) 시점에 이미 보장됨.
    private fun notify(context: Context, id: Int, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(id, notification)
    }
}
