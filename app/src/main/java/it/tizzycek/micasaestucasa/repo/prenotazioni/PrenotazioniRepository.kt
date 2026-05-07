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

    suspend fun getPrenotazioni(idProprieta: String): Result<List<Prenotazioni>> {
        return try {
            val querySnapshot = firestore.collection(collection).whereEqualTo("proprietaId", idProprieta).get().await()
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

    suspend fun loadStatistics(): Result<Map<String, Int>> {
        return try {
            val snapshot = firestore.collection(collection).get().await()

            val stats = mutableMapOf(
                "totali" to 0,
                "inAttesa" to 0,
                "accettate" to 0,
                "rifiutate" to 0,
                "annullate" to 0
            )

            for (doc in snapshot) {
                stats["totali"] = stats["totali"]!! + 1

                when (doc.getString("statoPrenotazione")) {
                    "IN_ATTESA" -> stats["inAttesa"] = stats["inAttesa"]!! + 1
                    "ACCETTATA" -> stats["accettate"] = stats["accettate"]!! + 1
                    "RIFIUTATA" -> stats["rifiutate"] = stats["rifiutate"]!! + 1
                    "ANNULLATA" -> stats["annullate"] = stats["annullate"]!! + 1
                }
            }

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}