package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import it.tizzycek.micasaestucasa.databinding.LayoutPrenotazioneBinding
import it.tizzycek.micasaestucasa.utils.mostraErrore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Currency

class PrenotazioneSheet(val listaDisponibilita: List<Disponibilita>, val ospiti: Int, val onPrenotazioneApplicata: (Map<String, Any>) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutPrenotazioneBinding

    private var guestCount = 1
    private var dataInizio: LocalDate? = null
    private var dataFine: LocalDate? = null

    private var totale: Double? = 0.0

    private var currency: Currency? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutPrenotazioneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnAddGuest.setOnClickListener {
            if (guestCount < ospiti) {
                guestCount++
                binding.tvGuestNumber.text = guestCount.toString()
            }
        }

        binding.btnRemoveGuest.setOnClickListener {
            if (guestCount > 1) {
                guestCount--
                binding.tvGuestNumber.text = guestCount.toString()
            }
        }

        binding.cardSelectDates.setOnClickListener {
            setupDatePicker()
        }

        binding.btnCloseBooking.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmBooking.setOnClickListener {
            val inizio = dataInizio
            val fine = dataFine
            val tot = totale
            val valuta = currency

            if (inizio == null || fine == null || tot == null || valuta == null) {
                binding.root.mostraErrore(getString(R.string.campo_obbligatorio))
                return@setOnClickListener
            }

            val a = mapOf(
                "dataInizio" to inizio,
                "dataFine" to fine,
                "ospiti" to guestCount,
                "totale" to tot,
                "valuta" to valuta
            )
            onPrenotazioneApplicata(a)
            dismiss()
        }
    }

    // ── Date picker ─────────────────────────────────────────────────────────

    private fun setupDatePicker() {
        // Guard: nothing to pick if there are no availability windows
        if (listaDisponibilita.isEmpty()) {
            requireActivity().mostraErrore(R.string.nessuna_disponibilita)
            return
        }

        // Limit calendar scrolling to the span of actual availability windows
        val startMs = listaDisponibilita.minOf { it.dataInizio }
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val endMs = listaDisponibilita.maxOf { it.dataFine }
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val constraints = CalendarConstraints.Builder()
            .setStart(startMs)
            .setEnd(endMs)
            .setValidator(DisponibilitaValidator(listaDisponibilita))
            .build()

        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.seleziona_date))
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            // selection.first / second are UTC-midnight milliseconds
            val inizio = Instant.ofEpochMilli(selection.first)
                .atZone(ZoneOffset.UTC).toLocalDate()
            val fine = Instant.ofEpochMilli(selection.second)
                .atZone(ZoneOffset.UTC).toLocalDate()

            // Guard: the picker validates individual dates but not the whole range,
            // so a selection spanning a gap between two windows must be rejected here
            if (trovaPeriodoCorrispondente(inizio, fine) == null) {
                binding.root.mostraErrore(getString(R.string.range_non_valido))
                return@addOnPositiveButtonClickListener
            }

            dataInizio = inizio
            dataFine = fine
            aggiornaUI()
        }

        picker.show(parentFragmentManager, "DateRangePicker")
    }

    // ── UI helpers ───────────────────────────────────────────────────────────

    private fun aggiornaUI() {
        val inizio = dataInizio ?: return
        val fine = dataFine ?: return

        // Show the selected range on the card
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        binding.tvBookingDateRange.text =
            "${inizio.format(formatter)}  –  ${fine.format(formatter)}"

        // Calculate cost
        val notti = ChronoUnit.DAYS.between(inizio, fine)
        val disp = trovaPeriodoCorrispondente(inizio, fine)

        if (disp != null && notti > 0) {
            totale = disp.prezzo * notti
            currency = disp.valuta
            val simbolo = disp.valuta.symbol
            binding.tvSubtotal.text = "$simbolo ${"%.2f".format(totale)}"
            binding.tvTotalAmount.text = "$simbolo ${"%.2f".format(totale)}"
        } else {
            binding.tvSubtotal.text = getString(R.string.non_disponibile)
            binding.tvTotalAmount.text = getString(R.string.non_disponibile)
        }
    }

    /**
     * Returns the first [Disponibilita] whose window fully contains [inizio]..[fine],
     * or `null` if no such window exists.
     */
    private fun trovaPeriodoCorrispondente(
        inizio: LocalDate,
        fine: LocalDate
    ): Disponibilita? = listaDisponibilita.firstOrNull { disp ->
        disp.contiene(inizio) && disp.contiene(fine)
    }
}
