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
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.databinding.ActivityDettaglioProprietaBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.DisponibilitaAdapter
import it.tizzycek.micasaestucasa.ui.adapters.ImageDetailAdapter
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa.AggiungiModificaProprieta
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa.PrenotazioneSheet
import it.tizzycek.micasaestucasa.utils.mostraErrore
import it.tizzycek.micasaestucasa.utils.paroleChiaveProprieta
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class DettaglioProprieta: AppCompatActivity() {
    private lateinit var binding: ActivityDettaglioProprietaBinding
    private var idProprieta: String? = null
    private var proprietarioId: String = ""
    private var listaDisponibilita: List<Disponibilita> = emptyList()
    private var numeroOspiti: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDettaglioProprietaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idProprieta = intent.getStringExtra("PROPRIETA_ID")

        if (idProprieta != null) {
            caricaDatiPropieta(idProprieta!!)
        } else {
            finish()
        }
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        if (idProprieta != null) {
            caricaDatiPropieta(idProprieta!!)
        } else {
            finish()
        }
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
                        Toast.makeText(this@DettaglioProprieta, getString(R.string.unknown_error), Toast.LENGTH_LONG)
                            .show()
                },
                onFailure = { e ->
                    Toast.makeText(this@DettaglioProprieta, "${e.message}", Toast.LENGTH_LONG).show()
                }
            )
            binding.progressBar.visibility = View.GONE
        }
    }

    private suspend fun mostraDati(p: Proprieta) {
        binding.tvNome.text = p.nome
        proprietarioId = p.proprietarioId
        numeroOspiti = p.posti

        // TODO: rendere il profilo cliccabile
        if (MainRepo.getUserRepository().getCurrentUser()!!.tipo != TipoUtente.PROPRIETARIO) {
            val locatore = MainRepo.getUserRepository().beccaDatiUtente(p.proprietarioId)
            binding.tvLocatore.text = locatore!!.nome + " " + locatore!!.cognome
        } else {
            binding.tvLocatore.text = getString(R.string.tu)
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
                Toast.makeText(this, "Contatta l'host", Toast.LENGTH_SHORT).show()
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
        val prenotazione = Prenotazioni(UUID.randomUUID().toString(), proprietarioId, MainRepo.getUserRepository().getCurrentUser()!!.id!!, pl["dataInizio"] as LocalDate, pl["dataFine"] as LocalDate, pl["ospiti"] as Int)

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val risultato = MainRepo.getPrenotazioniRepository().aggiungiPrenotazione(prenotazione)
            binding.progressBar.visibility = View.GONE
            risultato.fold(
                onSuccess = {
                    Toast.makeText(this@DettaglioProprieta, getString(R.string.prenotazione_effettuata), Toast.LENGTH_LONG).show()
                },
                onFailure = { e ->
                    mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
        }
    }
}
