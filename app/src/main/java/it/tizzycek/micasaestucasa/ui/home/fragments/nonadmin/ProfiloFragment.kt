package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.databinding.FragmentProfiloBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.utente.LoginActivity
import it.tizzycek.micasaestucasa.ui.utente.ModificaProfilo
import it.tizzycek.micasaestucasa.ui.utente.RecensioniActivity

class ProfiloFragment: Fragment() {
    private lateinit var utente: Utente
    private lateinit var binding: FragmentProfiloBinding

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
        binding.mediaRatingBar.rating = utente.statistiche.mediaRecensioni.toFloat()
        binding.mediaValoreTextView.text = utente.statistiche.mediaRecensioni.toString() + "/5"
        binding.numeroRecensioniTextView.text = getString(R.string.numero_recensioni, utente.statistiche.totaleRecensioni)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}