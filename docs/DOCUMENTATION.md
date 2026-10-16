WORKOUT TRACKER - DOCUMENTAZIONE UFFICIALE E SPECIFICHE FUNZIONALI

Applicazione: Workout Tracker
Package: it.armandopagliara.workouttracker
Versione Corrente: 1.0.0
Piattaforma: Android (API minimale 21 / Target SDK 34)


1. PANORAMICA DEL SISTEMA

Workout Tracker e un'applicazione nativa Android concepita per tracciare e gestire schede di allenamento e tempi di recupero in palestra in modo rapido, intuitivo e senza distrazioni. L'applicazione opera in modalita Offline-First: archivia automaticamente la configurazione attiva in memoria locale e permette la gestione di preset di schede memorizzati nell'app e l'import/export tramite file CSV esterni. Durante l'allenamento, lo schermo del dispositivo viene mantenuto attivo (FLAG_KEEP_SCREEN_ON) per evitare il blocco del display durante i tempi di recupero.


2. ARCHITETTURA DEL SISTEMA E CONFIGURAZIONI DI BUILD

2.1 Pattern Architetturale e Struttura Modulare
Il sistema adotta l'architettura Unidirectional Data Flow (UDF) implementata in Jetpack Compose con Material Design 3. La struttura del codice e organizzata come segue:

it.armandopagliara.workouttracker/
- MainActivity.kt: Entry point, gestione dello stato reattivo, TopBar, menu e dialogs
- data/WorkoutCsvManager.kt: Lettura, scrittura, import/export e parsing del CSV
- data/LocalWorkoutStore.kt: Gestione dell'archivio interno dei preset delle schede
- model/Exercise.kt: Data class del modello dati dell'esercizio (campo 'scheda')
- ui/components/ExerciseCard.kt: Card esercizio con frecce direzionali e azioni rapide
- ui/components/StopwatchPanel.kt: Pannello reattivo per Timer / Cronometro con allarmi
- ui/dialogs/ExerciseFormDialog.kt: Modal per inserimento e modifica esercizi
- ui/dialogs/WelcomeTutorialDialog.kt: Guida introduttiva e changelog della versione

2.2 Archiviazione Dati e Preset Locali
1. File CSV Locale Attivo (workout_config.csv): Salvato nella cartella context.filesDir della memoria interna. Conserva la configurazione corrente in uso.
2. Archivio Preset Locali (saved_presets/): Cartella interna all'app dove l'utente puo salvare e ricaricare diverse configurazioni di schede (es. "Massa Inverno", "Definizione 2026") tramite l'interfaccia dell'app.
3. SharedPreferences (workout_tracker_prefs): Memorizza le preferenze di sistema, inclusa la versione dell'app relativa all'ultimo onboarding completato.

2.3 Gestione della Versione e Configurazioni Gradle
- Versione Centralizzata: La versione dell'applicazione e definita esclusivamente nel file app/build.gradle tramite la proprieta versionName ed e resa disponibile al codice Kotlin attraverso la classe generata BuildConfig.VERSION_NAME.
- MultiDex: Abilitato (multiDexEnabled true) in app/build.gradle per gestire l'indicizzazione DEX di librerie di icone estese (material-icons-extended).
- Memoria JVM Build: Configurato org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m in gradle.properties per prevenire errori di saturazione memoria (JVM heap space) in fase di compilazione.


3. SPECIFICHE FUNZIONALI (DEV_0.2.12)

3.1 Gestione Schede (Tab) e Riordinamento
- Navigazione: Le schede sono organizzate in una barra a scorrimento orizzontale (ScrollableTabRow).
- Riordinamento Schede (< e >): Sulla scheda attiva compaiono le frecce < e > per spostare l'intera scheda a sinistra o a destra nell'elenco.
- Creazione, Rinomina ed Eliminazione: Pulsante "+ Nuova scheda" per creare una scheda; icona matita sulla scheda attiva per rinominarla; pulsante in fondo alla lista esercizi per eliminare l'intera scheda previa conferma.
- Persistenza Stato: Il cambio di orientamento dello schermo o l'invio dell'app in background preserva la scheda selezionata grazie a rememberSaveable.

