# **Documentazione Ufficiale e Specifiche Funzionali** 

**Applicazione: Workout Tracker** 

**Package: it.armandopagliara.workouttracker** 

**Versione Corrente: DEV_0.2.12** **_(Versione target per la release ufficiale: 1.0.0)_ Piattaforma: Android (API minimale 21 / Target 34)** 

## **1. Panoramica del Sistema** 

**Workout Tracker** è un'applicazione nativa Android concepita per tracciare e gestire schede di allenamento e tempi di recupero in palestra in modo rapido, intuitivo e senza distrazioni. L'applicazione opera interamente in modalità **Offline-First** , archiviando i dati localmente in formato CSV per garantire massima portabilità ed edilità da parte dell'utente. Durante le sessioni d'allenamento, lo schermo del dispositivo viene mantenuto costantemente attivo (FLAG_KEEP_SCREEN_ON) per evitare il blocco del display durante i tempi di recupero. 

### **2. Architettura del Sistema e Configurazioni di Build** 

### **2.1 Pattern Architetturale e Struttura Modulare** 

Il sistema adotta l'architettura **Unidirectional Data Flow (UDF)** implementata in **Jetpack Compose** con **Material Design 3** . La struttura modulare del codice è organizzata come segue: 

```
it.armandopagliara.workouttracker/
```

```
├── MainActivity.kt               # Entry point, gestione dello stato reattivo e
TopBar/Dialogs
├── data/
│   └── WorkoutCsvManager.kt      # Lettura, scrittura, import/export e parsing del CSV
├── model/
│   └── Exercise.kt               # Data class del modello dati dell'esercizio
└── ui/
    ├── components/
    │   ├── ExerciseCard.kt       # Card esercizio con frecce ↑ / ↓ e azioni rapide
    │   └── StopwatchPanel.kt     # Pannello reattivo per Timer / Cronometro con allarmi
    └── dialogs/
        ├── ExerciseFormDialog.kt # Modal per inserimento e modifica esercizi
        └── WelcomeTutorialDialog.kt # Guida introduttiva e changelog della versione
```

### **2.2 Archiviazione Dati** 

1. **File CSV Locale (workout_config.csv):** Salvato nella cartella context.filesDir della memoria interna. Conserva le schede, gli esercizi, i carichi (Kg) e le note. 

2. **SharedPreferences (workout_tracker_prefs):** Memorizza le preferenze di sistema, inclusa la versione dell'app relativa all'ultimo onboarding completato. 

### **2.3 Gestione della Versione e Configurazioni Gradle** 

- **Versione Centralizzata:** La versione dell'applicazione è definita esclusivamente in app/build.gradle tramite versionName ed esposta al codice Kotlin attraverso la classe generata BuildConfig.VERSION_NAME. 

- **MultiDex:** Abilitato (multiDexEnabled true) in app/build.gradle per gestire l'indicizzazione DEX di librerie di icone estese (material-icons-extended). 

- **Memoria JVM Build:** Configurato org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m in gradle.properties per prevenire errori di saturazione memoria (JVM heap space) in fase di compilazione. 

### **3. Specifiche Funzionali (DEV_0.2.12)** 

### **3.1 Gestione Schede (Tab) e Riordinamento** 

- **Navigazione:** Le schede sono organizzate in una barra a scorrimento orizzontale (ScrollableTabRow). 

- **Riordinamento Schede (< e >):** Sulla scheda attiva compaiono le frecce **<** e **>** per spostare l'intera scheda a sinistra o a destra nell'elenco. 

- **Creazione, Rinomina ed Eliminazione:** Pulsante + Nuova scheda per creare una scheda; icona matita sulla scheda attiva per rinominarla; pulsante in fondo alla lista esercizi per eliminare l'intera scheda previa conferma. 

- **Persistenza Stato:** Il cambio di orientamento dello schermo o l'invio dell'app in background preserva la scheda selezionata grazie a rememberSaveable. 

### **3.2 Gestione Esercizi (CRUD, Riordinamento e Copia Smart)** 

- **Campi Dinamici:** La scheda mostra solo gli attributi popolati (muscoli, target, recupero, kg, note), nascondendo i campi vuoti. 

