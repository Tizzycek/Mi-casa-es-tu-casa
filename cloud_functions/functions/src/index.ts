/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

//import {setGlobalOptions} from "firebase-functions";
//import {onRequest} from "firebase-functions/https";
//import * as logger from "firebase-functions/logger";

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
//setGlobalOptions({ maxInstances: 10 });

// export const helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { setGlobalOptions } from "firebase-functions/v2";
import * as admin from "firebase-admin";

admin.initializeApp();
setGlobalOptions({ region: "europe-west1" });

// 1. Definiamo i testi in un oggetto dedicato
const TRANSLATIONS: any = {
    it: {
        NUOVA: { title: "Nuova Prenotazione!", body: "Hai una nuova richiesta di prenotazione." },
        MODIFICATA: { title: "Prenotazione Modificata", body: "L'affittuario ha modificato i dettagli della richiesta." },
        ANNULLATA: { title: "Prenotazione Annullata", body: "Una prenotazione è stata annullata dall'utente." },
        ACCETTATA: { title: "Richiesta Accettata!", body: "Il proprietario ha confermato il tuo soggiorno." },
        RIFIUTATA: { title: "Richiesta Rifiutata", body: "Purtroppo la tua richiesta non è stata accettata." }
    },
    en: {
        NUOVA: { title: "New Booking!", body: "You have a new booking request." },
        MODIFICATA: { title: "Booking Modified", body: "The tenant has modified the request details." },
        ANNULLATA: { title: "Booking Cancelled", body: "A booking has been cancelled by the user." },
        ACCETTATA: { title: "Request Accepted!", body: "The owner has confirmed your stay." },
        RIFIUTATA: { title: "Request Rejected", body: "Unfortunately, your request was not accepted." }
    }
};

export const gestoreNotifichePrenotazioni = onDocumentWritten("prenotazioni/{prenotazioneId}", async (event) => {
    const after = event.data?.after.data();
    const before = event.data?.before.data();

    if (!after) return;

    let recipientId = "";
    let actionType = ""; // Useremo questo per pescare la traduzione corretta

    const statoNuovo = after.statoPrenotazione;
    const statoVecchio = before?.statoPrenotazione;

    // --- LOGICA DI SELEZIONE AZIONE ---
    if (!before) {
        recipientId = after.proprietarioId;
        actionType = "NUOVA";
    } else if (statoNuovo === "IN_ATTESA" && statoVecchio === "IN_ATTESA" && JSON.stringify(before) !== JSON.stringify(after)) {
        recipientId = after.proprietarioId;
        actionType = "MODIFICATA";
    } else if (statoNuovo === "ANNULLATA" && statoVecchio !== "ANNULLATA") {
        recipientId = after.proprietarioId;
        actionType = "ANNULLATA";
    } else if (statoNuovo === "ACCETTATA" && statoVecchio === "IN_ATTESA") {
        recipientId = after.affittuarioId;
        actionType = "ACCETTATA";
    } else if (statoNuovo === "RIFIUTATA" && statoVecchio === "IN_ATTESA") {
        recipientId = after.affittuarioId;
        actionType = "RIFIUTATA";
    }

    if (recipientId && actionType) {
        await inviaPushTradotta(recipientId, actionType/*, after.nome || ""*/);
    }
});

/**
 * Funzione che recupera la lingua dell'utente e invia la notifica corretta
 */
async function inviaPushTradotta(userId: string, action: string/*, nomeProprieta: string*/) {
    try {
        // Recuperiamo il documento dell'utente destinatario
        const userDoc = await admin.firestore().collection("utenti").doc(userId).get();
        const userData = userDoc.data();

        if (!userData || !userData.fcmToken) {
            console.log(`Notifica non inviata: Token mancante per ${userId}`);
            return;
        }

        // 2. Determiniamo la lingua (default "it" se non specificata)
        const lang = userData.lingua === "en" ? "en" : "it";
        
        // 3. Prendiamo i testi corrispondenti
        let title = TRANSLATIONS[lang][action].title;
        let body = TRANSLATIONS[lang][action].body;

        // Aggiungiamo il nome della proprietà se serve (es. Nuova o Accettata)
        // if (action === "NUOVA" || action === "ACCETTATA") {
        //     body += nomeProprieta;
        // }

        
        const message = {
            notification: { title, body },
            token: userData.fcmToken,
            data: { type: "PRENOTAZIONE" }
        };
        
        if (((action === "NUOVA" || action === "MODIFICATA" || action === "ANNULLATA") && userData.tipoUtente === "PROPRIETARIO") || (((action === "ACCETTATA" || action === "RIFIUTATA") && userData.tipoUtente === "AFFITTUARIO"))) {
            await admin.messaging().send(message);
            console.log(`Notifica [${action}] inviata in lingua [${lang}] a ${userId}`);
        }
    } catch (error) {
        console.error("Errore invio push multilingua:", error);
    }
}