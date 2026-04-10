package it.tizzycek.micasaestucasa.repo.utente

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.data.utente.Utente.Companion.toHashMap
import it.tizzycek.micasaestucasa.data.utente.Utente.Companion.toUtente
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

    fun logout() {
        auth.signOut()
        utente = null
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun caricaUtente(): Utente? {
        if (this.utente != null) return this.utente
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val dati = beccaDatiUtente(firebaseUser.uid)
            this.utente = dati
            aggiornaTokenFCM(firebaseUser.uid)
            return dati
        }
        return null
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
                println("FCM: Errore silenzioso (normale in emulatore): ${e.message}")
            }
        }
    }
}