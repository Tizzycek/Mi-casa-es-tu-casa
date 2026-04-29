package it.tizzycek.micasaestucasa.utils.notifiche

import android.app.NotificationManager

sealed class NotificationType(val id: String, val channelName: String, val importance: Int = NotificationManager.IMPORTANCE_HIGH) {
    object Prenotazioni: NotificationType("prenotazioni_channel", "Notifiche Prenotazioni")
    object Messaggi: NotificationType("messaggi_channel", "Notifiche Messaggi")
}