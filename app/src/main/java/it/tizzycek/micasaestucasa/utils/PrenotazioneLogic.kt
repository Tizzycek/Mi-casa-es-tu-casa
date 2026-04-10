package it.tizzycek.micasaestucasa.utils

import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import java.time.temporal.ChronoUnit
import java.util.UUID

object PrenotazioneLogic {
    private fun eseguiModificaPrenotazioneCompleta(listaAttuale: List<Disponibilita>, prenotazione: Prenotazioni): List<Disponibilita> {
        val vecchiaInizio = prenotazione.vecchiaDataInizio!!
        val vecchiaFine = prenotazione.vecchiaDataFine!!

        //    Il prezzo per notte corretto è quello del blocco originale da cui la
        //    prenotazione era stata ritagliata.  Quel blocco non esiste più in
        //    listaAttuale, ma i suoi vicini immediati (il blocco che termina il
        //    giorno prima dell'inizio vecchio e/o quello che inizia il giorno dopo
        //    la fine vecchia) conservano lo stesso prezzo.
        //    Se entrambi i vicini concordano → usiamo quel prezzo.
        //    Se c'è un solo vicino → usiamo il suo prezzo.
        //    Se i vicini hanno prezzi diversi o non esistono (prenotazione che
        //    copriva tutta la disponibilità) → fallback al calcolo proporzionale
        //    sul totale della nuova prenotazione (comportamento precedente).
        val vicini = listaAttuale.filter { disp ->
            disp.dataFine == vecchiaInizio.minusDays(1) || disp.dataInizio == vecchiaFine.plusDays(1)
        }
        val nottiVecchie = ChronoUnit.DAYS.between(vecchiaInizio, vecchiaFine)
        val prezzoGiorno = when {
            vicini.isNotEmpty() && vicini.all { it.prezzo == vicini[0].prezzo } ->
                vicini[0].prezzo

            else ->
                prenotazione.prezzoTotale / if (nottiVecchie > 0) nottiVecchie else 1
        }

        val bloccoDaRipristinare = Disponibilita(
            id = UUID.randomUUID().toString(),
            dataInizio = vecchiaInizio,
            dataFine = vecchiaFine,
            prezzo = prezzoGiorno,
            valuta = prenotazione.valuta
        )

        // merge
        val listaRipristinata = unisciDisponibilita(listaAttuale + bloccoDaRipristinare)

        // split nuovo
        return aggiornaListaDisponibilita(listaRipristinata, prenotazione)
    }

    fun unisciDisponibilita(lista: List<Disponibilita>): List<Disponibilita> {
        if (lista.size < 2) return lista

        val ordinate = lista.sortedBy { it.dataInizio }
        val risultato = mutableListOf<Disponibilita>()

        var corrente = ordinate[0].copy()

        for (i in 1 until ordinate.size) {
            val successiva = ordinate[i]

            // controllo dei settori (se hanno stessa valuta e prezzo, per attaccarli)
            val giorniDifferenza = ChronoUnit.DAYS.between(corrente.dataFine, successiva.dataInizio)
            val stessoPrezzo = corrente.prezzo == successiva.prezzo && corrente.valuta == successiva.valuta

            if (giorniDifferenza == 1L && stessoPrezzo) {
                corrente.dataFine = successiva.dataFine
            } else {
                risultato.add(corrente)
                corrente = successiva.copy()
            }
        }

        risultato.add(corrente)
        return risultato
    }

    private fun aggiornaListaDisponibilita(listaAttuale: List<Disponibilita>, prenotazione: Prenotazioni): List<Disponibilita> {
        val inizioP = prenotazione.dataInizio
        val fineP = prenotazione.dataFine
        val nuovaLista = mutableListOf<Disponibilita>()

        var bloccoTrovato = false

        for (disp in listaAttuale) {
            // Se il blocco non ha intersezioni con la prenotazione, lo teniamo così com'è
            if (fineP.isBefore(disp.dataInizio) || inizioP.isAfter(disp.dataFine)) {
                nuovaLista.add(disp)
                continue
            }

            // Se arriviamo qui, c'è un'intersezione. Segnamo che abbiamo trovato dove tagliare.
            bloccoTrovato = true

            // Caso 1: C'è un pezzo che avanza PRIMA della prenotazione
            if (disp.dataInizio.isBefore(inizioP)) {
                nuovaLista.add(disp.copy(id = UUID.randomUUID().toString(), dataFine = inizioP.minusDays(1)))
            }

            // Caso 2: C'è un pezzo che avanza DOPO della prenotazione
            if (disp.dataFine.isAfter(fineP)) {
                nuovaLista.add(disp.copy(id = UUID.randomUUID().toString(), dataInizio = fineP.plusDays(1)))
            }
        }

        if (!bloccoTrovato) throw Exception("Le date selezionate non sono più disponibili.")

        return nuovaLista.sortedBy { it.dataInizio }
    }

    /**
     * Decides whether the old blocked dates need to be restored before blocking
     * the new ones (booking was ACCETTATA before being edited) or whether they
     * are still present in the availability, and we can just block the new dates
     * directly (booking was IN_ATTESA when edited, so they were never removed).
     *
     * This is the single entry-point called by onPrenotazioneConfermata so that
     * both Bug 1 (stale proprieta) and Bug 2 (wrong routing for edited bookings)
     * are handled in one place with fresh Firestore data.
     */
    fun computaNuovaDisponibilita(listaAttuale: List<Disponibilita>, p: Prenotazioni): List<Disponibilita> {
        val vecchiaInizio = p.vecchiaDataInizio
        val vecchiaFine = p.vecchiaDataFine

        return if (vecchiaInizio != null && vecchiaFine != null) {
            // controlla che le date vecchie ci siano (ovvero che la prenotazione era in attesa, e le date sono ancora lì da modificare).
            val vecchieDateAncorePresenti = listaAttuale.any { disp ->
                !vecchiaInizio.isBefore(disp.dataInizio) && !vecchiaFine.isAfter(disp.dataFine)
            }
            if (vecchieDateAncorePresenti) {
                // non c'è niente da fare, a parte aggiornare
                aggiornaListaDisponibilita(listaAttuale, p)
            } else {
                // bisogna ripristinare e sottrarre il nuovo range, perché le date sono occupate
                eseguiModificaPrenotazioneCompleta(listaAttuale, p)
            }
        } else {
            // prenotazione nuova
            aggiornaListaDisponibilita(listaAttuale, p)
        }
    }
}