# Mi casa es tu casa

Il progetto Android si intitola “**Mi casa es tu casa**” e ha lo scopo di aiutare le persone ad affittare la propria casa per brevi periodo quando non la usano.  
Quindi sono previsti 3 tipi di utenti: chi affitta casa (**proprietario**), chi la prende in affitto (**affittuario**) e il proprietario del sistema (**admin**).  
L'utente admin è da prevedere solo se si implementa almeno una delle funzionalità che lo riguardano (vedi sotto).  
Di seguito sono riportate tutte le funzionalità delle due modalità di sviluppo, quella con voto massimo 25/30 e quella con voto massimo 30 e lode. Nelle due tabelle a fine descrizione saranno distinte quali sono le funzionalità di base quali quelle avanzate.

Ogni casa (o stanza) è definita dal nome del proprietario, dalla posizione geografica, da una serie di foto, da una descrizione, dal costo giornaliero, il numero di persone che si possono ospitare e della disponibilità (ovvero il periodo in cui è possibile affittare).  
Oltre alla descrizione a testo libero, l'utente che affitta potrà descrivere la propria proposta attraverso parole chiave preimpostate (ad. esempio, bagno in condivisione o privato, piscina, wifi, vicino alla metrò, cucina disponibile, etc).

Esistono tre tipi di utente:

- admin: il proprietario dell'app, che può decidere quali solo ne caratteristiche pre-impostate (vedi punto sopra), bannare o sospendere utenti
- utente che propone (proprietario): descrive quello che vuole affittare (casa o stanza) e riceve richieste
- utente che vuole affittare (affittuario): scorre le varie proposte (mettendo dei filtri di ricerca) e si propone come affittuario

Tra gli utenti è possibile avere una chat che può includere delle foto oltre ai messaggi testuali, per ogni nuovo messaggio in chat deve apparire una notifica nel device del destinatario.  

L'utente che propone l'affitto può poter gestire più proposte, lo stesso dicasi per l'utente che cerca un affitto: può fare più prenotazioni.

Le varie proposte devono essere visibili su google maps sotto forma di marker.

Sia l'affittuario che il proprietario posso lasciare una votazione e un commento riguardante la contro parte. Per entrambi le parti è possibile prevedere dei badge o dei livelli a seconda della media del punteggio che ottengono tramite le votazioni.

## Consegna

Consegnare tramite l'apposita risorsa "Consegna relazione finale" la relazione in PDF la quale deve iniziare cone le seguenti informazioni (oltre alla tabella indicata nella risorsa modalità d'esame):

- nome utente e password di almeno 2 utenti per tiologia (e dell'utente admin se si implementano le funzionalità che lo coinvolgono)
	
Le seguenti 2 tabelle compilate in cui si specifica cosa è stato implementato, cosa non è stato implementato e cosa è stato implementato in modo parziale

Funzionalità per voto max 25/30	
1) Creare/modificare/cancellare una prenotazione (operazioni fatte da parte dell'affittuario) o le caratteristiche di una proposta (operazioni fatte dal proprietario)
2) Ricerca proposte tramite data, luogo e numero di persone
3) Commento e voti a fine esperienza sia da parte del proprietario che dell'affittuario e viceversa
4) Chat tra utenti con foto
5) Un proprietario può accettare più prenotazioni e un affittuario può fare prenotare
6) Ricezione notifiche (nuovo messaggio in chat, nuova richiesta di prenotazione)
7) Visualizzazione delle proposte su google maps

Funzionalità per voto >25/30
1) Riconoscimento per utenti che superano un certo voto medio (badge o altro)
9) Storico delle prenotazioni effettuate (affittuario) o ricevute (proprietario)
10) Possibilità di segnalare un utente (sia proprietario che affittuario) che si è comportato male
11) Ricerca avanzata: oltre ai parametri del punto 2, anche tramite "parole chiave"
12) Admin ==> possibilità di aggiunge/modificare/eliminare parole chiave
13) Admin ==> possibilità di sospendere o bloccare un utente
14) Admin ==> Schermata statistiche: n. prenotazioni totali/concluse/in corso/da iniziare.	 
    Il resto della relazione deve contenere informazioni utili sulle strutture dati scelte, descrizione delle scelte implementative più significative corredate da pezzi di codice commentati.