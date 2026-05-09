package it.tizzycek.micasaestucasa.data.recensioni

data class Segnalazione(val id: String, val segnalatoreId: String, val testo: String, val motivazione: SegnalazioneMotivazione) {
    companion object {
        fun Segnalazione.toHashMap(): Map<String, Any?> {
            return hashMapOf(
                "id" to id,
                "segnalatoreId" to segnalatoreId,
                "testo" to testo,
                "motivazione" to motivazione.name
            )
        }

        fun Map<String, Any?>.toSegnalazione(): Segnalazione {
            return Segnalazione(
                id = this["id"] as? String ?: "",
                segnalatoreId = this["segnalatoreId"] as? String ?: "",
                testo = this["testo"] as? String ?: "",
                motivazione = SegnalazioneMotivazione.valueOf(this["motivazione"] as? String ?: SegnalazioneMotivazione.CONTENUTO_INAPPROPRIATO.name)
            )
        }
    }
}
