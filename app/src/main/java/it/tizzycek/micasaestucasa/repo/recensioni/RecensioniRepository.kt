package it.tizzycek.micasaestucasa.repo.recensioni

import com.google.firebase.firestore.FirebaseFirestore
import it.tizzycek.micasaestucasa.data.recensioni.Recensione
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RecensioniRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // TODO: questa classe non è testata

    suspend fun aggiungiRecensione(autoreId: String, destinatarioId: String, voto: Int, descrizione: String): Result<Recensione> {

        return try {
            val utenteDoc = firestore.collection("utenti").document(destinatarioId).get().await()

            if (!utenteDoc.exists()) {
                return Result.failure(Exception("Utente destinatario non trovato"))
            }

            val mediaAttuale = utenteDoc.getDouble("mediaRecensioni") ?: 0.0
            val totaleAttuale = utenteDoc.getLong("totaleRecensioni")?.toInt() ?: 0


            val nuovoTotale = totaleAttuale + 1
            val nuovaMedia = ((mediaAttuale * totaleAttuale) + voto) / nuovoTotale


            val recensione = Recensione(
                id = UUID.randomUUID().toString(),
                autoreId = autoreId,
                destinatarioId = destinatarioId,
                voto = voto,
                descrizione = descrizione
            )

            val batch = firestore.batch()

            val recensioneID = requireNotNull(recensione.id) {
                "ID cannot be NULL"
            }
            val recensioneRef = firestore.collection("recensioni").document(recensioneID)
            batch.set(recensioneRef, recensione)

            val utenteRef = firestore.collection("utenti").document(destinatarioId)
            batch.update(utenteRef, "mediaRecensioni", nuovaMedia, "totaleRecensioni", nuovoTotale)

            batch.commit().await()

            Result.success(recensione)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun modificaRecensione(
        recensioneId: String,
        nuovoVoto: Int,
        nuovoCommento: String
    ): Result<Unit> {

        return try {
            val recensioneDoc = firestore.collection("recensioni").document(recensioneId).get().await()

            if (!recensioneDoc.exists()) {
                return Result.failure(Exception("Recensione non trovata"))
            }

            val recensione = recensioneDoc.toObject(Recensione::class.java)!!
            val votoVecchio = recensione.voto

            if (votoVecchio != nuovoVoto) {
                val utenteDoc = firestore.collection("utenti").document(recensione.destinatarioId).get().await()

                val mediaAttuale = utenteDoc.getDouble("mediaRecensioni") ?: 0.0
                val totale = utenteDoc.getLong("totaleRecensioni")?.toInt() ?: 0

                // Formula per aggiornare media cambiando un voto:
                // nuova_media = (vecchia_media * totale - voto_vecchio + voto_nuovo) / totale
                val nuovaMedia = ((mediaAttuale * totale) - votoVecchio + nuovoVoto) / totale

                val batch = firestore.batch()

                val recensioneRef = firestore.collection("recensioni").document(recensioneId)
                batch.update(recensioneRef, "voto", nuovoVoto, "commento", nuovoCommento)

                val utenteRef = firestore.collection("utenti").document(recensione.destinatarioId)
                batch.update(utenteRef, "mediaRecensioni", nuovaMedia)

                batch.commit().await()
            } else {
                firestore.collection("recensioni").document(recensioneId).update("commento", nuovoCommento).await()
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun eliminaRecensione(recensioneId: String): Result<Unit> {

        return try {
            val recensioneDoc = firestore.collection("recensioni").document(recensioneId).get().await()

            if (!recensioneDoc.exists()) {
                return Result.failure(Exception("Recensione non trovata"))
            }

            val recensione = recensioneDoc.toObject(Recensione::class.java)!!

            val utenteDoc = firestore.collection("utenti").document(recensione.destinatarioId).get().await()

            val mediaAttuale = utenteDoc.getDouble("mediaRecensioni") ?: 0.0
            val totaleAttuale = utenteDoc.getLong("totaleRecensioni")?.toInt() ?: 0

            val nuovoTotale = totaleAttuale - 1
            val nuovaMedia = if (nuovoTotale > 0) {
                ((mediaAttuale * totaleAttuale) - recensione.voto) / nuovoTotale
            } else {
                0.0
            }

            val batch = firestore.batch()

            val recensioneRef = firestore.collection("recensioni").document(recensioneId)
            batch.delete(recensioneRef)

            val utenteRef = firestore.collection("utenti").document(recensione.destinatarioId)
            batch.update(utenteRef, "mediaRecensioni", nuovaMedia, "totaleRecensioni", nuovoTotale)

            batch.commit().await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * PRENDI RECENSIONI CON PAGINAZIONE
     */
    suspend fun getRecensioniUtente(
        utenteId: String,
        pageSize: Int = 20,
        lastRecensioneId: String? = null
    ): Result<List<Recensione>> {

        return try {
            var query = firestore.collection("recensioni")
                .whereEqualTo("destinatarioId", utenteId)
                .orderBy("data", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            // Se abbiamo un punto di partenza, iniziamo da lì
            lastRecensioneId?.let { id ->
                val lastDoc = firestore.collection("recensioni")
                    .document(id)
                    .get()
                    .await()
                query = query.startAfter(lastDoc)
            }

            val result = query.get().await()

            val recensioni = result.documents.mapNotNull { doc ->
                doc.toObject(Recensione::class.java)
            }

            Result.success(recensioni)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}