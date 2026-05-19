package it.tizzycek.micasaestucasa.utils.notifiche

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.tizzycek.micasaestucasa.R

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val helper = NotificationHelper(this)
        val data = message.data
        val type = data["type"]

        val targetId = data["chatId"]

        val messageData = mapOf(
            "nome" to data["nome"],
            "cognome" to data["cognome"],
            "messagePreview" to data["messagePreview"],
            "senderId" to data["senderId"]
        )

        val bookingData = mapOf(
            "azione" to data["actionType"]
        )

        when (type) {
            "PRENOTAZIONE" -> helper.sendNotification(NotificationType.Prenotazioni, getString(R.string.notifica_prenotazione), bookingData, targetId)
            "MESSAGGIO" -> helper.sendNotification(NotificationType.Messaggi, getString(R.string.nuovo_messaggio, data["nome"], data["cognome"]), messageData, targetId)
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

}