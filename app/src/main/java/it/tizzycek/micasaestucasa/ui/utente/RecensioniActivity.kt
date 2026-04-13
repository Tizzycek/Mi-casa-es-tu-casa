package it.tizzycek.micasaestucasa.ui.utente

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.recensioni.Recensione
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.databinding.ActivityRecensioniBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.PrenotazioniAdapter
import it.tizzycek.micasaestucasa.ui.adapters.RecensioniAdapter
import kotlinx.coroutines.launch

class RecensioniActivity: AppCompatActivity() {
    private lateinit var binding: ActivityRecensioniBinding
    private lateinit var adapter: RecensioniAdapter
    private lateinit var listaRecensioni: List<Recensione>

    private lateinit var utente: Utente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecensioniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUserInfo()
        setupSpinner()
        setupRecyclerView()
    }


    private fun setupRecyclerView() {
        adapter = RecensioniAdapter(listaRecensioni)
        binding.recensioniRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        setUserInfo()
    }

    private fun setUserInfo() {
        loadUser()

        binding.avatarInitialsTextView.text = utente.nome[0].toString() + utente.cognome[0].toString()
        binding.nomeCognomeTextView.text = utente.nome + " " + utente.cognome
        binding.mediaRatingBar.rating = utente.statistiche.mediaRecensioni.toFloat()
        binding.mediaValoreTextView.text = utente.statistiche.mediaRecensioni.toString() + "/5"
        binding.numeroRecensioniTextView.text = getString(R.string.numero_recensioni, utente.statistiche.totaleRecensioni)
    }

    private fun loadUser() {
        val idUtente = intent.getStringExtra("idUtente")
        if (idUtente != null) {
            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                utente = MainRepo.getUserRepository().beccaDatiUtente(idUtente)!!

                binding.progressBar.visibility = View.GONE
            }
        } else {
            utente = MainRepo.getUserRepository().getCurrentUser()!!
        }

        listaRecensioni = utente.statistiche.listaRecensioni
    }

    private fun setupSpinner() {
        val ordinamenti = resources.getStringArray(R.array.ordinamenti_recensioni)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ordinamenti)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOrdinamento.adapter = adapter

        binding.spinnerOrdinamento.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        listaRecensioni = listaRecensioni.sortedByDescending { it.dataCreazione }
                        adapter.notifyDataSetChanged()
                        binding.recensioniRecyclerView.scrollToPosition(0)
                    }
                    1 -> {
                        listaRecensioni = listaRecensioni.sortedByDescending { it.voto }
                        adapter.notifyDataSetChanged()
                    }
                    2 -> {
                        listaRecensioni = listaRecensioni.sortedBy { it.voto }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                listaRecensioni = listaRecensioni.sortedByDescending { it.dataCreazione }
                adapter.notifyDataSetChanged()
            }
        }
    }
}