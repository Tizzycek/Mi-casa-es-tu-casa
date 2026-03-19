package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.tizzycek.micasaestucasa.R

class MappaFragment: Fragment() {
    private lateinit var binding: MappaFragment
    private val LOCATION_REQUEST_CODE = 101

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_mappa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Esempio di navigazione verso un altro fragment

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    /*private fun requestPermission(permissionType: String, requestCode: Int) {
        ActivityCompat.requestPermissions(
            FragmentManager, arrayOf(permissionType), requestCode
        )
    }


        override fun onRequestPermissionsResult(requestCode: Int,  permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] !=
                    PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                        "Unable to show location - permission required",
                        Toast.LENGTH_LONG).show()
                } else {
                    val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                }
            }
        }
    }
    */
}