<!-- INTESTAZIONE -->
<div style="display: flex; justify-content: space-between; align-items: center; padding: 20px 0; border-bottom: 1px solid #ccc;">
  <div style="text-align: left;">
    <img src="LogoUniba.jpg" alt="Università degli Studi di Bari Aldo Moro" style="height: 60px;">
  </div>
  <div style="text-align: right;">
    <p style="margin: 0; font-size: 16px; color: #666;">Dipartimento di Informatica</p>
    <p style="margin: 0; font-size: 16px; color: #666;">CdL in Informatica</p>
    <p style="margin: 0; font-size: 16px; color: #666;">Metodi Avanzati di Programmazione</p>
  </div>
</div>

<!-- PAGINA DI COPERTINA -->
<div style="page-break-after: always; min-height: 100vh; display: flex; align-items: center; justify-content: center;">
  <div style="text-align: center; border: 2px solid ##FF0000	; padding: 30px 40px; background-color: #f9f9f9; max-width: 600px;">
    <h1 style="font-size: 30px; margin-bottom: 5px; color: #000;">PoggioAdventure</h1>
    <h2 style="font-size: 22px; margin-top: 0; margin-bottom: 15px; color: #000;">Spaccamelons</h2>
    <!-- IMMAGINE CENTRATA -->
    <div style="margin: 15px 0;">
      <img src="Welcome.png" alt="Logo PoggioAdventure" style="width: 300px; height: auto; border-radius: 10px;">
    </div>
    <!-- SEZIONE AUTORI STILIZZATA PIÙ COMPATTA -->
    <div style="margin: 20px 0; padding: 10px 0; border-top: 1px solid #FF0000		; border-bottom: 1px solid #FF0000	;">
      <h3 style="font-size: 16px; font-weight: bold; margin: 0 0 8px 0; color: #FF0000	; letter-spacing: 2px;">AUTORI</h3>
      <div style="margin: 0;">
        <p style="font-size: 15px; margin: 3px 0; color: #000; font-weight: 600; text-shadow: 1px 1px 2px rgba(0,0,0,0.1);">Tommaso Orlando</p>
        <p style="font-size: 15px; margin: 3px 0; color: #000; font-weight: 600; text-shadow: 1px 1px 2px rgba(0,0,0,0.1);">Michele Russo</p>
        <p style="font-size: 15px; margin: 3px 0; color: #000; font-weight: 600; text-shadow: 1px 1px 2px rgba(0,0,0,0.1);">Elia Valenza</p>
      </div>
    </div>
    <!-- SEZIONE ESAME PIÙ COMPATTA -->
    <div style="margin-top: 15px;">
      <p style="font-size: 14px; margin: 2px 0; color: #000;">Esame di</p>
      <p style="font-size: 18px; margin: 2px 0; font-weight: bold; color: #000;">Metodi Avanzati di Programmazione</p>
      <p style="font-size: 13px; margin: 2px 0; font-style: italic; color: #000;">(track M-Z)</p>
    </div>
    <div style="margin-top: 15px;">
      <p style="font-size: 14px; margin: 0; color: #000;">A.A. 2024/2025</p>
    </div>
  </div>
</div>

<!-- CONTENUTO PRINCIPALE -->

# INDICE

## Sommario

