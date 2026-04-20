package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.data.recensioni.Recensione
import it.tizzycek.micasaestucasa.databinding.ItemRecensioneBinding
import it.tizzycek.micasaestucasa.utils.DateValidator

class RecensioniAdapter(private var listaRecensioni: List<Recensione>?, private val onRecensioneScelta: (String) -> Unit): RecyclerView.Adapter<RecensioniAdapter.ViewHolder>() {

    fun updateData(nuovaLista: List<Recensione>) {
        this.listaRecensioni = nuovaLista
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemRecensioneBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(recensione: Recensione, onRecensioneScelta: (String) -> Unit) {
            binding.nomeUtenteTextView.text = recensione.nomeAutore
            binding.recensioneRatingBar.rating = recensione.voto.toFloat()
            binding.dataTextView.text = DateValidator.formatDate(recensione.dataCreazione)
            binding.testoRecensioneTextView.text = recensione.descrizione

            binding.root.setOnClickListener {
                onRecensioneScelta(recensione.autoreId)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listaRecensioni?.let { holder.bind(it[position], onRecensioneScelta) }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val binding = ItemRecensioneBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaRecensioni?.size ?: 0
    }
}