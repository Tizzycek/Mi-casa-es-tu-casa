package it.tizzycek.micasaestucasa.data.messaggi

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant

data class ChatPreview(val chatId: String = "", val participants: List<String> = emptyList(), val userData: Map<String, Map<String, String>> = emptyMap(), val lastMessage: String = "", val lastUpdated: Instant = Instant.now(), val unreadCounts: Map<String, Int> = emptyMap()) {
    companion object {

        fun DocumentSnapshot.toChatPreview(): ChatPreview? {
            val data = data ?: return null

            val rawUserData = data["userData"] as? Map<*, *>

            val safeUserData = mutableMapOf<String, Map<String, String>>()

            val campoUserData = data["userData"]
            println("DEBUG: Tipo di userData è ${campoUserData?.javaClass?.name}")
            println("DEBUG: Contenuto di userData è $campoUserData")

            rawUserData?.forEach { (key, value) ->
                val userId = key.toString()
                val userDataInternal = value as? Map<*, *> // nome e cognome

                if (userDataInternal != null) {
                    val infoMap = mutableMapOf<String, String>()
                    infoMap["nome"] = userDataInternal["nome"]?.toString() ?: ""
                    infoMap["cognome"] = userDataInternal["cognome"]?.toString() ?: ""

                    safeUserData[userId] = infoMap
                }
            }

            val rawUnread = data["unreadCounts"] as? Map<*, *>
            val safeUnread = rawUnread?.mapNotNull { (k, v) ->
                k.toString() to (v as? Long ?: 0L).toInt()
            }?.toMap() ?: emptyMap()

            return ChatPreview(
                chatId = data["chatId"] as? String ?: "",
                participants = data["participants"] as? List<String> ?: emptyList(),
                userData = safeUserData, // dimensione 2, partecipanti
                lastMessage = data["lastMessage"] as? String ?: "",
                lastUpdated = (data["lastUpdated"] as? Timestamp)?.toDate()?.toInstant() ?: Instant.now(),
                unreadCounts = safeUnread
            )
        }
    }
}