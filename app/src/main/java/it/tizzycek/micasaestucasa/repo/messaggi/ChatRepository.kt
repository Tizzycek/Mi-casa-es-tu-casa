package it.tizzycek.micasaestucasa.repo.messaggi

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import it.tizzycek.micasaestucasa.data.messaggi.ChatPreview
import it.tizzycek.micasaestucasa.data.messaggi.ChatPreview.Companion.toChatPreview
import it.tizzycek.micasaestucasa.data.messaggi.Messaggio
import it.tizzycek.micasaestucasa.data.messaggi.Messaggio.Companion.toHashMap
import it.tizzycek.micasaestucasa.data.messaggi.Messaggio.Companion.toMessaggio
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.repo.MainRepo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.time.Instant

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance()
    private val collectionChats = "chats"
    private val collectionMessages = "messages"

    suspend fun sendMessage(chatId: String, messaggio: Messaggio, immagini: List<ByteArray>, infoPreview: HashMap<String, Map<String, String>>): Result<Boolean> = coroutineScope {
        try {
            val uploadTasks = immagini.withIndex().map { (index, bytes) ->
                async {
                    val imageName = "${UUID.randomUUID()}_$index.jpg"
                    val ref = storageRef.reference.child(collectionChats).child(imageName)
                    ref.putBytes(bytes).await()
                    ref.downloadUrl.await().toString()
                }
            }
            val nuoviUrls = uploadTasks.awaitAll()
            val messaggioFinale = messaggio.copy(imageUrl = messaggio.imageUrl + nuoviUrls)

            val batch = firestore.batch()
            val chatRef = firestore.collection(collectionChats).document(chatId)
            val nuovoMessaggioRef = chatRef.collection(collectionMessages).document()

            batch.set(nuovoMessaggioRef, messaggioFinale.toHashMap())

            val previewUpdates = hashMapOf(
                "chatId" to chatId,
                "participants" to listOf(messaggio.mittente, messaggio.destinatario),
                "lastMessage" to messaggio.contenuto,
                "lastUpdated" to FieldValue.serverTimestamp(),
                "userData" to infoPreview
            )


            batch.set(chatRef, previewUpdates, SetOptions.merge())  // il merge gestisce la creazione della stanza in automatico

            batch.update(chatRef, "unreadCounts.${messaggio.destinatario}", FieldValue.increment(1))

            batch.commit().await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessagesFlow(chatId: String): Flow<List<Messaggio>> = callbackFlow {
        val listenerRegistration =
            firestore.collection(collectionChats).document(chatId).collection(collectionMessages)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        close(exception)
                    } else if (snapshots != null) {
                        val listaMessaggi = snapshots.documents.mapNotNull { it.toMessaggio() }
                        trySend(listaMessaggi)
                    }
                }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    fun getChatListFlow(currentUser: String): Flow<List<ChatPreview>> = callbackFlow {
        val listener = firestore.collection(collectionChats).whereArrayContains("participants", currentUser).orderBy("lastUpdated", Query.Direction.DESCENDING).addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                close(exception)
            } else if (snapshots != null) {
                val chats = mutableListOf<ChatPreview>()
                snapshots.documents.mapNotNull { documentSnapshot ->
                    val chatPrev = documentSnapshot.toChatPreview()
                    if (chatPrev != null)
                        chats.add(chatPrev)
                }
                trySend(chats)
            }
        }
        awaitClose {
            listener.remove()
        }
    }

    fun azzeraMessaggiNonLetti(chatId: String, mioId: String) {
        firestore.collection(collectionChats).document(chatId).update("unreadCounts.$mioId", 0)
    }
}