package it.tizzycek.micasaestucasa.repo.parolechiave

import com.google.firebase.firestore.FirebaseFirestore
import it.tizzycek.micasaestucasa.data.parolechiave.ParoleChiave
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.repo.MainRepo
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ParoleChiaveRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collection = "paroleChiave"

    suspend fun getParoleChiave(): Result<List<ParoleChiave>> {
        return try {
            val snapshot = firestore.collection(collection).get().await()
            val paroleChiave = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ParoleChiave::class.java)?.copy(id = doc.id)
            }
            Result.success(paroleChiave)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun aggiungiParolaChiave(parola: ParoleChiave): Result<Boolean> {
        if (MainRepo.getUserRepository().getCurrentUser()!!.tipo != TipoUtente.ADMIN) {
            return Result.failure(Exception("Non hai i permessi per aggiungere una parola chiave")) // TODO: stringa
        }
        return try {
            parola.id = UUID.randomUUID().toString()
            firestore.collection(collection).document(requireNotNull(parola.id)).set(parola).await()

            Result.success(true)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}