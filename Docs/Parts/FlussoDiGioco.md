
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