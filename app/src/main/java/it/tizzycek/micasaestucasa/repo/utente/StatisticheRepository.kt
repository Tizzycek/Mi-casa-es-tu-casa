package it.tizzycek.micasaestucasa.repo.utente

import com.google.firebase.firestore.FirebaseFirestore
import it.tizzycek.micasaestucasa.data.recensioni.Statistiche
import it.tizzycek.micasaestucasa.data.recensioni.Statistiche.Companion.toStatistiche
import kotlinx.coroutines.tasks.await

class StatisticheRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val collection = "statistiche"

    private val statisticheCache = mutableMapOf<String, Statistiche>()


    suspend fun inizializzaStatistiche(idUtente: String): Result<Boolean> {
        return try {
            val statistiche = Statistiche(idUtente)
            firestore.collection(collection).document(idUtente).set(statistiche).await()
//            statisticheCache[idUtente] = statistiche
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStatistiche(idUtente: String): Result<Statistiche> {
        return /*if (statisticheCache.containsKey(idUtente)) {
            Result.success(statisticheCache[idUtente]!!)
        }
        else */try {
            val doc = firestore.collection(collection).document(idUtente).get().await()
            Result.success(doc.toStatistiche())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aggiornaStatistiche(idUtente: String, statistica: Statistiche): Result<Boolean> {
        return try {
            firestore.collection(collection).document(idUtente).set(statistica).await()

//            statisticheCache[idUtente] = statistica
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}