3.2 Gestione Esercizi (CRUD, Riordinamento e Copia Smart)
- Campi Dinamici: La scheda mostra solo gli attributi popolati (muscoli, target, recupero, kg, note), nascondendo i campi vuoti.
- Riordinamento Esercizi (frecce su e giu): Toccare una card esercizio per evidenziarla fa comparire sulla sinistra le frecce per spostare l'esercizio in alto o in basso nella scheda corrente.
- Copia e Incolla Smart con Gestione Duplicati: Tap sull'icona di copia per aprire il modal "Incolla in:" e selezionare la scheda di destinazione. Se l'esercizio esiste gia nella scheda target, viene automaticamente rinominato con il suffisso "copia" (es. "Squat copia").

3.3 Cronometro e Timer Reattivo Intelligente
- Modalita Cronometro / Timer: Evidenziando un esercizio con recupero valido (>0 sec), la barra inferiore si converte automaticamente nel conto alla rovescia.
- Sincronizzazione Immediata: La modifica del tempo di recupero di un esercizio selezionato aggiorna e ricarica all'istante il timer senza dover deselezionare la card.
- Protezione Cambio Esercizio: Se un timer e in esecuzione, il cambio di esercizio richiede conferma per evitare interruzioni accidentali.
- Allarme Visivo e Sonoro: Al termine del recupero, il pannello lampeggia in rosso/arancio ed esegue 3 bip cadenzati basati sul tono TONE_CDMA_LOW_L (~500 Hz, nota Re 5). L'audio e disattivabile tramite il toggle campanella.

3.4 Gestione Preset Locali, Import/Export e TopBar
- TopBar Essenziale: Pulsante delle impostazioni contrassegnato dalla sola icona dell'ingranaggio.
- Salvataggi Locali nell'App: Menu "Salva in Archivio Locale" e "Carica da Archivio Locale" per salvare o ripristinare modelli di schede direttamente nella memoria interna dell'applicazione.
- Gestione CSV Esterna: Modifica del testo CSV integrata, Importazione ed Esportazione su File System tramite Storage Access Framework di Android.

3.5 Onboarding e Tracciamento Aggiornamenti
- Modal di benvenuto (WelcomeTutorialDialog) visualizzato alla prima installazione e ad ogni aggiornamento di versione (con opzione "Non mostrare piu per questa versione"). Accessibile anche in qualsiasi momento tramite l'icona Info.


4. STRUTTURA DEL MODELLO DATI CSV

Esempio di file CSV:
scheda,nome,muscoli,target,recupero,kg,note
Scheda A: Spinta,Panca Piana,Pettorali,4x8,90,70,Bilanciere olimpico
Scheda A: Spinta,Dip,Pettorali / Tricipiti,3x10,60,,Zavorra 10kg
Scheda B: Trazione,Trazioni,Dorsali,4x6,120,,Presa prona

Tabella dei Campi CSV:
- scheda (String): Nome della scheda d'allenamento
- nome (String): Nome dell'esercizio
- muscoli (String): Gruppo muscolare coinvolto (opzionale)
- target (String): Serie e ripetizioni (es. 3x10)
- recupero (Integer/String): Tempo di recupero target espresso in secondi
- kg (String): Carico o peso utilizzato (opzionale)
- note (String): Note libere dell'utente (opzionale)


5. REGISTRO DELLE VERSIONI (CHANGELOG)

- 1.0.0: Versione pronta per il caricamento su Play Store
- DEV_0.3.0: Rinominato il campo "giorno" in "scheda" in tutto il codice e nel CSV; integrato il Gestore di Salvataggi Locali nell'app (LocalWorkoutStore)
- DEV_0.2.12: Aggiunte frecce direzionali < e > per riordinare le schede.
- DEV_0.2.11: Spostata la versione in BuildConfig.VERSION_NAME legata a app/build.gradle.
- DEV_0.2.10: Sincronizzazione automatica del timer alle modifiche del recupero; ammorbidito l'allarme sonoro (TONE_CDMA_LOW_L / Re 5); TopBar con sola icona ingranaggio.
- DEV_0.2.9: Gestione automatica del suffisso "copia" negli esercizi duplicati.
- DEV_0.2.8: Frecce su e giu per riordinare gli esercizi; nuovo modal "Incolla in:".
- v1.1.1 - v1.1.7: Onboarding tutorial, Storage Access Framework, supporto MultiDex e gestione della memoria Gradle a 2GB.