package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni
import it.tizzycek.micasaestucasa.data.prenotazioni.StatoPrenotazione
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.databinding.ActivityDettaglioProprietaBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.DisponibilitaAdapter
import it.tizzycek.micasaestucasa.ui.adapters.ImageDetailAdapter
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa.AggiungiModificaProprieta
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa.PrenotazioneSheet
import it.tizzycek.micasaestucasa.ui.utente.ChatActivity
import it.tizzycek.micasaestucasa.ui.utente.RecensioniActivity
import it.tizzycek.micasaestucasa.utils.DateValidator
import it.tizzycek.micasaestucasa.utils.mostraErrore
import it.tizzycek.micasaestucasa.utils.paroleChiaveProprieta
import it.tizzycek.micasaestucasa.utils.sistemaSchermo
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.Currency
import java.util.UUID

class DettaglioProprieta: AppCompatActivity() {
    private lateinit var binding: ActivityDettaglioProprietaBinding
    private var idProprieta: String? = null
    private var locatore: Utente? = null
    private var listaDisponibilita: List<Disponibilita> = emptyList()
    private var numeroOspiti: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDettaglioProprietaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sistemaSchermo(binding.root.rootView)
    }

    override fun onResume() {
        super.onResume()

        idProprieta = intent.getStringExtra("PROPRIETA_ID")

        if (idProprieta != null) {
            caricaDatiPropieta(idProprieta!!)
        } else {
            finish()
        }
        setupButtons()
    }

    private fun caricaDatiPropieta(id: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE

            val risultato = MainRepo.getProprietaRepository().getProprieta(id)
            risultato.fold(
                onSuccess = { proprieta ->
                    if (proprieta != null)
                        mostraDati(proprieta)
                    else
                        mostraErrore(R.string.unknown_error)
                },
                onFailure = { e ->
                    mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
            binding.progressBar.visibility = View.GONE
        }
    }

    private suspend fun mostraDati(p: Proprieta) {
        binding.tvNome.text = p.nome
        numeroOspiti = p.posti

        if (MainRepo.getUserRepository().getCurrentUser()!!.tipo != TipoUtente.PROPRIETARIO) {
            locatore = MainRepo.getUserRepository().beccaDatiUtente(p.proprietarioId)
            binding.tvLocatore.text = locatore?.nome + " " + locatore?.cognome
            binding.tvAvatarLocatoreCompact.text = locatore?.nome[0].toString() + locatore?.cognome[0].toString()

            binding.containerLocatore.setOnClickListener {
                val intent = Intent(this, RecensioniActivity::class.java)
                intent.putExtra("idUtente", p.proprietarioId)
                startActivity(intent)
            }

        } else {
            binding.containerLocatore.visibility = View.GONE
        }


        val posti = getString(R.string.posti_disponibili) + " ${p.posti}"
        binding.tvPosti.text = posti

        binding.tvDescrizione.text = p.descrizione

        binding.cgKeywords.paroleChiaveProprieta(lifecycleScope, p.paroleChiaveIds)


        val urls = p.immaginiUrl ?: emptyList()
        if (urls.isNotEmpty()) {
            val adapter = ImageDetailAdapter(urls)
            binding.viewPagerImages.adapter = adapter
        } else {
            binding.viewPagerImages.visibility = View.GONE
        }

        disponibilitaAdapter(p.disponibilita)
    }

    private fun disponibilitaAdapter(ld: List<Disponibilita>) {
        listaDisponibilita = ld
        if (binding.rvDisponibilita.layoutManager == null) {
            binding.rvDisponibilita.layoutManager = LinearLayoutManager(this)
        }

        val adapter = DisponibilitaAdapter(ld)
        binding.rvDisponibilita.adapter = adapter
    }

    private fun setupButtons() {
        if (MainRepo.getUserRepository().getCurrentUser()?.tipo == TipoUtente.PROPRIETARIO) {
            binding.btnSinistro.text = getString(R.string.modifica)
            binding.btnSinistro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edit_24px, 0, 0, 0)
            binding.btnSinistro.setOnClickListener {
                val intent = Intent(this, AggiungiModificaProprieta::class.java)
                intent.putExtra("PROPERTY_ID", idProprieta)
                startActivity(intent)
            }

            binding.btnDestro.text = getString(R.string.elimina)
            binding.btnDestro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.delete_24px, 0, 0, 0)
            binding.btnDestro.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            binding.btnDestro.setOnClickListener {
                eliminaProprieta()
            }
        } else {
            binding.btnSinistro.text = getString(R.string.contatta)
            binding.btnSinistro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.forward_to_inbox_24px, 0, 0, 0)
            binding.btnSinistro.setOnClickListener {
                val intent = Intent(this@DettaglioProprieta, ChatActivity::class.java)
                val currentUserId = MainRepo.getUserRepository().getCurrentUser()?.id ?: "" // senza questa soluzione il sorted non ne vuole sapere

                val chatId = listOf(currentUserId, locatore?.id!!).sorted().joinToString("_")

                intent.putExtra("CHAT_ID", chatId)
                intent.putExtra("destinatarioId", locatore?.id!!)
                intent.putExtra("nomeDestinatario", locatore?.nome)
                intent.putExtra("cognomeDestinatario", locatore?.cognome)

                startActivity(intent)
            }

            binding.btnDestro.text = getString(R.string.prenota)
            binding.btnDestro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.event_upcoming_24px, 0, 0, 0)
            binding.btnDestro.setOnClickListener {
                val sheet = PrenotazioneSheet(listaDisponibilita, numeroOspiti) { prenotazione ->
                    gestionePrenotazione(prenotazione)
                }
                sheet.show(supportFragmentManager, "PrenotazioneSheet")
            }
        }
    }

    private fun eliminaProprieta() {
        MaterialAlertDialogBuilder(this).setTitle(R.string.elimina).setMessage(R.string.sei_sicuro_elimina)
            .setNegativeButton(R.string.annulla) { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton(R.string.elimina) { _, _ ->
                lifecycleScope.launch {
                    binding.progressBar.visibility = View.VISIBLE
                    val res = MainRepo.getProprietaRepository().eliminaProprieta(idProprieta!!)
                    binding.progressBar.visibility = View.GONE
                    res.fold(
                        onSuccess = {
                            Toast.makeText(this@DettaglioProprieta, getString(R.string.deleted), Toast.LENGTH_LONG)
                                .show()
                            rifiutaPrenotazioni(idProprieta!!) // rifiuta le prenotazioni non ancora accettate
                            finish()
                        },
                        onFailure = { e ->
                            mostraErrore(e.message ?: getString(R.string.unknown_error))
                        }
                    )
                }
            }.show()
    }

    private fun gestionePrenotazione(pl: Map<String, Any>) {
        val prenotazione = Prenotazioni(UUID.randomUUID().toString(), locatore?.id!!, MainRepo.getUserRepository().getCurrentUser()!!.id!!, idProprieta!!, pl["dataInizio"] as LocalDate, pl["dataFine"] as LocalDate, pl["ospiti"] as Int, null, null, pl["totale"] as Double, pl["valuta"] as Currency, dataPrenotazione = Instant.now())

        binding.progressBar.visibility = View.VISIBLE
        binding.btnDestro.isEnabled = false
        binding.btnSinistro.isEnabled = false

        lifecycleScope.launch {
            val risultato = MainRepo.getPrenotazioniRepository().aggiungiPrenotazione(prenotazione)
            risultato.fold(
                onSuccess = {
                    Toast.makeText(this@DettaglioProprieta, getString(R.string.prenotazione_effettuata), Toast.LENGTH_LONG).show()
                },
                onFailure = { e ->
                    mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
            binding.progressBar.visibility = View.GONE
            binding.btnDestro.isEnabled = true
            binding.btnSinistro.isEnabled = true
        }
    }

    private suspend fun rifiutaPrenotazioni(idProprieta: String)  {
        val prenotazioni = MainRepo.getPrenotazioniRepository().getPrenotazioni(idProprieta)
        for (p in prenotazioni.getOrThrow()) {
            if (!DateValidator.isBeforeToday(p.dataInizio))
                MainRepo.getPrenotazioniRepository().cambiaStatoPrenotazione(p, StatoPrenotazione.RIFIUTATA)
        }
    }
}
