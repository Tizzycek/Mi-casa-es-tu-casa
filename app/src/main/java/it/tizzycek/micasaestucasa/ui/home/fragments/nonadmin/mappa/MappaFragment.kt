package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.databinding.FragmentMappaBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MappaFragment: Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentMappaBinding
    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient // serve per fissare la nostra posizione

    private val markerMap = mutableMapOf<Marker, String>()  // posizione - id proprietà

    private var authPosition = false


    // launcher richiesta permessi
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            abilitaPosizioneUtente()
        } else {
            Toast.makeText(requireContext(), "Permesso negato", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        binding = FragmentMappaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermissions()

        setupMap()
        mMap.setOnCameraIdleListener { loadOnZoom() }
    }

    private fun setupButtons() {
        if (MainRepo.getUserRepository().getCurrentUser()!!.tipo == TipoUtente.PROPRIETARIO){
            binding.fabAddProperty.visibility = View.VISIBLE
            binding.fabAddProperty.setOnClickListener {
                startActivity(Intent(requireContext(), AggiungiProprieta::class.java))
            }
        }
        binding.searchBarContainer.setOnClickListener {
            Toast.makeText(requireContext(), "Ricerca", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermissions() {
        when {
            checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                authPosition = true
                abilitaPosizioneUtente()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    // attiva il pallino blu sulla mappa
    @SuppressLint("MissingPermission") // i permessi sono attivi (se no non viene chiamata)
    private fun abilitaPosizioneUtente() {
        if (!::mMap.isInitialized) return

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
            }
        }
    }

    private fun setupMap() {
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Setta il listener per i marker
        mMap.setOnMarkerClickListener { marker ->
            Toast.makeText(requireContext(), "Cliccato su ${marker.title}", Toast.LENGTH_SHORT).show()
            false
        }
    }

//    @SuppressLint("MissingPermission")
//    private fun caricaProprieta() {
//        if (authPosition) {
//            lifecycleScope.launch {
//                binding.progressBar.visibility = View.VISIBLE
//                val location = fusedLocationClient.lastLocation.await()
//                if (location != null) {
//                    if (MainRepo.getUserRepository().getCurrentUser()?.tipo == TipoUtente.PROPRIETARIO)
//                        caricaTutte()
//                    else
//                        caricaProprietaVicine(location)
//                }
//                else {
//                    caricaTutte()
//                }
//            }
//        }
//        else
//            caricaTutte()
//    }

//    private fun caricaProprietaVicine(location: Location) {
//
//        val latLng = LatLng(location.latitude, location.longitude)
//
//
//        lifecycleScope.launch {
//            val risultato = MainRepo.getProprietaRepository().cercaDisponibili(
//                lat = latLng.latitude,
//                lng = latLng.longitude,
//                raggioKm = 20.0
//            )
//
//            binding.progressBar.visibility = View.GONE
//
//            risultato.fold(
//                onSuccess = { proprieta ->
//                    aggiungiProprietaAllaMappa(proprieta)
//                },
//                onFailure = { errore ->
//                    Toast.makeText(requireContext(), "Errore: ${errore.message}", Toast.LENGTH_SHORT).show()
//                }
//            )
//        }
//    }

//    private fun caricaTutte() {
//        lifecycleScope.launch {
//            val risultato: Result<List<Proprieta>> = if (MainRepo.getUserRepository().getCurrentUser()?.tipo == TipoUtente.PROPRIETARIO)
//                MainRepo.getProprietaRepository().caricaProprietaPerProprietario(MainRepo.getUserRepository().getCurrentUser()?.id!!)
//            else
//                MainRepo.getProprietaRepository().getProprieta()
//
//            binding.progressBar.visibility = View.GONE
//
//            risultato.fold(
//                onSuccess = { proprieta ->
//                    aggiungiProprietaAllaMappa(proprieta)
//                },
//                onFailure = { errore ->
//                    Toast.makeText(requireContext(), "Errore: ${errore.message}", Toast.LENGTH_SHORT).show()
//                }
//            )
//        }
//    }

    private fun aggiungiProprietaAllaMappa(proprieta: List<Proprieta>) {
        if (!::mMap.isInitialized) return

        mMap.clear()
        markerMap.clear()

        proprieta.forEach { proprieta ->
            val posizione = LatLng(proprieta.latitudine, proprieta.longitudine)
            val marker = mMap.addMarker(MarkerOptions().position(posizione).title(proprieta.nome).snippet("${proprieta.posti} posti"))

            marker?.let {
                markerMap[it] = requireNotNull(proprieta.id)
            }
        }
    }

    private fun loadOnZoom() {
        val visualeCorrente = mMap.projection.visibleRegion.latLngBounds
        val zoomAttuale = mMap.cameraPosition.zoom

        // Carichiamo solo se lo zoom è abbastanza vicino (evitiamo di caricare tutto il mondo)
        if (zoomAttuale > 8f) {
            caricaProprietaNellaVisuale(visualeCorrente)
        } else {
            Toast.makeText(requireContext(), "Ingrandisci per vedere le case", Toast.LENGTH_SHORT).show()
        }
    }

    private fun caricaProprietaNellaVisuale(bounds: LatLngBounds) {
        val user = MainRepo.getUserRepository().getCurrentUser()

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE

            val risultato = if (user?.tipo == TipoUtente.PROPRIETARIO) {
                MainRepo.getProprietaRepository().caricaProprietaPerProprietario(user.id!!)
            } else {
                // I conduttori caricano solo quello che entra nel "quadrato" della mappa
                // NOTA: Firestore ha limiti sulle query "box". Usiamo il centro + raggio per ora.
                val centro = bounds.center
                val raggio = calcolaRaggioApprossimativo(bounds)

                MainRepo.getProprietaRepository().cercaDisponibili(
                    lat = centro.latitude,
                    lng = centro.longitude,
                    raggioKm = raggio
                )
            }

            binding.progressBar.visibility = View.GONE
            risultato.onSuccess { aggiungiProprietaAllaMappa(it) }
        }
    }

    // Funzione utile per capire quanto "grande" è la porzione di mappa visibile
    private fun calcolaRaggioApprossimativo(bounds: LatLngBounds): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            bounds.center.latitude, bounds.center.longitude,
            bounds.northeast.latitude, bounds.northeast.longitude,
            results
        )
        return (results[0] / 1000.0) // Convertiamo in KM
    }
}