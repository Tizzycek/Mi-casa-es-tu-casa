package it.tizzycek.micasaestucasa.data.proprieta

import com.google.android.gms.maps.GoogleMap
import it.tizzycek.micasaestucasa.data.utente.Utente

data class Proprieta(var proprietario: Utente, var posti: Int, var location: GoogleMap)
//proprietario, posti, luogo, foto?