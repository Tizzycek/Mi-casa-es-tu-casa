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
import { onDocumentCreated } from "firebase-functions/v2/firestore"
import { setGlobalOptions } from "firebase-functions/v2";
import * as admin from "firebase-admin";

admin.initializeApp();
setGlobalOptions({ region: "europe-west1" });

export const gestoreNotifichePrenotazioni = onDocumentWritten("prenotazioni/{prenotazioneId}", async (event) => {
    const after = event.data?.after.data();
    const before = event.data?.before.data();

    if (!after) return;

    let recipientId = ""; // chi riceve la notifica
    let actionType = ""; // per cosa (filtro per traduzione sull'app)

    const statoNuovo = after.statoPrenotazione;
    const statoVecchio = before?.statoPrenotazione;

    // --- LOGICA DI SELEZIONE AZIONE ---
    if (!before) {
        recipientId = after.proprietarioId;
        actionType = "NUOVA";
    } else if (statoNuovo === "IN_ATTESA" && statoVecchio === "IN_ATTESA" || statoNuovo === "IN_ATTESA" && statoVecchio === "ACCETTATA") {//&& JSON.stringify(before) !== JSON.stringify(after)) {
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

        try {
            const userDoc = await admin.firestore().collection("utenti").doc(recipientId).get();
            const userData = userDoc.data();
    
            if (!userData || !userData.fcmToken) {
                console.log(`Notifica non inviata: Token mancante per ${recipientId}`);
                return;
            }
    
            const message = {
                //notification: { title, body },
                token: userData.fcmToken,
                data: {
                    type: "PRENOTAZIONE",
                    actionType
                }
            };
    
            await admin.messaging().send(message);
            
        } catch (error) {
            console.error("Errore: ", error)
        }

    }
});


export const gestoreNotificheMessaggi = onDocumentCreated("chats/{chatId}/messages/{messageId}",  async (event) => {
        const messageData = event.data?.data();
        if (!messageData) return;

        // Campi data class Messaggio
        const senderId = messageData.mittente as string; 
        const receiverId = messageData.destinatario as string;
        const chatId = event.params.chatId;
        const contenuto = (messageData.contenuto as string) || "";

        let messagePreview: string;
        const imageUrls = messageData.imageUrl as string[] || [];
        if (imageUrls.length > 0) {
            messagePreview = contenuto
                ? `📷 ${contenuto.substring(0, 40)}...`
                : "📷";
        } else {
            messagePreview = contenuto.length > 60
                ? contenuto.substring(0, 60) + "..."
                : contenuto;
        }

        // Evita notifiche a se stessi
        if (senderId === receiverId) return;

        let nome = "";
        let cognome = ""
        try {
            const chatDoc = await admin.firestore().collection("chats") .doc(chatId).get();

            const chatData = chatDoc.data();
            const userData = chatData?.userData as Record<string, any> | undefined;

            // userData ha struttura: { "userId": { "nome": "...", "cognome": "..." } }
            if (userData?.[senderId]) {
                const nomeDoc = userData[senderId].nome as string || "";
                const cognomeDoc = userData[senderId].cognome as string || "";
                nome = nomeDoc
                cognome = cognomeDoc
            }
        } catch (e) {
            // Fallback: query diretta su utenti (meno efficiente ma sicuro)
            console.warn(`Fallback: recupero nome da utenti per ${senderId}`);
            const userDoc = await admin.firestore()
                .collection("utenti")
                .doc(senderId)
                .get();
            const u = userDoc.data();
            nome = u?.nome
            cognome = u?.cognome
        
        }

        // Recupera token e lingua del destinatario
        const receiverDoc = await admin.firestore()
            .collection("utenti")
            .doc(receiverId)
            .get();
        const receiverData = receiverDoc.data();

        if (!receiverData?.fcmToken) {
            console.log(`⚠️ Token FCM mancante per ${receiverId}`);
            return;
        }

        const messagePayload = {
            //notification: { title, body },
            token: receiverData.fcmToken,
            data: {
                type: "MESSAGGIO",     // ✅ Deve coincidere con il when() in MyFirebaseMessagingService
                chatId,                // ✅ Per aprire la chat corretta
                senderId,              // ✅ Utile per logiche avanzate
                receiverId,             // ✅ Utile per validazioni lato client
                messagePreview,
                nome,
                cognome
            }
        };


        await admin.messaging().send(messagePayload);
        console.log(`✅ Notifica messaggio inviata a ${receiverId} (chat: ${chatId})`);
    }
);