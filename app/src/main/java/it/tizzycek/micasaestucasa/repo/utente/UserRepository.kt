package it.tizzycek.micasaestucasa.repo.utente

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.data.utente.Utente
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var utente: Utente? = null


    fun getCurrentUser(): Utente? {
        return this.utente
    }

    // suspend ti serve per non piantare il thread se fa altro (attendere firebase)
    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            // TODO: utente = Utente(auth.currentUser?.uid, email, auth.currentUser., "", LocalDate.now(), tipo = TipoUtente.ADMIN)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registration(email: String, password: String, nome: String, cognome: String, nascita: LocalDate, tipoUtente: TipoUtente): Result<Boolean> {
        return try {
            utente = Utente(null, email, nome, cognome, nascita, tipo = tipoUtente)
            auth.createUserWithEmailAndPassword(email, password).await()
            utente?.id = auth.currentUser?.uid
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
        utente = null
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

}