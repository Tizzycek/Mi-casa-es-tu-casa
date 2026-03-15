package it.tizzycek.micasaestucasa.data.utente

import android.util.Patterns
import it.tizzycek.micasaestucasa.data.recensioni.Statistiche
import java.time.LocalDate

data class Utente (var id: String?, val email: String, val nome: String, val cognome: String, val nascita: LocalDate, var stato: Stato = Stato.OK, val tipo: TipoUtente, var statistiche: Statistiche = Statistiche()) {
    init {
        require(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Not a valid Email"
        }
        require(validAge(nascita)) {
            "Age must be over 18"
        }
    }
    private fun validAge(nascita: LocalDate): Boolean {
        val today = LocalDate.now()
        return today.year - nascita.year >= 18
    }
}