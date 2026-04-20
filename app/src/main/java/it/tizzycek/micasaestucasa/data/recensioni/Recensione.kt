package it.tizzycek.micasaestucasa.data.recensioni

import java.time.Instant

data class Recensione(val id: String, val autoreId: String, val nomeAutore: String, val voto: Int, val descrizione: String, val dataCreazione: Instant = Instant.now()) {
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
                "nomeAutore" to nomeAutore,
                "voto" to voto,
                "descrizione" to descrizione,
                "dataCreazione" to com.google.firebase.Timestamp(java.util.Date.from(dataCreazione))// conversione Instant in Timestamp di Firebase

            )
        }

        fun Map<String, Any?>.toRecensioni(): Recensione {
            val dataRaw = this["dataCreazione"]

            // metodo per evitare eccezioni col timestamp
            val dataCreazione = when (dataRaw) {
                is com.google.firebase.Timestamp -> dataRaw.toDate().toInstant() // Se Firebase restituisce un Timestamp
                is String -> Instant.parse(dataRaw) // Se Firebase restituisce la stringa che hai salvato
                else -> Instant.now() // Fallback di sicurezza
            }

            return Recensione(
                id = this["id"] as String,
                autoreId = this["autoreId"] as String,
                nomeAutore = this["nomeAutore"] as String,
                voto = (this["voto"] as Number).toInt(),
                descrizione = this["descrizione"] as String,
                dataCreazione = dataCreazione
            )
        }
    }
}
