package it.tizzycek.micasaestucasa.repo.utente

import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingConfig
import com.firebase.ui.firestore.SnapshotParser
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import it.tizzycek.micasaestucasa.data.utente.Stato
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.data.utente.Utente.Companion.toHashMap
import it.tizzycek.micasaestucasa.data.utente.Utente.Companion.toUtente
import it.tizzycek.micasaestucasa.repo.MainRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

class UserRepository {
    private val auth = FirebaseAuth.getInstance()

    private var utente: Utente? = null


    fun getCurrentUser(): Utente? {
        return this.utente
    }

    // suspend ti serve per non piantare il thread se fa altro (attendere firebase)
    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser?.uid ?: throw Exception("UID nullo")
            utente = beccaDatiUtente(uid)

            utente?.suspension?.let { suspension ->
                controlloSospensione(suspension.toInstant())
            }

            aggiornaTokenFCM(uid)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // deserializza da firebase
    suspend fun beccaDatiUtente(uid: String?): Utente? {
        val id = requireNotNull(uid) {
            "UID cannot be NULL"
        }
        val document = Firebase.firestore.collection("utenti").document(id).get().await()

        return if (document.exists()) {
            document.toUtente()
        } else {
            null
        }
    }

    suspend fun registration(email: String, password: String, nome: String, cognome: String, nascita: LocalDate, tipoUtente: TipoUtente): Result<Boolean> {
        return try {
            utente = Utente(null, email, nome, cognome, nascita, tipo = tipoUtente)
            auth.createUserWithEmailAndPassword(email, password).await()
            utente?.id = auth.currentUser?.uid
            salvaUtenteSuFirestore(utente)

            MainRepo.getStatisticheRepository().inizializzaStatistiche(utente?.id!!)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(nome: String, cognome: String, nascita: LocalDate, nuovaPassword: String): Result<Boolean> {
        return try {
            if (nuovaPassword.isNotEmpty()) {
                auth.currentUser?.updatePassword(nuovaPassword)?.await()
            }

            val nuovoUtente = Utente(utente?.id!!, utente!!.email, nome, cognome, nascita, utente!!.stato, false, utente!!.tipo, utente!!.fcmToken)
            salvaUtenteSuFirestore(nuovoUtente)

            utente = nuovoUtente

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // serializza su firebase
    private suspend fun salvaUtenteSuFirestore(utente: Utente?) {
        val uid = requireNotNull(utente?.id) {
            "UID cannot be NULL"
        }

        FirebaseFirestore.getInstance().collection("utenti").document(uid).set(utente.toHashMap()).await()
    }

    suspend fun logout(): Result<Boolean> {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                FirebaseFirestore.getInstance().collection("utenti").document(uid).update("fcmToken", FieldValue.delete()).await()

//                FirebaseMessaging.getInstance().deleteToken().await()

                println("FCM: Token rimosso con successo al logout")
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }

        auth.signOut()
        utente = null

        return Result.success(true)
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun caricaUtente(): Utente? {
        if (this.utente != null) return this.utente
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val dati = beccaDatiUtente(firebaseUser.uid)

            utente?.suspension?.let { suspension ->
                controlloSospensione(suspension.toInstant())
            }

            this.utente = dati
            aggiornaTokenFCM(firebaseUser.uid)
            return dati
        }
        return null
    }

    suspend fun setReport(idUtente: String?, reported: Boolean): Result<Boolean> {
        return if (idUtente != null) {
            try {
                Firebase.firestore.collection("utenti").document(idUtente).update("segnalato", reported).await()
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else Result.failure(Exception("UID cannot be null"))
    }

    private fun aggiornaTokenFCM(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val lingua = Locale.getDefault().language
                val mapAggiornamento = mapOf(
                    "fcmToken" to token,
                    "lingua" to lingua,
                    "tipoUtente" to utente?.tipo
                )

                FirebaseFirestore.getInstance().collection("utenti")
                    .document(uid).update(mapAggiornamento).await()

                utente?.fcmToken = token
                println("FCM: Token aggiornato con successo")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getUserPaging(lifecycleOwner: LifecycleOwner, filterReport: Boolean, filterSuspended: Boolean, filterBanned: Boolean): FirestorePagingOptions<Utente> {
        var nuovaQuery = Firebase.firestore.collection("utenti").orderBy("nome", Query.Direction.ASCENDING)

        if (filterReport)
            nuovaQuery = nuovaQuery.whereEqualTo("segnalato", true)

        if (filterSuspended)
            nuovaQuery = nuovaQuery.whereEqualTo("stato", Stato.SOSPESO.toString())

        if (filterBanned)
            nuovaQuery = nuovaQuery.whereEqualTo("stato", Stato.BANDITO.toString())

        val config = PagingConfig(20)
        val parser = SnapshotParser<Utente> { sn ->
            sn.toUtente() ?: throw Exception("Could not deserialize user")
        }

        return FirestorePagingOptions.Builder<Utente>().setLifecycleOwner(lifecycleOwner).setQuery(nuovaQuery, config, parser).build()

    }

    suspend fun changeStatus(id: String?, stato: Stato): Result<Boolean> {
        return if (id != null) {
            try {
                Firebase.firestore.collection("utenti").document(id).update("stato", stato.toString()).await()
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("User id cannot be null"))
        }
    }

    suspend fun setSuspension(id: String?, timestamp: Timestamp): Result<Boolean> {
        return if (id != null) {
            try {
                Firebase.firestore.collection("utenti").document(id).update("suspension", timestamp).await()
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("User id cannot be null"))
        }
    }

    private suspend fun controlloSospensione(sus: Instant) {
        val now = Instant.now()
        if (now.isAfter(sus)) {
            val userId = utente?.id
            if (userId != null) {
                try {
                    Firebase.firestore.collection("utenti").document(userId).update("suspension", null).await()

                    changeStatus(userId, Stato.OK)

                    utente?.suspension = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}