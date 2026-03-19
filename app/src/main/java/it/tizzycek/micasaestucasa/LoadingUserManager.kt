package it.tizzycek.micasaestucasa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.repo.MainRepo
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoadingUserManager: ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()
    var utente: Utente? = null

    init {
        viewModelScope.launch {
            val loadingJob = async { MainRepo.getUserRepository().caricaUtente() }
            val timerJob = async { delay(2000) }

            utente = loadingJob.await()
            timerJob.await()

            _isReady.value = true
        }
    }
}