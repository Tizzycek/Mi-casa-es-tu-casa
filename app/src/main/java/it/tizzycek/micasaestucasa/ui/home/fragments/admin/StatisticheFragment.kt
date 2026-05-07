package it.tizzycek.micasaestucasa.ui.home.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.databinding.FragmentStatisticheBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.launch

class StatisticheFragment: Fragment() {
    private lateinit var binding: FragmentStatisticheBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistiche, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentStatisticheBinding.bind(view)

        loadStatistics()
    }

    private fun loadStatistics() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = MainRepo.getPrenotazioniRepository().loadStatistics()
            result.fold(
                onSuccess = { m ->
                    binding.totalBookingsText.text = m["totali"].toString()
                    binding.pendingBookingsText.text = m["inAttesa"].toString()
                    binding.acceptedBookingsText.text = m["accettate"].toString()
                    binding.rejectedBookingsText.text = m["rifiutate"].toString()
                    binding.cancelledBookingsText.text = m["annullate"].toString()
                },
                onFailure = { e ->
                    requireActivity().mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
            binding.progressBar.visibility = View.GONE
        }
    }
}