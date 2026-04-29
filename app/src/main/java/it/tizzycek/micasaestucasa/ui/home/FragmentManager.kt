package it.tizzycek.micasaestucasa.ui.home

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationBarView
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.databinding.ActivityHomeBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.MessaggisticaFragment
import it.tizzycek.micasaestucasa.ui.utente.ChatActivity
import it.tizzycek.micasaestucasa.ui.utente.LoginActivity
import it.tizzycek.micasaestucasa.utils.notifiche.NotificationHelper

class FragmentManager: AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationHelper = NotificationHelper(this)
        notificationHelper.checkNotificationPermission()

        // doppio controllo se loggato
        val utente = MainRepo.getUserRepository().getCurrentUser() ?: run {
            finish()
            return
        }

        setupNavigation(utente.tipo)
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent != null) {
            val type = intent.getStringExtra("NOTIFICATION_TYPE")
            val targetId = intent.getStringExtra("TARGET_ID")

            if (type != null && targetId != null) {
                when (type) {
                    "messaggi_channel" -> navigaAChat(targetId)
                    "prenotazioni_channel" -> navigaAPrenotazioni()
                }
                // 🗑️ Pulisci gli extra per evitare loop su rotazione o ripristino
                intent.removeExtra("NOTIFICATION_TYPE")
                intent.removeExtra("TARGET_ID")
            }
        }
    }

    private fun navigaAChat(chatId: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("CHAT_ID", chatId)
        }
        startActivity(intent)
    }

    private fun navigaAPrenotazioni() {
        navController.navigate(R.id.prenotazioniFragment)
        binding.bottomNavigation.selectedItemId = R.id.prenotazioniFragment
    }


    private fun setupNavigation(tipo: TipoUtente) {
        // trova
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        // cerca tipo utente e lo collega
        val navGraphId = when (tipo) {
            TipoUtente.PROPRIETARIO -> R.navigation.nav_graph_non_admin
            TipoUtente.AFFITTUARIO -> R.navigation.nav_graph_non_admin
            TipoUtente.ADMIN -> R.navigation.navgraph_admin
        }

        navController.setGraph(navGraphId)

        setupBottomNavigationMenu(tipo)

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setupBottomNavigationMenu(tipo: TipoUtente) {
        val menu = binding.bottomNavigation.menu
        menu.clear()

        if (tipo == TipoUtente.ADMIN) {
            /*
            menu.add(0, R.id.homeProprietarioFragment, 0, "Home").setIcon(R.drawable.ic_home)
            menu.add(0, R.id.annunciFragment, 1, "Annunci").setIcon(R.drawable.ic_annunci)
            menu.add(0, R.id.prenotazioniFragment, 2, "Prenotazioni").setIcon(R.drawable.ic_prenotazioni)
            menu.add(0, R.id.profiloProprietarioFragment, 3, "Profilo").setIcon(R.drawable.ic_profilo)
*/
        } else {
            menu.add(0, R.id.mappaFragment, 0, getString(R.string.esplora)).setIcon(R.drawable.explore_24px)
            menu.add(0, R.id.prenotazioniFragment, 1, getString(R.string.prenotazioni)).setIcon(R.drawable.hotels_24px)
            menu.add(0, R.id.messaggisticaFragment, 2, getString(R.string.messaggistica)).setIcon(R.drawable.stacked_email_24px)
            menu.add(0, R.id.profiloFragment, 3, getString(R.string.profilo)).setIcon(R.drawable.account_circle_24px)
        }

        when (tipo) {
            TipoUtente.PROPRIETARIO -> {
                binding.bottomNavigation.itemTextColor = getColorStateList(R.color.primary)
                binding.bottomNavigation.itemIconTintList = getColorStateList(R.color.primary)
            }
            TipoUtente.AFFITTUARIO -> {
                binding.bottomNavigation.itemTextColor = getColorStateList(R.color.secondary)
                binding.bottomNavigation.itemIconTintList = getColorStateList(R.color.secondary)
            } TipoUtente.ADMIN -> {

            }
        }
        binding.bottomNavigation.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        binding.bottomNavigation.backgroundTintList = getColorStateList(R.color.backgroundColor)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}