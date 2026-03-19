package it.tizzycek.micasaestucasa.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

class FragmentManager: AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // doppio controllo se loggato
        val utente = MainRepo.getUserRepository().getCurrentUser() ?: run {
            finish()
            return
        }

        setupNavigation(utente.tipo)
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
            menu.add(0, R.id.messaggiFragment, 2, getString(R.string.messaggistica)).setIcon(R.drawable.stacked_email_24px)
            menu.add(0, R.id.profiloFragment, 3, getString(R.string.profilo)).setIcon(R.drawable.account_circle_24px)
        }

        binding.bottomNavigation.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        binding.bottomNavigation.itemTextColor = getColorStateList(R.color.red)
        binding.bottomNavigation.itemIconTintList = getColorStateList(R.color.red)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}