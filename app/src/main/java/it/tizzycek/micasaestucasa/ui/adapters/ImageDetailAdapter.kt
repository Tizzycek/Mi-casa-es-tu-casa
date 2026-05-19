package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import coil3.request.placeholder
import coil3.request.error
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.databinding.ItemImageDetailBinding
import it.tizzycek.micasaestucasa.databinding.ItemImagePreviewBinding

class ImageDetailAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ImageDetailAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemImageDetailBinding) : RecyclerView.ViewHolder(binding.imageViewItem) {
        val imageView: ImageView = binding.imageViewItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.load(images[position]) {
            crossfade(true)
            placeholder(R.drawable.house_24px) // Immagine temporanea
            error(android.R.drawable.stat_notify_error)
        }
    }

    override fun getItemCount() = images.size
}