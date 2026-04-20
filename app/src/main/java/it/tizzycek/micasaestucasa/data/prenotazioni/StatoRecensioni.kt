package it.tizzycek.micasaestucasa.data.prenotazioni

data class StatoRecensioni(var daRecensireProprietario: Boolean = true, var daRecensireAffittuario: Boolean = true) {
    companion object {
        fun StatoRecensioni.toHashMap(): Map<String, Any?> {
            return hashMapOf(
                "daRecensireProprietario" to daRecensireProprietario,
                "daRecensireAffittuario" to daRecensireAffittuario
            )
        }

        fun Map<String, Any?>.toStatoRecensioni(): StatoRecensioni {
            return StatoRecensioni(
                daRecensireProprietario = this["daRecensireProprietario"] as? Boolean ?: true,
                daRecensireAffittuario = this["daRecensireAffittuario"] as? Boolean ?: true
            )
        }
    }
}
