package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import it.tizzycek.micasaestucasa.databinding.ItemDisplayAvailabilityBinding

class DisponibilitaAdapter(private var items: List<Disponibilita>): RecyclerView.Adapter<DisponibilitaAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemDisplayAvailabilityBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(disponibilita: Disponibilita) {
            binding.tvStartDate.text = disponibilita.dataInizio.toString()
            binding.tvEndDate.text = disponibilita.dataFine.toString()
            binding.tvPrice.text = disponibilita.prezzo.toString()
            binding.tvCurrencySymbol.text = disponibilita.valuta.symbol
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDisplayAvailabilityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}