* [1. Descrizione dell'avventura](#1-descrizione-dellavventura)
* [2. Come giocare](#2-come-giocare)
* [3. Flusso di Gioco](#3-flusso-di-gioco)
* [4. Progettazione](#4-progettazione)
  * [4.1 Diagrammi delle classi](#41-diagrammi-delle-classi)
  * [4.2 Specifica Algebrica](#42-specifica-algebrica)
* [5. Dettagli implementativi](#5-dettagli-implementativi)
  * [5.1 Programmazione generica](#51-programmazione-generica)
  * [5.2 File](#52-file)
  * [5.3 Database (JDBC) - REST](#53-database-jdbc-rest)
  * [5.4 Lambda Expression, Stream e Pipeline](#54-lambda-expression-stream-e-pipeline)
  * [5.5 Swing](#55-swing)
  * [5.6 Thread e programmazione concorrente](#56-thread-e-programmazione-concorrente)


---

## 1. Descrizione dell'avventura

**PoggioAdventure** è un'avventura testuale ambientata nel **Collegio di PoggioLevante** a Bari, un collegio che gli _sviluppatori_ conoscono molto bene essendone **residenti**. L'obiettivo è quello di ripercorrere le tappe che un possibile collegiale o matricola deve affrontare per entrare a far parte di quella realtà surreale che esiste nel cuore di Bari.

La mappa è stata costruita con lo scopo di riprodurre più o meno fedelmente la planimetria reale, gli stessi personaggi e i possibili oggetti che è possibile incontrare durante il gioco sono un'imitazione o letteralmente una copia di quello che è possibile trovare all'interno di quelle mura.

> Si vuole precisare che **ogni riferimento a fatti, eventi o persone reali è da intendersi in chiave puramente ironica e umoristica.** **Nessun membro del team di sviluppo intende ferire o mancare di rispetto a chicchessia.** La satira e l'ironia sono strumenti utilizzati per stimolare la ***riflessione*** e il ***sorriso***, non per arrecare danno; in nessun modo.

L'avventura si snoda attraverso **tre prove, ciascuna progettata per testare diverse qualità del candidato:

1. **Il Test di Logica** - Una prova intellettuale che mette alla prova le capacità di ragionamento e problem-solving del giocatore.
2. **La Sfida Tecnica** - Un'esperienza pratica che richiede competenze tecniche.
3. **La Crisi dei Robot** - Un'emergenza inaspettata che trasforma il candidato da esaminato a salvatore del collegio.

Durante l'esplorazione e l'evolversi del gioco è possibile incontrare diversi personaggi, che possono aiutarti durante le prove, ma possono anche peggiorare l'esperienza:
- **Il Direttore** - Una figura particolare con un comportamento molto eccentrico.
- **Guido il Portinaio** - Un nano dal cuore d'oro, grande tifoso dell'Inter.
- **Don Matteo** - Il sommo kayoshin sacrestano dal volto misterioso, sempre pronto a offrire consigli spirituali.
- **Giuseppe il Tutor** - Un personaggio un po' imbranato che parla in dialetto materano.
- **Lorenzo Burdo** - Il logorroico vicedirettore entusiasta dell'arrivo di una nuova matricola.
- **Luigi la Scimmia** - Un genio a cui piace far esplodere cose, esperto anche di cose illegali.
- **Pino il Manutentore** - Il "tuttofare" del collegio, oltre ad essere un fumatore incallito.
 
> Per far immergere il giocatore ancora di più nella quotidianità degli sviluppatori sono state prese foto reali dei personaggi e sono state convertite in stile ***Studio Ghibli***; stessa cosa è stata fatta con le stanze del collegio.
> Queste immagini saranno visualizzabili però se e solo se il gioco verrà avviato in modalità *interfaccia grafica*.

Nel corso della propria avventura è possibile trovare, prendere e usare una moltitudine di oggetti, alcuni serviranno per effettuare le prove, altri nascondono segreti che solo gli sviluppatori conoscono e altri ancora avranno comportamenti insoliti che altereranno il corso dell'avventura.

*Meno tempo* verrà impiegato per finire il gioco e *meno comandi verranno inseriti*, **maggiore** sarà il punteggio del giocatore. Avviando il gioco sarà possibile visualizzare la classifica dei giocatori che hanno *completato il gioco* e sarà possibile scaricare il file di log di coloro che hanno vinto per poter aiutare i nuovi giocatori a fare di meglio e di conseguenza a raggiungere un punteggio maggiore in classifica.

*Sei pronto a intraprendere questo viaggio verso la beatificazione eterna? Il collegio e tutti i santi ti stanno aspettando.*..

> Se vuoi sapere di più sul collegio di *Poggiolevante*, visita il sito: https://www.poggiolevante.it/
## Mappa di gioco
![Mappa di gioco](GameMap.jpeg)

---
## 2 Come Giocare

Per iniziare la tua avventura in PoggioAdventure, dovrai prima compilare il gioco visto che nella repository non è incluso il `.jar`. Trattandosi di un progetto Maven, il processo è standard:
- C'è bisogno di avere `maven` installato. 
- C'è bisogno anche di avere la Java JDK 19`.
- Apri un terminale nella directory principale del progetto `poggioAdventure` (stessa cosa per `poggioServer`) e digita:
```shell
\\Pulizia
& "<your_maven_absolute_path>\mvn.cmd" clean -f "<your_repo_absolute_path>\poggioAdventure\poggioServer\pom.xml"

& "<your_maven_absolute_path>\mvn.cmd" clean -f "<your_repo_absolute_path>\poggioAdventure\poggioAdventure\pom.xml"

\\Compilazione
& "<your_maven_absolute_path>\mvn.cmd" install -f "<your_repo_absolute_path>\poggioAdventure\poggioServer\pom.xml"

& "<your_maven_absolute_path>\mvn.cmd"  install -f "<your_repo_absolute_path>\poggioAdventure\poggioAdventure\pom.xml"
```
-  Questi comandi si occuperanno di compilare il codice sorgente e creare un file `.jar`    eseguibile. Sia per `poggioServer` che per `poggioAdventure`.
- I file `.jar` si troveranno nella cartella `target` relativa al progetto di riferimento.

>**NB:** Si consiglia di non spostare i file `.jar` compilati perché questi necessitano di alcune cartelle per funzionare.

Una volta compilati i file `.jar` il gioco è fatto, l'unica cosa che rimane da fare è avviare prima `poggioServer`:
```shell
<poggioServer_absolute_path>\target\> java -jar poggioServer.jar
```

>Avviare il DB in locale sarà necessario fino a quando gli sviluppatori non pubblicheranno l'applicazione online su un server **adHoc**.

Successivamente avviare il gioco:
Il gioco supporta due modalità di interfaccia:
*   Per l'interfaccia grafica (GUI), avvialo con: 
```shell
<poggioAdventure_absolute_path>\target\> java -jar poggioAdventure.jar --gui
```
*   Per l'interfaccia a riga di comando (CLI), avvialo con: 
```shell
java -jar poggioAdventure.jar --cli` (questa è anche la modalità predefinita se non specifichi nulla).
```

## Flusso di gioco

## 1. Avvio dell'Applicazione:

Quando l'utente avvia il file JAR (`java -jar poggioAdventure.jar`), l'applicazione inizializza l'interfaccia utente principale, che può essere grafica (GUI) o a riga di comando (CLI) a seconda del parametro passato .
*   **GUI**: Viene istanziata e visualizzata la finestra `UI_Init`.
*   **CLI**: Viene avviato `CLIMenu` per gestire le interazioni.
>Entrambe le classi implementano l'interfaccia `MenuManager.java`
## 2. Menu Principale (`UI_Init` / `CLIMenu`):

L'utente si trova di fronte a un menu con le seguenti opzioni:
*   **Nuova Partita**: Inizia una nuova avventura.
*   **Carica Partita**: Riprende una partita salvata in precedenza.
*   **Classifica**: Visualizza la classifica con i giocatori.
*   **Esci**: Termina l'applicazione.
### 2.1. Nuova Partita:

1.  **Interfaccia Utente**:
    *   **GUI**: Viene aperta la finestra `UI_NewGame`.
    *   **CLI**: `CLIMenu.showNewGame()` gestisce l'input.
1.  **Inserimento Nome**: L'utente inserisce il nome del giocatore.
2.  **Validazione e Controllo Esistenza**:
    *   Il nome non può essere vuoto.
    *   Viene utilizzato `PoggioClientJersey` per interrogare il server e verificare se un utente con quel nome esiste già (`checkUserExists`).
3.  **Esito Controllo**:
    *   **Utente NON Trovato (`ApiClientResult.USER_NOT_FOUND`)**:
        *   Si procede con la creazione della nuova partita.
        *   Viene creato un nuovo `Engine` tramite `EngineFactory.createNewGame(...)`. I parametri includono il nome del giocatore, gli handler di I/O (`OutputHandler`, `InputHandler`), `ErrorHandler` e `LoggerInput` (Ovviamente vengono passate istanze in base alla modalità di gioco scelta ).
        *   **GUI**: Viene chiusa `UI_NewGame` (e la `UI_Init` parente) e viene aperta `UI_Game` con il nuovo `Engine`.
        *   **CLI**: `CLIMenu` avvia `engine.startGameLoop()`.
    *  Nel caso venisse inserito un **Utente Già Esistente nel DB (`ApiClientResult.SUCCESS_OK`)**:
        *   Viene mostrato un messaggio di errore.
        *   L'utente viene reindirizzato al menu principale (`UI_Init` o `CLIMenu`) per caricare la partita o scegliere un nome diverso.
    *   **Errore di Connessione/Altro**: Viene mostrato un messaggio di errore appropriato.
### 2.2. Carica Partita:

1.  **Interfaccia Utente**:
    *   **GUI**: Viene aperta la finestra `UI_LoadGame`.
    *   **CLI**: `CLIMenu.showLoadGame()` gestisce l'input.
1.  **Visualizzazione Salvataggi**:
    *   `SaveGame.getSaveList()` recupera l'elenco dei file di salvataggio (`.dat`) disponibili nella directory dei salvataggi.
    *   La lista viene presentata all'utente.
1.  **Selezione e Caricamento**:
    *   L'utente seleziona un salvataggio.
    *   Viene chiamato `SaveGame.loadSave(...)`. Questo metodo:
        *   Deserializza lo stato del gioco dal file `.dat` selezionato. Gli oggetti deserializzati includono: `playerName`, `GameDescription game`, `gameTime`, `logFileName`, `currentLevelIndex`, `levelElapsedTime`, `levelMapSnapshot`, `levelInventorySnapshot`, `levelStartingRoomSnapshot`.
        *   Verifica l'integrità del file di log associato (`LoggerInput.checkLog`).
        *   Ricostruisce un'istanza di `Engine` tramite `EngineFactory.createFromSave(...)`, passando i dati deserializzati e i necessari handler.
        *   Ripristina lo stato del `GameStateManager` dell'engine caricato utilizzando i dati deserializzati (`gsm.restoreFromSave(...)`).
2.  **Avvio Partita Caricata**:
    *   **GUI**: Viene chiusa `UI_LoadGame` e viene aperta `UI_Game` con l'`Engine` caricato.
    *   **CLI**: `CLIMenu` avvia `engine.startGameLoop()`.
3.  **Eliminazione Salvataggio (Opzionale)**:
    *   **GUI**: `UI_LoadGame` permette di eliminare un salvataggio selezionato (premendo CANC).
    *   **CLI**: `CLIMenu` permette all'utente di manifestare la volontà di cancellare un salvataggio inserendo **'!'** prima del numero.
    *   Viene chiamato `SaveGame.deleteSave(saveName, errorHandler, deletePlayerFromServerBoolean)`. Questo metodo:
        *   Estrae il nome utente dal file di salvataggio (`SaveGame.getUsernameFromSave`).
        *   Se `deletePlayerFromServerBoolean` è true, contatta il server tramite `PoggioClientJersey.deletePlayer()` per rimuovere l'utente.
        *   Trova e elimina il file di log associato (`SaveGame.findLogFileName` e `LoggerInput.deleteLogFile`).
        *   Elimina il file di salvataggio `.dat`.
### 2.3. Classifica:

1.  **Interfaccia Utente**:
    *   **GUI**: Viene aperta la finestra `UI_Rank`.
    *   **CLI**: `CLIMenu.showRanking()` gestisce l'output.
2.  **Recupero Dati**:
    *   Viene utilizzato `PoggioClientJersey` per interrogare il server e recuperare i dati della classifica (`getRanking`).
3.  **Visualizzazione**: I dati vengono formattati a seconda della modalità grafica scelta e mostrati all'utente.
4. **Download  Salvataggio (Opzionale)**:  
   -  **GUI**: Si può scaricare il file di log di chi è nella classifica (cliccando **BARRA SPAZIATRICE**)
   -  **CLI:** Comparirà un sotto menu che guiderà l'utente qualora voglia scaricare un salvataggio.
### 2.4. Esci

L'applicazione viene terminata (`Utils.exitApplication(0)`).
## 3. Focus Engine:

Una volta iniziata o caricata una partita, l'`Engine` di gioco prende il controllo.
#### 3.1.1. Creazione e Inizializzazione

Un'istanza di `Engine` viene creata da `EngineFactory` (sia per nuove partite che per caricamenti).
Necessita dei seguenti componenti principali:

*   `GameDescription game`: Il modello del mondo di gioco (es. `PoggioAdventureDesc`).
*   `String playerName`: Nome del giocatore.
*   `OutputHandler output`: Per visualizzare messaggi (GUI o CLI).
*   `InputHandler input`: Per ricevere comandi (GUI o CLI).
*   `ErrorHandler errorHandler`: Per gestire errori.
*   `LoggerInput logger`: Per registrare i comandi del giocatore.
*   `boolean fromSave`: Flag che indica se l'engine è stato creato da un salvataggio. (il comportamento deve variare, es. non deve reinizializzare `GameStateManager`)

Durante l'inizializzazione, l'`Engine`:
1.  Inizializza il `Parser` con un set di stopwords caricate da file (`ResourceLoader.STOPWORDS_PATH`).
2.  Avvia uno `StopWatch` globale (`gameTime`) per tracciare il tempo totale di gioco.
3.  Crea un `GameContext`, un oggetto contenitore per dipendenze condivise come handler I/O, `ErrorHandler`, `logTemp` (buffer per comandi da loggare), `gameTime`, l'inventario del gioco e una callback per il comando "osserva". Sarà utilizzato dagli **Observers** per ad esempio stampare altri messaggi sulla tipologia di `OutputHandler` scelto.
4.  Inizializza il `GameStateManager`. Questo gestore è cruciale per la progressione dei livelli e riceve:
    *   L'istanza di `GameDescription`.
    *   L'`OutputHandler`.
    *   Il nome del giocatore.
    *   Callback a metodi dell'`Engine` per:
        *   `this::handleGameCompleted` (vittoria finale).
        *   `this::handleGameLoss` (sconfitta finale).
        *   `this::saveGame` (salvataggio, es. al reset di un livello).
1.  Se non si sta caricando da un salvataggio (`!fromSave`):
    *   Mostra il messaggio di benvenuto (`game.getGUIWelcomeMsg()` o `game.getCLIWelcomeMsg()`).
    *   Chiama `gameStateManager.startGame()` per avviare il primo livello.
    *   Mostra la descrizione della stanza iniziale.

#### 3.1.2. Ciclo di Gioco e Processamento Comandi
*   **GUI**: `UI_Game` ha un campo di input per i comandi. Quando l'utente invia un comando, `UI_Game` chiama `engine.processCommand(commandText)`.
*   **CLI**: `engine.startGameLoop()` entra in un ciclo che attende l'input dall'utente tramite `inputHandler.getInput()` e poi chiama `engine.processCommand(command)`.

Il metodo `Engine.processCommand(String command)`:
1.  Aggiunge il comando grezzo al buffer `logTemp`.
2.  Utilizza il `Parser` per analizzare la stringa di comando. Il parser può gestire comandi multipli concatenati ad esempio da congiunzioni es: `nord e ovest e est`. Restituisce una lista di `ParserOutput`.
3.  Se il parsing fallisce o non produce comandi validi, mostra un messaggio di errore.
4.  Per ogni `ParserOutput` valido:
    *   Se il comando è di tipo `CommandType.SAVE`, chiama `engine.saveGame()`.
    *   Altrimenti, chiama `game.nextMove(List.of(p), gameContext)`. `game` è un'istanza di `GameDescription` (es. `PoggioAdventureDesc`).
    *   Se il comando è di tipo `CommandType.END`, termina l'applicazione.
1.  Dopo aver processato tutti i comandi nella stringa, chiama `gameStateManager.checkStateAfterCommand()` per valutare se lo stato del gioco è cambiato (superamento livello, tempo scaduto, sconfitta, vittoria). Questo implica ovviamente che se il tempo di gioco scade, bisognerà prima inserire il comando per farsi che il `GameStateManger` resetti il livello e quindi tutti gli elementi di gioco.
> E' stata fatta questa scelta implementativa per farsi che l'utente subisca poi una penalizzazione di punteggio dovuto al tempo di gioco che continua a scorrere se l'utente non inserisce nulla per far resettare il livello.

Il metodo `Engine.saveGame()`:
1.  Ferma il `gameTime`.
2.  Scrive i comandi accumulati in `logTemp` nel file di log tramite `logger.logInput(logTemp)`.
3.  Chiama `SaveGame.saveGame(this, output)` per serializzare lo stato corrente dell'`Engine` in un file `.dat`. Questo include: `playerName`, `game` (l'intera `GameDescription`), `gameTime` (tempo trascorso), nome del file di log, e gli snapshot dal `GameStateManager` (`currentLevelIndex`, `levelElapsedTime`, `levelMapSnapshot`, `levelInventorySnapshot`, `levelStartingRoomSnapshot`).
4.  Svuota `logTemp`.
5.  Riavvia `gameTime`.
6.  Contatta il server tramite `PoggioClientJersey.addUser(playerName)` (usato dalla classe funzionale `SaveGame`) per assicurarsi che l'utente esista nel database del server e nel caso non esistesse, di inserirlo.

### 3.2. Gestione dei Livelli (`GameStateManager`)

Il `GameStateManager` (GSM) è responsabile della logica di progressione tra i livelli, della gestione del tempo per ogni livello, dei checkpoint e delle condizioni di vittoria/sconfitta (grazie a `Engine`).
Contiene un array di `GameState` (es. `Level1State`, `Level2State`, `Level3State`), ognuno rappresentante un livello del gioco.
#### 3.2.1. `checkStateAfterCommand()`
Questo metodo viene chiamato dall'`Engine` dopo ogni comando.
1.  **Controllo Tempo Scaduto**: Verifica `timeManager.getTempoRimanente()`. Se è <= 0:
    *   Mostra un messaggio di tempo scaduto.
    *   Chiama `resetCurrentLevel()`.
1.  **Controllo Condizioni di Fallimento**: Verifica `currentState.isFailureConditionMet(gameDescription)`. Se true:
    *   Chiama `currentState.handleFailure(this::handleGameLoss, gameDescription)`. `handleGameLoss` è una callback al metodo dell'`Engine` che gestisce la sconfitta definitiva.
1.  **Controllo Condizioni di Completamento Livello**: Verifica `currentState.isCompleted(gameDescription)`. Se true:
    *   Chiama `currentState.handleSuccess(this::advanceToNextLevel, gameDescription)`. `advanceToNextLevel` è un metodo del GSM.
#### 3.2.2. `advanceToNextLevel()`

1.  Incrementa `currentLevelIndex`.
2.  Se `currentLevelIndex` supera il numero di livelli disponibili, chiama `handleGameCompletion()` (callback all'`Engine` per la vittoria finale).
3.  Altrimenti, chiama `transitionToLevel(levels[currentLevelIndex], true)` per passare al livello successivo, creando un nuovo checkpoint (Utile per il reset del livello).

#### 3.2.3. `transitionToLevel(GameState newState, boolean createSnapshot)`

1.  Imposta `currentState = newState`.
2.  Chiama `currentState.enter(gameDescription, output, playerName)`. Questo metodo, implementato in ogni classe `LevelXState`, configura il mondo di gioco per quel livello (posiziona NPC, oggetti specifici del livello, ecc.).
3.  Imposta la stanza corrente del giocatore (`gameDescription.setCurrentRoom()`) alla `startingRoom` definita nel `newState`.
4.  Se `createSnapshot` è `true`:
    *   Crea snapshot (copie profonde tramite `Utils.deepClone`) di:
        *   `gameDescription.getGameMap()` e lo salva in `levelMapSnapshot`.
        *   `gameDescription.getInventory()` e lo salva in `levelInventorySnapshot`.
        *   `currentState.getStartingRoom()` e lo salva in `levelStartingRoomSnapshot`.
    Questi snapshot servono per il `resetCurrentLevel`.
5.  Inizializza e avvia un `TimeManager` specifico per il livello, usando `currentState.getTimeLimit()`.
6.  Mostra la descrizione del nuovo livello (`currentState.getLevelDescription(...)`).
#### 3.2.4. `resetCurrentLevel()` (Checkpoint)
Chiamato quando il tempo per un livello scade.
1.  Mostra un messaggio di reset.
2.  Ferma il `timeManager` corrente.
3.  Ripristina lo stato del gioco dagli snapshot:
    *   `gameDescription.setInventory(Utils.cloneList(levelInventorySnapshot))`.
    *   `gameDescription.setGameMap(levelMapSnapshot)`. La `setGameMap` esegue una copia profonda e ricrea correttamente i collegamenti tra le stanze clonate.
    *   `gameDescription.setCurrentRoom(...)` usando `levelStartingRoomSnapshot`.
4.  Resetta e riavvia il `timeManager` con il limite di tempo originale del livello.
5.  Chiama la callback `onSaveGame.run()` (che punta a `Engine.saveGame()`) per effettuare un salvataggio automatico. (Sempre per la questione del tempo di gioco che aumenta e per il possibile punteggio che diminuisce se il giocatore termina l'avventura con successo).
6.  Mostra nuovamente la descrizione del livello e la stanza corrente.

#### 3.2.5. `restoreFromSave(...)`

Chiamato quando si carica una partita.

1.  Ripristina `currentLevelIndex`, `levelMapSnapshot`, `levelInventorySnapshot`, `levelStartingRoomSnapshot` dai dati del salvataggio.
2.  Imposta `currentState` al livello corretto.
3.  Calcola il tempo rimanente per il livello (`levelTimeLimit - savedLevelElapsedTime`).
4.  Se il tempo rimanente è positivo, inizializza e avvia il `timeManager` con questo tempo.
5.  Altrimenti (tempo scaduto durante il salvataggio/caricamento), chiama `resetCurrentLevel()`
### 3.3. Interazione con il Mondo di Gioco (`GameDescription` e Observer)

`GameDescription` (es. `PoggioAdventureDesc`) definisce la struttura del mondo: stanze, oggetti, NPC, comandi.
Il metodo `PoggioAdventureDesc.nextMove(List<ParserOutput> list, GameContext gameContext)` è centrale:
1.  Per ogni `ParserOutput` (comando parsato):
    *   Recupera la stanza corrente prima dell'azione.
    *   Notifica una serie di `GameObserver` registrati (es. `MoveObserver`, `InventoryObserver`, `PushObserver`, `OpenObserver`, `UseObserver`, `TalkObserver`, `PutObserver`). Ogni observer è specializzato per un tipo di comando.
    *   L'observer appropriato esegue la logica del comando, modificando lo stato del gioco (es. cambiando `currentRoom`, aggiungendo/rimuovendo oggetti dall'inventario o dalla stanza, interagendo con NPC), etc...
    *   L'observer aggiunge messaggi di feedback a una lista interna (`messages` in `PoggioAdventureDesc`).
    *   Dopo la notifica, `flushObserverMessages()` scrive i messaggi raccolti sull'`OutputHandler`.
    *   Se la stanza è cambiata, `displayRoomInfo()` mostra nome e descrizione della nuova stanza.
## 4. Scenari Specifici

### 4.1. Superamento di un Livello
  
1.  Il giocatore compie un'azione che soddisfa la condizione di completamento del livello corrente. Gli oggetti *Fobidden* e *Required* vengono passato come costruttori, in questo caso nel `GameStateManager`.
    *   Esempio `Level1State`: Il giocatore completa il test del `DirettoreGalileo`, e il `Test.handleTestCompletion` aggiunge l'oggetto `level1Complete` (ID `Utils.OBJ_LEVEL1_COMPLETE_ID`) all'inventario.
2.  `GameState.isCompleted(gameDescription)` (es. `Level1State.isCompleted`) verifica la presenza dell'oggetto richiesto nell'inventario e restituisce `true`.
3.  `GameStateManager.checkStateAfterCommand()` rileva il completamento e chiama `currentState.handleSuccess(...)`, che a sua volta chiama `GameStateManager.advanceToNextLevel()`.
4.  `GameStateManager.advanceToNextLevel()`:
    *   Se ci sono altri livelli, chiama `transitionToLevel()` per caricare il livello successivo, creare un nuovo checkpoint e resettare il timer.
    *   Se è l'ultimo livello, chiama `handleGameCompletion()` (callback a `Engine.handleGameCompleted()`).
### 4.2. Tempo Scaduto

1.  Il `TimeManager` del livello corrente raggiunge lo zero.
2.  `GameStateManager.checkStateAfterCommand()` rileva che `timeManager.getTempoRimanente() <= 0`.
3.  Viene chiamato `GameStateManager.resetCurrentLevel()`.
4.  Il gioco viene ripristinato allo stato del checkpoint dell'inizio del livello corrente (snapshot di mappa, inventario, stanza).
5.  Il `TimeManager` del livello viene resettato al suo limite originale.
6.  Viene effettuato un salvataggio automatico (`Engine.saveGame()`).
7.  Il giocatore ricomincia il livello corrente.
### 4.3. Vittoria Finale
  
1.  Il giocatore completa l'ultimo livello.
2.  `GameStateManager.advanceToNextLevel()` determina che non ci sono più livelli e chiama la callback `onGameCompleted` (che punta a `Engine.handleGameCompleted()`).
3.  `Engine.handleGameCompleted()`:
    *   Ferma il `gameTime` (cronometro globale).
    *   Effettua un ultimo `saveGame()` (per registrare i log finali).
    *   Mostra messaggi di congratulazioni.
    *   Chiama `sendVictoryDataToServer()`:
        *   Crea una versione decrittata temporanea del file di log (`LoggerInput.createDecryptedTempLogFile`) (perchè nel file di log locale i comandi sono *"crittografati"*).
        *   Usa `PoggioClientJersey.recordVictoryWithLog()` per inviare al server: nome giocatore, data, ora, durata del gioco e il file di log decrittato.
        *   Mostra feedback sull'esito della registrazione.
        *   Elimina il file di log temporaneo.
    *   Chiama `deleteCurrentSaveEndGame()`:
        *   Trova il file di salvataggio del giocatore corrente.
        *   Chiama `SaveGame.deleteSave(targetSave, errorHandler, false)` (il `false` indica di non eliminare il giocatore dal server in questo frangente, dato che ha vinto).
    *   Elimina il file di log originale (`LoggerInput.deleteLogFile(logger.getPathFile())`).
    *   Chiama `returnToAppropriateMenu()` per tornare a `UI_Init` (GUI) o al `CLIMenu` (CLI).
### 4.4. Sconfitta Finale

1.  Il giocatore compie un'azione che porta a una condizione di fallimento definita dal livello.
    *   Esempio `Level3State`: Il giocatore usa il Flipper Zero con il comando `Override`. `FlipperCommandProcessor.notifyGameEngine` aggiunge l'oggetto `level3Lose` (ID `Utils.OBJ_LOSE_GAME_ID`) all'inventario.
1.  `GameState.isFailureConditionMet(gameDescription)` (es. `Level3State.isFailureConditionMet`) rileva la presenza dell'oggetto di sconfitta e restituisce `true`.
2.  `GameStateManager.checkStateAfterCommand()` rileva il fallimento e chiama `currentState.handleFailure(...)`, che a sua volta chiama la callback `onGameLoss` (che punta a `Engine.handleGameLoss()`).
3.  `Engine.handleGameLoss()`:
    *   Ferma il `gameTime`.
    *   Effettua un `saveGame()` (per registrare i log finali).
    *   Mostra messaggi di sconfitta.
    *   Chiama `deleteCurrentSaveEndGame()` per eliminare il file di salvataggio locale.
    *   Chiama `deletePlayerFromServer()`:
        *   Usa `PoggioClientJersey.deletePlayer(playerName)` per rimuovere il giocatore dal database del server.
    *   Elimina il file di log originale (`LoggerInput.deleteLogFile(logger.getPathFile())`).
    *   Chiama `returnToAppropriateMenu()`.


## 2. Progettazione

[Inserire qui l'introduzione alla progettazione]

### 2.1 Diagrammi delle classi

[Inserire qui i diagrammi delle classi e la loro descrizione]

<!-- Esempio per inserire un diagramma:
![Diagramma delle classi principale](immagini/diagramma-classi.png)
-->

### 2.2 Specifica Algebrica

[Inserire qui le specifiche algebriche]

### 2.3 Dettagli implementativi

[Inserire qui un'introduzione ai dettagli implementativi]

#### 2.3.1 Programmazione generica

[Inserire qui i dettagli sulla programmazione generica utilizzata nel progetto]

<!-- Esempio di codice:
```java
// Inserire esempi di codice qui