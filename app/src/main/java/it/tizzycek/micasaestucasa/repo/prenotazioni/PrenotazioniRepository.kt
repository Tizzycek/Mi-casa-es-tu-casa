package it.tizzycek.micasaestucasa.repo.prenotazioni

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni
import kotlinx.coroutines.tasks.await

class PrenotazioniRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val collection = "prenotazioni"

    suspend fun aggiungiPrenotazione(p: Prenotazioni): Result<Boolean> {
        return try {
            firestore.collection(collection).document(p.id).set(p).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}