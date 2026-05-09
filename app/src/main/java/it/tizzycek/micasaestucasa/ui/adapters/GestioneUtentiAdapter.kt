package it.tizzycek.micasaestucasa.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.utente.Stato
import it.tizzycek.micasaestucasa.data.utente.TipoUtente
import it.tizzycek.micasaestucasa.data.utente.Utente
import it.tizzycek.micasaestucasa.databinding.ItemGestioneUtentiBinding
import it.tizzycek.micasaestucasa.ui.home.fragments.admin.GestioneUtentiBottomSheet

class GestioneUtentiAdapter(options: FirestorePagingOptions<Utente>, val fragmentManager: FragmentManager, val setUserReportRead: (String?) -> Unit): FirestorePagingAdapter<Utente, GestioneUtentiAdapter.ViewHolder>(options) {
    inner class ViewHolder(private val binding: ItemGestioneUtentiBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(utente: Utente) {
            binding.usernameText.text = utente.nome + " " + utente.cognome
            binding.avatarInitialsTextView.text = utente.nome[0].toString() + utente.cognome[0].toString()
            binding.emailText.text = utente.email
            binding.reportedIcon.visibility = if (utente.segnalato) View.VISIBLE else View.GONE
            binding.reportBadge.visibility = if (utente.segnalato) View.VISIBLE else View.GONE
            binding.avatarInitialsTextView.setBackgroundResource(if (utente.segnalato) R.drawable.avatar_circle_background_reported else R.drawable.avatar_circle_background)
            if (utente.segnalato)
                binding.avatarInitialsTextView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))


            when (utente.stato) {
                Stato.OK -> {
                    binding.tvStatoUtente.setText(R.string.ok)
                    binding.tvStatoUtente.setBackgroundResource(R.drawable.bg_status_accepted)
                }
                Stato.SOSPESO -> {
                    binding.tvStatoUtente.setText(R.string.sospeso)
                    binding.tvStatoUtente.setBackgroundResource(R.drawable.bg_status_pending)
                }
                Stato.BANDITO -> {
                    binding.tvStatoUtente.setText(R.string.bandito)
                    binding.tvStatoUtente.setBackgroundResource(R.drawable.bg_status_rejected)
                }
            }

            if (utente.tipo != TipoUtente.ADMIN) {
                binding.root.setOnClickListener {
                    setUserReportRead(utente.id)
                    val sheet = GestioneUtentiBottomSheet(utente) { st ->
                        when (st) {
                            Stato.SOSPESO -> {
                                binding.tvStatoUtente.setText(R.string.sospeso)
                                binding.tvStatoUtente.setBackgroundResource(R.drawable.bg_status_pending)
                            }
                            Stato.BANDITO -> {
                                binding.tvStatoUtente.setText(R.string.bandito)
                                binding.tvStatoUtente.setBackgroundResource(R.drawable.bg_status_rejected)
                            }
                            else -> {
                                binding.tvStatoUtente.setText(R.string.ok)
                                binding.tvStatoUtente.setBackgroundResource(R.drawable.bg_status_accepted)
                            }
                        }
                    }
                    sheet.show(fragmentManager, "GestioneUtentiBottomSheet")
                }
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val binding = ItemGestioneUtentiBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: Utente) {
        p0.bind(p2)
    }
}