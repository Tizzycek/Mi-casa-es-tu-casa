package it.tizzycek.micasaestucasa.ui.utente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.recensioni.Segnalazione
import it.tizzycek.micasaestucasa.data.recensioni.Statistiche
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.databinding.ActivityRecensioniBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.RecensioniAdapter
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.launch

class RecensioniActivity: AppCompatActivity() {
    private lateinit var binding: ActivityRecensioniBinding
    private lateinit var adapter: RecensioniAdapter
    private var statistiche: Statistiche? = null

    private lateinit var utente: Utente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecensioniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSpinner()
    }


    override fun onResume() {
        super.onResume()
        loadData(false)
    }

    private fun setupRecyclerView() {
        adapter = RecensioniAdapter(statistiche?.listaRecensioni) { idAutoreCliccato ->
            val intent = Intent(this, RecensioniActivity::class.java)

            intent.putExtra("idUtente", idAutoreCliccato)

            startActivity(intent)
        }
        binding.recensioniRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.recensioniRecyclerView.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData(true)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadData(reload: Boolean) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {

            try {
                val idUtente = intent.getStringExtra("idUtente")
                utente = if (idUtente != null) {
                    MainRepo.getUserRepository().beccaDatiUtente(idUtente)!!
                } else {
                    MainRepo.getUserRepository().getCurrentUser()!!
                }

                if (utente.id != MainRepo.getUserRepository().getCurrentUser()!!.id) {
                    binding.btnSegnala.visibility = View.VISIBLE

                    binding.btnSegnala.setOnClickListener {
                        val sheet = SegnalazioneBottomSheet { segnalazione ->
                            newSegnalazione(segnalazione)
                        }
                        sheet.show(supportFragmentManager, "SegnalazioneBottomSheet")
                    }
                } else {
                    binding.btnSegnala.visibility = View.GONE
                }


                binding.avatarInitialsTextView.text = utente.nome[0].toString() + utente.cognome[0].toString()
                binding.nomeCognomeTextView.text = utente.nome + " " + utente.cognome

                val resultList = if (reload) MainRepo.getStatisticheRepository().reloadStatistiche(utente.id!!) else MainRepo.getStatisticheRepository().getStatistiche(utente.id!!)
                resultList.fold(
                    onSuccess = { stat ->
                        statistiche = stat
                        updateStatisticheUI()
                    }, onFailure = { e ->
                        mostraErrore(e.message ?: getString(R.string.unknown_error))
                    }
                )
            } catch (e: Exception) {
                mostraErrore(e.message ?: getString(R.string.unknown_error))
            }

            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updateStatisticheUI() {
        statistiche?.let { s ->
            binding.mediaRatingBar.rating = s.mediaRecensioni.toFloat()
            binding.mediaValoreTextView.text = String.format("%.1f/5", statistiche?.mediaRecensioni)
            binding.numeroRecensioniTextView.text = getString(R.string.numero_recensioni, s.totaleRecensioni)

            statistiche?.let {
                if (it.mediaRecensioni >= 4.5 && it.totaleRecensioni >= 10)
                    binding.badgeImageView.visibility = View.VISIBLE
            }

            // Notifica l'adapter con la nuova lista
            adapter.updateData(s.listaRecensioni)
        }
    }


    private fun setupSpinner() {
        val ordinamenti = resources.getStringArray(R.array.ordinamenti_recensioni)
        val arrAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ordinamenti)

        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOrdinamento.adapter = arrAdapter

        binding.spinnerOrdinamento.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val stats = statistiche ?: return
                when (position) {
                    0 -> {
                        stats.listaRecensioni = statistiche?.listaRecensioni!!.sortedByDescending { r -> r.dataCreazione }
                        adapter.notifyDataSetChanged()
                        binding.recensioniRecyclerView.scrollToPosition(0)
                    }
                    1 -> {
                        stats.listaRecensioni = statistiche?.listaRecensioni!!.sortedByDescending { r -> r.voto }
                        adapter.notifyDataSetChanged()
                    }
                    2 -> {
                        stats.listaRecensioni = statistiche?.listaRecensioni!!.sortedBy { r -> r.voto }
                        adapter.notifyDataSetChanged()
                    }
                }
                this@RecensioniActivity.adapter.updateData(stats.listaRecensioni)
                binding.recensioniRecyclerView.scrollToPosition(0)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                val stats = statistiche ?: return
                stats.listaRecensioni = stats.listaRecensioni.sortedByDescending { it.dataCreazione }
                this@RecensioniActivity.adapter.updateData(stats.listaRecensioni)
            }
        }
    }

    private fun newSegnalazione(segnalazione: Segnalazione) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val statistica = MainRepo.getStatisticheRepository().getStatistiche(utente.id!!)

            statistica.fold(
                onSuccess = { s ->
                    s.aggiungiSegnalazione(segnalazione)
                    val risultato = MainRepo.getStatisticheRepository().aggiornaStatistiche(utente.id!!, s)
                    risultato.fold(
                        onSuccess = {
                            Toast.makeText(this@RecensioniActivity, getString(R.string.segnalazione_inviata), Toast.LENGTH_LONG).show()
                        },
                        onFailure = { e ->
                            mostraErrore(e.message ?: getString(R.string.unknown_error))
                            s.rimuoviSegnalazione(segnalazione)
                        }
                    )
                },
                onFailure = { e ->
                    mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
            binding.progressBar.visibility = View.GONE
        }
    }
}