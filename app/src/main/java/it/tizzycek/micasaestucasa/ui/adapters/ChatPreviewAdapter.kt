package it.tizzycek.micasaestucasa.ui.adapters

import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.messaggi.ChatPreview
import it.tizzycek.micasaestucasa.data.messaggi.Messaggio
import it.tizzycek.micasaestucasa.databinding.ItemMessageBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.utente.ChatActivity
import it.tizzycek.micasaestucasa.utils.DateValidator
import kotlin.collections.sorted

class ChatPreviewAdapter(private var items: List<ChatPreview>): RecyclerView.Adapter<ChatPreviewAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemMessageBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(chatPreview: ChatPreview) {
            val currentUserId = MainRepo.getUserRepository().getCurrentUser()?.id ?: ""
            val idAltroUtente = chatPreview.participants.find { it != currentUserId } ?: ""

            val infoAltroUtente = chatPreview.userData.entries.find { it.key != currentUserId }?.value
            val nome = infoAltroUtente?.get("nome") ?: "Not"
            val cognome = infoAltroUtente?.get("cognome") ?: "available"

            val messaggiNonLetti = chatPreview.unreadCounts[MainRepo.getUserRepository().getCurrentUser()!!.id] ?: 0

            binding.contactNameTextView.text = "$nome $cognome"
            binding.avatarInitialsTextView.text = "${nome[0]}${cognome[0]}"

            binding.messagePreviewTextView.text = chatPreview.lastMessage.ifBlank { binding.root.context.getString(R.string.immagine) }
            binding.timestampTextView.text = DateValidator.formatDate(chatPreview.lastUpdated)

            binding.contactNameTextView.setTypeface(null, Typeface.NORMAL)
            binding.messagePreviewTextView.setTypeface(null, Typeface.NORMAL)

            if (messaggiNonLetti > 0) {
                binding.unreadBadgeContainer.visibility = View.VISIBLE
                binding.unreadCountTextView.visibility = View.VISIBLE

                binding.unreadCountTextView.text = if (messaggiNonLetti < 100) messaggiNonLetti.toString() else "99+"

                binding.contactNameTextView.setTypeface(binding.contactNameTextView.typeface,
                    Typeface.BOLD)
                binding.messagePreviewTextView.setTypeface(binding.messagePreviewTextView.typeface, Typeface.BOLD)
            } else {
                binding.unreadBadgeContainer.visibility = View.GONE
                binding.unreadCountTextView.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                val chatId = listOf(currentUserId, idAltroUtente).sorted().joinToString("_")

                val intent = Intent(binding.root.context, ChatActivity::class.java)

                intent.putExtra("CHAT_ID", chatId)
                intent.putExtra("destinatarioId", idAltroUtente)
                intent.putExtra("nomeDestinatario", nome)
                intent.putExtra("cognomeDestinatario", cognome)

                MainRepo.getChatRepository().azzeraMessaggiNonLetti(chatId, currentUserId)

                startActivity(binding.root.context, intent, null)
            }
        }
    }

    fun aggiornaLista(nuovaLista: List<ChatPreview>) {
        this.items = nuovaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int){
        p0.bind(items[p1])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}