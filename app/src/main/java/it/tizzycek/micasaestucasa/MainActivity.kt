package it.tizzycek.micasaestucasa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.home.FragmentManager
import it.tizzycek.micasaestucasa.ui.utente.LoginActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // togli poi quello che non serve
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !LoadingUserManager().isReady.value
        }

        lifecycleScope.launch {
            LoadingUserManager().isReady.collect { pronto ->
                if (pronto) {
                    checkUserAndNavigate()
                    finish()
                }
            }
        }
    }

    private fun checkUserAndNavigate() {
        if (MainRepo.getUserRepository().isLoggedIn() && MainRepo.getUserRepository().getCurrentUser() != null) {
            startActivity(Intent(this, FragmentManager::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}