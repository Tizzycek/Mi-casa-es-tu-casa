package it.tizzycek.micasaestucasa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.auth.User
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.repo.utente.UserRepository
import it.tizzycek.micasaestucasa.ui.utente.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // togli poi quello che non serve
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (MainRepo.getUserRepository().isLoggedIn()) {
            setContentView(R.layout.activity_main)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
}