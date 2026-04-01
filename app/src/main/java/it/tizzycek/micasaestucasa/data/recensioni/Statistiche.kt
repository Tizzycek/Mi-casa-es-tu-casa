package it.tizzycek.micasaestucasa.data.recensioni

data class Statistiche(var mediaRecensioni: Double = 0.0, var totaleRecensioni: Int = 0) {
    companion object {
        fun Statistiche.toHashMap(): Map<String, Any?> {
            return hashMapOf(
                "mediaRecensioni" to mediaRecensioni,
                "totaleRecensioni" to totaleRecensioni
            )
        }

        fun Map<String, Any?>.toStatistiche(): Statistiche {
            return Statistiche(
                mediaRecensioni = this["mediaRecensioni"] as? Double ?: 0.0,
                totaleRecensioni = this["totaleRecensioni"] as? Int ?: 0
            )
        }
    }
}
