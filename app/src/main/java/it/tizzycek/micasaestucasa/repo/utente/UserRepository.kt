package it.tizzycek.micasaestucasa.repo.utente

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.tizzycek.micasaestucasa.data.utente.Utente
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // suspend ti serve per non piantare il thread se fa altro (attendere firebase)
    suspend fun getCurrentUser(): Utente? {
        val uid = auth.currentUser?.uid ?: return null

        val snapshot = db.collection("utenti").document(uid).get().await()
        return snapshot.toObject(Utente::class.java)
    }

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

}