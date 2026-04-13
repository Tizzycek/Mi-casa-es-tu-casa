package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.profilo

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.databinding.EditProfileBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.utils.DateValidator
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

class ModificaProfilo: AppCompatActivity() {
    private lateinit var binding: EditProfileBinding
    private lateinit var utente: Utente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = EditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        utente = MainRepo.getUserRepository().getCurrentUser()!!

        setFields()

        binding.btnAnnulla.setOnClickListener {
            finish()
        }

        binding.btnSalvaModifiche.setOnClickListener {
            updateUser()
        }
    }

    private fun setFields() {
        binding.avatarInitialsTextView.text = utente.nome[0].toString() + utente.cognome[0].toString()
        binding.emailEditText.setText(utente.email)
        binding.nomeEditText.setText(utente.nome)
        binding.cognomeEditText.setText(utente.cognome)

        binding.dataNascita.init(utente.nascita.year, utente.nascita.monthValue - 1, utente.nascita.dayOfMonth, null)
    }

    private fun updateUser() {
        val nome = binding.nomeEditText.text.toString().trim()
        val cognome = binding.cognomeEditText.text.toString().trim()

        val giorno = binding.dataNascita.dayOfMonth
        val mese = binding.dataNascita.month + 1
        val anno = binding.dataNascita.year
        val dataNascita = LocalDate.of(anno, mese, giorno)

        val password = binding.nuovaPasswordEditText.text.toString().trim()
        val conferma = binding.confermaPasswordEditText.text.toString().trim()

        if (nome.isEmpty() || cognome.isEmpty()) {
            mostraErrore(R.string.data_missing)
        } else if (DateValidator.isValidBirthDate(dataNascita)) {
            mostraErrore(R.string.invalid_age)
        } else if (password.isNotEmpty() && password.length < 6) {
            mostraErrore(R.string.password_minima)
        } else if (password != conferma) {
            mostraErrore(R.string.invalid_password)
        } else {
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                val risultato = MainRepo.getUserRepository().update(nome, cognome, dataNascita, password)
                risultato.fold(
                    onSuccess = {
                        Toast.makeText(this@ModificaProfilo, getString(R.string.updated_user), Toast.LENGTH_LONG).show()
                        finish()
                    },
                    onFailure = { e ->
                        mostraErrore(e.message ?: getString(R.string.unknown_error))
                    }
                )
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}