package it.tizzycek.micasaestucasa.data.recensioni

import it.tizzycek.micasaestucasa.data.utente.Utente
import java.time.LocalDateTime

data class Recensione(val id: String, val autoreId: String, val voto: Int, val descrizione: String, val dataCreazione: LocalDateTime = LocalDateTime.now()) {
    init {
        require (voto in 1..5) {
            "Mark must be between 1 and 5"
        }
    }

    companion object {
        fun Recensione.toHashMap(): Map<String, Any?> {
            return hashMapOf(
                "id" to id,
                "autoreId" to autoreId,
                "voto" to voto,
                "descrizione" to descrizione,
                "dataCreazione" to dataCreazione.toString()
            )
        }

        fun Map<String, Any?>.toRecensioni(): Recensione {
            return Recensione(
                id = this["id"] as String,
                autoreId = this["autoreId"] as String,
                voto = this["voto"] as Int,
                descrizione = this["descrizione"] as String,
                dataCreazione = LocalDateTime.parse(this["dataCreazione"] as String)
            )
        }
    }
}
