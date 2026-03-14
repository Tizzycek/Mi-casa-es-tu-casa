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
import kotlinx.coroutines.launch

class LoginActivity() : AppCompatActivity() {

    private lateinit var binding: LoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login)

        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginBtn = binding.btnLogIn
        val signUpBtn = binding.btnSignUp

        loginBtn.setOnClickListener {
            val email = binding.tvUsername.toString()
            val password = binding.tvPassword.toString()

            lifecycleScope.launch {
                login(email, password)
            }
        }

        signUpBtn.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    suspend fun login(email: String, password: String) {
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
                mostraErrore(errore.message ?: R.string.unknown_error.toString())
                binding.btnLogIn.isEnabled = true
                binding.btnSignUp.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        )
        binding.progressBar.visibility = View.GONE
    }

    private fun mostraErrore(messaggio: String) {
        Snackbar.make(binding.root, messaggio, Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(this, R.color.red)).setTextColor(ContextCompat.getColor(this, R.color.white)).show()
    }
}