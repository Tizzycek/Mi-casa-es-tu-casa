package it.tizzycek.micasaestucasa.data.utente

import android.util.Patterns

data class Utente (var id: String?, val email: String, val nome: String, val cognome: String, val eta: Int, var stato: Stato = Stato.OK, val tipo: TipoUtente) {
    init {
        require(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Not a valid Email"
        }
        require(validAge(eta)) {
            "Age must be over 18"
        }
        //require(validPw())
    }
    private fun validAge(eta : Int): Boolean {
        return eta >= 18
    }

    /*private fun validPw(pw: String): Boolean {
        return pw.length >= 8
    }*/
}