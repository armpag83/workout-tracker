# Documentazione Ufficiale e Specifiche Funzionali
**Applicazione:** Workout Tracker  
**Package:** `it.armandopagliara.workouttracker`  
**Versione Release:** `1.1.1`  
**Piattaforma:** Android (API minimale 21 / Target 34)  

---

## 1. Panoramica del Sistema
Workout Tracker è un'applicazione nativa Android concepita per tracciare e gestire schede di allenamento e tempi di recupero in palestra in modo rapido e senza distrazioni.
L'applicazione opera interamente in modalità **Offline-First**, archiviando i dati localmente in formato CSV per garantire massima portabilità ed edilità da parte dell'utente.

---

## 2. Architettura del Sistema

### 2.1 Pattern Architetturale
Il sistema adotta l'architettura **Unidirectional Data Flow (UDF)** implementata con **Jetpack Compose**:
* **UI Layer (`MainActivity.kt`):** Interamente sviluppato in dichiarativo con Material Design 3. Gestisce lo stato reattivo dell'interfaccia via `remember` e `rememberSaveable`.
* **Data & Storage Layer (`WorkoutModel.kt`):** Gestisce il modello dati (`Exercise`) e l'I/O su memoria interna tramite `WorkoutCsvManager`.

### 2.2 Archiviazione Dati
1. **File CSV Locale (`workout_config.csv`):** Salvato nella cartella `context.filesDir`. Conserva le schede, gli esercizi, i carichi (Kg) e le note.
2. **SharedPreferences (`workout_tracker_prefs`):** Memorizza le preferenze di sistema, inclusa la versione dell'app relativa all'ultimo onboarding completato.

---

## 3. Specifiche Funzionali (Versione 1.1.1)

### 3.1 Gestione Schede (Tab)
* **Navigazione:** Le schede sono organizzate in una barra a scorrimento orizzontale (`ScrollableTabRow`).
* **Rinominazione Scheda:** Icona di modifica (matita) sulla scheda attiva per aggiornarne il nome ed allineare automaticamente tutti gli esercizi.
* **Persistenza Stato:** In caso di rotazione dello schermo (bloccato in Portrait) o di invio dell'app in background, la scheda attiva viene mantenuta grazie a `rememberSaveable`.

### 3.2 Gestione Esercizi (CRUD)
* **Campi Dinamici:** La scheda dell'esercizio mostra solo gli attributi popolati (`muscoli`, `target`, `recupero`, `kg`, `note`). Ogni campo vuoto o pari a 0 viene automaticamente nascosto.
* **Modalità Editing Pulita:** Modifica accessibile esclusivamente tramite tap sull'icona della matita.
* **Selezione Esercizio Corrente:** Tap su una card per evidenziare l'esercizio in esecuzione con un bordo e uno sfondo marcati.

### 3.3 Cronometro e Timer Intelligente
* **Modalità Cronometro Standard:** Precisione al decimo di secondo (`MM:SS.d`).
* **Modalità Timer (Conto alla Rovescia Automatico):** Quando viene evidenziato un esercizio con tempo di recupero valido ($>0$ sec), la barra inferiore si converte automaticamente in un conto alla rovescia.
* **Segnale Visivo di Fine Recupero:** Allo scadere dello 0, il pannello lampeggia 4 volte in rosso/arancio brillante.
* **Annullamento/Reset (RST):** Il pulsante **RST** cancella qualsiasi conto alla rovescia attivo o lampeggio e ripristina la modalità cronometro normale.

### 3.4 Importazione/Esportazione CSV
* Menu dedicato per importare o esportare liberamente la propria scheda in formato `.csv` attraverso lo Storage Access Framework di Android.
* Editor di testo CSV integrato con validazione della sintassi prima del salvataggio.

### 3.5 Onboarding e Tracciamento Aggiornamenti
* Dialogo tutorial di benvenuto visualizzato alla prima installazione e ad ogni aggiornamento di versione.
* Option box *"Non mostrare più per questo aggiornamento"* per memorizzare le preferenze dell'utente.

---

## 4. Struttura del Modello Dati CSV

```csv
giorno,nome,muscoli,target,recupero,kg,note
Giorno 1: Gambe,Riscaldamento,Tapis roulant,5',300,,
Giorno 1: Gambe,Leg Extension,Quadricipiti,3 x 8,120,,