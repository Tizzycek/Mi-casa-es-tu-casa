package it.tizzycek.micasaestucasa.data.recensioni

import it.tizzycek.micasaestucasa.data.utente.Utente
import java.time.LocalDateTime

data class Recensione (
    val id: String?,
    val autoreId: String,
    val destinatarioId: String,
    val voto: Int,
    val descrizione: String,
    val dataCreazione: LocalDateTime = LocalDateTime.now()
) {
    init {
        require (voto in 1..5) {
            "Mark must be between 1 and 5"
        }
    }
}
