package it.tizzycek.micasaestucasa.repo

import com.google.firebase.firestore.FirebaseFirestore
import it.tizzycek.micasaestucasa.data.parolechiave.ParoleChiave
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.repo.parolechiave.ParoleChiaveRepository
import it.tizzycek.micasaestucasa.repo.proprieta.ProprietaRepository
import it.tizzycek.micasaestucasa.repo.utente.UserRepository

object MainRepo {
    private val userRepository = UserRepository()

    private val proprietaRepository = ProprietaRepository()

    private val paroleChiaveRepository = ParoleChiaveRepository()

    fun getUserRepository(): UserRepository {
        return userRepository
    }

    fun getProprietaRepository(): ProprietaRepository {
        return proprietaRepository
    }

    fun getParoleChiave(): ParoleChiaveRepository {
        return paroleChiaveRepository
    }
}