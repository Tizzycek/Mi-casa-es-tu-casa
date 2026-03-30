package it.tizzycek.micasaestucasa.ui.utente

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.databinding.SignupBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.home.FragmentManager
import it.tizzycek.micasaestucasa.utils.mostraErrore
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
            1 -> TipoUtente.PROPRIETARIO
            2 -> TipoUtente.AFFITTUARIO
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
                                    startActivity(Intent(this, FragmentManager::class.java))
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

        val listaFinale = mutableListOf<String>()
        listaFinale.add(getString(R.string.seleziona_tipo))
        listaFinale.addAll(tipiUtente)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listaFinale
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUserType.adapter = adapter
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