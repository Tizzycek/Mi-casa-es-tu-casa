package it.tizzycek.micasaestucasa.repo.prenotazioni

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni
import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni.Companion.toHashMap
import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni.Companion.toPrenotazioni
import it.tizzycek.micasaestucasa.data.prenotazioni.StatoPrenotazione
import it.tizzycek.micasaestucasa.data.utente.Stato
import kotlinx.coroutines.tasks.await

class PrenotazioniRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val collection = "prenotazioni"

    suspend fun aggiungiPrenotazione(p: Prenotazioni): Result<Boolean> {
        return try {
            firestore.collection(collection).document(p.id).set(p.toHashMap()).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPrenotazioni(idUtente: String, typeId: String): Result<List<Prenotazioni>> {
        return try {
            val querySnapshot = firestore.collection(collection).whereEqualTo(typeId, idUtente).get().await()
            val listaPrenotazioni = querySnapshot.documents.mapNotNull { pren ->
                pren.toPrenotazioni()
            }
            Result.success(listaPrenotazioni)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cambiaStatoPrenotazione(p: Prenotazioni, nuovoStato: StatoPrenotazione): Result<Boolean> {
        return try {
            firestore.collection(collection).document(p.id).update("statoPrenotazione", nuovoStato.toString()).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aggiornaPrenotazione(p: Prenotazioni): Result<Boolean> {
        return try {
            firestore.collection(collection).document(p.id).set(p.toHashMap()).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}