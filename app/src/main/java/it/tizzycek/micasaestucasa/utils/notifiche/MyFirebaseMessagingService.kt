package it.tizzycek.micasaestucasa.utils.notifiche

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.tizzycek.micasaestucasa.repo.MainRepo
import kotlinx.coroutines.MainScope

class MyFirebaseMessagingService: FirebaseMessagingService() {
    // TODO: guardare l'avviso

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val helper = NotificationHelper(this)
        val data = message.data
        val type = data["type"]

        val title = message.notification?.title ?: data["title"] ?: "Notifica"
        val body = message.notification?.body ?: data["body"] ?: ""
        val targetId = data["chatId"] ?: data["prenotazioneId"]

        when (type) {
            "PRENOTAZIONE" -> helper.sendNotification(NotificationType.Prenotazioni, title, body, targetId)
            "MESSAGGIO" -> helper.sendNotification(NotificationType.Messaggi, title, body, targetId)
        }

    }

}