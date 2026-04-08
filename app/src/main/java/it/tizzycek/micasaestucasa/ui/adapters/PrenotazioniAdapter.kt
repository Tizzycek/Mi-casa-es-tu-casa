package it.tizzycek.micasaestucasa.ui.adapters

import android.content.Intent
import android.graphics.Paint
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni
import it.tizzycek.micasaestucasa.data.prenotazioni.StatoPrenotazione
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.databinding.ItemPrenotazioneBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.DettaglioProprieta
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa.PrenotazioneSheet
import it.tizzycek.micasaestucasa.utils.DateValidator
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Currency
import java.util.UUID

class PrenotazioniAdapter(private val items: List<Prenotazioni>, private val lifecycleOwner: LifecycleOwner, private val tipoUtente: TipoUtente): RecyclerView.Adapter<PrenotazioniAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPrenotazioneBinding) : RecyclerView.ViewHolder(binding.root) {
        private var job: Job? = null

        private var proprieta: Proprieta? = null

        fun bind(prenotazione: Prenotazioni, lifecycleOwner: LifecycleOwner, tipoUtente: TipoUtente) {
            job?.cancel()

            binding.tvNomeProprieta.setText(R.string.pending_loading)  // Restava. Aggiustato col passaggio di lifecycleOwner nella classe e nei vari metodi

            job = lifecycleOwner.lifecycleScope.launch {
                val result = MainRepo.getProprietaRepository().getProprieta(prenotazione.proprietaId)

                result.fold(
                    onSuccess = { prop ->
                        proprieta = prop
                        binding.tvNomeProprieta.text = prop?.nome ?: "_"
                    },
                    onFailure = {
                        binding.tvNomeProprieta.setText(R.string.unknown_error)
                    }
                )
            }

            when (prenotazione.statoPrenotazione) {
                StatoPrenotazione.ACCETTATA -> {
                    binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_accepted)
                    binding.tvStatoPrenotazione.setText(R.string.accettata)
                }

                StatoPrenotazione.IN_ATTESA -> {
                    binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_pending)
                    binding.tvStatoPrenotazione.setText(R.string.in_attesa)
                }

