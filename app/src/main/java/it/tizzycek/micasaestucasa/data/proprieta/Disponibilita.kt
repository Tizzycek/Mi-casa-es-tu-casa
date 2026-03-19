package it.tizzycek.micasaestucasa.data.proprieta

import java.util.Currency
import java.util.Date

data class Disponibilita(val id: String, var dataInizio: Date, var dataFine: Date, var prezzo: Double, var valuta: Currency) {
    init {
        require (dataFine.after(dataInizio)) {
            "Data fine deve essere successiva alla data inizio"
        }
    }

    fun contiene(data: Date): Boolean {
        return !data.before(dataInizio) && !data.after(dataFine)
    }
}
