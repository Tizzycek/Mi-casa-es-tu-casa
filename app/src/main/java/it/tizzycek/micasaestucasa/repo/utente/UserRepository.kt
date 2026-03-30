package it.tizzycek.micasaestucasa.repo.utente

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import it.tizzycek.micasaestucasa.data.recensioni.Statistiche
import it.tizzycek.micasaestucasa.data.utente.Stato
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.data.utente.Utente
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
            Utente(
                id = document.getString("uid") ?: id,
                email = document.getString("email") ?: "",
                nome = document.getString("nome") ?: "",
                cognome = document.getString("cognome") ?: "",
                nascita = LocalDate.parse(document.getString("nascita")),
                tipo = TipoUtente.valueOf(document.getString("tipo") ?: "AFFITTUARIO"),
                stato = Stato.valueOf(document.getString("stato") ?: "OK"),
                statistiche = Statistiche(
                    mediaRecensioni = document.getDouble("mediaRecensioni") ?: 0.0,
                    totaleRecensioni = document.getLong("totaleRecensioni")?.toInt() ?: 0
                )
            )
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
        val dataMap = mapOf(
            "uid" to utente?.id,
            "email" to utente?.email,
            "nome" to utente?.nome,
            "cognome" to utente?.cognome,
            "nascita" to utente?.nascita.toString(),
            "tipo" to utente?.tipo,
            "stato" to utente?.stato?.name,
            "statistiche" to mapOf(
                "mediaRecensioni" to utente?.statistiche?.mediaRecensioni,
                "totaleRecensioni" to utente?.statistiche?.totaleRecensioni
            )
        )

        val uid = requireNotNull(utente?.id) {
            "UID cannot be NULL"
        }

        FirebaseFirestore.getInstance().collection("utenti").document(uid).set(dataMap).await()
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