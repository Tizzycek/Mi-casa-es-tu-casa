package it.tizzycek.micasaestucasa.ui

import android.app.Activity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import it.tizzycek.micasaestucasa.R

fun Activity.mostraErrore(messaggio: String) {
    Snackbar.make(findViewById(android.R.id.content), messaggio, Snackbar.LENGTH_LONG)
        .setBackgroundTint(ContextCompat.getColor(this, R.color.red))
        .setTextColor(ContextCompat.getColor(this, R.color.white))
        .show()
}

fun Activity.mostraErrore(resId: Int) {
    mostraErrore(getString(resId))
}