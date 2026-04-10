package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa

import android.app.DatePickerDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.parolechiave.ParoleChiave
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import it.tizzycek.micasaestucasa.data.proprieta.Proprieta
import it.tizzycek.micasaestucasa.databinding.AggiungiModificaProprietaBinding
import it.tizzycek.micasaestucasa.databinding.ItemAvailabilityBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.ui.adapters.ImageAdapter
import it.tizzycek.micasaestucasa.utils.DateValidator
import it.tizzycek.micasaestucasa.utils.findPlace
import it.tizzycek.micasaestucasa.utils.mostraErrore
import it.tizzycek.micasaestucasa.utils.setupParoleChiave
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import java.util.UUID

class AggiungiModificaProprieta: AppCompatActivity() {
    private lateinit var binding: AggiungiModificaProprietaBinding

    private val keywordList = mutableListOf<Chip>()
    private val listaImmagini = mutableListOf<Any>()
    private var vecchieImmagini: List<String> = emptyList()

    private lateinit var imageAdapter: ImageAdapter

    private var isEdit = false
    private var idProprieta: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aggiungi_modifica_proprieta)

        binding = AggiungiModificaProprietaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idProprieta = intent.getStringExtra("PROPERTY_ID")
        isEdit = idProprieta != null

        getKeyWords()
        setupListener()
        setupAdapter()

        if (isEdit) {
            binding.tvAddProperty.setText(R.string.modifica_proprieta)
            caricaDatiProprieta()
        } else {
            setupAvailability() // Mostra riga vuota solo se stiamo aggiungendo
        }
    }

    private fun getKeyWords() {
        binding.cgKeywords.setupParoleChiave(lifecycleScope) { selectedIds ->
            keywordList.addAll(selectedIds)
        }
    }

    private fun setupAvailability() {
        val inflater = LayoutInflater.from(this)
        val rowView = inflater.inflate(R.layout.item_availability, binding.llAvailabilityContainer, false)
        val bindingRowView = ItemAvailabilityBinding.bind(rowView)

        setupAvailabilityButtons(bindingRowView, rowView)

        val defaultCurrency = Currency.getInstance(Locale.getDefault())
        val codes = Currency.getAvailableCurrencies().toList()
        val currencies = codes.sortedWith(compareBy<Currency> { currency ->
            if (currency.currencyCode == defaultCurrency.currencyCode) 0 else 1
        }.thenBy { it.currencyCode })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindingRowView.spinnerCurrency.adapter = adapter

        binding.llAvailabilityContainer.addView(rowView)
    }

    private fun setupAvailabilityButtons(
        bindingRowView: ItemAvailabilityBinding,
        rowView: View?
    ) {
        bindingRowView.btnRemoveRow.setOnClickListener {
            binding.llAvailabilityContainer.removeView(rowView)
        }

        bindingRowView.btnStartDate.setOnClickListener { v ->
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                v.context, { _, selectedYear, selectedMonth, selectedDay ->
                    bindingRowView.btnStartDate.text =
                        LocalDate.of(selectedYear, selectedMonth + 1, selectedDay).toString()
                },
                year, month, day
            ).show()
        }

        bindingRowView.btnEndDate.setOnClickListener { v ->
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                v.context, { _, selectedYear, selectedMonth, selectedDay ->
                    bindingRowView.btnEndDate.text =
                        LocalDate.of(selectedYear, selectedMonth + 1, selectedDay).toString()
                },
                year, month, day
            ).show()
        }
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
                listaImmagini.addAll(uris)
                imageAdapter.updateData(listaImmagini.toList())
            }
        }
        binding.btnAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setupAdapter() {
        imageAdapter = ImageAdapter(listaImmagini) { itemDaRimuovere ->
            listaImmagini.remove(itemDaRimuovere)
            imageAdapter.updateData(listaImmagini.toList()) // Passiamo una copia
        }

        binding.rvPropertyImages.apply {
            layoutManager = LinearLayoutManager(this@AggiungiModificaProprieta, LinearLayoutManager.HORIZONTAL, false)

            adapter = imageAdapter
        }
    }

    private fun newProperty() {
        setButtons(false)

        val nome = binding.etPropertyName.text.toString()
        val indirizzo = binding.etPropertyAddress.text.toString()
        val posti = binding.etPropertySpots.text.toString()
        val descrizione = binding.etPropertyDescription.text.toString()
        val paroleChiave = getSelectedKeywordIds()
        val disponibilita = getDisponibilita()

        
        if (nome.isEmpty() || indirizzo.isEmpty() || posti.isEmpty() || posti.toInt() < 1 || descrizione.isEmpty() || paroleChiave.isEmpty() || listaImmagini.isEmpty()) {
            mostraErrore(R.string.data_missing)
            setButtons(true)
        } else if (disponibilita.isEmpty()) {
            setButtons(true)
        }
        else {
            lifecycleScope.launch {
                try {
                    val immaginiDaCaricare = mutableListOf<ByteArray>()
                    val urlsGiaPresenti = mutableListOf<String>()

                    listaImmagini.forEach { item ->
                        when (item) {
                            is Uri -> {
                                uriToByteArray(item)?.let { immaginiDaCaricare.add(it) }
                            }
                            is String -> {
                                urlsGiaPresenti.add(item)
                            }
                        }
                    }

                    if (immaginiDaCaricare.isEmpty() && urlsGiaPresenti.isEmpty())
                        throw Exception(getString(R.string.nessuna_immagine))

                    val coordinates = requireNotNull(findPlace(indirizzo)) {
                        throw Exception(getString(R.string.invalid_address))
                    }

                    val proprieta = Proprieta(idProprieta ?: UUID.randomUUID().toString(), requireNotNull(MainRepo.getUserRepository().getCurrentUser()!!.id), nome, posti.toInt(), coordinates.latitude, coordinates.longitude, descrizione, paroleChiave, urlsGiaPresenti, disponibilita)

                    val risultato = if (isEdit) {
                        val immaginiDaEliminare = vecchieImmagini.filterNot { urlsGiaPresenti.contains(it) }
                        MainRepo.getProprietaRepository().aggiornaProprieta(proprieta, immaginiDaCaricare, immaginiDaEliminare)
                    } else {
                        MainRepo.getProprietaRepository().aggiungiProprieta(proprieta, immaginiDaCaricare)
                    }

                    risultato.fold(
                        onSuccess = {
                            val intent = Intent()
                            intent.putExtra("LATITUDINE", coordinates.latitude)
                            intent.putExtra("LONGITUDINE", coordinates.longitude)
                            setResult(RESULT_OK, intent)

                            finish()
                        },
                        onFailure = { errore ->
                            val messaggio = errore.message ?: getString(R.string.unknown_error)
                            mostraErrore(messaggio)
                            setButtons(true)
                        }
                    )
                } catch (e: Exception) {
                    mostraErrore(e.message ?: getString(R.string.unknown_error))
                    setButtons(true)
                }
            }
        }
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
                return emptyList()
            } else if (DateValidator.startIsAfterEnd(LocalDate.parse(startDate), LocalDate.parse(endDate)) || DateValidator.isBeforeToday(LocalDate.parse(startDate))){
                mostraErrore(R.string.date_error)
                return emptyList()
            }
            else {
                availabilityList.add(Disponibilita(UUID.randomUUID().toString(), LocalDate.parse(startDate), LocalDate.parse(endDate), prezzo.toDouble(),requireNotNull(currency)))
            }
        }

        if (DateValidator.hasOverlaps(availabilityList)) {
            mostraErrore(R.string.date_overlap)
            return emptyList()
        } else {
            return availabilityList
        }
    }


    private fun setButtons(enabled: Boolean) {
        if (enabled) {
            binding.progressBar.visibility = View.GONE
            binding.btnSaveProperty.isEnabled = true
            binding.btnAddImage.isEnabled = true
            binding.btnAddAvailability.isEnabled = true
        } else {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSaveProperty.isEnabled = false
            binding.btnAddImage.isEnabled = false
            binding.btnAddAvailability.isEnabled = false
        }
    }

    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            mostraErrore(e.message ?: getString(R.string.unknown_error))
            null
        }
    }

    private fun caricaDatiProprieta() {
        setButtons(false)
        lifecycleScope.launch {
            val result = MainRepo.getProprietaRepository().getProprieta(idProprieta!!)
            result.fold(
                onSuccess = { proprieta ->
                    if (proprieta != null)
                        caricaDatiUI(proprieta)
                    setButtons(true)
                },
                onFailure = {
                    mostraErrore(it.message ?: getString(R.string.unknown_error))
                    setButtons(true)
                    finish()
                }
            )
        }
    }

    private fun caricaDatiUI(p: Proprieta) {
        binding.etPropertyName.setText(p.nome)
        binding.etPropertySpots.setText(p.posti.toString())
        binding.etPropertyDescription.setText(p.descrizione)

        val geocoder = Geocoder(this, Locale.getDefault())
        geocoder.getFromLocation(p.latitudine, p.longitudine, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    runOnUiThread {
                        binding.etPropertyAddress.setText(address)
                    }
                }
            }

            override fun onError(errorMessage: String?) {
                runOnUiThread {
                    binding.etPropertyAddress.setText(errorMessage)
                }
            }
        })

        p.immaginiUrl?.let { urls ->
            vecchieImmagini = urls
            listaImmagini.addAll(urls)
            imageAdapter.updateData(listaImmagini.toList())
        }

        for (i in 0 until binding.cgKeywords.childCount) {
            val chip = binding.cgKeywords.getChildAt(i) as? Chip
            val keyword = chip?.tag as? ParoleChiave
            if (keyword != null && p.paroleChiaveIds.contains(keyword.id)) {
                chip.isChecked = true
                chip.isCheckedIconVisible = true
                chip.setChipBackgroundColorResource(R.color.secondary_light)
                keywordList.add(chip)
            }
        }

        p.disponibilita.forEach { dis ->
            aggiungiRigaDisponibilitaPopolata(dis)
        }
    }

    private fun aggiungiRigaDisponibilitaPopolata(d: Disponibilita) {
        val inflater = LayoutInflater.from(this)
        val rowView = inflater.inflate(R.layout.item_availability, binding.llAvailabilityContainer, false)
        val bindingRowView = ItemAvailabilityBinding.bind(rowView)

        bindingRowView.btnStartDate.text = d.dataInizio.toString()
        bindingRowView.btnEndDate.text = d.dataFine.toString()
        bindingRowView.etPrice.setText(d.prezzo.toString())

        val codes = Currency.getAvailableCurrencies().toList()
        val currencies = codes.sortedWith(compareBy<Currency> { currency ->
            if (currency.currencyCode == d.valuta.currencyCode) 0 else 1
        }.thenBy { it.currencyCode })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindingRowView.spinnerCurrency.adapter = adapter
        bindingRowView.spinnerCurrency.setSelection(currencies.indexOf(d.valuta))

        setupAvailabilityButtons(bindingRowView, rowView)

        binding.llAvailabilityContainer.addView(rowView)
    }
}