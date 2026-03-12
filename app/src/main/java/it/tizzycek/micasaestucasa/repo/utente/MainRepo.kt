package it.tizzycek.micasaestucasa.repo.utente

object MainRepo {
    private val userRepository = UserRepository()

    fun getUserRepository(): UserRepository {
        return userRepository
    }
}