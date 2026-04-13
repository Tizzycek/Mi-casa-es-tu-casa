package it.tizzycek.micasaestucasa.data.recensioni

import it.tizzycek.micasaestucasa.data.recensioni.Recensione.Companion.toHashMap
import it.tizzycek.micasaestucasa.data.recensioni.Recensione.Companion.toRecensioni

data class Statistiche(var mediaRecensioni: Double = 0.0, var totaleRecensioni: Int = 0, val listaRecensioni: List<Recensione> = emptyList(), val listaSegnalazioni: List<Segnalazione> = emptyList()) {
    init {
        totaleRecensioni = listaRecensioni.size
        mediaRecensioni = if (totaleRecensioni > 0) {
            listaRecensioni.map { it.voto }.average()
        } else {
            0.0
        }
    }
    companion object {
        fun Statistiche.toHashMap(): Map<String, Any?> {
            return hashMapOf(
                "mediaRecensioni" to mediaRecensioni,
                "totaleRecensioni" to totaleRecensioni,
                "listaRecensioni" to listaRecensioni.map { it.toHashMap() },
                "listaSegnalazioni" to listaSegnalazioni
            )
        }

        fun Map<String, Any?>.toStatistiche(): Statistiche {
            return Statistiche(
                mediaRecensioni = this["mediaRecensioni"] as? Double ?: 0.0,
                totaleRecensioni = this["totaleRecensioni"] as? Int ?: 0,
                listaRecensioni = (this["listaRecensioni"] as? List<Map<String, Any?>>)?.map { it.toRecensioni() } ?: emptyList(),
                listaSegnalazioni = this["listaSegnalazioni"] as? List<Segnalazione> ?: emptyList()
            )
        }
    }
}
