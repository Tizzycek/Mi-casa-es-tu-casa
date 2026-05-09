package it.tizzycek.micasaestucasa.ui.home.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.recensioni.Statistiche
import it.tizzycek.micasaestucasa.data.utente.Stato
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.databinding.BottomSheetUserActionBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.ReportReasonAdapter
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.launch
import java.time.Instant

class GestioneUtentiBottomSheet(val utente: Utente, val changeStatus: (Stato) -> Unit): BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetUserActionBinding
    private lateinit var statistiche: Statistiche
    private lateinit var adapter: ReportReasonAdapter

    private val durationValuesMillis = listOf(
        6 * 60 * 60 * 1000L,        // 6 ore
        12 * 60 * 60 * 1000L,       // 12 ore
        24 * 60 * 60 * 1000L,       // 24 ore
        2 * 24 * 60 * 60 * 1000L,   // 2 gg
        7 * 24 * 60 * 60 * 1000L,   // 7 gg
        30 * 24 * 60 * 60 * 1000L,  // 30 gg
        90 * 24 * 60 * 60 * 1000L   // 90 gg
    )

    private var selectedPosition = 2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetUserActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        caricaStatistiche()

        binding.bottomSheetUsername.text = utente.nome + " " + utente.cognome
        binding.bottomSheetUserEmail.text = utente.email
        binding.avatarInitialsTextView.text = utente.nome[0].toString() + utente.cognome[0].toString()

        setupButtons()
        setupSuspensionDurationDropdown()
    }

    private fun setupRecyclerView() {
        adapter = ReportReasonAdapter(emptyList())

        binding.reportReasonsRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        binding.reportReasonsRecyclerView.adapter = adapter
    }

    private fun caricaStatistiche() {
        viewLifecycleOwner.lifecycleScope.launch {
            val risultato = MainRepo.getStatisticheRepository().getStatistiche(utente.id!!)
            risultato.fold(
                onSuccess = { s ->
                    statistiche = s
                    adapter.updateData(s.listaSegnalazioni)

                    if (s.listaSegnalazioni.isNotEmpty()) {
                        binding.reportReasonsContainer.visibility = View.VISIBLE
                        binding.noReportsCard.visibility = View.GONE
                    } else {
                        binding.reportReasonsContainer.visibility = View.GONE
                        binding.noReportsCard.visibility = View.VISIBLE
                    }

                    binding.badgeImageView.visibility = if (s.mediaRecensioni >= 4.5 && s.totaleRecensioni >= 10) View.VISIBLE else View.GONE
                },
                onFailure = { e ->
                    binding.root.mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
        }
    }

    private fun setupButtons() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        if (utente.stato == Stato.OK) {
            binding.blockButton.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch {
                    val risultato = MainRepo.getUserRepository().changeStatus(utente.id, Stato.BANDITO)
                    risultato.fold(
                        onSuccess = {
                            Toast.makeText(binding.root.context, getString(R.string.utente_bandito), Toast.LENGTH_LONG).show()
                            changeStatus(Stato.BANDITO)
                            dismiss()
                        },
                        onFailure = { e ->
                            binding.root.mostraErrore(e.message ?: getString(R.string.unknown_error))
                        }
                    )
                    binding.progressBar.visibility = View.GONE
                }
            }

            binding.suspendButton.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                val timestamp = Timestamp(Instant.ofEpochMilli(getSelectedDurationInMillis() + System.currentTimeMillis()))
                lifecycleScope.launch {
                    val risultato = MainRepo.getUserRepository().setSuspension(utente.id, timestamp)
                    risultato.fold(
                        onSuccess = {
                            val result = MainRepo.getUserRepository().changeStatus(utente.id, Stato.SOSPESO)
                            result.fold(
                                onSuccess = {
                                    Toast.makeText(binding.root.context, getString(R.string.utente_sospeso), Toast.LENGTH_LONG).show()
                                    changeStatus(Stato.SOSPESO)
                                    dismiss()
                                },
                                onFailure = { e ->
                                    binding.root.mostraErrore(e.message ?: getString(R.string.unknown_error))
                                }
                            )
                        },
                        onFailure = { e ->
                            binding.root.mostraErrore(e.message ?: getString(R.string.unknown_error))
                        }
                    )
                    binding.progressBar.visibility = View.GONE
                }
            }
        } else {
            binding.suspendButton.isEnabled = false
            binding.suspendButton.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray))
            binding.blockButton.isEnabled = false
            binding.blockButton.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray))
        }

    }

    private fun getSelectedDurationInMillis(): Long {
        return durationValuesMillis.getOrElse(selectedPosition) { 24 * 60 * 60 * 1000L }
    }

    private fun setupSuspensionDurationDropdown() {
        val context = binding.root.context
        val durationOptions = context.resources.getStringArray(R.array.suspension_durations)

        val aAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_dropdown_item_1line,
            durationOptions
        )

        binding.suspensionDurationInput.apply {
            setAdapter(aAdapter)

            setText(durationOptions[selectedPosition], false)

            setOnItemClickListener { _, _, position, _ ->
                selectedPosition = position
            }
        }
    }
}