package it.tizzycek.micasaestucasa.data.prenotazioni

import java.time.LocalDate

data class Prenotazioni(val id: String, val proprietarioId: String, val affittuarioId: String, val dataInizio: LocalDate, val dataFine: LocalDate, val ospiti: Int, var statoPrenotazione: StatoPrenotazione = StatoPrenotazione.IN_ATTESA) {

}