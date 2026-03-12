package it.tizzycek.micasaestucasa.ui.utente

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.databinding.ActivityMainBinding
import it.tizzycek.micasaestucasa.databinding.LoginBinding
import it.tizzycek.micasaestucasa.repo.utente.MainRepo
import it.tizzycek.micasaestucasa.repo.utente.UserRepository
import kotlinx.coroutines.launch

class LoginActivity() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login)

        val binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginBtn = binding.btnLogIn
        val signUpBtn = binding.btnSignUp

        loginBtn.setOnClickListener {
            val email = binding.tvUsername.toString()
            val password = binding.tvPassword.toString()

            lifecycleScope.launch {
                login(binding, email, password)
            }
        }
    }

    private suspend fun login(binding: LoginBinding, email: String, password: String) {
        binding.btnLogIn.isEnabled = false
        binding.btnSignUp.isEnabled = false

        binding.progressBar.visibility = View.VISIBLE

        val risultato = MainRepo.getUserRepository().login(email, password)

        risultato.fold(
            onSuccess = {
                val profilo = MainRepo.getUserRepository().getCurrentUser()
                if (profilo != null) {
                    //vaiAllaHome(profilo)
                    Toast.makeText(this, "To be implemented!", Toast.LENGTH_LONG).show()
                }
            },
            onFailure = { errore ->
                mostraErrore(errore.message ?: "Errore sconosciuto", binding)
                binding.btnLogIn.isEnabled = true
                binding.btnSignUp.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        )
        binding.progressBar.visibility = View.GONE
    }

    @SuppressLint("ResourceAsColor")
    private fun mostraErrore(messaggio: String, binding: LoginBinding) {
        Snackbar.make(binding.root, messaggio, Snackbar.LENGTH_LONG).setBackgroundTint(R.color.red).setTextColor(R.color.white).show()
    }
}