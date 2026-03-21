package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa

import android.app.DatePickerDialog
import android.location.Address
import android.location.Geocoder
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.parolechiave.ParoleChiave
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import it.tizzycek.micasaestucasa.databinding.AggiungiProprietaBinding
import it.tizzycek.micasaestucasa.databinding.ItemAvailabilityBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.ImageAdapter
import it.tizzycek.micasaestucasa.ui.mostraErrore
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import java.util.UUID

class AggiungiProprieta: AppCompatActivity() {
    private lateinit var binding: AggiungiProprietaBinding

    private val keywordList = mutableListOf<Chip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aggiungi_proprieta)

        binding = AggiungiProprietaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getKeyWords()
        setupAvailability()
        setupListener()
    }

    private fun getKeyWords() {
        lifecycleScope.launch {
            MainRepo.getParoleChiave().getParoleChiave().fold(
                onSuccess = { listaParole ->
                    binding.cgKeywords.removeAllViews()

                    listaParole.filter { it.attiva }.forEach { parolaOggetto ->
                        val chip = Chip(this@AggiungiProprieta) 
                        chip.text = parolaOggetto.parola

                        chip.tag = parolaOggetto
                        keywordList.add(chip)

                        chip.isCheckable = true
                        chip.isChecked = false
                        chip.setChipBackgroundColorResource(R.color.secondary_dark)

                        chip.chipIcon = null
                        chip.isChipIconVisible = true
                        chip.setCheckedIconResource(R.color.gray)

                        chip.setOnCheckedChangeListener { button, isChecked ->
                            if (isChecked)
                                chip.setChipBackgroundColorResource(R.color.secondary_light)
                            else
                                chip.setChipBackgroundColorResource(R.color.secondary_dark)
                        }

                        binding.cgKeywords.addView(chip)
                    }
                },
                onFailure = { errore ->
                    mostraErrore("Errore nel caricamento filtri: ${errore.message}")
                }
            )
        }
    }

    private fun setupAvailability() {
        val inflater = LayoutInflater.from(this)
        val rowView = inflater.inflate(R.layout.item_availability, binding.llAvailabilityContainer, false)
        val bindingRowView = ItemAvailabilityBinding.bind(rowView)

        bindingRowView.btnRemoveRow.setOnClickListener {
            binding.llAvailabilityContainer.removeView(rowView)
        }

        bindingRowView.btnStartDate.setOnClickListener { v ->
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog( v.context,{ _, selectedYear, selectedMonth, selectedDay ->
                    bindingRowView.btnStartDate.text = LocalDate.of(selectedYear, selectedMonth, selectedDay).toString()
                },
                year, month, day).show()
        }

        bindingRowView.btnEndDate.setOnClickListener { v ->
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog( v.context,{ _, selectedYear, selectedMonth, selectedDay ->
                bindingRowView.btnEndDate.text = LocalDate.of(selectedYear, selectedMonth, selectedDay).toString()
            },
                year, month, day).show()
        }

        val codes = Currency.getAvailableCurrencies().sortedBy { it.currencyCode }
        val currencies = codes.mapNotNull { code ->
            try {
                Currency.getInstance(code.currencyCode)
            } catch (e: Exception) {
                null
            }
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            currencies
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindingRowView.spinnerCurrency.adapter = adapter

        binding.llAvailabilityContainer.addView(rowView)
    }

    private fun setupListener() {
        binding.btnSaveProperty.setOnClickListener {
            lifecycleScope.launch {
                newProperty()
            }
        }

        binding.btnAddAvailability.setOnClickListener { setupAvailability() }

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                val adapter = binding.rvPropertyImages.adapter as ImageAdapter
                adapter.setImages(uris)
            }
        }
        binding.btnAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun newProperty() {
        val nome = binding.etPropertyName.text.toString()
        val indirizzo = binding.etPropertyAddress.text.toString()
        val posti = binding.etPropertySpots.text.toString()
        val descrizione = binding.etPropertyDescription.text.toString()
        val paroleChiave = getSelectedKeywordIds()
        val disponibilita = getDisponibilita()

        
        if (nome.isEmpty() || indirizzo.isEmpty() || posti.toInt() < 1 || descrizione.isEmpty() || paroleChiave.isEmpty() || disponibilita.isEmpty()) {
            mostraErrore(R.string.data_missing)
        } else {
            val coordinates = findPlace(indirizzo)

        }

    }

    private fun findPlace(address: String): Address? {
        val geocoder = Geocoder(this, Locale.getDefault())
        var returnAddress: Address? = null
        geocoder.getFromLocationName(address, 1, object: Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (addresses.isNotEmpty()) {
                    returnAddress = addresses[0]
                }
            }
            override fun onError(errorMessage: String?) {
                mostraErrore("Errore: $errorMessage")
            }
        })
        return returnAddress
    }

    private fun getSelectedKeywordIds(): List<String> {
        return keywordList
            .filter { it.isChecked }
            .mapNotNull { (it.tag as? ParoleChiave)?.id }
    }

    private fun getDisponibilita(): List<Disponibilita> {
        val availabilityList = mutableListOf<Disponibilita>()

        for (i in 0 until binding.llAvailabilityContainer.childCount) {
            val rowView = binding.llAvailabilityContainer.getChildAt(i)

            val rowBinding = ItemAvailabilityBinding.bind(rowView)

            val startDate = rowBinding.btnStartDate.text.toString()
            val endDate = rowBinding.btnEndDate.text.toString()
            val prezzo = rowBinding.etPrice.text.toString()
            val currency = rowBinding.spinnerCurrency.selectedItem as? Currency

            if (startDate.isEmpty() || endDate.isEmpty()  || prezzo.isEmpty()) {
                mostraErrore(R.string.data_missing)
            } else {
                availabilityList.add(Disponibilita(UUID.randomUUID().toString(), LocalDate.parse(startDate), LocalDate.parse(endDate), prezzo.toDouble(),requireNotNull(currency)))
            }
        }

        return availabilityList
    }
}