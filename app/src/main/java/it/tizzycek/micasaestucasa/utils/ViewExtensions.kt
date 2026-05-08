package it.tizzycek.micasaestucasa.utils
import android.app.Activity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.parolechiave.ParoleChiave
import it.tizzycek.micasaestucasa.repo.MainRepo

/**
 * Popola un ChipGroup con le parole chiave recuperate dal repository.
 * @param scope Il lifecycleScope della View chiamante.
 * @param onSelectionChanged Callback facoltativa che restituisce la lista degli ID selezionati.
 */
fun ChipGroup.setupParoleChiave(scope: CoroutineScope, onSelectionChanged: (List<Chip>) -> Unit = {}) {
    scope.launch {
        MainRepo.getParoleChiave().getParoleChiave().fold(
            onSuccess = { listaParole ->
                this@setupParoleChiave.removeAllViews()

                listaParole.filter { it.attiva }.forEach { parolaOggetto ->
                    val chip = Chip(context)
                    chip.text = parolaOggetto.parola
                    chip.isCheckable = true

                    chip.setChipBackgroundColorResource(R.color.secondary_dark)
                    chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.check_small_24px)
                    chip.isChipIconVisible = false

                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            chip.isCheckedIconVisible = true
                            chip.setChipBackgroundColorResource(R.color.secondary_light)
                        } else {
                            chip.isCheckedIconVisible = false
                            chip.setChipBackgroundColorResource(R.color.secondary_dark)
                        }


                        val keywordList = (0 until childCount)
                            .map { getChildAt(it) as Chip }
                            .filter { it.isChecked }

                        onSelectionChanged(keywordList)
                    }

                    chip.tag = parolaOggetto

                    this@setupParoleChiave.addView(chip)
                }
            },
            onFailure = { e ->
                throw Exception(e)
            }
        )
    }
}

fun ChipGroup.paroleChiaveProprieta(scope: CoroutineScope, listaParole: List<String>) {
    scope.launch {
        MainRepo.getParoleChiave().getParoleChiave().fold(
            onSuccess = { parole ->
                this@paroleChiaveProprieta.removeAllViews()
                parole.filter { listaParole.contains(it.id) }.forEach {
                    val chip = Chip(context)
                    chip.text = it.parola
                    chip.isCheckable = false
                    chip.setChipBackgroundColorResource(R.color.secondary_dark)
                    chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.check_small_24px)
                    chip.isChipIconVisible = false

                    this@paroleChiaveProprieta.addView(chip)
                }
            },
            onFailure = { e ->
                throw Exception(e)
            }
        )

    }
}