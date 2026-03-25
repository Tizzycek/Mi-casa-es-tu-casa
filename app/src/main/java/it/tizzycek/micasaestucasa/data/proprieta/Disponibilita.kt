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

    companion object {
        fun Disponibilita.toHashMap(): Map<String, Any?> {
            return hashMapOf(
                "id" to id,
                "dataInizio" to dataInizio.toString(),
                "dataFine" to dataFine.toString(),
                "prezzo" to prezzo,
                "valuta" to valuta.currencyCode
            )
        }

        fun Map<String, Any?>.toDisponibilita(): Disponibilita {
            return Disponibilita(
                id = this["id"] as String,
                dataInizio = LocalDate.parse(this["dataInizio"] as String),
                dataFine = LocalDate.parse(this["dataFine"] as String),
                prezzo = (this["prezzo"] as Number).toDouble(),
                valuta = Currency.getInstance(this["valuta"] as String)
            )
        }
    }
}
