package it.tizzycek.micasaestucasa.data.prenotazioni

import com.google.firebase.firestore.DocumentSnapshot
import java.time.LocalDate
import java.util.Currency

data class Prenotazioni(val id: String, val proprietarioId: String, val affittuarioId: String, val proprietaId: String, val dataInizio: LocalDate, val dataFine: LocalDate, val ospiti: Int, val prezzoTotale: Double, val valuta: Currency, var statoPrenotazione: StatoPrenotazione = StatoPrenotazione.IN_ATTESA) {
    companion object {
        fun Prenotazioni.toHashMap(): Map<String, Any> {
            return hashMapOf(
                "id" to id,
                "proprietarioId" to proprietarioId,
                "affittuarioId" to affittuarioId,
                "proprietaId" to proprietaId,
                "dataInizio" to dataInizio.toString(),
                "dataFine" to dataFine.toString(),
                "ospiti" to ospiti,
                "prezzoTotale" to prezzoTotale,
                "valuta" to valuta.currencyCode,
                "statoPrenotazione" to statoPrenotazione.toString()
            )
        }

        fun DocumentSnapshot.toPrenotazioni(): Prenotazioni? {
            val data = data ?: return null
            return Prenotazioni(
                id = data["id"] as? String ?: id,
                proprietarioId = data["proprietarioId"] as String,
                affittuarioId = data["affittuarioId"] as String,
                proprietaId = data["proprietaId"] as String,
                dataInizio = LocalDate.parse(data["dataInizio"] as String),
                dataFine = LocalDate.parse(data["dataFine"] as String),
                ospiti = (data["ospiti"] as Number).toInt(),
                prezzoTotale = data["prezzoTotale"] as Double,
                valuta = Currency.getInstance(data["valuta"] as String),
                statoPrenotazione = StatoPrenotazione.valueOf(data["statoPrenotazione"] as String)
            )
        }
    }
}