package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.prenotazioni.Prenotazioni
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.databinding.FragmentPrenotazioniBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.PrenotazioniAdapter
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.launch

class PrenotazioniFragment: Fragment() {

    private lateinit var binding: FragmentPrenotazioniBinding

    private var listaPrenotazioni: List<Prenotazioni> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPrenotazioniBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE

        val utente = MainRepo.getUserRepository().getCurrentUser()!!

        lifecycleScope.launch {
            val risultato = if (utente.tipo == TipoUtente.PROPRIETARIO)
                MainRepo.getPrenotazioniRepository().getPrenotazioni(utente.id!!, "proprietarioId")
            else
                MainRepo.getPrenotazioniRepository().getPrenotazioni(utente.id!!, "affittuarioId")

            risultato.fold(
                onSuccess = {
                    listaPrenotazioni = it.sortedByDescending { prenotazioni ->
                        prenotazioni.dataPrenotazione
                    }

                    val adapter = PrenotazioniAdapter(listaPrenotazioni, viewLifecycleOwner, utente.tipo)
                    binding.rvPrenotazioni.adapter = adapter
                },
                onFailure = { e ->
                    requireActivity().mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}