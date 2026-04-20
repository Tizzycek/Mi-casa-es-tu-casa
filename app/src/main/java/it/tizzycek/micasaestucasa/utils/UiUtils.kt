package it.tizzycek.micasaestucasa.utils

import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import it.tizzycek.micasaestucasa.R
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

fun Activity.mostraErrore(messaggio: String) {
    Snackbar.make(findViewById(android.R.id.content), messaggio, Snackbar.LENGTH_LONG)
        .setBackgroundTint(ContextCompat.getColor(this, R.color.red))
        .setTextColor(ContextCompat.getColor(this, R.color.white))
        .show()
}

fun Activity.mostraErrore(resId: Int) {
    mostraErrore(getString(resId))
}

fun View.mostraErrore(messaggio: String) {
    Snackbar.make(this, messaggio, Snackbar.LENGTH_LONG)
        .setBackgroundTint(ContextCompat.getColor(context, R.color.red))
        .setTextColor(ContextCompat.getColor(context, R.color.white))
        .show()
}

suspend fun Context.findPlace(address: String): Address? = suspendCancellableCoroutine { continuation ->
    val geocoder = Geocoder(this, Locale.getDefault())
    
    try {
        // Usiamo la versione con listener (API 33+) che è asincrona
        geocoder.getFromLocationName(address, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                continuation.resume(addresses.firstOrNull())
            }

            override fun onError(errorMessage: String?) {
                continuation.resume(null)
            }
        })
    } catch (e: Exception) {
        // Fallback per versioni vecchie o errori di rete
        continuation.resume(null)
    }
}
