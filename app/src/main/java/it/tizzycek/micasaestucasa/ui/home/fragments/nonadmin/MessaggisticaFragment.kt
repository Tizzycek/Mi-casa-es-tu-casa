package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.messaggi.ChatPreview
import it.tizzycek.micasaestucasa.databinding.FragmentMessaggisticaBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.ChatPreviewAdapter
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MessaggisticaFragment: Fragment() {
    private val utente = MainRepo.getUserRepository().getCurrentUser()

    private lateinit var listaAnteprime: List<ChatPreview>

    private lateinit var messagePreviewAdapter: ChatPreviewAdapter

    private lateinit var binding: FragmentMessaggisticaBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_messaggistica, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMessaggisticaBinding.bind(view)

        setupRecyclerView()


        lifecycleScope.launch {
            binding.loadingProgressBar.visibility = View.VISIBLE
            val a = MainRepo.getChatRepository().getChatListFlow(utente!!.id!!)
            a.catch { e ->
                requireActivity().mostraErrore(e.message ?: getString(R.string.unknown_error))
            }.collect { lista ->
                messagePreviewAdapter.aggiornaLista(lista)
                listaAnteprime = lista
            }
        }
        binding.loadingProgressBar.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        messagePreviewAdapter = ChatPreviewAdapter(emptyList())
        val layoutManager = LinearLayoutManager(requireContext())

        layoutManager.stackFromEnd = false

        binding.messagesRecyclerView.layoutManager = layoutManager
        binding.messagesRecyclerView.adapter = messagePreviewAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}