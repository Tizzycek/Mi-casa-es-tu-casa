package it.tizzycek.micasaestucasa.ui.utente

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.recensioni.Segnalazione
import it.tizzycek.micasaestucasa.data.recensioni.SegnalazioneMotivazione
import it.tizzycek.micasaestucasa.databinding.ScriviSegnalazioneBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.utils.mostraErrore
import java.util.UUID

class SegnalazioneBottomSheet(val onSegnalazione: (Segnalazione) -> Unit): BottomSheetDialogFragment() {
    private lateinit var binding: ScriviSegnalazioneBinding
    private var motivazioneSelezionata: SegnalazioneMotivazione? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ScriviSegnalazioneBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRadioLogic()

        binding.btnAnnulla.setOnClickListener {
            dismiss()
        }

        binding.btnInviaSegnalazione.setOnClickListener {
            inviaSegnalazione()
        }
    }

    private fun setupRadioLogic() {
        val setupMap = mapOf(
            binding.cardContenutoInappropriato to (binding.radioContenutoInappropriato to SegnalazioneMotivazione.CONTENUTO_INAPPROPRIATO),
            binding.cardInfoFalse to (binding.radioInfoFalse to SegnalazioneMotivazione.INFORMAZIONI_FALSE),
            binding.cardComportamento to (binding.radioComportamentoScorretto to SegnalazioneMotivazione.COMPORTAMENTO_SCORRETTO),
            binding.cardSpam to (binding.radioSpam to SegnalazioneMotivazione.SPAM),
            binding.cardAltro to (binding.radioAltro to SegnalazioneMotivazione.ALTRO)
        )

        setupMap.forEach { (card, pair) ->
            val (radio, motivazione) = pair
            val clickListener = View.OnClickListener {
                selezionaOpzione(motivazione, radio, setupMap.keys)
            }

            card.setOnClickListener(clickListener)
            radio.setOnClickListener(clickListener)
        }
    }

    private fun selezionaOpzione(motivazione: SegnalazioneMotivazione, radioButton: RadioButton, cards: Set<MaterialCardView>) {
        motivazioneSelezionata = motivazione
        // reset di tutte le card e radio button
        cards.forEach { card ->
            card.strokeColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
            card.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics).toInt()

            findRadioButtonInCard(card)?.isChecked = false
        }

        // selezione di una
        radioButton.isChecked = true
        val cardSelezionata = radioButton.parent.parent as MaterialCardView
        cardSelezionata.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary)
        cardSelezionata.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()

        if (motivazione.richiedeDettagli) {
            binding.dettagliEditText.requestFocus()
        }

        binding.btnInviaSegnalazione.isEnabled = true
    }

    private fun findRadioButtonInCard(card: MaterialCardView): RadioButton? {
        return when(card.id) {
            R.id.cardContenutoInappropriato -> binding.radioContenutoInappropriato
            R.id.cardInfoFalse -> binding.radioInfoFalse
            R.id.cardComportamento -> binding.radioComportamentoScorretto
            R.id.cardSpam -> binding.radioSpam
            R.id.cardAltro -> binding.radioAltro
            else -> null
        }
    }

    private fun inviaSegnalazione() {
        val dettagli = binding.dettagliEditText.text.toString()

        if (motivazioneSelezionata == null || (motivazioneSelezionata!!.richiedeDettagli && dettagli.isEmpty())) {
            binding.root.mostraErrore(getString(R.string.data_missing))
        } else {
            val segnalazione = Segnalazione(UUID.randomUUID().toString(), MainRepo.getUserRepository().getCurrentUser()!!.id!!, dettagli, motivazioneSelezionata!!)
            onSegnalazione(segnalazione)
            dismiss()
        }
    }
}