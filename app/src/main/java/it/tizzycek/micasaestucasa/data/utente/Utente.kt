package it.tizzycek.micasaestucasa.data.utente

import android.util.Patterns
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.LocalDate

data class Utente (var id: String?, val email: String, val nome: String, val cognome: String, val nascita: LocalDate, var stato: Stato = Stato.OK, var segnalato: Boolean = false, val tipo: TipoUtente, var fcmToken: String? = null, var suspension: Timestamp? = null) {
    init {
        require(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Not a valid Email"
        }
        require(validAge(nascita)) {
            "Age must be over 18"
        }
    }
    private fun validAge(nascita: LocalDate): Boolean {
        val today = LocalDate.now()
        return today.year - nascita.year >= 18
    }

    companion object {
        fun Utente.toHashMap(): Map<String, Any?> {
            return hashMapOf(
                "uid" to id,
                "email" to email,
                "nome" to nome,
                "cognome" to cognome,
                "nascita" to nascita.toString(),
                "tipo" to tipo,
                "stato" to stato.toString(),
                "segnalato" to segnalato,
                "fcmToken" to fcmToken,
                "suspension" to suspension
            )
        }

        fun DocumentSnapshot.toUtente(): Utente? {
            val data = data ?: return null
            return Utente(
                id = data["uid"] as? String ?: id,
                email = data["email"] as? String ?: "",
                nome = data["nome"] as? String ?: "",
                cognome = data["cognome"] as? String ?: "",
                nascita = LocalDate.parse(data["nascita"] as String),
                tipo = TipoUtente.valueOf(data["tipo"] as? String ?: "AFFITTUARIO"),
                stato = Stato.valueOf(data["stato"] as? String ?: "OK"),
                segnalato = data["segnalato"] as? Boolean ?: false,
                fcmToken = data["fcmToken"] as? String,
                suspension = data["suspension"] as? Timestamp
            )
        }
    }
}