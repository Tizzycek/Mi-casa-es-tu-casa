package it.tizzycek.micasaestucasa.ui.utente

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.messaggi.Messaggio
import it.tizzycek.micasaestucasa.databinding.ActivityChatBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.MessageAdapter
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.BaseImageManagerActivity
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChatActivity: BaseImageManagerActivity() {
    private lateinit var chatId: String
    private lateinit var infoUtente: Map<String, String?>
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatId = intent.getStringExtra("CHAT_ID") ?: ""
        infoUtente = mapOf(
            "destinatarioId" to intent.getStringExtra("destinatarioId"),
            "nomeDestinatario" to intent.getStringExtra("nomeDestinatario"),
            "cognomeDestinatario" to intent.getStringExtra("cognomeDestinatario")
        )

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        super.setupImageRecyclerView(binding.imagePreviewRecyclerView, null)

        setDetails()
        setupRecyclerView()

        binding.sendButton.setOnClickListener {
            inviaMessaggio()
        }

        val pickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            addImages(uris)
        }

        binding.attachButton.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        observeMessages()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(emptyList())
        val layoutManager = LinearLayoutManager(this)

        layoutManager.stackFromEnd = true

        binding.messagesRecyclerView.layoutManager = layoutManager
        binding.messagesRecyclerView.adapter = messageAdapter
    }

    private fun setDetails() {
        binding.toolbarContactName.text = infoUtente["nomeDestinatario"] + " " + infoUtente["cognomeDestinatario"]
        binding.toolbarAvatarInitials.text = infoUtente["nomeDestinatario"]?.get(0).toString() + infoUtente["cognomeDestinatario"]?.get(0).toString()

        binding.toolbarContactName.setOnClickListener {
            val intent = Intent(this, RecensioniActivity::class.java)
            intent.putExtra("idUtente", infoUtente["destinatarioId"])
            startActivity(intent)
        }
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            MainRepo.getChatRepository().getMessagesFlow(chatId).catch { e ->
                mostraErrore(e.message ?: getString(R.string.unknown_error))
            }.collect { listaMessaggi ->
                messageAdapter.aggiornaLista(listaMessaggi)

                if (listaMessaggi.isNotEmpty()) {
                    binding.messagesRecyclerView.scrollToPosition(listaMessaggi.size - 1)
                }
            }
        }
    }

    private fun inviaMessaggio() {
        val messaggio = binding.messageInputEditText.text.toString().trim()
        binding.messageInputEditText.text?.clear()

        binding.sendButton.isEnabled = false
        binding.sendButton.visibility = View.GONE
        binding.sendProgressBar.visibility = View.VISIBLE

        val mittente = MainRepo.getUserRepository().getCurrentUser()!!
        val destinatarioId = infoUtente["destinatarioId"] ?: ""

        val daInviare = Messaggio(mittente.id!!, destinatarioId, messaggio)

        val infoPreview = hashMapOf(
            mittente.id!! to mapOf("nome" to mittente.nome, "cognome" to mittente.cognome),
            destinatarioId to mapOf("nome" to infoUtente["nomeDestinatario"]!!, "cognome" to infoUtente["cognomeDestinatario"]!!)
        )

        val (immaginiDaCaricare, urlsGiaPresenti) = super.separateImages()

        if (immaginiDaCaricare.isEmpty() && daInviare.contenuto.isBlank()) {
            mostraErrore("Impossibile inviare un messaggio vuoto")
        } else {
            lifecycleScope.launch {
                val risultato = MainRepo.getChatRepository().sendMessage(chatId, daInviare, immaginiDaCaricare, infoPreview)
                risultato.fold(
                    onSuccess = {
                        super.clearImages()
                    }, onFailure = { e ->
                        mostraErrore(e.message ?: getString(R.string.unknown_error))
                        binding.messageInputEditText.setText(messaggio)
                    }
                )
            }
        }
        binding.sendButton.isEnabled = true
        binding.sendProgressBar.visibility = View.GONE
        binding.sendButton.visibility = View.VISIBLE
    }
}