- **Riordinamento Esercizi (↑ e ↓):** Toccare una card esercizio per evidenziarla fa comparire sulla sinistra le frecce **↑** e **↓** per spostare l'esercizio in alto o in basso nella scheda corrente. 

- **Copia & Incolla Smart con Gestione Duplicati:** Tap sull'icona di copia (📋) per aprire il modal _"Incolla in:"_ e selezionare la scheda di destinazione. Se l'esercizio esiste già nella scheda target, viene automaticamente rinominato con il suffisso " copia" (es. _"Squat copia"_ ). 

### **3.3 Cronometro e Timer Reattivo Intelligente** 

- **Modalità Cronometro / Timer:** Evidenziando un esercizio con recupero valido (>0 sec), la barra inferiore si converte automaticamente nel conto alla rovescia. 

- **Sincronizzazione Immediata:** La modifica del tempo di recupero di un esercizio selezionato aggiorna e ricarica all'istante il timer senza dover deselezionare la card. 

- **Protezione Cambio Esercizio:** Se un timer è in esecuzione, il cambio di esercizio richiede conferma per evitare interruzioni accidentali. 

- **Allarme Visivo e Sonoro:** Al termine del recupero, il pannello lampeggia in rosso/arancio ed esegue 3 bip cadenzati basati sul tono TONE_CDMA_LOW_L (~500 Hz, nota Re 5). L'audio è disattivabile tramite il toggle 🔔/🔔. 

### **3.4 Interfaccia TopBar e Gestione CSV** 

- **TopBar Essenziale:** Pulsante delle impostazioni contrassegnato dalla sola icona dell'ingranaggio **⚙️�** . 

- **Gestione CSV Integrata:** Accesso protetto da conferma al menu per Modifica Testo CSV, Importazione/Esportazione via Storage Access Framework e Ripristino della configurazione predefinita. 

### **3.5 Onboarding e Tracciamento Aggiornamenti** 

- Modal di benvenuto (WelcomeTutorialDialog) visualizzato alla prima installazione e ad ogni aggiornamento di versione (con opzione _"Non mostrare più per questa versione"_ ). Accessibile anche in qualsiasi momento tramite l'icona Info (🛈). 

## **4. Struttura del Modello Dati CSV** 

Esempio di file CSV: 

```
    giorno,nome,muscoli,target,recupero,kg,note
    Giorno 1: Gambe,Riscaldamento,Tapis roulant,5',300,,
    Giorno 1: Gambe,Leg Extension,Quadricipiti,3 x 8,120,,
```

### Struttura dei campi CSV: 

|**Campo**|**Tipo**|**Descrizione**|
|---|---|---|
|giorno|String|Nome della scheda|
|nome|String|Nome dell'esercizio|



|**Campo**|**Tipo**|**Descrizione**|
|---|---|---|
|muscoli|String|Gruppo muscolare (opzionale)|
|target|String|Serie e reps (es. 3 x 8)|
|recupero|Integer (String)|Tempo in secondi|
|kg|String|Carico utilizzato (opzionale)|
|note|String|Note libere dell'utente (opzionale)|



### **5. Registro delle Versioni (Changelog)** 

- **DEV_0.2.12:** Aggiunte frecce direzionali **<** e **>** per riordinare le schede nella barra delle Tab; aggiornato il tutorial di benvenuto. 

- **DEV_0.2.11:** Spostata la versione in BuildConfig.VERSION_NAME legata a app/build.gradle. 

- **DEV_0.2.10:** Sincronizzazione automatica del timer alle modifiche del recupero; ammorbidito l'allarme sonoro (TONE_CDMA_LOW_L / Re 5); TopBar con icona ingranaggio **⚙️�** . 

- **DEV_0.2.9:** Gestione automatica del suffisso " copia" negli esercizi duplicati. 

- **DEV_0.2.8:** Frecce **↑** / **↓** per riordinare gli esercizi; nuovo modal _"Incolla in:"_ . 

- **v1.1.1 - v1.1.7:** Onboarding tutorial, Storage Access Framework, supporto MultiDex e gestione della memoria Gradle a 2GB. 

