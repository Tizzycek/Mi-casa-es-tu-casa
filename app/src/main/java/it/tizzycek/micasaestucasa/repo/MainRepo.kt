package it.tizzycek.micasaestucasa.repo

import com.google.firebase.firestore.FirebaseFirestore
import it.tizzycek.micasaestucasa.data.parolechiave.ParoleChiave
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.repo.messaggi.ChatRepository
import it.tizzycek.micasaestucasa.repo.parolechiave.ParoleChiaveRepository
import it.tizzycek.micasaestucasa.repo.prenotazioni.PrenotazioniRepository
import it.tizzycek.micasaestucasa.repo.proprieta.ProprietaRepository
import it.tizzycek.micasaestucasa.repo.utente.StatisticheRepository
import it.tizzycek.micasaestucasa.repo.utente.UserRepository

object MainRepo {
    private val userRepository = UserRepository()

    private val proprietaRepository = ProprietaRepository()

    private val paroleChiaveRepository = ParoleChiaveRepository()

    private val prenotazioniRepository = PrenotazioniRepository()

    private val statisticheRepository = StatisticheRepository()

    private val chatRepository = ChatRepository()


    fun getUserRepository(): UserRepository {
        return userRepository
    }

    fun getProprietaRepository(): ProprietaRepository {
        return proprietaRepository
    }

    fun getParoleChiave(): ParoleChiaveRepository {
        return paroleChiaveRepository
    }

    fun getPrenotazioniRepository(): PrenotazioniRepository {
        return prenotazioniRepository
    }

    fun getStatisticheRepository(): StatisticheRepository {
        return statisticheRepository
    }

    fun getChatRepository(): ChatRepository {
        return chatRepository
    }
}