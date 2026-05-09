package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.parolechiave.ParoleChiave
import it.tizzycek.micasaestucasa.databinding.ItemKeywordBinding

class ParoleChiaveAdapter(private var items: List<ParoleChiave>, private val onEditClick: (ParoleChiave) -> Unit, private val onDeleteClick: (ParoleChiave) -> Unit, private val onStatusChanged: (ParoleChiave, Boolean) -> Unit): RecyclerView.Adapter<ParoleChiaveAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemKeywordBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(keyword: ParoleChiave) {
            binding.tvKeyword.text = keyword.parola
            binding.switchActive.setOnCheckedChangeListener(null) // Evita trigger durante il bind
            binding.switchActive.isChecked = keyword.attiva
            binding.tvKeyword.alpha = if (keyword.attiva) 1.0f else 0.5f

            binding.btnEdit.setOnClickListener {
                onEditClick(keyword)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(keyword)
            }

            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                onStatusChanged(keyword, isChecked)
            }
        }
    }

    fun aggiornaLista(nuovaLista: List<ParoleChiave>) {
        this.items = nuovaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val binding = ItemKeywordBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(items[p1])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}