package it.tizzycek.micasaestucasa.utils.notifiche

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import it.tizzycek.micasaestucasa.MainActivity
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.PrenotazioniFragment
import androidx.core.content.ContextCompat.checkSelfPermission
import it.tizzycek.micasaestucasa.ui.home.FragmentManager


class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        setupChannels()
    }

    private fun setupChannels() {
        val channels = listOf(
            createChannel(NotificationType.Prenotazioni),
            createChannel(NotificationType.Messaggi)
        )
        notificationManager.createNotificationChannels(channels)
    }

    private fun createChannel(type: NotificationType): NotificationChannel {
        return NotificationChannel(type.id, type.channelName, type.importance)
    }

    fun checkNotificationPermission() {
        if (checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
    }

    // targetId utile per aprire una specifica chat o prenotazione
    fun sendNotification(type: NotificationType, title: String, message: String, targetId: String? = null) {
        // TODO: da controllare
        val intent = Intent(context, FragmentManager::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_TYPE", if (type == NotificationType.Messaggi) "MESSAGGIO" else "PRENOTAZIONE")
            putExtra("TARGET_ID", targetId)
        }

        val requestCode = System.currentTimeMillis().hashCode()

        val pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, type.id).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(message).setPriority(if (type == NotificationType.Messaggi) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT).setContentIntent(pendingIntent).setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}