package it.tizzycek.micasaestucasa.ui.utente

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.databinding.SignupBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.mostraErrore
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

class RegistrationActivity: AppCompatActivity() {
    private lateinit var binding: SignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.signup)

        binding = SignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupListener()
        setupDatePicker()
    }

    private fun setupListener() {
        binding.btnSignUp.setOnClickListener {
            lifecycleScope.launch {
                tryRegister()
            }
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private suspend fun tryRegister() {

        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val nome = binding.nomeEditText.text.toString().trim()
        val cognome = binding.cognomeEditText.text.toString().trim()

        // data
        val giorno = binding.dataNascita.dayOfMonth
        val mese = binding.dataNascita.month
        val anno = binding.dataNascita.year
        val dataNascita = LocalDate.of(anno, mese + 1, giorno)

        // tipo utente
        val tipoUtenteSelezionato = when (binding.spinnerUserType.selectedItemPosition) {
            0 -> TipoUtente.AFFITTUARIO
            1 -> TipoUtente.PROPRIETARIO
            else -> {
                mostraErrore(R.string.seleziona_tipo)
                return
            }
        }

        if (email.isEmpty() || password.isEmpty() || nome.isEmpty() || cognome.isEmpty()) {
            mostraErrore(R.string.data_missing)
        }

        binding.btnSignUp.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        if (validData(email, dataNascita, tipoUtenteSelezionato)) {
            try {
                val risultatoRegistrazione = MainRepo.getUserRepository().registration(
                    email = email,
                    password = password,
                    nome = nome,
                    cognome = cognome,
                    nascita = dataNascita,
                    tipoUtente = tipoUtenteSelezionato
                )

                risultatoRegistrazione.fold(
                    onSuccess = {
                        val risultatoLogin = MainRepo.getUserRepository().login(email, password)
                        risultatoLogin.fold(
                            onSuccess = {
                                val profilo = MainRepo.getUserRepository().getCurrentUser()
                                if (profilo != null) {
                                    //vaiAllaHome(profilo)
                                    Toast.makeText(this, "To be implemented!", Toast.LENGTH_LONG)
                                        .show()
                                    finish()
                                }
                            },
                            onFailure = { errore ->
                                val messaggio = errore.message ?: getString(R.string.unknown_error)
                                mostraErrore(messaggio)
                                finish()
                            })
                    },
                    onFailure = { errore ->
                        val messaggioFinal = errore.message ?: getString(R.string.unknown_error)
                        mostraErrore(messaggioFinal)
                    }
                )
            } finally {
                binding.btnSignUp.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        } else {
            binding.btnSignUp.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setupSpinner() {
        val tipiUtente = TipoUtente.entries.toTypedArray().filter {
            it != TipoUtente.ADMIN
        }.map {
            tipo -> when (tipo) {
                TipoUtente.PROPRIETARIO -> getString(R.string.landlord)
                TipoUtente.AFFITTUARIO -> getString(R.string.tenant)
                else -> ""
            }
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tipiUtente
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUserType.adapter = adapter

        // Listener per lo spinner
        /*binding.spinnerUserType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                binding.selection.text = tipiUtente[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                binding.selection.text = getString(R.string.seleziona_tipo)
            }
        }*/
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 0)
        binding.dataNascita.maxDate = calendar.timeInMillis
    }

    private fun validData(email: String, nascita: LocalDate, tipoUtente: TipoUtente): Boolean {
        val today = LocalDate.now()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostraErrore(R.string.invalid_email)
            return false
        } else if (today.year - nascita.year < 18) {
            mostraErrore(R.string.invalid_age)
            return false
        } else if (tipoUtente == TipoUtente.ADMIN) {
            mostraErrore(R.string.seleziona_tipo)
            return false
        } else {
            return true
        }
    }

}