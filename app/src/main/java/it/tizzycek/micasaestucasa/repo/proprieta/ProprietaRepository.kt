package it.tizzycek.micasaestucasa.repo.proprieta

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita.Companion.toDisponibilita
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

    private val cacheProprieta = mutableMapOf<String, Proprieta>()

    suspend fun caricaProprietaPerProprietario(id: String): Result<List<Proprieta>> {
        return try {
            val snapshot = firestore.collection(collection).whereEqualTo("proprietarioId", id).get().await()

            val proprieta = snapshot.documents.mapNotNull { doc ->
                doc.toProprieta()
            }
            cacheProprieta.putAll(proprieta.associateBy { it.id!! })

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
        if (cacheProprieta.containsKey(id)) return Result.success(cacheProprieta[id])
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

    suspend fun aggiornaProprieta(
        proprieta: Proprieta,
        immagini: List<ByteArray>,
        immaginiDaEliminare: List<String>
    ): Result<Boolean> = coroutineScope {
        try {
            if (immaginiDaEliminare.isNotEmpty()) {
                val deleteTask = immaginiDaEliminare.map { url ->
                    async {
                        storageRef.getReferenceFromUrl(url).delete().await()
                    }
                }
                deleteTask.awaitAll()
            }

            val uploadTasks = immagini.withIndex().map { (index, bytes) ->
                async {
                    val imageName = "${UUID.randomUUID()}_$index.jpg"
                    val ref = storageRef.reference.child(collection).child(imageName)
                    ref.putBytes(bytes).await()
                    ref.downloadUrl.await().toString()
                }
            }
            val nuoviUrl = uploadTasks.awaitAll()
            val listaFinale = (proprieta.immaginiUrl ?: emptyList()) + nuoviUrl
            proprieta.immaginiUrl = listaFinale

            firestore.collection(collection).document(requireNotNull(proprieta.id)).set(proprieta.toHashMap()).await()

            cacheProprieta.remove(proprieta.id)

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

            cacheProprieta.remove(id)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Atomically reads the current disponibilita list, applies [trasforma] to it,
     * and writes the result back in a single Firestore transaction.
     *
     * Using a transaction ensures that two concurrent approvals for the same property
     * never overwrite each other: if a conflict is detected Firestore retries the
     * operation automatically against the already-committed state.
     *
     * If [trasforma] throws (e.g. "dates no longer available") the transaction is
     * aborted immediately without a retry and the exception is returned as a failure.
     */
    suspend fun aggiornaDisponibilitaTransazionale(id: String, trasforma: (List<Disponibilita>) -> List<Disponibilita>): Result<List<Disponibilita>> {
        return try {
            val docRef = firestore.collection(collection).document(id)
            val nuovaDisponibilita = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val listaAttuale = (snapshot.data?.get("disponibilita") as? List<Map<String, Any?>>)
                    ?.map { with(Disponibilita) { it.toDisponibilita() } }
                    ?: emptyList()

                val nuova = trasforma(listaAttuale)

                val mappata = nuova.map { dis -> with(Disponibilita) { dis.toHashMap() } }
                transaction.update(docRef, "disponibilita", mappata)

                nuova
            }.await()

            Result.success(nuovaDisponibilita)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cercaDisponibili(lat: Double? = null, lng: Double? = null, raggioKm: Double = 20.0, dal: LocalDate? = null, al: LocalDate? = null, posti: Int? = null, paroleChiaveIds: List<String>? = null, prezzoMin: Double? = null, prezzoMax: Double? = null): Result<List<Proprieta>> {
        return try {
            val risultatiDaCache = if (cacheProprieta.isNotEmpty()) {
                cacheProprieta.values.filter { prop ->
                    // Applichiamo gli stessi filtri sulla cache
                    posti?.let { prop.posti >= it } ?: true
                }.filter { prop ->
                    if (dal != null && al != null) {
                        prop.isDisponibile(dal, al)
                    } else true
                }.filter { prop ->
                    if (lat != null && lng != null) {
                        calcolaDistanza(lat, lng, prop.latitudine, prop.longitudine) <= raggioKm
                    } else true
                }.filter { prop ->
                    if (prezzoMin != null && prezzoMax != null) {
                        prop.disponibilita.any { dis ->
                            dis.prezzo in prezzoMin..prezzoMax
                        }
                    } else true
                }.filter { prop ->
                    paroleChiaveIds?.let { ids ->
                        prop.paroleChiaveIds.any { it in ids }
                    } ?: true
                }
            } else emptyList()

            // Se la cache ha risultati validi E contiene la maggior parte dei dati,
            // potremmo usarli direttamente
            if (risultatiDaCache.isNotEmpty() && cacheProprieta.size > 10) {
                return Result.success(risultatiDaCache)
            }

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

/*class ProprietaRepository {
    // ... codice esistente ...

    private data class CacheEntry(
        val proprieta: Proprieta,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val cacheProprieta = mutableMapOf<String, CacheEntry>()
    private val CACHE_TTL = 5 * 60 * 1000L // 5 minuti

    private fun isCacheValid(entry: CacheEntry): Boolean {
        return System.currentTimeMillis() - entry.timestamp < CACHE_TTL
    }

    private fun getCachedProprieta(id: String): Proprieta? {
        return cacheProprieta[id]?.takeIf { isCacheValid(it) }?.proprieta
    }

    suspend fun cercaDisponibiliConCacheIntelligente(
        lat: Double? = null,
        lng: Double? = null,
        raggioKm: Double = 20.0,
        dal: LocalDate? = null,
        al: LocalDate? = null,
        posti: Int? = null,
        paroleChiaveIds: List<String>? = null,
        prezzoMin: Double? = null,
        prezzoMax: Double? = null,
        forceRefresh: Boolean = false
    ): Result<List<Proprieta>> {
        return try {
            // Se forceRefresh è false e la cache ha dati validi, proviamo dalla cache
            if (!forceRefresh && cacheProprieta.isNotEmpty()) {
                val cachedResults = cacheProprieta.values
                    .filter { isCacheValid(it) }
                    .map { it.proprieta }
                    .filter { prop ->
                        posti?.let { prop.posti >= it } ?: true
                    }
                    .filter { prop ->
                        if (dal != null && al != null) prop.isDisponibile(dal, al) else true
                    }
                    .filter { prop ->
                        if (lat != null && lng != null) {
                            calcolaDistanza(lat, lng, prop.latitudine, prop.longitudine) <= raggioKm
                        } else true
                    }
                    .filter { prop ->
                        if (prezzoMin != null && prezzoMax != null) {
                            prop.disponibilita.any { it.prezzo in prezzoMin..prezzoMax }
                        } else true
                    }
                    .filter { prop ->
                        paroleChiaveIds?.let { ids ->
                            prop.paroleChiaveIds?.any { it in ids } ?: false
                        } ?: true
                    }

                // Se abbiamo almeno un risultato dalla cache, lo restituiamo
                if (cachedResults.isNotEmpty()) {
                    return Result.success(cachedResults)
                }
            }

            // Query Firestore come fallback
            var query: Query = firestore.collection(collection)

            posti?.let { query = query.whereGreaterThanOrEqualTo("posti", it) }
            paroleChiaveIds?.let { ids ->
                if (ids.isNotEmpty()) query = query.whereArrayContainsAny("paroleChiaveIds", ids)
            }

            val snapshot = query.limit(100).get().await()
            var risultati = snapshot.documents.mapNotNull { it.toProprieta() }

            // Aggiorna cache
            risultati.forEach { prop ->
                prop.id?.let { id ->
                    cacheProprieta[id] = CacheEntry(prop)
                }
            }

            // Filtri post-query
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
                    prop.disponibilita.any { it.prezzo in prezzoMin..prezzoMax }
                }
            }

            Result.success(risultati)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}*/
