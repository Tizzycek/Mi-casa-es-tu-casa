package it.tizzycek.micasaestucasa.data.utente

import android.util.Patterns
import java.time.LocalDate

data class Utente (var id: String?, val email: String, val nome: String, val cognome: String, val nascita: LocalDate, var stato: Stato = Stato.OK, val tipo: TipoUtente) {
    init {
        require(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Not a valid Email"
        }
        require(validAge(nascita)) {
            "Age must be over 18"
        }
        //require(validPw())
    }
    private fun validAge(nascita: LocalDate): Boolean {
        val today = LocalDate.now()
        return today.year - nascita.year >= 18
    }

    /*private fun validPw(pw: String): Boolean {
        return pw.length >= 8
    }*/
}