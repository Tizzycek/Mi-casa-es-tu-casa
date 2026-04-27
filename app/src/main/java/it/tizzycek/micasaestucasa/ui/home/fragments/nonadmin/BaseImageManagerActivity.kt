package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.ui.adapters.ImageAdapter
import it.tizzycek.micasaestucasa.utils.mostraErrore

abstract class BaseImageManagerActivity: AppCompatActivity() {
    protected val selectedImages = mutableListOf<Any>()
    protected lateinit var imageAdapter: ImageAdapter

    protected fun setupImageRecyclerView(recyclerView: RecyclerView, onImageRemoved: ((Any) -> Unit)? = null) {
        imageAdapter = ImageAdapter(selectedImages) { itemDaRimuovere ->
            selectedImages.remove(itemDaRimuovere)
            imageAdapter.updateData(selectedImages.toList())
            onImageRemoved?.invoke(itemDaRimuovere)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BaseImageManagerActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    protected fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            mostraErrore(e.message ?: getString(R.string.unknown_error))
            null
        }
    }

    protected fun separateImages(): Pair<List<ByteArray>, List<String>> {
        val nuoveImmagini = mutableListOf<ByteArray>()
        val urlsEsistenti = mutableListOf<String>()

        selectedImages.forEach { item ->
            when (item) {
                is Uri -> {
                    uriToByteArray(item)?.let { nuoveImmagini.add(it) }
                }
                is String -> {
                    urlsEsistenti.add(item)
                }
            }
        }

        return Pair(nuoveImmagini, urlsEsistenti)
    }

    protected fun loadExistingImages(urls: List<String>) {
        selectedImages.clear()
        selectedImages.addAll(urls)
        imageAdapter.updateData(selectedImages.toList())
    }

    protected fun addImages(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            selectedImages.addAll(uris)
            imageAdapter.updateData(selectedImages.toList())
        }
    }

    protected fun clearImages() {
        selectedImages.clear()
        imageAdapter.updateData(emptyList())
    }
}