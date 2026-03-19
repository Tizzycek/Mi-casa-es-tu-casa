package it.tizzycek.micasaestucasa.data.proprieta

import com.google.android.gms.maps.GoogleMap
import it.tizzycek.micasaestucasa.data.utente.Utente
import java.util.Date

data class Proprieta(var id: String?, val proprietarioId: String, var posti: Int, var latitudine: Double, var longitudine: Double, var descrizione: String, var paroleChiaveIds: List<String> = emptyList(), var immaginiUrl: List<String>? = emptyList(), val disponibilita: List<Disponibilita> = emptyList()) {

    fun isDisponibile(dal: Date, al: Date): Boolean {
        val periodiCoprenti = disponibilita.filter { disp ->
            !dal.before(disp.dataInizio) && !al.after(disp.dataFine)
        }
        return !periodiCoprenti.isEmpty()
    }
}

// recensioni, valutazione (media)

// val prezzoBase