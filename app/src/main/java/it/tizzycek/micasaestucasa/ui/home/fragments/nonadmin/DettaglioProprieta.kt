package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log.e
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.databinding.ActivityDettaglioProprietaBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.ImageDetailAdapter
import kotlinx.coroutines.launch

class DettaglioProprieta: AppCompatActivity() {
    private lateinit var binding: ActivityDettaglioProprietaBinding
    private var idProprieta: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDettaglioProprietaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idProprieta = intent.getStringExtra("PROPRIETA_ID")

        if (idProprieta != null) {
            caricaDatiPropieta(idProprieta!!)
        } else {
            finish()
        }
    }

    private fun caricaDatiPropieta(id: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE

            val risultato = MainRepo.getProprietaRepository().getProprieta(id)
            risultato.fold(
                onSuccess = { proprieta ->
                    if (proprieta != null)
                        mostraDati(proprieta)
                    else
                        Toast.makeText(this@DettaglioProprieta, "Errore proprietà", Toast.LENGTH_SHORT).show()
                },
                onFailure = { e->
                    Toast.makeText(this@DettaglioProprieta, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun mostraDati(p: Proprieta) {
        binding.tvNome.text = p.nome
        binding.tvPosti.text = "${p.posti} posti disponibili"
        binding.tvDescrizione.text = p.descrizione

        val urls = p.immaginiUrl ?: emptyList()
        if (urls.isNotEmpty()){
            val adapter = ImageDetailAdapter(urls)
            binding.viewPagerImages.adapter = adapter
        } else {
            binding.viewPagerImages.visibility = View.GONE
        }
    }
}