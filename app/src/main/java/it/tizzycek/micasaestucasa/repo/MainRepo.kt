package it.tizzycek.micasaestucasa.repo

import com.google.firebase.firestore.FirebaseFirestore
import it.tizzycek.micasaestucasa.repo.utente.UserRepository

object MainRepo {
    private val userRepository = UserRepository()

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getUserRepository(): UserRepository {
        return userRepository
    }

    fun getFirestore(): FirebaseFirestore {
        return firestore
    }
}