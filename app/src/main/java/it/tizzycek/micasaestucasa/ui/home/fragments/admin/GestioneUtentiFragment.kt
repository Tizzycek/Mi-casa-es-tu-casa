package it.tizzycek.micasaestucasa.ui.home.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.databinding.FragmentGestioneUtentiBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.GestioneUtentiAdapter
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.launch

class GestioneUtentiFragment: Fragment() {
    private lateinit var binding: FragmentGestioneUtentiBinding
    private lateinit var adapter: GestioneUtentiAdapter
    private var filtroSegnalati = false
    private var filtroSospesi = false
    private var filtroBanditi = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gestione_utenti, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGestioneUtentiBinding.bind(view)

        setupRecyclerView()
        setupFilters()
    }

    private fun setupRecyclerView() {
        adapter = GestioneUtentiAdapter(MainRepo.getUserRepository().getUserPaging(this, filtroSegnalati, filtroSospesi, filtroBanditi), parentFragmentManager) { id ->
            setReportedFalse(id)
        }

        binding.usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.usersRecyclerView.adapter = adapter
    }

    private fun setReportedFalse(id: String?) {
        lifecycleScope.launch {
            val result = MainRepo.getUserRepository().setReport(id, false)
            if (result.isFailure) {
                requireActivity().mostraErrore(
                    result.exceptionOrNull()?.message ?: getString(R.string.unknown_error)
                )
            }
        }
    }

    // TODO i filtri non funzionano
    private fun setupFilters() {
        binding.chipReported.setOnCheckedChangeListener { _, isChecked ->
            filtroSegnalati = isChecked
            aggiornaFiltri()
        }

        binding.chipSuspended.setOnCheckedChangeListener { _, isChecked ->
            filtroSospesi = isChecked
            aggiornaFiltri()
        }

        binding.chipBanned.setOnCheckedChangeListener { _, isChecked ->
            filtroBanditi = isChecked
            aggiornaFiltri()
        }
    }

    private fun aggiornaFiltri() {
        // Ricrea le opzioni con i nuovi filtri
        val nuoveOpzioni = MainRepo.getUserRepository().getUserPaging(this, filtroSegnalati, filtroSospesi, filtroBanditi)

        // Aggiorna l'adapter con le nuove opzioni
        adapter.updateOptions(nuoveOpzioni)
    }
}