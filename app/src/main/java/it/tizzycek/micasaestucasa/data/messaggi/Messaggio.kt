package it.tizzycek.micasaestucasa.data.messaggi

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date

// Usiamo un chat id. L'id serve per la conversazione, a noi basta sapere chi l'ha inviato.
data class Messaggio(val mittente: String, val destinatario: String, val contenuto: String, var imageUrl: List<String> = emptyList(), val timestamp: Instant = Instant.now()) {
    companion object {

        fun Messaggio.toHashMap(): Map<String, Any> {
            return hashMapOf(
                "mittente" to mittente,
                "destinatario" to destinatario,
                "contenuto" to contenuto,
                "imageUrl" to imageUrl,
                "timestamp" to Timestamp(Date.from(timestamp))
            )
        }

        fun DocumentSnapshot.toMessaggio(): Messaggio? {
            val data = data ?: return null

            return Messaggio(
                mittente = data["mittente"] as? String? ?: "",
                destinatario = data["destinatario"] as? String? ?: "",
                contenuto = data["contenuto"] as? String? ?: "",
                imageUrl = data["imageUrl"] as? List<String> ?: emptyList(),
                timestamp = when (val dataRaw = this["timestamp"]) {
                    is Timestamp -> dataRaw.toDate().toInstant()
                    is String -> Instant.parse(dataRaw)
                    else -> Instant.now()
                }
            )
        }
    }
}