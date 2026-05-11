package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.databinding.ItemImagePreviewBinding

// any è meglio: si usano stringhe e uri
class ImageAdapter(private var images: List<Any> = emptyList(), private val onRemoveClick: (Any) -> Unit) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    fun updateData(newImages: List<Any>) {
        this.images = newImages
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemImagePreviewBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImagePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val source = images[position]

        holder.binding.btnDeleteImg.setOnClickListener {
            onRemoveClick(source)
        }

        holder.binding.ivPreview.load(source) {
            crossfade(true)
            placeholder(R.color.light_gray)
            error(android.R.drawable.stat_notify_error)
            transformations(RoundedCornersTransformation(8f))
        }
    }

    override fun getItemCount() = images.size
}