package it.tizzycek.micasaestucasa.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.databinding.ItemImagePreviewBinding

class ImageAdapter(private val onRemoveClick: (Uri) -> Unit): RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private val images = mutableListOf<Any>()

    fun setImages(newImages: List<Uri>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bindingImageView = ItemImagePreviewBinding.bind(view)
        val imageView: ImageView = bindingImageView.ivPreview
        val btnDelete: ImageButton = bindingImageView.btnDeleteImg
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val source = images[position]
        holder.btnDelete.setOnClickListener { onRemoveClick(source as Uri) }
        holder.imageView.load(source) {
            crossfade(true)
            placeholder(R.color.light_gray)
            transformations(RoundedCornersTransformation(8f))
        }
    }

    override fun getItemCount() = images.size
}