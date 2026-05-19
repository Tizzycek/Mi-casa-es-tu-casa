package it.tizzycek.micasaestucasa.data.proprieta

import com.google.firebase.firestore.DocumentSnapshot
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita.Companion.toDisponibilita
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita.Companion.toHashMap
import java.time.LocalDate

data class Proprieta(var id: String?, val proprietarioId: String, var nome: String, var posti: Int, var latitudine: Double, var longitudine: Double, var descrizione: String, var paroleChiaveIds: List<String> = emptyList(), var immaginiUrl: List<String>? = emptyList(), var disponibilita: List<Disponibilita> = emptyList()) {

    fun isDisponibile(dal: LocalDate, al: LocalDate): Boolean {
        val periodiCoprenti = disponibilita.filter { disp ->
            disp.contiene(dal) && disp.contiene(al)
        }
        return !periodiCoprenti.isEmpty()
    }

    companion object {
        fun Proprieta.toHashMap(): Map<String, Any?> {
            return hashMapOf(
                "proprietarioId" to proprietarioId,
                "nome" to nome,
                "posti" to posti,
                "latitudine" to latitudine,
                "longitudine" to longitudine,
                "descrizione" to descrizione,
                "paroleChiaveIds" to paroleChiaveIds,
                "immaginiUrl" to immaginiUrl,
                "disponibilita" to disponibilita.map { it.toHashMap() } // Trasforma la lista
            )
        }

        fun DocumentSnapshot.toProprieta(): Proprieta? {
            val data = data ?: return null
            return Proprieta(
                id = id,
                proprietarioId = data["proprietarioId"] as? String ?: "",
                nome = data["nome"] as? String ?: "",
                posti = (data["posti"] as? Number)?.toInt() ?: 0,
                latitudine = (data["latitudine"] as? Number)?.toDouble() ?: 0.0,
                longitudine = (data["longitudine"] as? Number)?.toDouble() ?: 0.0,
                descrizione = data["descrizione"] as? String ?: "",
                paroleChiaveIds = data["paroleChiaveIds"] as? List<String> ?: emptyList(),
                immaginiUrl = data["immaginiUrl"] as? List<String> ?: emptyList(),
                disponibilita = (data["disponibilita"] as? List<Map<String, Any?>>)?.map { it.toDisponibilita() } ?: emptyList()
            )
        }
    }
}