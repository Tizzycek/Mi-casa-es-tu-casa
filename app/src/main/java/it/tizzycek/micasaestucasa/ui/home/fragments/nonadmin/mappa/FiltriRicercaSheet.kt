package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.parolechiave.ParoleChiave
import it.tizzycek.micasaestucasa.databinding.LayoutFiltriRicercaBinding
import it.tizzycek.micasaestucasa.utils.setupParoleChiave
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FiltriRicercaSheet(val onFiltriApplicati: (Map<String, Any?>) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutFiltriRicercaBinding

    private var guestCount = 1
    private var dataInizio: LocalDate? = null
    private var dataFine: LocalDate? = null

    private var paroleSelezionatePerRicerca = listOf<String>()
    private var paroleChiave = listOf<Chip>()

    // l'unica decente
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        binding = LayoutFiltriRicercaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rangeSliderPrice.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            val min = values[0].toInt()
            val max = values[1].toInt()

            binding.tvMinPrice.text = "$min"
            binding.tvMaxPrice.text = "$max"
        }

//        binding.btnPlusGuests.setOnClickListener {
//            guestCount++
//            binding.tvGuestCount.text = guestCount.toString()
//        }
//        binding.btnMinusGuests.setOnClickListener {
//            if (guestCount > 1) {
//                guestCount--
//                binding.tvGuestCount.text = guestCount.toString()
//            }
//        }

        binding.cardDateRange.setOnClickListener {
            setupDatePicker()
        }


        binding.cgKeywords.setupParoleChiave(lifecycleScope) { ids ->
            paroleChiave = ids
            paroleSelezionatePerRicerca = ids.map { it.text.toString() }
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnShowResults.setOnClickListener {
            val filtri = mapOf(
                "dal" to dataInizio,
                "al" to dataFine,
                "posti" to binding.etGuestCount.text?.toString()?.toIntOrNull(),
                "prezzoMin" to binding.tvMinPrice.text?.toString()?.toDoubleOrNull(),
                "prezzoMax" to binding.tvMaxPrice.text?.toString()?.toDoubleOrNull(),
                "paroleChiaveIds" to paroleChiave.mapNotNull { chip ->
                    when (val tag = chip.tag) {
                        is ParoleChiave -> tag.id
                        else -> null
                    }
                },
                "indirizzo" to binding.etLocation.text?.toString()
            )
            onFiltriApplicati(filtri)
            dismiss()
        }

        binding.btnClearLocation.setOnClickListener {
            binding.etLocation.setText("")
        }

        binding.btnResetFilters.setOnClickListener {
            binding.etGuestCount.setText("1")
            binding.tvDateRange.text = getString(R.string.seleziona_date)
            binding.tvMinPrice.text = "0"
            binding.tvMaxPrice.text = "500"
            binding.rangeSliderPrice.values = listOf(0f, 500f)
            binding.cgKeywords.clearCheck()
            paroleSelezionatePerRicerca = emptyList()
            binding.etLocation.setText("")
        }
    }

    private fun setupDatePicker() {
        binding.cardDateRange.setOnClickListener {
            binding.cardDateRange.isEnabled = false
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            builder.setTitleText(getString(R.string.seleziona_date))

            val picker = builder.build()
            picker.show(childFragmentManager, "range_picker")

            picker.addOnPositiveButtonClickListener { selection ->

                val inizioMillis = selection?.first
                val fineMillis = selection?.second

                inizioMillis?.let {
                    dataInizio = Instant.ofEpochMilli(inizioMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                }

                fineMillis?.let {
                    dataFine = Instant.ofEpochMilli(fineMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                }


                binding.tvDateRange.text = "$dataInizio - $dataFine"
                binding.cardDateRange.isEnabled = true
            }
        }
    }
}