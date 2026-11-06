WORKOUT TRACKER - DOCUMENTAZIONE UFFICIALE E SPECIFICHE FUNZIONALI

Applicazione: Workout Tracker
Package: it.armandopagliara.workouttracker
Versione Corrente: 1.1.0 (versionCode 24)
Piattaforma: Android (API minimale 24 / Target SDK 36 / Compile SDK 36)


1. PANORAMICA DEL SISTEMA

Workout Tracker è un'applicazione nativa Android concepita per tracciare e gestire schede di allenamento e tempi di recupero in palestra in modo rapido, intuitivo e senza distrazioni. L'applicazione opera in modalità Offline-First: archivia automaticamente la configurazione attiva in memoria locale e permette la gestione di preset di schede memorizzati nell'app e l'import/export tramite file CSV esterni. Durante l'allenamento, lo schermo del dispositivo viene mantenuto attivo (FLAG_KEEP_SCREEN_ON) per evitare il blocco del display durante i tempi di recupero.


2. ARCHITETTURA DEL SISTEMA E CONFIGURAZIONI DI BUILD

2.1 Pattern Architetturale e Struttura Modulare
Il sistema adotta l'architettura Unidirectional Data Flow (UDF) implementata in Jetpack Compose con Material Design 3. La struttura del codice è organizzata come segue:

it.armandopagliara.workouttracker/
- MainActivity.kt: Entry point, gestione dello stato reattivo, rotazione tablet/phone, callback di foreground e dialogs
- data/WorkoutCsvManager.kt: Parsing, salvataggio, validazione rigida CSV, gestione preset predefinito "Full Body"
- data/LocalWorkoutStore.kt: Gestione dell'archivio interno dei preset delle schede (assicura la presenza di "Full Body")
- model/Exercise.kt: Data class del modello dati dell'esercizio
- ui/components/ExerciseCard.kt: Card esercizio con layout ottimizzato, supporto scaling +20% e azioni rapide
- ui/components/StopwatchPanel.kt: Pannello reattivo per Timer / Cronometro al decimo di secondo con allarmi visivi/sonori
- ui/dialogs/ExerciseFormDialog.kt: Modal per inserimento e modifica esercizi con limiti di caratteri strict
- ui/dialogs/WelcomeTutorialDialog.kt: Guida introduttiva, changelog e collegamento alla prima installazione

2.2 Archiviazione Dati e Preset Locali
1. File CSV Locale Attivo (workout_config.csv): Salvato nella cartella context.filesDir della memoria interna. Conserva la configurazione corrente in uso.
2. Archivio Preset Locali (saved_presets / SharedPreferences): Archivio interno all'app dove l'utente può salvare e ricaricare diverse configurazioni di schede. Il preset "Full Body" viene sempre pre-caricato in archivio come punto di ripristino.
3. SharedPreferences (workout_tracker_prefs): Memorizza le preferenze di sistema e l'indicatore di versione dell'ultimo onboarding completato.

2.3 Gestione della Versione e Adaptability Layout
- Versione Centralizzata: Definito versionCode 24 e versionName "1.1.0" nel file app/build.gradle.
- Responsive Scaling Tablet: Per dispositivi con $smallestScreenWidthDp \ge 600$, l'interfaccia applica una scala ingrandita del 20% su font, spaziatura ed icone.
- Gestione Rotazione Schermo: Rotazione automatica Landscape/Portrait abilitata solo su Tablet; blocco rigido su Portrait per gli Smartphone.
- Visibility Notification Bar: Configurazione tramite WindowInsetsControllerCompat per garantire visibilità e contrasto alla barra di stato delle notifiche.


3. SPECIFICHE FUNZIONALI (v1.1.0 / DEV_1.0.3)

3.1 Prima Installazione e Preset "Full Body"
- Fallback Iniziale: Alla prima installazione o in assenza di esercizi salvati, l'app si avvia mostrando una scheda vuota intitolata "Scheda personale".
- Popup di Benvenuto & Importazione Guidata: Alla chiusura del WelcomeTutorialDialog dopo il primo avvio, viene proposto il popup "Caricare la scheda d'esempio Full body? (Sì / No, lascia vuoto)".
- Scheda "Full Body" Predefinita: Contiene il riscaldamento "Riscaldamento tapisroulant (10 min, pendenza 3 velocità 5.5)" e i 7 esercizi target (Leg Press, Lat Machine, Chest Press, Shoulder Press, Pulley Basso, Crunch Addominale, Plank Statico). È sempre disponibile e ricaricabile dall'Archivio Locale.

