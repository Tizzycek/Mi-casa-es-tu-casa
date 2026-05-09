package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.data.recensioni.Segnalazione
import it.tizzycek.micasaestucasa.databinding.ItemReportReasonBinding

class ReportReasonAdapter(private var items: List<Segnalazione>): RecyclerView.Adapter<ReportReasonAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val binding = ItemReportReasonBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(items[p1])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(private val binding: ItemReportReasonBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(s: Segnalazione) {
            binding.reportTypeText.setText(s.motivazione.titoloResId)
            binding.reportDescriptionText.setText(s.motivazione.descrizioneResId)
            if (s.testo.isNotEmpty()) {
                binding.reportAdditionalDetails.text = s.testo
                binding.reportAdditionalDetails.visibility = android.view.View.VISIBLE
            } else {
                binding.reportAdditionalDetails.visibility = android.view.View.GONE
            }
        }
    }

    fun updateData(list: List<Segnalazione>) {
        this.items = list
        notifyDataSetChanged()
    }
}