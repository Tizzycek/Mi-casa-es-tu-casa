package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.databinding.FragmentProfiloBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.utente.LoginActivity

class ProfiloFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profilo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentProfiloBinding.bind(view)
        // TODO: è una soluzione tampone
        binding.btnLogout.setOnClickListener {
            MainRepo.getUserRepository().logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        // Esempio di navigazione verso un altro fragment

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}