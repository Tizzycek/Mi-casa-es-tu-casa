package it.tizzycek.micasaestucasa.ui.home.fragments.admin

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.parolechiave.ParoleChiave
import it.tizzycek.micasaestucasa.databinding.DialogKeywordFormBinding
import it.tizzycek.micasaestucasa.databinding.FragmentParoleChiaveBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.ParoleChiaveAdapter
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.launch
import java.util.UUID

class ParoleChiaveFragment: Fragment() {
    private lateinit var adapter: ParoleChiaveAdapter
    private lateinit var binding: FragmentParoleChiaveBinding
    private lateinit var lista: MutableList<ParoleChiave>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_parole_chiave, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentParoleChiaveBinding.bind(view)

        setupRecyclerView()
        getListParoleChiave()

        binding.fabAddKeyword.setOnClickListener {
            showKeywordDialog(null)
        }
    }

    private fun getListParoleChiave() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val risultato = MainRepo.getParoleChiave().getParoleChiave()
            risultato.fold(
                onSuccess = {
                    lista = it.toMutableList()
                    adapter.aggiornaLista(lista)
                },
                onFailure = { e ->
                    requireActivity().mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = ParoleChiaveAdapter(emptyList(), onEditClick = { keyword -> showKeywordDialog(keyword) },
            onDeleteClick = { keyword -> showDeleteConfirmation(keyword) },
            onStatusChanged = { keyword, isActive -> toggleKeywordStatus(keyword, isActive) }
        )

        binding.recyclerViewKeywords.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewKeywords.adapter = adapter
    }

    private fun addKeyword(text: String, isActive: Boolean) {
        val newKeyword = ParoleChiave(UUID.randomUUID().toString(), text, isActive)
        lifecycleScope.launch {
            val result = MainRepo.getParoleChiave().aggiungiParolaChiave(newKeyword)
            result.fold(
                onSuccess = {
                    lista.add(0, newKeyword) // Aggiunge all'inizio
                    adapter.aggiornaLista(lista)
                    updateEmptyState()
                    Toast.makeText(requireContext(), "Parola aggiunta correttamente", Toast.LENGTH_LONG).show()
                },
                onFailure = { e ->
                    requireActivity().mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
        }
    }

    private fun updateKeyword(keyword: ParoleChiave, newText: String, newStatus: Boolean) {
        val index = lista.indexOfFirst { it.id == keyword.id }
        if (index != -1) {
            lista[index] = keyword.copy(
                parola = newText,
                attiva = newStatus
            )

            lifecycleScope.launch {
                val risultato = MainRepo.getParoleChiave().aggiornaParolaChiave(lista[index])
                risultato.fold(
                    onSuccess = {
                        adapter.notifyItemChanged(index)
                        updateEmptyState()

                        Toast.makeText(requireContext(), "Parola aggiornata correttamente", Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        requireActivity().mostraErrore(e.message ?: getString(R.string.unknown_error))
                    }
                )
            }
        }
    }

    private fun updateEmptyState() {
        binding.tvEmptyState.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showKeywordDialog(keyword: ParoleChiave?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_keyword_form, null)

        val bindingDialog = DialogKeywordFormBinding.bind(dialogView)

        val tilKeyword = bindingDialog.tilKeyword
        val etKeyword = bindingDialog.etKeyword
        val cbActive = bindingDialog.cbActive

        val isEditMode = keyword != null

        // Pre-compila se modifica
        if (isEditMode) {
            etKeyword.setText(keyword.parola)
            cbActive.isChecked = keyword.attiva
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (isEditMode) "Modifica parola chiave" else "Nuova parola chiave")
            .setView(dialogView)
            .setPositiveButton(if (isEditMode) "Salva" else "Aggiungi") { dialogInterface, _ ->
                val text = etKeyword.text.toString().trim()

                // Validazione semplice
                when {
                    text.isEmpty() -> {
                        tilKeyword.error = "Campo obbligatorio"
                        // Non chiudere il dialog - richiede gestione aggiuntiva
                    }
                    text.length < 2 -> {
                        tilKeyword.error = "Minimo 2 caratteri"
                    }
                    else -> {
                        if (isEditMode) {
                            updateKeyword(keyword, text, cbActive.isChecked)
                        } else {
                            addKeyword(text, cbActive.isChecked)
                        }
                        dialogInterface.dismiss()
                    }
                }
            }
            .setNegativeButton("Annulla", null)
            .create()

        dialog.show()

        // Per mantenere il dialog aperto in caso di errore
        // bisogna override del listener dopo show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = etKeyword.text.toString().trim()

            when {
                text.isEmpty() -> {
                    tilKeyword.error = "Campo obbligatorio"
                }
                text.length < 2 -> {
                    tilKeyword.error = "Minimo 2 caratteri"
                }
                else -> {
                    if (isEditMode) {
                        updateKeyword(keyword, text, cbActive.isChecked)
                    } else {
                        addKeyword(text, cbActive.isChecked)
                    }
                    dialog.dismiss()
                }
            }
        }

        // Reset errori quando l'utente digita
        etKeyword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilKeyword.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showDeleteConfirmation(keyword: ParoleChiave) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Elimina parola chiave")
            .setMessage("Sei sicuro di voler eliminare \"${keyword.parola}\"?")
            .setPositiveButton("Elimina") { _, _ ->
                deleteKeyword(keyword)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun toggleKeywordStatus(keyword: ParoleChiave, isActive: Boolean) {
        val index = lista.indexOfFirst { it.id == keyword.id }
        if (index != -1) {
            lista[index] = keyword.copy(attiva = isActive)
            // Non serve aggiornare tutta la lista, solo notificare il cambiamento
            adapter.notifyItemChanged(index)
        }
    }

    private fun deleteKeyword(keyword: ParoleChiave) {
        lifecycleScope.launch {
            val risultato = MainRepo.getParoleChiave().rimuoviParolaChiave(keyword.id!!)
            risultato.fold(
                onSuccess = {
                    lista.removeAll { it.id == keyword.id }
                    adapter.aggiornaLista(lista)
                    updateEmptyState()
                    Toast.makeText(requireContext(), "Parola eliminata", Toast.LENGTH_LONG).show()
                },
                onFailure = { e ->
                    requireActivity().mostraErrore(e.message ?: getString(R.string.unknown_error))
                }
            )
        }
    }
}