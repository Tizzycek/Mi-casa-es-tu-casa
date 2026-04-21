package it.tizzycek.micasaestucasa.data.recensioni

import it.tizzycek.micasaestucasa.R

// TODO: vedere cosa tenere o eliminare di tutto questo
enum class SegnalazioneMotivazione(val id: Int, val titoloResId: Int, val descrizioneResId: Int, val richiedeDettagli: Boolean = false) {
    CONTENUTO_INAPPROPRIATO(id = 1, titoloResId = R.string.contenuto_inappropriato, descrizioneResId = R.string.contenuto_inappropriato_desc),
    INFORMAZIONI_FALSE(id = 2, titoloResId = R.string.informazioni_false, descrizioneResId = R.string.informazioni_false_desc),
    COMPORTAMENTO_SCORRETTO(id = 3, titoloResId = R.string.comportamento_scorretto, descrizioneResId = R.string.comportamento_scorretto_desc),
    SPAM(id = 4, titoloResId = R.string.spam, descrizioneResId = R.string.spam_desc),
    ALTRO(id = 5, titoloResId = R.string.altro, descrizioneResId = R.string.altro_desc, richiedeDettagli = true);
}