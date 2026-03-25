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

    suspend fun getProprieta(): Result<List<Proprieta>> {
        try {
            val snapshot = firestore.collection(collection).get().await()
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

    suspend fun getProprietaDisponibili(dal: LocalDate, al: LocalDate, posti: Int, lat: Double, lng: Double, raggioKm: Double = 20.0): Result<List<Proprieta>> {
        return getProprieta().map { lista ->
            lista.filter { proprieta ->
                proprieta.isDisponibile(dal, al) && proprieta.posti <= posti && calcolaDistanza(lat, lng, proprieta.latitudine, proprieta.longitudine) <= raggioKm
            }
        }
    }

//    suspend fun cercaDisponibili(dal: LocalDate? = null, al: LocalDate? = null, titolo: String? = null, posti: Int? = null, lat: Double? = null, lng: Double? = null, raggioKm: Double = 20.0, paroleChiaveIds: List<String>? = null, prezzoMin: Double? = null, prezzoMax: Double? = null): Result<List<Proprieta>> {
//        // Serve per fare la ricerca. Prende POTENZIALMENTE tutti i dati.
//        // Possibilmente fa la query prima. Dove non riesce, smista dopo
//        // TODO:
//        return try {
//            var query: Query
//            query = firestore.collection(collection)
//
//            titolo?.let {
//                query = query.whereEqualTo("nome", titolo)
//            }
//
//            posti?.let {
//                query = query.whereGreaterThanOrEqualTo("posti", posti)
//            }
//
//            paroleChiaveIds?.let { ids ->
//                if (ids.isNotEmpty()) {
//                    query = query.whereArrayContainsAny("paroleChiaveIds", ids)
//                }
//            }
//
//            prezzoMin?.let {
//                query = query.whereGreaterThanOrEqualTo("prezzo", it)
//            }
//
//            prezzoMax?.let {
//                query = query.whereLessThanOrEqualTo("prezzo", it)
//            }
//
//            query = query.orderBy("prezzo", Query.Direction.ASCENDING).limit(50)
//
//            val snapshot = query.get().await()
//
//            var risultati = snapshot.documents.mapNotNull { doc ->
//                doc.toObject(Proprieta::class.java)?.copy(id = doc.id)
//            }
//
//            if (dal != null && al != null) {
//                risultati = risultati.filter { proprieta ->
//                    proprieta.isDisponibile(dal, al)
//                }
//            }
//
//            if (lat != null && lng != null) {
//                risultati = risultati.filter { prop ->
//                    calcolaDistanza(lat, lng, prop.latitudine, prop.longitudine) <= raggioKm
//                }
//            }
//
//            Result.success(risultati)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    suspend fun cercaDisponibili(
        lat: Double? = null,
        lng: Double? = null,
        raggioKm: Double = 20.0,
        dal: LocalDate? = null,
        al: LocalDate? = null,
        posti: Int? = null,
        paroleChiaveIds: List<String>? = null,
        prezzoMin: Double? = null,
        prezzoMax: Double? = null
    ): Result<List<Proprieta>> {
        return try {
            var query: Query = firestore.collection(collection)

            // 1. FILTRI LATO SERVER (Firestore) - Riducono drasticamente i dati scaricati
            posti?.let { query = query.whereGreaterThanOrEqualTo("posti", it) }

            paroleChiaveIds?.let { ids ->
                if (ids.isNotEmpty()) query = query.whereArrayContainsAny("paroleChiaveIds", ids)
            }

            // Nota: prezzoMin/Max richiedono un indice se combinati con altri filtri
            prezzoMin?.let { query = query.whereGreaterThanOrEqualTo("prezzo", it) }

            val snapshot = query.limit(100).get().await() // Limite di sicurezza per non scaricare troppo

            // 2. FILTRI LATO CLIENT (Kotlin) - Per logica complessa come date e distanza
            var risultati = snapshot.documents.mapNotNull { it.toProprieta() } // Usa il mapper manuale!

            // Filtro date
            if (dal != null && al != null) {
                risultati = risultati.filter { it.isDisponibile(dal, al) }
            }

            // Filtro distanza (Fondamentale per la mappa)
            if (lat != null && lng != null) {
                risultati = risultati.filter { prop ->
                    calcolaDistanza(lat, lng, prop.latitudine, prop.longitudine) <= raggioKm
                }
            }

            Result.success(risultati)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // TODO: da vedere se è utile
    suspend fun cercaProprietaPerParoleChiave(parole: List<String>, lat: Double? = null, lng: Double? = null, raggioKm: Double = 20.0): Result<List<Proprieta>> {
        return try {
            // Firestore può cercare array con "array-contains-any"
            val snapshot = firestore.collection(collection)
                .whereEqualTo("paroleChiave", parole) // Attenzione: vuole match esatto
                .whereArrayContainsAny("paroleChiave", parole) // Per match parziale
                .limit(50)
                .get()
                .await()

            val risultati = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Proprieta::class.java)?.copy(id = doc.id)
            }

            // Filtra per distanza se abbiamo coordinate
            val filtrati = if (lat != null && lng != null) {
                risultati.filter { prop ->
                    calcolaDistanza(lat, lng, prop.latitudine, prop.longitudine) <= raggioKm
                }
            } else {
                risultati
            }

            Result.success(filtrati)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calcolaDistanza(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Raggio della Terra in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
