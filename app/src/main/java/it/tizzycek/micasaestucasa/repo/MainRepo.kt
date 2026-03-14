package it.tizzycek.micasaestucasa.repo

import it.tizzycek.micasaestucasa.repo.utente.UserRepository

object MainRepo {
    private val userRepository = UserRepository()

    fun getUserRepository(): UserRepository {
        return userRepository
    }
}