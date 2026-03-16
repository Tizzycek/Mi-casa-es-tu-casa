package it.tizzycek.micasaestucasa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.home.FragmentManager
import it.tizzycek.micasaestucasa.ui.utente.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // togli poi quello che non serve
        installSplashScreen()
        super.onCreate(savedInstanceState)

        MainRepo.getUserRepository().logout()

        if (MainRepo.getUserRepository().isLoggedIn()) {
            val profilo = MainRepo.getUserRepository().getCurrentUser()
            if (profilo != null) {
                startActivity(Intent(this, FragmentManager::class.java))
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}