package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.messaggi.Messaggio
import it.tizzycek.micasaestucasa.databinding.ItemMessageReceivedBinding
import it.tizzycek.micasaestucasa.databinding.ItemMessageSentBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.utils.DateValidator

class MessageAdapter(private var messageList: List<Messaggio>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ricevuto = 1
    private val inviato = 2

    interface BindingAccess {
        val messageTextTextView: TextView
        val messageImageView: ImageView
    }

    class InviatoViewHolder(val binding: ItemMessageSentBinding): RecyclerView.ViewHolder(binding.root), BindingAccess {
        override val messageImageView = binding.messageImageView
        override val messageTextTextView = binding.messageTextTextView
        fun bind(messaggio: Messaggio) {
            binding.messageTimeTextView.text = DateValidator.formatDate(messaggio.timestamp)
        }
    }

    class RicevutoViewHolder(val binding: ItemMessageReceivedBinding): RecyclerView.ViewHolder(binding.root), BindingAccess {
        override val messageImageView = binding.messageImageView
        override val messageTextTextView = binding.messageTextTextView
        fun bind(messaggio: Messaggio) {
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
        val bindingHolder = p0 as BindingAccess

        if (messaggio.contenuto.isNotEmpty()) {
            bindingHolder.messageTextTextView.text = messaggio.contenuto
            bindingHolder.messageTextTextView.visibility = View.VISIBLE
        } else {
            bindingHolder.messageTextTextView.visibility = View.GONE
        }

        if (messaggio.imageUrl.isNotEmpty()) {
            bindingHolder.messageImageView.visibility = View.VISIBLE

            messaggio.imageUrl.forEach { img ->
                bindingHolder.messageImageView.load(img) {
                    crossfade(true)
                    placeholder(R.drawable.house_24px)
                    error(android.R.drawable.stat_notify_error)
                    transformations(RoundedCornersTransformation(8f))
                }
            }

        } else {
            bindingHolder.messageImageView.visibility = View.GONE
        }

        when (p0) {
            is InviatoViewHolder -> p0.bind(messaggio)
            is RicevutoViewHolder -> p0.bind(messaggio)
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