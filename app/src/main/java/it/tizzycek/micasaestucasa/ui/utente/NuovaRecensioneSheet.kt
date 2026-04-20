package it.tizzycek.micasaestucasa.ui.utente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.recensioni.Recensione
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.databinding.ScriviRecensioneBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.utils.mostraErrore
import java.util.UUID

class NuovaRecensioneSheet(val onRecensione: (Recensione) -> Unit): BottomSheetDialogFragment() {
    private lateinit var binding: ScriviRecensioneBinding
    private lateinit var utente: Utente

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ScriviRecensioneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        utente = MainRepo.getUserRepository().getCurrentUser()!!

        binding.nomeDestinatarioTextView.text = utente.nome + " " + utente.cognome
        binding.avatarInitialsTextView.text = utente.nome[0].toString() + utente.cognome[0].toString()

        binding.btnAnnulla.setOnClickListener {
            dismiss()
        }

        binding.btnPubblica.setOnClickListener {
            creaRecensione()
        }
    }

    private fun creaRecensione() {
        val testo = binding.testoRecensioneEditText.text.toString()
        val voto = binding.ratingBar.rating

        if (testo.isEmpty() || voto == 0f) {
            binding.root.mostraErrore(getString(R.string.data_missing))
        } else {
            val recensione = Recensione(UUID.randomUUID().toString(), utente.id!!, utente.nome + " " + utente.cognome, voto.toInt(), testo)
            onRecensione(recensione)
            dismiss()
        }
    }
}