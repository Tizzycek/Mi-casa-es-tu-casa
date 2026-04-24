package it.tizzycek.micasaestucasa.data.messaggi

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import it.tizzycek.micasaestucasa.data.utente.Utente
import java.time.Instant
// TODO refactor

data class ChatPreview(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    // Cambiato da HashMap a Map per flessibilità
    val userData: Map<String, Map<String, String>> = emptyMap(),
    val lastMessage: String = "",
    val lastUpdated: Instant = Instant.now(),
    val unreadCounts: Map<String, Int> = emptyMap()
) {
    companion object {

        fun DocumentSnapshot.toChatPreview(): ChatPreview? {
            val data = data ?: return null

            // 1. Prendi "userData" come mappa generica (Map<*, *>)
            // Questo NON fallisce mai se il campo esiste
            val rawUserData = data["userData"] as? Map<*, *>

            val safeUserData = mutableMapOf<String, Map<String, String>>()

            val campoUserData = data["userData"]
            println("DEBUG: Tipo di userData è ${campoUserData?.javaClass?.name}")
            println("DEBUG: Contenuto di userData è $campoUserData")

            // 2. Cicla sulla mappa per estrarre gli ID utente e i loro dati
            rawUserData?.forEach { (key, value) ->
                val userId = key.toString() // Questo è l'idUtente
                val userDataInternal = value as? Map<*, *> // Questa è la mappa con nome e cognome

                if (userDataInternal != null) {
                    val infoMap = mutableMapOf<String, String>()
                    infoMap["nome"] = userDataInternal["nome"]?.toString() ?: ""
                    infoMap["cognome"] = userDataInternal["cognome"]?.toString() ?: ""

                    safeUserData[userId] = infoMap
                }
            }

            // 3. Fai lo stesso per unreadCounts (che ha lo stesso problema)
            val rawUnread = data["unreadCounts"] as? Map<*, *>
            val safeUnread = rawUnread?.mapNotNull { (k, v) ->
                k.toString() to (v as? Long ?: 0L).toInt()
            }?.toMap() ?: emptyMap()

            return ChatPreview(
                chatId = data["chatId"] as? String ?: "",
                participants = data["participants"] as? List<String> ?: emptyList(),
                userData = safeUserData, // Ora questa avrà dimensione 2 (tu e l'altro)
                lastMessage = data["lastMessage"] as? String ?: "",
                lastUpdated = (data["lastUpdated"] as? Timestamp)?.toDate()?.toInstant() ?: Instant.now(),
                unreadCounts = safeUnread
            )
        }

        // Funzione di utilità per il repository (opzionale)
        fun generatePreviewUpdateMap(
            chatId: String,
            mittente: Utente,
            destinatario: Utente,
            testoUltimoMessaggio: String
        ): HashMap<String, Any> {
            return hashMapOf(
                "chatId" to chatId,
                "participants" to listOf(mittente.id!!, destinatario.id!!),
                "userData.${mittente.id}" to mapOf(
                    "nome" to (mittente.nome ?: ""),
                    "cognome" to (mittente.cognome ?: "")
                ),
                "userData.${destinatario.id}" to mapOf(
                    "nome" to (destinatario.nome ?: ""),
                    "cognome" to (destinatario.cognome ?: "")
                ),
                "lastMessage" to testoUltimoMessaggio,
                "lastUpdated" to FieldValue.serverTimestamp(),
                "unreadCounts.${destinatario.id}" to FieldValue.increment(1)
            )
        }
    }
}