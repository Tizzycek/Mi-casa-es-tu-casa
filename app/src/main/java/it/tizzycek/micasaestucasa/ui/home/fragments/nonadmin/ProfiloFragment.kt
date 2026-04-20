package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.recensioni.Statistiche
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.databinding.FragmentProfiloBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.utente.LoginActivity
import it.tizzycek.micasaestucasa.ui.utente.ModificaProfilo
import it.tizzycek.micasaestucasa.ui.utente.RecensioniActivity
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.launch

class ProfiloFragment: Fragment() {
    private lateinit var utente: Utente
    private lateinit var binding: FragmentProfiloBinding

    private lateinit var statisticheUtente: Statistiche

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profilo, container, false)
    }

    override fun onResume() {
        super.onResume()
        utente = MainRepo.getUserRepository().getCurrentUser()!!
        setFields()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfiloBinding.bind(view)
        utente = MainRepo.getUserRepository().getCurrentUser()!!

        setFields()


        binding.logoutLayout.setOnClickListener {
            MainRepo.getUserRepository().logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.modificaUtenteLayout.setOnClickListener {
            startActivity(Intent(requireContext(), ModificaProfilo::class.java))
        }

        binding.visualizzaRecensioniLayout.setOnClickListener {
            startActivity(Intent(requireContext(), RecensioniActivity::class.java))
        }

    }

    private fun setFields() {
        binding.avatarInitialsTextView.text = utente.nome[0].toString() + utente.cognome[0].toString()
        binding.nomeCognomeTextView.text = utente.nome + " " + utente.cognome

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val risultato = MainRepo.getStatisticheRepository().getStatistiche(utente.id!!)
            risultato.fold(
                onSuccess = {
                    statisticheUtente = it
                    binding.mediaRatingBar.rating = statisticheUtente.mediaRecensioni.toFloat()
                    binding.mediaValoreTextView.text = String.format("%.1f/5", statisticheUtente.mediaRecensioni)
                    binding.numeroRecensioniTextView.text = getString(R.string.numero_recensioni, statisticheUtente.totaleRecensioni)

                    if (statisticheUtente.mediaRecensioni >= 4.5 && statisticheUtente.totaleRecensioni >= 10) {
                        binding.badgeImageView.visibility = View.VISIBLE
                    }

                }, onFailure = { e ->
                    requireActivity().mostraErrore(e.message ?: getString(R.string.unknown_error))
                    binding.mediaRatingBar.rating = 0.0F
                    binding.mediaValoreTextView.text = "0/5"
                    binding.numeroRecensioniTextView.text = getString(R.string.numero_recensioni, 0)
                }
            )
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}