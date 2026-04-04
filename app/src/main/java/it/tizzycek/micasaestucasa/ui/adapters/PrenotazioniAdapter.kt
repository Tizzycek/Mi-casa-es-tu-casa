package it.tizzycek.micasaestucasa.ui.adapters

import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni
import it.tizzycek.micasaestucasa.data.prenotazioni.StatoPrenotazione
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.databinding.ItemPrenotazioneBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.DettaglioProprieta
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PrenotazioniAdapter(private val items: List<Prenotazioni>, private val lifecycleOwner: LifecycleOwner, private val tipoUtente: TipoUtente): RecyclerView.Adapter<PrenotazioniAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemPrenotazioneBinding): RecyclerView.ViewHolder(binding.root) {
        private var job: Job? = null
        fun bind(prenotazione: Prenotazioni, lifecycleOwner: LifecycleOwner, tipoUtente: TipoUtente) {
            job?.cancel()

            binding.tvNomeProprieta.setText(R.string.pending_loading)  // Restava. Aggiustato col passaggio di lifecycleOwner nella classe e nei vari metodi

            job = lifecycleOwner.lifecycleScope.launch {
                val result = MainRepo.getProprietaRepository().getProprieta(prenotazione.proprietaId)

                result.fold(
                    onSuccess = { proprieta ->
                        binding.tvNomeProprieta.text = proprieta?.nome ?: "_"
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

            binding.tvOspitiItem.text = prenotazione.ospiti.toString()
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
                        Toast.makeText(binding.root.context, "approva", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(binding.root.context, "modifica", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        private fun onPrenotazioneRifiutata(lifecycleOwner: LifecycleOwner, p: Prenotazioni) {
            binding.btnSinistro.isEnabled = false
            binding.btnDestro.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            
            lifecycleOwner.lifecycleScope.launch { 
                val risultato = MainRepo.getPrenotazioniRepository().cambiaStatoPrenotazione(p, StatoPrenotazione.RIFIUTATA)
                risultato.fold(
                    onSuccess = {
                        Toast.makeText(binding.root.context, binding.root.context.getString(R.string.esito_prenotazione) + " " + binding.root.context.getString(R.string.rifiutata), Toast.LENGTH_LONG).show()
                        
                        binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_rejected)
                        binding.tvStatoPrenotazione.setText(R.string.rifiutata)
                        
                        binding.btnSinistro.visibility = View.GONE
                        binding.btnDestro.visibility = View.GONE
                    }, onFailure = { e ->
                        (binding.root.context as? android.app.Activity)?.mostraErrore(e.message ?: binding.root.context.getString(R.string.unknown_error))
                        binding.btnSinistro.isEnabled = true
                        binding.btnDestro.isEnabled = true
                    }
                )
            }
            binding.progressBar.visibility = View.GONE
        }

        private fun onPrenotazioneAnnullata(lifecycleOwner: LifecycleOwner, p: Prenotazioni) {
            binding.btnSinistro.isEnabled = false
            binding.btnDestro.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            lifecycleOwner.lifecycleScope.launch {
                val risultato = MainRepo.getPrenotazioniRepository().cambiaStatoPrenotazione(p, StatoPrenotazione.ANNULLATA)
                risultato.fold(
                    onSuccess = {
                        Toast.makeText(binding.root.context, binding.root.context.getString(R.string.esito_prenotazione) + " " + binding.root.context.getString(R.string.annullata), Toast.LENGTH_LONG).show()

                        binding.tvStatoPrenotazione.setBackgroundResource(R.drawable.bg_status_rejected)
                        binding.tvStatoPrenotazione.setText(R.string.annullata)

                        binding.tvNomeProprieta.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        binding.tvOspitiItem.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        binding.tvDateRangeItem.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        binding.tvPrezzoTotale.paintFlags = binding.tvNomeProprieta.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                        binding.btnSinistro.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray))
                        binding.btnDestro.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.light_gray))

                    }, onFailure = { e ->
                        (binding.root.context as? android.app.Activity)?.mostraErrore(e.message ?: binding.root.context.getString(R.string.unknown_error))
                        binding.btnSinistro.isEnabled = true
                        binding.btnDestro.isEnabled = true
                    }
                )
            }
            binding.progressBar.visibility = View.GONE
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
class PrenotazioniAdapter(
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
    // Gestisci l'intent qui nel Fragment
    val intent = Intent(requireContext(), DettaglioActivity::class.java)
    startActivity(intent)
}

 */