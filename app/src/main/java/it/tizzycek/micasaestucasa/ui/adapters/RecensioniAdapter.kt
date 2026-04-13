package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.data.recensioni.Recensione
import it.tizzycek.micasaestucasa.databinding.ItemRecensioneBinding

class RecensioniAdapter(private val listaRecensioni: List<Recensione>): RecyclerView.Adapter<RecensioniAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemRecensioneBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(recensione: Recensione) {
            binding.recensioneRatingBar.rating = recensione.voto.toFloat()
            binding.dataTextView.text = recensione.dataCreazione.toString()
            binding.testoRecensioneTextView.text = recensione.descrizione
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listaRecensioni[position])
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val binding = ItemRecensioneBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaRecensioni.size
    }
}