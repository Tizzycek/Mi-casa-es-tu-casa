package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.data.messaggi.Messaggio
import it.tizzycek.micasaestucasa.databinding.ItemMessageReceivedBinding
import it.tizzycek.micasaestucasa.databinding.ItemMessageSentBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.utils.DateValidator

class MessageAdapter(private var messageList: List<Messaggio>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ricevuto = 1
    private val inviato = 2
    class InviatoViewHolder(private val binding: ItemMessageSentBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(messaggio: Messaggio) {
            binding.messageTextTextView.text = messaggio.contenuto
            binding.messageTimeTextView.text = DateValidator.formatDate(messaggio.timestamp)
        }
    }

    class RicevutoViewHolder(private val binding: ItemMessageReceivedBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(messaggio: Messaggio) {
            binding.messageTextTextView.text = messaggio.contenuto
            binding.messageTimeTextView.text = DateValidator.formatDate(messaggio.timestamp)
        }
    }

    fun aggiornaLista(nuovaLista: List<Messaggio>) {
        this.messageList = nuovaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        if (p1 == inviato) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(p0.context), p0, false)
            return InviatoViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(p0.context), p0, false)
            return RicevutoViewHolder(binding)
        }
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val messaggio = messageList[p1]
        if (p0 is InviatoViewHolder) {
            p0.bind(messaggio)
        } else {
            (p0 as RicevutoViewHolder).bind(messaggio)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val messaggio = messageList[position]
        return if (messaggio.mittente == MainRepo.getUserRepository().getCurrentUser()!!.id) inviato else ricevuto
    }
}