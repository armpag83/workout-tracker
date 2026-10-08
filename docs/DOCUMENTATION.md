# Workout Tracker - Documentazione Tecnica

App Android nativa sviluppata in **Kotlin** con **Jetpack Compose**, progettata per il tracciamento degli allenamenti, la gestione di schede personalizzate via file CSV e la sincronizzazione in tempo reale dei tempi di recupero tramite cronometro/timer integrato.

---

## 📋 Indice
1. [Architettura del Progetto](#architettura-del-progetto)
2. [Requisiti e Configurazioni di Build](#requisiti-e-configurazioni-di-build)
3. [Funzionalità Principali](#funzionalità-principali)
4. [Registro delle Versioni (Changelog)](#registro-delle-versioni-changelog)
5. [Struttura Dati (CSV)](#struttura-dati-csv)

---

## 🏗 Architettura del Progetto

Il progetto segue una struttura modulare e reattiva basata sulle specifiche di Jetpack Compose:

```text
it.armandopagliara.workouttracker/
├── MainActivity.kt               # Entry point, gestione dello stato globale e TopBar/Dialogs
├── data/
│   └── WorkoutCsvManager.kt      # Lettura, scrittura, import/export e parsing del CSV
├── model/
│   └── Exercise.kt               # Data class per la rappresentazione dell'esercizio
└── ui/
    ├── components/
    │   ├── ExerciseCard.kt       # Card esercizio con frecce direzionali e azioni rapide
    │   └── StopwatchPanel.kt     # Pannello reattivo per Timer / Cronometro con allarmi
    └── dialogs/
        ├── ExerciseFormDialog.kt # Form per creazione e modifica esercizi
        └── WelcomeTutorialDialog.kt # Guida introduttiva e changelog