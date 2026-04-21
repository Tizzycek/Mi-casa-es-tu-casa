package it.tizzycek.micasaestucasa.data.recensioni

import com.google.firebase.firestore.DocumentSnapshot
import it.tizzycek.micasaestucasa.data.recensioni.Recensione.Companion.toHashMap
import it.tizzycek.micasaestucasa.data.recensioni.Recensione.Companion.toRecensioni

data class Statistiche(val idUtente: String, var mediaRecensioni: Double = 0.0, var totaleRecensioni: Int = 0, var listaRecensioni: List<Recensione> = emptyList(), var listaSegnalazioni: List<Segnalazione> = emptyList()) {
    init {
        update()
    }

    fun update() {
        totaleRecensioni = listaRecensioni.size
        mediaRecensioni = if (totaleRecensioni > 0) {
            listaRecensioni.map { it.voto }.average()
        } else {
            0.0
        }
    }

    fun aggiungiRecensione(nuova: Recensione) {
        listaRecensioni = listaRecensioni + nuova
        update()
    }

    fun rimuoviRecensione(daRimuovere: Recensione) {
        listaRecensioni = listaRecensioni - daRimuovere
        update()
    }

    fun aggiungiSegnalazione(nuova: Segnalazione) {
        listaSegnalazioni = listaSegnalazioni + nuova
    }

    fun rimuoviSegnalazione(daRimuovere: Segnalazione) {
        listaSegnalazioni = listaSegnalazioni - daRimuovere
    }

    companion object {
        fun Statistiche.toHashMap(): Map<String, Any?> {
            return hashMapOf(
                "idUtente" to idUtente,
                "mediaRecensioni" to mediaRecensioni,
                "totaleRecensioni" to totaleRecensioni,
                "listaRecensioni" to listaRecensioni.map { it.toHashMap() },
                "listaSegnalazioni" to listaSegnalazioni
            )
        }

        fun DocumentSnapshot.toStatistiche(): Statistiche {
            return Statistiche(
                idUtente = this["idUtente"] as String,
                mediaRecensioni = this["mediaRecensioni"] as? Double ?: 0.0,
                totaleRecensioni = this["totaleRecensioni"] as? Int ?: 0,
                listaRecensioni = (this["listaRecensioni"] as? List<Map<String, Any?>>)?.map { it.toRecensioni() } ?: emptyList(),
                listaSegnalazioni = this["listaSegnalazioni"] as? List<Segnalazione> ?: emptyList()
            )
        }
    }
}
