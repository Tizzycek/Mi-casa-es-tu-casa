package it.tizzycek.micasaestucasa.repo.proprieta

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import kotlinx.coroutines.tasks.await
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
            val snapshot =
                firestore.collection(collection).whereEqualTo("proprietarioId", id).get().await()

            val proprieta = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Proprieta::class.java)?.copy(id = doc.id)
            }

            return Result.success(proprieta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProprieta(): Result<List<Proprieta>> {
        try {
            val snapshot = firestore.collection(collection).get().await()
            val proprieta = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Proprieta::class.java)?.copy(id = doc.id)
            }
            return Result.success(proprieta)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getProprieta(id: String): Result<Proprieta?> {
        return try {
        val doc = firestore.collection(collection).document(id).get().await()
        if (!doc.exists()) {
            return Result.failure(Exception("Proprietà non trovata"))
        }
        val proprieta = doc.toObject(Proprieta::class.java)?.copy(id = doc.id)
        Result.success(proprieta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aggiungiProprieta(proprieta: Proprieta, immagini: List<ByteArray>): Result<Boolean> {
        return try {
            val urls = mutableListOf<String>()
            for ((index, bytes) in immagini.withIndex()) {
                val imageName = "proprieta/${UUID.randomUUID()}_$index.jpg"
                val storageRef = storageRef.reference.child(imageName)
                storageRef.putBytes(bytes).await()

                val downloadURL = storageRef.downloadUrl.await()
                urls.add(downloadURL.toString())
            }

            proprieta.immaginiUrl = urls
            proprieta.id = UUID.randomUUID().toString()

            firestore.collection(collection).document(requireNotNull(proprieta.id)).set(proprieta).await()

            Result.success(true)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // TODO: da vedere se è utile
    suspend fun cercaProprietaPerParoleChiave(parole: List<String>, lat: Double? = null, lng: Double? = null, raggioKm: Double = 20.0): Result<List<Proprieta>> {
        return try {
            // Firestore può cercare array con "array-contains-any"
            val snapshot = firestore.collection(collection)
                .whereEqualTo("paroleChiave", parole) // Attenzione: vuole match esatto
                // .whereArrayContainsAny("paroleChiave", parole) // Per match parziale
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
