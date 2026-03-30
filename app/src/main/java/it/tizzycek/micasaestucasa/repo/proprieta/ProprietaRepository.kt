package it.tizzycek.micasaestucasa.repo.proprieta

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta.Companion.toHashMap
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta.Companion.toProprieta
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ProprietaRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val storageRef = FirebaseStorage.getInstance()

    private val collection = "proprieta"

    suspend fun caricaProprietaPerProprietario(id: String): Result<List<Proprieta>> {
        return try {
            val snapshot = firestore.collection(collection).whereEqualTo("proprietarioId", id).get().await()

            val proprieta = snapshot.documents.mapNotNull { doc ->
                doc.toProprieta()
            }

            Result.success(proprieta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProprieta(limite: Long? = null): Result<List<Proprieta>> {
        try {
            var query: Query = firestore.collection(collection)

            limite?.let {
                query = query.limit(limite)
            }

            val snapshot = query.get().await()
            val proprieta = snapshot.documents.mapNotNull { doc ->
                doc.toProprieta()
            }
            return Result.success(proprieta)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getProprieta(id: String): Result<Proprieta?> {
        return try {
            val doc = firestore.collection(collection).document(id).get().await()
            if (!doc.exists())
                return Result.failure(Exception("Not found"))

            // mapper del companion object
            Result.success(doc.toProprieta())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aggiungiProprieta(proprieta: Proprieta, immagini: List<ByteArray>): Result<Boolean> = coroutineScope {
        try {
            val uploadTasks = immagini.withIndex().map { (index, bytes) ->
                async {
                    val imageName = "${UUID.randomUUID()}_$index.jpg"
                    val ref = storageRef.reference.child(collection).child(imageName)
                    ref.putBytes(bytes).await()
                    ref.downloadUrl.await().toString()
                }
            }
            val nuoviUrls = uploadTasks.awaitAll()
            val listaFinale = (proprieta.immaginiUrl?.plus(nuoviUrls))?.toMutableList()

            proprieta.immaginiUrl = listaFinale
            proprieta.id = UUID.randomUUID().toString()

            firestore.collection(collection).document(requireNotNull(proprieta.id)).set(proprieta.toHashMap()).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminaProprieta(id: String): Result<Boolean> {
        return try {
            val document = firestore.collection(collection).document(id).get().await()
            val proprieta = document.toProprieta()

            if (proprieta != null) {
                // multithread
                val deleteImage = proprieta.immaginiUrl?.map { url ->
                    coroutineScope {
                        async {
                            storageRef.getReferenceFromUrl(url).delete().await()
                        }
                    }
                }
                deleteImage?.awaitAll()
            }
            firestore.collection(collection).document(id).delete().await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun cercaDisponibili(lat: Double? = null, lng: Double? = null, raggioKm: Double = 20.0, dal: LocalDate? = null, al: LocalDate? = null, posti: Int? = null, paroleChiaveIds: List<String>? = null, prezzoMin: Double? = null, prezzoMax: Double? = null): Result<List<Proprieta>> {
        return try {
            var query: Query = firestore.collection(collection)

            posti?.let { query = query.whereGreaterThanOrEqualTo("posti", it) }

            paroleChiaveIds?.let { ids ->
                if (ids.isNotEmpty()) query = query.whereArrayContainsAny("paroleChiaveIds", ids)
            }

            val snapshot = query.limit(100).get().await()

            var risultati = snapshot.documents.mapNotNull { it.toProprieta() }

            if (dal != null && al != null) {
                risultati = risultati.filter { it.isDisponibile(dal, al) }
            }

            if (lat != null && lng != null) {
                risultati = risultati.filter { prop ->
                    calcolaDistanza(lat, lng, prop.latitudine, prop.longitudine) <= raggioKm
                }
            }

            if (prezzoMin != null && prezzoMax != null) {
                risultati = risultati.filter { prop ->
                    prop.disponibilita.any { dis ->
                        dis.prezzo in prezzoMin..prezzoMax
                    }
                }
            }

            Result.success(risultati)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calcolaDistanza(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Raggio della Terra in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