                StatoPrenotazione.RIFIUTATA -> {
                    binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_rejected)
                    binding.tvStatoPrenotazione.setText(R.string.rifiutata)
                }

                StatoPrenotazione.ANNULLATA -> {
                    binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_rejected)
                    binding.tvStatoPrenotazione.setText(R.string.annullata)

                    binding.tvNomeProprieta.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvOspitiItem.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvDateRangeItem.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvPrezzoTotale.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }

            binding.tvOspitiItem.text = "${prenotazione.ospiti} ${binding.root.context.getString(R.string.ospiti)}"
            binding.tvDateRangeItem.text = "${prenotazione.dataInizio} - ${prenotazione.dataFine}"
            binding.tvPrezzoTotale.text = "${prenotazione.valuta.symbol} ${"%.2f".format(prenotazione.prezzoTotale)}"

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, DettaglioProprieta::class.java)
                intent.putExtra("PROPRIETA_ID", prenotazione.proprietaId)
                startActivity(binding.root.context, intent, null)
            }

            configureButtons(tipoUtente, lifecycleOwner, prenotazione)
        }

        private fun configureButtons(tipoUtente: TipoUtente, lifecycleOwner: LifecycleOwner, prenotazione: Prenotazioni) {
            if (tipoUtente == TipoUtente.PROPRIETARIO) {
                if (prenotazione.statoPrenotazione == StatoPrenotazione.IN_ATTESA) {
                    binding.btnSinistro.setText(R.string.rifiuta)
                    binding.btnSinistro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.delete_24px, 0, 0, 0)
                    binding.btnSinistro.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_red_dark))
                    binding.btnSinistro.setStrokeColorResource(android.R.color.holo_red_dark)

                    binding.btnDestro.setText(R.string.accetta)
                    binding.btnDestro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_24px, 0, 0, 0)
                    binding.btnDestro.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.emerald_green))

                    binding.btnSinistro.setOnClickListener {
                        onPrenotazioneRifiutata(lifecycleOwner, prenotazione)
                    }

                    binding.btnDestro.setOnClickListener {
                        onPrenotazioneConfermata(lifecycleOwner, prenotazione)
                    }
                } else {
                    binding.btnSinistro.visibility = View.GONE
                    binding.btnDestro.visibility = View.GONE
                }
            } else {
                binding.btnSinistro.setText(R.string.annulla)
                binding.btnSinistro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cancel_24px, 0, 0, 0)
                binding.btnSinistro.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_red_dark))
                binding.btnSinistro.setStrokeColorResource(android.R.color.holo_red_dark)

                binding.btnDestro.setText(R.string.modifica)
                binding.btnDestro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edit_24px, 0, 0, 0)
                binding.btnDestro.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primary))

                if (prenotazione.statoPrenotazione == StatoPrenotazione.RIFIUTATA || prenotazione.statoPrenotazione == StatoPrenotazione.ANNULLATA) {
                    binding.btnSinistro.isEnabled = false
                    binding.btnSinistro.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray))
                    binding.btnDestro.isEnabled = false
                    binding.btnDestro.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.light_gray))
                } else {
                    binding.btnSinistro.setOnClickListener {
                        onPrenotazioneAnnullata(lifecycleOwner, prenotazione)
                    }
                    binding.btnDestro.setOnClickListener {
                        onPrenotazioneModificata(prenotazione, lifecycleOwner)
                    }
                }
            }
        }

        private fun onPrenotazioneModificata(prenotazione: Prenotazioni, lifecycleOwner: LifecycleOwner) {
            val prop = proprieta ?: return

            // data temporanea per la scelta dell'utente (così vede le sue date + quelle globali)
            val notti = ChronoUnit.DAYS.between(prenotazione.dataInizio, prenotazione.dataFine)
            val prezzoGiorno = prenotazione.prezzoTotale / if (notti > 0) notti else 1

            val bloccoDaAggiungere = Disponibilita(id = "TEMP", dataInizio = prenotazione.dataInizio, dataFine = prenotazione.dataFine, prezzo = prezzoGiorno, valuta = prenotazione.valuta)

            val listaPerCalendario = unisciDisponibilita(prop.disponibilita + bloccoDaAggiungere)

            val sheet = PrenotazioneSheet(listaPerCalendario, prop.posti) { datiNuovi ->
                processaRichiestaModifica(prenotazione, datiNuovi, lifecycleOwner)
            }

            val fragmentManager = (binding.root.context as FragmentActivity).supportFragmentManager
            sheet.show(fragmentManager, "PrenotazioneSheet")
        }

        private fun onPrenotazioneRifiutata(lifecycleOwner: LifecycleOwner, p: Prenotazioni) {
            binding.btnSinistro.isEnabled = false
            binding.btnDestro.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            lifecycleOwner.lifecycleScope.launch {
                try {
                    val risultato = MainRepo.getPrenotazioniRepository().cambiaStatoPrenotazione(p, StatoPrenotazione.RIFIUTATA)
                    risultato.fold(
                        onSuccess = {
                            Toast.makeText(binding.root.context, binding.root.context.getString(R.string.esito_prenotazione) + " " + binding.root.context.getString(R.string.rifiutata), Toast.LENGTH_LONG).show()

                            binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_rejected)
                            binding.tvStatoPrenotazione.setText(R.string.rifiutata)

                            p.statoPrenotazione = StatoPrenotazione.RIFIUTATA

                            binding.btnSinistro.visibility = View.GONE
                            binding.btnDestro.visibility = View.GONE
                        }, onFailure = { e ->
                            (binding.root.context as? android.app.Activity)?.mostraErrore(
                                e.message ?: binding.root.context.getString(R.string.unknown_error)
                            )
                            binding.btnSinistro.isEnabled = true
                            binding.btnDestro.isEnabled = true
                        }
                    )
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        private fun onPrenotazioneConfermata(lifecycleOwner: LifecycleOwner, p: Prenotazioni) {
            binding.btnSinistro.isEnabled = false
            binding.btnDestro.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            lifecycleOwner.lifecycleScope.launch {
                try {
                    // meglio aggiornare tutto (se è modificata non c'è da scrivere altro)
                    val risultatoAggiornamento = MainRepo.getProprietaRepository()
                        .aggiornaDisponibilitaTransazionale(p.proprietaId) { listaAttuale ->
                            computaNuovaDisponibilita(listaAttuale, p)
                        }

                    risultatoAggiornamento.fold(
                        onSuccess = { nuovaDisponibilita ->

                            // metodo intelligente per copiare invece di dargli solo l'uguale
                            proprieta = proprieta?.copy(disponibilita = nuovaDisponibilita)

                            val prenotazioneAggiornata = p.copy(
                                statoPrenotazione = StatoPrenotazione.ACCETTATA,
                                vecchiaDataInizio = null,
                                vecchiaDataFine = null
                            )

                            // meglio usare aggiorna anziché cambia stato (nel caso di approvazione di modifica, aggiorna direttamente tutto)
                            val risultatoStato = MainRepo.getPrenotazioniRepository().aggiornaPrenotazione(prenotazioneAggiornata)

                            risultatoStato.fold(
                                onSuccess = {
                                    Toast.makeText(binding.root.context, binding.root.context.getString(R.string.esito_prenotazione) + " " + binding.root.context.getString(
                                            R.string.accettata
                                        ), Toast.LENGTH_LONG).show()

                                    p.statoPrenotazione = StatoPrenotazione.ACCETTATA
                                    p.vecchiaDataInizio = null
                                    p.vecchiaDataFine = null

                                    notifyItemChanged(adapterPosition)

                                    binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_accepted)
                                    binding.tvStatoPrenotazione.setText(R.string.accettata)

                                    binding.btnSinistro.visibility = View.GONE
                                    binding.btnDestro.visibility = View.GONE
                                }, onFailure = { e ->
                                    (binding.root.context as? android.app.Activity)?.mostraErrore(
                                        e.message ?: binding.root.context.getString(R.string.unknown_error)
                                    )
                                    binding.btnSinistro.isEnabled = true
                                    binding.btnDestro.isEnabled = true
                                }
                            )
                        }, onFailure = { e ->
                            (binding.root.context as? android.app.Activity)?.mostraErrore(
                                e.message ?: binding.root.context.getString(R.string.unknown_error)
                            )
                            binding.btnSinistro.isEnabled = true
                            binding.btnDestro.isEnabled = true
                        }
                    )
                } catch (e: Exception) {
                    (binding.root.context as? android.app.Activity)?.mostraErrore(
                        e.message ?: binding.root.context.getString(R.string.unknown_error)
                    )
                    binding.btnSinistro.isEnabled = true
                    binding.btnDestro.isEnabled = true
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        private fun onPrenotazioneAnnullata(lifecycleOwner: LifecycleOwner, p: Prenotazioni) {
            binding.btnSinistro.isEnabled = false
            binding.btnDestro.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            lifecycleOwner.lifecycleScope.launch {
                try {
                    // bisogna ripristinare le date che c'erano
                    if (p.statoPrenotazione == StatoPrenotazione.ACCETTATA) {
                        val notti = ChronoUnit.DAYS.between(p.dataInizio, p.dataFine)
                        val prezzoGiorno = p.prezzoTotale / if (notti > 0) notti else 1

                        val risultatoRipristino = MainRepo.getProprietaRepository().aggiornaDisponibilitaTransazionale(p.proprietaId) { listaAttuale ->
                                val bloccoDaRipristinare = Disponibilita(id = UUID.randomUUID().toString(), dataInizio = p.dataInizio, dataFine = p.dataFine, prezzo = prezzoGiorno, valuta = p.valuta)
                                unisciDisponibilita(listaAttuale + bloccoDaRipristinare)
                            }

                        if (risultatoRipristino.isFailure) {
                            (binding.root.context as? android.app.Activity)?.mostraErrore(
                                risultatoRipristino.exceptionOrNull()?.message
                                    ?: binding.root.context.getString(R.string.unknown_error)
                            )
                            binding.btnSinistro.isEnabled = true
                            binding.btnDestro.isEnabled = true
                            return@launch
                        }

                        proprieta = proprieta?.copy(
                            disponibilita = risultatoRipristino.getOrNull() ?: emptyList()
                        )
                    }

                    val risultato = MainRepo.getPrenotazioniRepository().cambiaStatoPrenotazione(p, StatoPrenotazione.ANNULLATA)
                    risultato.fold(
                        onSuccess = { Toast.makeText(binding.root.context, binding.root.context.getString(R.string.esito_prenotazione) + " " + binding.root.context.getString(
                                    R.string.annullata
                                ), Toast.LENGTH_LONG).show()

                            binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_rejected)
                            binding.tvStatoPrenotazione.setText(R.string.annullata)

                            p.statoPrenotazione = StatoPrenotazione.ANNULLATA

                            binding.tvNomeProprieta.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            binding.tvOspitiItem.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            binding.tvDateRangeItem.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            binding.tvPrezzoTotale.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                            binding.btnSinistro.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray))
                            binding.btnDestro.setBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.light_gray))

                        }, onFailure = { e ->
                            (binding.root.context as? android.app.Activity)?.mostraErrore(
                                e.message ?: binding.root.context.getString(R.string.unknown_error)
                            )
                            binding.btnSinistro.isEnabled = true
                            binding.btnDestro.isEnabled = true
                        }
                    )
                } catch (e: Exception) {
                    (binding.root.context as? android.app.Activity)?.mostraErrore(
                        e.message ?: binding.root.context.getString(R.string.unknown_error)
                    )
                    binding.btnSinistro.isEnabled = true
                    binding.btnDestro.isEnabled = true
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
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
        private fun computaNuovaDisponibilita(listaAttuale: List<Disponibilita>, p: Prenotazioni): List<Disponibilita> {
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

        private fun unisciDisponibilita(lista: List<Disponibilita>): List<Disponibilita> {
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

        private fun processaRichiestaModifica(prenotazione: Prenotazioni, datiNuovi: Map<String, Any>, lifecycleOwner: LifecycleOwner) {
            val nuovaDataInizio = datiNuovi["dataInizio"] as LocalDate
            val nuovaDataFine = datiNuovi["dataFine"] as LocalDate
            val nuoviOspiti = datiNuovi["ospiti"] as Int
            val nuovoPrezzo = datiNuovi["totale"] as Double
            val nuovaValuta = datiNuovi["valuta"] as Currency

            val richiestaModifica = Prenotazioni(id = prenotazione.id, proprietarioId = prenotazione.proprietarioId, affittuarioId = prenotazione.affittuarioId, proprietaId = prenotazione.proprietaId, dataInizio = nuovaDataInizio, dataFine = nuovaDataFine, ospiti = nuoviOspiti, vecchiaDataInizio = prenotazione.dataInizio, vecchiaDataFine = prenotazione.dataFine, prezzoTotale = nuovoPrezzo, valuta = nuovaValuta, statoPrenotazione = StatoPrenotazione.IN_ATTESA, dataPrenotazione = prenotazione.dataPrenotazione)

            lifecycleOwner.lifecycleScope.launch {
                val risultato = MainRepo.getPrenotazioniRepository().aggiornaPrenotazione(richiestaModifica)
                risultato.fold(
                    onSuccess = {
                        Toast.makeText(binding.root.context, R.string.prenotazione_effettuata, Toast.LENGTH_LONG).show()

                        prenotazione.statoPrenotazione = StatoPrenotazione.IN_ATTESA

                        binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_pending)
                        binding.tvStatoPrenotazione.setText(R.string.in_attesa)
                        binding.tvDateRangeItem.text = "$nuovaDataInizio - $nuovaDataFine"
                        binding.tvOspitiItem.text = "$nuoviOspiti ${binding.root.context.getString(R.string.ospiti)}"
                        binding.tvPrezzoTotale.text = "${nuovaValuta.symbol} ${"%.2f".format(nuovoPrezzo)}"
                    },
                    onFailure = { e ->
                        (binding.root.context as? android.app.Activity)?.mostraErrore(e.message ?: "Errore")
                    }
                )
            }
        }

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPrenotazioneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], lifecycleOwner, tipoUtente)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

/*
Class PrenotazioniAdapter()
    private val items: List<Prenotazioni>,
    private val lifecycleOwner: LifecycleOwner,
    private val onTitleClick: (Prenotazioni) -> Unit // <-- Callback
) : RecyclerView.Adapter<PrenotazioniAdapter.ViewHolder>() {

    // ... nel bind ...
    binding.tvNomeProprieta.setOnClickListener {
        onTitleClick(prenotazione)
    }
}

val adapter = PrenotazioniAdapter(lista, viewLifecycleOwner) { prenotazione ->
    // Gestisci l' intent qui nel Fragment
    val intent = Intent(requireContext(), DettaglioActivity::class.java)
    startActivity(intent)
}

 */
