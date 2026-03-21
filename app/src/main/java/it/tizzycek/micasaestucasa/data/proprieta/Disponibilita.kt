package it.tizzycek.micasaestucasa.data.proprieta

import java.time.LocalDate
import java.util.Currency
import java.util.Date

data class Disponibilita(val id: String, var dataInizio: LocalDate, var dataFine: LocalDate, var prezzo: Double, var valuta: Currency) {
    init {
        require (dataFine.isAfter((dataInizio))) {
            "Data fine deve essere successiva alla data inizio"
        }
    }

    fun contiene(data: LocalDate): Boolean {
        return !data.isBefore(dataInizio) && !data.isAfter(dataFine)
    }
}
