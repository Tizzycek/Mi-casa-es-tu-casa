package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import it.tizzycek.micasaestucasa.R

class MappaFragment : Fragment() {
    //private lateinit var binding: MappaFragment
    //private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mappa, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Esempio di navigazione verso un altro fragment

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}