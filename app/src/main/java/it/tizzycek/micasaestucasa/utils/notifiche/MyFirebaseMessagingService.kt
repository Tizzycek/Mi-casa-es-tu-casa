package it.tizzycek.micasaestucasa.utils.notifiche

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.prenotazioni.StatoPrenotazione
import it.tizzycek.micasaestucasa.repo.MainRepo
import kotlinx.coroutines.MainScope

class MyFirebaseMessagingService: FirebaseMessagingService() {
    // TODO: guardare l'avviso

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val helper = NotificationHelper(this)
        val data = message.data
        val type = data["type"]

        val targetId = data["chatId"]

        val messageData = mapOf(
            "nome" to data["nome"],
            "cognome" to data["cognome"],
            "messagePreview" to data["messagePreview"]
        )

        val bookingData = mapOf(
            "azione" to data["action"]
        )

        when (type) {
            "PRENOTAZIONE" -> helper.sendNotification(NotificationType.Prenotazioni, getString(R.string.notifica_prenotazione), bookingData, targetId)
            "MESSAGGIO" -> helper.sendNotification(NotificationType.Messaggi, getString(R.string.nuovo_messaggio, data["nome"], data["cognome"]), messageData, targetId)
        }

    }

}