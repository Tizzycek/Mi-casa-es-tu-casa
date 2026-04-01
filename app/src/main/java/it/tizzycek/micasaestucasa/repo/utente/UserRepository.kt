package it.tizzycek.micasaestucasa.repo.utente

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import it.tizzycek.micasaestucasa.data.recensioni.Statistiche
import it.tizzycek.micasaestucasa.data.utente.Stato
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.data.utente.Utente.Companion.toHashMap
import it.tizzycek.micasaestucasa.data.utente.Utente.Companion.toUtente
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

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
            utente = beccaDatiUtente(auth.currentUser?.uid)
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
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {

            val dati = beccaDatiUtente(firebaseUser.uid)
            this.utente = dati
            return dati
        }
        return null
    }
}