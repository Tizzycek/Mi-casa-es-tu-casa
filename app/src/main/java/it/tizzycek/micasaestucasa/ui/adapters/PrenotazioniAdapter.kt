package it.tizzycek.micasaestucasa.ui.adapters

import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni
import it.tizzycek.micasaestucasa.data.prenotazioni.StatoPrenotazione
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.data.recensioni.Recensione
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.databinding.ItemPrenotazioneBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.DettaglioProprieta
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa.PrenotazioneSheet
import it.tizzycek.micasaestucasa.ui.utente.NuovaRecensioneSheet
import it.tizzycek.micasaestucasa.ui.utente.RecensioniActivity
import it.tizzycek.micasaestucasa.utils.DateValidator
import it.tizzycek.micasaestucasa.utils.PrenotazioneLogic
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
                        binding.tvNomeProprieta.setText(R.string.non_disponibile)
                    }
                )
            }

            // dovrebbe rifiutare automaticamente le prenotazioni latenti che iniziano dopo la data odierna
            if (DateValidator.isBeforeToday(prenotazione.dataInizio) && prenotazione.statoPrenotazione == StatoPrenotazione.IN_ATTESA) {
                onPrenotazioneRifiutata(lifecycleOwner, prenotazione)
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

            if (tipoUtente == TipoUtente.PROPRIETARIO) {
                binding.root.setOnClickListener {
                    val intent = Intent(binding.root.context, RecensioniActivity::class.java)
                    intent.putExtra("idUtente", prenotazione.affittuarioId)
                    binding.root.context.startActivity(intent)
                }
            } else {
                binding.root.setOnClickListener {
                    val intent = Intent(binding.root.context, DettaglioProprieta::class.java)
                    intent.putExtra("PROPRIETA_ID", prenotazione.proprietaId)
                    binding.root.context.startActivity(intent)
                }
            }
            configureButtons(tipoUtente, lifecycleOwner, prenotazione)
        }

        private fun configureButtons(tipoUtente: TipoUtente, lifecycleOwner: LifecycleOwner, prenotazione: Prenotazioni) {
            if (DateValidator.isBeforeToday(prenotazione.dataFine) && prenotazione.statoPrenotazione == StatoPrenotazione.ACCETTATA) {
                binding.btnSinistro.visibility = View.GONE
                binding.btnDestro.visibility = View.GONE

                binding.btnRecensione.visibility = View.VISIBLE

                if ((tipoUtente == TipoUtente.PROPRIETARIO && prenotazione.daRecensire.daRecensireProprietario) || (tipoUtente == TipoUtente.AFFITTUARIO && prenotazione.daRecensire.daRecensireAffittuario)) {
                    binding.btnRecensione.isEnabled = true
                    binding.btnRecensione.setOnClickListener {
                        val sheet = NuovaRecensioneSheet { recensione ->
                            newRecensione(recensione, prenotazione, tipoUtente)
                        }
                        sheet.show((binding.root.context as FragmentActivity).supportFragmentManager, "NuovaRecensioneSheet")
                    }
                } else {
                    binding.btnRecensione.isEnabled = false
                    binding.btnRecensione.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray))
                }

            } else {
                if (tipoUtente == TipoUtente.PROPRIETARIO) {
                    if (prenotazione.statoPrenotazione == StatoPrenotazione.IN_ATTESA) {
                        binding.btnSinistro.visibility = View.VISIBLE
                        binding.btnDestro.visibility = View.VISIBLE

                        binding.btnSinistro.isEnabled = true
                        binding.btnDestro.isEnabled = true

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

                    if (prenotazione.statoPrenotazione == StatoPrenotazione.RIFIUTATA || prenotazione.statoPrenotazione == StatoPrenotazione.ANNULLATA || (prenotazione.statoPrenotazione == StatoPrenotazione.ACCETTATA && DateValidator.isBeforeToday(prenotazione.dataInizio))) {
                        binding.btnSinistro.isEnabled = false
                        binding.btnSinistro.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray))
                        binding.btnDestro.isEnabled = false
                        binding.btnDestro.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.light_gray))
                    } else {
                        binding.btnSinistro.isEnabled = true
                        binding.btnSinistro.setOnClickListener {
                            onPrenotazioneAnnullata(lifecycleOwner, prenotazione)
                        }
                        binding.btnDestro.isEnabled = true
                        binding.btnDestro.setOnClickListener {
                            onPrenotazioneModificata(prenotazione, lifecycleOwner)
                        }
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

            val listaPerCalendario = PrenotazioneLogic.unisciDisponibilita(prop.disponibilita + bloccoDaAggiungere)

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
                            PrenotazioneLogic.computaNuovaDisponibilita(listaAttuale, p)
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
                            PrenotazioneLogic.unisciDisponibilita(listaAttuale + bloccoDaRipristinare)
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

        private fun processaRichiestaModifica(prenotazione: Prenotazioni, datiNuovi: Map<String, Any>, lifecycleOwner: LifecycleOwner) {
            val nuovaDataInizio = datiNuovi["dataInizio"] as LocalDate
            val nuovaDataFine = datiNuovi["dataFine"] as LocalDate
            val nuoviOspiti = datiNuovi["ospiti"] as Int
            val nuovoPrezzo = datiNuovi["totale"] as Double
            val nuovaValuta = datiNuovi["valuta"] as Currency

            val richiestaModifica = Prenotazioni(id = prenotazione.id, proprietarioId = prenotazione.proprietarioId, affittuarioId = prenotazione.affittuarioId, proprietaId = prenotazione.proprietaId, dataInizio = nuovaDataInizio, dataFine = nuovaDataFine, ospiti = nuoviOspiti, vecchiaDataInizio = prenotazione.dataInizio, vecchiaDataFine = prenotazione.dataFine, prezzoTotale = nuovoPrezzo, valuta = nuovaValuta, statoPrenotazione = StatoPrenotazione.IN_ATTESA, dataPrenotazione = prenotazione.dataPrenotazione, daRecensire = prenotazione.daRecensire)

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

        private fun newRecensione(recensione: Recensione, p: Prenotazioni, tipoUtente: TipoUtente) {
            binding.btnRecensione.isEnabled = false
            binding.btnRecensione.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray))
            binding.progressBar.visibility = View.VISIBLE

            lifecycleOwner.lifecycleScope.launch {
                try {
                    val idUtente = if (tipoUtente == TipoUtente.PROPRIETARIO) p.affittuarioId else p.proprietarioId

                    // 1. Prendi statistiche
                    val statistica = MainRepo.getStatisticheRepository().getStatistiche(idUtente).getOrThrow()

                    // 2. Aggiorna statistiche
                    statistica.aggiungiRecensione(recensione)
                    MainRepo.getStatisticheRepository().aggiornaStatistiche(idUtente, statistica).getOrThrow()

                    if (tipoUtente == TipoUtente.PROPRIETARIO)
                        p.daRecensire = p.daRecensire.copy(daRecensireProprietario = false)
                    else
                        p.daRecensire = p.daRecensire.copy(daRecensireAffittuario = false)

                    // 3. Segna prenotazione come fatta
                    val fine = MainRepo.getPrenotazioniRepository().aggiornaPrenotazione(p)

                    if (fine.isFailure) {
                        statistica.rimuoviRecensione(recensione)
                        MainRepo.getStatisticheRepository().aggiornaStatistiche(idUtente, statistica)
                        throw fine.exceptionOrNull() ?: Exception("Review error")
                    }

                    Toast.makeText(binding.root.context, binding.root.context.getString(R.string.recensita), Toast.LENGTH_LONG).show()

                } catch (e: Exception) {
                    binding.root.mostraErrore(e.message ?: binding.root.context.getString(R.string.unknown_error))
                    binding.btnRecensione.isEnabled = true
                    binding.btnRecensione.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primary))
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
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