3.2 Gestione Schede (Tab) e Riordinamento
- Navigazione: Le schede sono organizzate in una barra a scorrimento orizzontale (ScrollableTabRow).
- Riordinamento Schede (< e >): Sulla scheda attiva compaiono le frecce < e > per spostare l'intera scheda a sinistra o a destra nell'elenco.
- Creazione, Rinomina ed Eliminazione: Pulsante "+ Nuova scheda" per creare una scheda; icona matita sulla scheda attiva per rinominarla; pulsante dedicato in fondo alla lista esercizi per eliminare l'intera scheda previa conferma.

3.3 Gestione Esercizi (CRUD, Limiti Input e Copia Smart)
- Validazione Rigida Input Form (ExerciseFormDialog):
  * Scheda: max 40 caratteri
  * Nome: max 70 caratteri
  * Muscoli coinvolti: max 40 caratteri
  * Target / Schema: max 20 caratteri
  * Recupero / durata (sec): max 4 caratteri (numeric format)
  * Carico / Kg: max 15 caratteri
  * Note Tecniche: max 300 caratteri
- Posizionamento Azioni: Icone Copia, Modifica ed Elimina posizionate in alto a destra sulla card dell'esercizio.
- Note a Larghezza Intera: I campi Note e Kg occupano l'intera larghezza inferiore per una migliore leggibilità.
- Copia e Incolla Smart: Duplicazione rapida verso qualsiasi scheda target con gestione automatica del suffisso "copia".

3.4 Cronometro, Timer e Foreground Recovery
- Precisione Decimi di Secondo: Modalità Timer / Cronometro sincronizzata reattivamente con l'esercizio selezionale.
- Allarme Visivo e Sonoro: Lampeggio rosso/arancio a tutto schermo al termine del conto alla rovescia ed esecuzione di toni acustici ammorbiditi (~500 Hz). Pulsante dedicato per attivare/disattivare l'audio (🔔/🔕).
- Background Recovery (bringToForeground): Quando il timer giunge a zero con l'app in background o a schermo bloccato, una callback dedicata (onTimerFinished) richiama l'Intent della MainActivity portando immediatamente l'app in primo piano.

3.5 Validazione Importazione CSV e Sezione Gestione
- Rinominata Sezione "Gestione": Il menu accessibile dall'icona ingranaggio è stato unificato sotto la voce "Gestione".
- Filtro Rigido Import CSV: Scarto immediato di file compressi (.zip), file di testo non conformi o file binari mediante controllo delle estensioni (.csv, .txt), scansione dei caratteri nulli (\u0000) e presenza delle intestazioni obbligatorie ("scheda", "nome"). In caso di errore viene mostrato il messaggio "Formato non valido".


4. STRUTTURA DEL MODELLO DATI CSV

Esempio di file CSV:
scheda,nome,muscoli,target,recupero,kg,note
Full Body,Riscaldamento tapisroulant,Cardio,10 min,0,,"pendenza 3 velocità 5.5"
Full Body,Leg Press (o Squat al multipower),Gambe / Glutei,3x10,90,,"Discesa lenta (3 secondi), ginocchia in linea con le punte dei piedi."

Tabella dei Campi CSV:
- scheda (String): Nome della scheda d'allenamento (es. "Full Body", "Scheda personale")
- nome (String): Nome dell'esercizio (max 70 car)
- muscoli (String): Muscoli coinvolti (max 40 car)
- target (String): Target / Schema di allenamento (max 20 car)
- recupero (Integer/String): Recupero/durata espresso in secondi (max 4 car)
- kg (String): Carico o peso utilizzato (max 15 car)
- note (String): Note tecniche libere dell'utente (max 300 car)


5. REGISTRO DELLE VERSIONI (CHANGELOG)

- 1.1.0 (versionCode 24):
  * Consolidamento release per Google Play Console (AAB).
  * Integrazione scheda di default "Full Body" e gestione prima installazione con "Scheda personale" vuota.
  * Callback bringToForeground() per ripristino app da background a fine timer.
  * Validazione rigida per l'importazione di file CSV (blocco file .zip e binari con avviso "Formato non valido").
  * Scaling +20% per dispositivi Tablet (smallestScreenWidthDp >= 600) e sblocco rotazione per tablet.
  * Limiti stringenti di caratteri nei form e rinomina nomenclature ("Muscoli coinvolti", "Recupero/durata", menu "Gestione").
  * Riposizionamento icone azioni in alto a destra e note estese nella card esercizio.
- 1.0.0: Versione iniziale per caricamento su Play Store.