package it.tizzycek.micasaestucasa.ui.utente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.databinding.LoginBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.home.FragmentManager
import it.tizzycek.micasaestucasa.ui.mostraErrore
import kotlinx.coroutines.launch

class LoginActivity() : AppCompatActivity() {

    private lateinit var binding: LoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login)

        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnLogin.setOnClickListener {
                lifecycleScope.launch {
                login()
            }
        }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    suspend fun login() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            mostraErrore(R.string.data_missing)
            return
        }

        binding.btnLogin.isEnabled = false
        binding.btnSignUp.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val risultato = MainRepo.getUserRepository().login(email, password)

        risultato.fold(
            onSuccess = {
                val profilo = MainRepo.getUserRepository().getCurrentUser()
                if (profilo != null) {
                    startActivity(Intent(this, FragmentManager::class.java))
                    finish()
                }
            },
            onFailure = { errore ->
                val messaggioFinal = errore.message ?: getString(R.string.unknown_error)
                mostraErrore(messaggioFinal)
                binding.btnLogin.isEnabled = true
                binding.btnSignUp.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        )
    }
}