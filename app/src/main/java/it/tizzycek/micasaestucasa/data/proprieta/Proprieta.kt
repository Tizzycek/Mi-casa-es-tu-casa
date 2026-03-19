package it.tizzycek.micasaestucasa.data.proprieta

import com.google.android.gms.maps.GoogleMap
import it.tizzycek.micasaestucasa.data.utente.Utente
import java.util.Date

data class Proprieta(var id: String?, val proprietarioId: String, var nome: String, var posti: Int, var latitudine: Double, var longitudine: Double, var descrizione: String, var paroleChiaveIds: List<String> = emptyList(), var immaginiUrl: List<String>? = emptyList(), val disponibilita: List<Disponibilita> = emptyList()) {

    fun isDisponibile(dal: Date, al: Date): Boolean {
        val periodiCoprenti = disponibilita.filter { disp ->
            disp.contiene(dal) && disp.contiene(al) // TODO: controllare logica
        }
        return !periodiCoprenti.isEmpty()
    }
}

// recensioni, valutazione (media)

// val prezzoBase