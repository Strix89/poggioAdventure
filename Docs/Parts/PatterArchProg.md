# Progettazione

Per sviluppare questa avventura è stato preso fondamentalmente il progetto base fornito durante il corso (**anno** ***2023-2024***) ed è stato esteso (modificato in alcune sue parti). Aggiungendo componenti cruciali come la progressione di livelli, alcuni observer e una gestione dell'*output/input* diversa, più granulare (*etc..*).
Successivamente è stato creato un secondo *modulo* che si occupa esclusivamente di gestire il server con DB per la classifica `poggioServer`.

## ***Pattern utilizzati per il progetto:***
#### 1. State
Per gestire la progressione del gioco attraverso i vari livelli, abbiamo implementato il pattern **State**. Invece di avere un'enorme classe `Engine` con un gigantesco `switch` o una catena di `if-else` per controllare in quale livello si trova il giocatore, abbiamo incapsulato il comportamento di ogni livello in un oggetto separato.
La classe `GameStateManager` agisce come "contesto". Mantiene un riferimento allo stato corrente (es. `Level1State`) e gestisce le transizioni tra gli stati.
L'interfaccia (in questo caso, la classe astratta) `GameState` definisce i metodi comuni a tutti gli stati, come `enter()`, `isCompleted()`, ecc.

Quando l'`Engine` deve controllare se un livello è completato, non lo fa direttamente. Chiama il `GameStateManager`, che a sua volta delega la chiamata all'oggetto stato corrente. Questo rende il codice pulito e facile da estendere: per aggiungere un "Livello 4", basta creare una nuova classe `Level4State` e aggiornare le regole di transizione nel `GameStateManager`, senza toccare il motore di gioco `GameDescription`.

--- 
#### 2. Singleton
Ci sono componenti che, per loro natura, devono esistere in una sola istanza per tutta l'applicazione, o meglio per tutta la partita. Un esempio **perfetto** è il cronometro di gioco (per il tempo totale di gioco).

- La classe `Stopwatch` è implementata come un Singleton. Ha un costruttore privato per impedire la creazione di nuove istanze e fornisce un metodo statico `getInstance()` che restituisce sempre la stessa, unica istanza del cronometro.

Il vantaggio è la garanzia di avere un **unico punto di accesso globale** a una risorsa condivisa. Qualsiasi parte del codice, che sia un observer, un LevelXState o l'interfaccia utente, può ottenere il riferimento al cronometro tramite `Stopwatch.getInstance()` e interagire con esso, con la certezza che si tratti sempre dello stesso oggetto.

---
#### 3. Observer
Per gestire le azioni di gioco in modo disaccoppiato, abbiamo fatto uso e esteso il pattern **Observer** (già presente nel *progetto base*).

- Il `PoggioAdventureDesc` implementa `GameObservable`. Quando un'azione viene eseguita, vengono notificati tutti gli `Observer` registrati.
- Classi come `UseObserver` o `InventoryObserver` sono gli Observer. Ognuna è specializzata per reagire a un tipo di comando.

Questo rende il sistema **estremamente flessibile**: per aggiungere un nuovo comando, mi basta creare un nuovo **observer** senza modificare il motore di gioco.

---
#### 4. Strategy
Il pattern **Strategy** è stato utilizzato per gestire le due modalità di **output** (CLI e GUI) o di **input** o di **error**. L'interfaccia `OutputHandler` ad esempio definisce il contratto, mentre `CLIOutputHandler` e `GUIOutputHandler` sono le strategie concrete. L'`Engine` lavora con l'interfaccia, permettendo di cambiare **"al volo"** la strategia di output.

>**Per gestire i colori** in modo coerente tra CLI e GUI, viene utilizzato un sistema di markup personalizzato. Il testo da colorare è racchiuso in tag, come `[RED]testo rosso[/]`. L'interfaccia `OutputHandler` definisce una `Pattern` (`COLOR_BLOCK_PATTERN`) per riconoscere questi blocchi.
>L'enum `ColorText` mappa un nome di colore (es. `RED` sia al codice di escape ANSI per la console, sia all'oggetto `java.awt.Color` per Swing.
>Quindi:
    - `CLIOutputHandler`: Processa la stringa, trova i blocchi di colore tramite regex e sostituisce i tag con i codici ANSI corrispondenti presi da `ColorText`.
    - `GUIOutputHandler`: Opera su un `JTextPane`. Utilizza `SwingUtilities.invokeLater` per la sicurezza dei thread. Quando trova un blocco di colore, usa uno `StyledDocument` per applicare il `java.awt.Color` corrispondente al segmento di testo.

>La **gestione delle Immagini** è simile a quella dei colori, tuttavia è supportata solo nell'interfaccia grafica. Un'immagine viene indicata nella stringa di *output* con un tag speciale, ad esempio `IMAGE: path/to/image.png`. Anche questo è definito da una regex in `OutputHandler`.
   Quindi:
    - `GUIOutputHandler`: Quando rileva un tag `IMAGE:`, carica l'immagine tramite `ResourceLoader`, la scala per adattarla all'interfaccia e la inserisce nel `JTextPane`.
    - `CLIOutputHandler`: Riconosce i tag `IMAGE:` e li ignora, poiché non può renderizzare immagini nella console.


---
#### 5. Template Method
Per dare una struttura coerente a tutte le interfacce grafiche (GUI) dell'applicazione. L'idea di questo pattern è definire lo "scheletro" di un algoritmo in una classe base, lasciando che le sottoclassi ne completino alcuni passaggi specifici senza poter modificare la struttura generale.

Nel nostro caso, la classe astratta `UI_Abstract` funge da "Template". Al suo interno, il metodo privato `initUI()` rappresenta il nostro algoritmo-scheletro.

Ogni finestra specifica, come `UI_Game` o  `UI_Inventory`, estende `UI_Abstract` e fornisce la propria implementazione di `initComponents()`. In questo modo, `UI_Game` crea i pannelli di gioco, `UI_Inventory` crea la lista degli oggetti e l'area dei dettagli, ma entrambe seguono la stessa sequenza di inizializzazione definita in `initUI()`.

Il vantaggio risiende nel fatto che tutte le finestre abbiano un comportamento e un aspetto di base uniformi, riutilizzando il codice di configurazione e demandando alle classi figlie solo la creazione delle loro *caratteristiche uniche*.

--- 
#### 6. Factory
E' stato utilizzato per nascondere la complessità legata alla creazione di oggetti. Invece di spargere logica di inizializzazione complessa in giro per il codice, la centralizziamo in un unico punto.

Creare un'istanza di `Engine` *non è banale*. `EngineFactory` si occupa di tutto questo, esponendo metodi semplici come `createNewGame(...)`. Il client (ad esempio `CLIMenu`) non deve fare altro che chiamare questo metodo, senza preoccuparsi dei dettagli interni. 

---
# Struttura dei Package del Server (`poggioServer`)
Il backend, è un'applicazione JAX-RS che espone API REST per gestire la persistenza dei dati dei giocatori, le classifiche e i file di log. L'architettura è suddivisa in package specializzati per separare le responsabilità.

---
## Package `com.mycompany.poggioserver`:
EntryPoint del server.
- `PoggioServer.java`
    -  È l'entry point dell'applicazione. Utilizza un server HTTP Grizzly embedded per ospitare l'applicazione JAX-RS. Si occupa di:
        - Configurare le risorse REST (`PlayerResource`), i filtri (`ApiKeyFilter`) e le feature (supporto JSON e multipart).
        - Inizializzare il gestore del database.
        - Avviare il server e gestire lo shutdown.
--- 
## Package `com.mycompany.poggioserver.db`:
Questo package gestisce tutta l'interazione con il database H2.
- `DatabaseManager.java`
    - Gestisce il ciclo di vita del database. Utilizza un pool di connessioni HikariCP per efficienza e robustezza delle pool di connessioni. Al primo avvio, crea lo schema del database (la tabella `player` se non esiste).
- `PlayerDAO.java`
    - Definisce il contratto (le operazioni) per l'accesso ai dati dei giocatori (*Data Access Object*). Specifica i metodi per creare, leggere, aggiornare ed eliminare i record dei giocatori e per recuperare la classifica.
- `PlayerDAOImpl.java`
    - È l'implementazione concreta di `PlayerDAO`. Contiene le query SQL e la logica JDBC per interagire con la tabella `players`.
---
## Package `com.mycompany.poggioserver.filters`
Contiene i filtri JAX-RS per intercettare e processare le richieste. Definendo interfacce e annotazioni standard.
-  `ApiKeyFilter.java`
    - Implementa un filtro di sicurezza che intercetta ogni richiesta in arrivo. Controlla la presenza e la validità di un header `X-API-Key`. Se la chiave non è corretta, la richiesta viene bloccata con uno stato `401 Unauthorized`, proteggendo così gli endpoint da accessi non autorizzati.
---
# Struttura dei Package dell'Avventura (`poggioAdventure`)
Si è cercato di progettare un'architettura modulare che separa la logica di gioco, il modello dei dati e l'interfaccia utente. Al suo interno è contenuto anche la classe che si occupa di fare da client per la comunicazione dei giocatori e punteggi al server.

---
## Package `com.mycompany.poggioadventure`:
*EntryPoint* dell'Avventura.
-  `PoggioAdventure.java`
    - È la classe principale del gioco. Il suo metodo `main` analizza gli argomenti della riga di comando (`--gui` o `--cli`) per avviare l'interfaccia utente appropriata.

---
## Package `com.mycompany.poggioadventure.core`:
-  `Engine.java`
     È il motore di gioco principale che orchestra l'intera applicazione.
     - Gestisce il game loop principale (`startGameLoop`) se chiamato con CLI.
     - Coordina l'interazione tra il parser, il modello di gioco (`PoggioAdventureDesc`) e l'interfaccia utente (CLI/GUI).
     - Gestisce lo stato della sessione, inclusi salvataggio, caricamento e tempo di gioco totale.
     - Implementa una strategia per tornare al menu corretto (`returnToGUIMenu` o `returnToCLIMenu`) a seconda della modalità di gioco scelta, al termine di una partita.
- `PoggioAdventureDesc.java`
    -  È l'implementazione concreta di `GameDescription` che contiene la logica specifica dell'avventura.
        - Inizializza il mondo di gioco, inclusi comandi, stanze e observer. Volendo si può inizializzare la mappa base di gioco (con tutti gli elementi) già al suo interno tramite il metodo `.init` ma abbiamo preferito assegnare questa responsabilità al `GameStateManager`.
        - Elabora i comandi dell'utente tramite il metodo `nextMove` e notifica gli observer competenti le modifiche da effettuare.
- `GameMap.java`
    - Modella la mappa del gioco, organizzando le stanze in una struttura a piani.
        - Fornisce metodi per la navigazione, la ricerca di stanze e la gestione delle connessioni tra di esse (che a livello logico rappresentano le connessioni tra i piani della mappa).
- `GameStateManager.java`
    - Gestisce il ciclo di vita e la transizione tra i livelli di gioco (`GameState`).
        - Monitora le condizioni di vittoria e sconfitta di ogni livello.
        - Gestisce i checkpoint e il reset dei livelli in caso di timeout.
        - Esegue la transizione automatica al livello successivo al completamento di quello corrente.

---
## Package `com.mycompany.poggioadventure.core.abstracts`: 
Definisce le astrazioni fondamentali su cui si basa l’intera architettura dell’avventura. Qui sono dichiarate le interfacce e le classi astratte che rappresentano i contratti e i comportamenti base per la logica di gioco, i livelli, l’osservabilità e la gestione dei comandi speciali che riguardano però altri elementi del gioco.
- `GameDescription.java`
    - Gestisce la mappa di gioco, l’inventario, la posizione del giocatore e i comandi da inserire. Espone metodi astratti come `init()` (per l’inizializzazione del mondo di gioco volendo) e `nextMove()` (per l’elaborazione dei comandi).  Funziona da base per le implementazioni concrete come `PoggioAdventureDesc`.
-  `GameState.java`
    - Definisce il contratto per ogni “livello” del gioco, secondo il pattern State. Gestisce il tempo limite, la stanza di partenza, gli oggetti richiesti e vietati per il completamento/fallimento del livello.
- `IFlipperCommandProcessor.java`
    - Contratto per la logica di elaborazione dei comandi del Flipper Zero (*livello 3*). Definisce i metodi che ogni processore di comandi del Flipper deve implementare.
-  `GameObservable.java`
    - Definisce il contratto per l’implementazione del pattern Observer all’interno del gioco. Permette di registrare, notificare e gestire observer che reagiscono a eventi di gioco (es. cambi di stato, azioni del giocatore).
- `MenuManager.java`
	- Contratto per la struttura che i menu di gioco devono rispettare (CLI/GUI). 

---
## Package `com.mycompany.poggioadventure.levels`:
Questo package contiene le implementazioni concrete dei livelli di gioco. Ogni classe `LevelXState` estende la classe astratta `GameState` e definisce la logica, gli obiettivi e il contenuto specifico di una fase dell'avventura. Il package include anche classi di supporto che incapsulano meccaniche che sono presenti nei livelli e usati negli `Observer`.
-  `Level1State.java`
    - Implementa il primo livello, _"Introduzione & Test di Logica"_. Posizionando/rimuovendo elementi del gioco che è possibile incontrare durante l'avventura.
-  `Level2State.java`
    - Implementa il secondo livello, _"Prova Tecnica"_. Posizionando/rimuovendo elementi del gioco che è possibile incontrare durante l'avventura.
-  `Level3State.java`
    - Implementa il livello finale, _"Rivoluzione Robot"_. Posizionando/rimuovendo elementi del gioco che è possibile incontrare durante l'avventura.
- `PcAssemblyHelper.java`
    - Classe funzionale di utilità statica che supporta la logica del `Level2State`.
- `Question.java`
	- Modella una singola domanda a risposta multipla utilizzata nei test logici del gioco (ad esempio nel primo livello).
-  `Test.java`
    - **Funzione**: Classe-dati che modella un test (con delle domande) a risposta multipla, serve solo a rappresentare dati, senza logica complessa.
- `TestSession.java`
    - Gestisce la logica di esecuzione interattiva di un `Test`. Si interfaccia con l'`OutputHandler` per la CLI e con `JOptionPane` per la GUI.
-  `FlipperLogic.java`
    - Agisce da **Facade** per l'interfaccia contestuale del "Flipper Zero" nel `Level3State`. Fornisce un'interfaccia unificata per avviare l'interazione con il Flipper, nascondendo la complessità di gestire la CLI o la GUI. Delega l'elaborazione dei comandi a `FlipperCommandProcessor`.
-  `FlipperCommandProcessor.java`
    - Implementa la logica di business. Esegue il parsing e la validazione dell'input dell'utente (es. `[frequenza] [comando]`). Contiene la logica per ogni comando (`GoToRecharge`, `Override`, `Stop`) e ne determina le conseguenze (vittoria, sconfitta, penalità di tempo).
- `PcAssemblyHelper.java`
	- Classe *funzionale* di utilità che centralizza la logica di validazione e gestione dell’assemblaggio del PC nel secondo livello. Definisce l’ordine corretto dei componenti da inserire nel case del PC (scheda madre, RAM, SSD, CPU, pasta termica, dissipatore, GPU, alimentatore). Garantisce coerenza e riutilizzo della logica di assemblaggio.

---
### Package `com.mycompany.poggioadventure.core.utils`
Questo package raccoglie tutte le classi di utilità trasversali e gli strumenti per l’integrazione con servizi esterni (come il backend REST). È pensato per fornire metodi riutilizzabili, helper per la gestione del tempo, *factory* di *engine* e soprattutto il client HTTP per la comunicazione con il server.
- **`PoggioClientJersey.java`**  
    Implementa il client REST per la comunicazione con il backend `PoggioServer`. Gestire tutte le chiamate HTTP verso le API (CRUD utenti, registrazione vittorie, download log, recupero classifica) e autenticazione tramite *API Key*.
-  `ApiClientResult.java`  
    *Enum* che standardizza i codici di ritorno delle chiamate **REST**, permettendo una gestione robusta degli errori e dei casi limite lato client.
-  `ResourceLoader.java`  
    Helper per la gestione dei path delle risorse (immagini, log, font, ecc.), centralizzando la logica di risoluzione dei file e delle directory.
-  `GameContext.java`  
    Incapsula il contesto di gioco condiviso tra engine, observer e UI (tutto quello che può e potrebbe essere modificato ad esempio dagli *observer*).
-  `EngineFactory.java`  
    Classe funzionale **Factory** per la creazione di istanze di engine.
    `StopWatch.java` `TimeManager.java`  
    Utility per la gestione del tempo di gioco totale, timer per il countdown dei livelli (es. per poi calcolare il punteggio).
- **`Utils.java`**  
    Classe *funzionale* *statica* con metodi di utilità generici: parsing, validazione, costanti di sistema e di gioco, metodi di supporto per oggetti e stanze.
-  `FlipperResult.java`
	Implementa un **value object** immutabile che rappresenta l’esito di un comando inviato al Flipper Zero.
	
---
### Package `com.mycompany.poggioadventure.model`  
Questo package contiene tutte le classi che rappresentano i modelli dati del mondo di gioco. Definisce le entità che costituiscono l’ambiente, gli oggetti interagibili, i personaggi non giocanti (NPC) e elementi fondamentali del gioco come l'inventario.
- `AdvObject.java`  
    Rappresenta un oggetto generico interagibile nel gioco (es. chiavi, strumenti, oggetti raccolti). Gestisce proprietà come nome, descrizione, alias, visibilità e interazioni base *(pickupable, pushable, ecc.)*.
- `AdvObjectContainer.java`
    Estende `AdvObject` e rappresenta un contenitore che può includere altri oggetti (es. armadi, cassetti). Permette la logica di apertura/chiusura e la gestione del contenuto.
- `AdvNPC.java`
    Estende `AdvObject` e modella un personaggio non giocante. Gestisce dialoghi, alias, immagini e logica di interazione con il giocatore (es. consegna oggetti, dialoghi ramificati).
- `Inventory.java` 
    Rappresenta l’inventario del giocatore. Gestisce l’aggiunta, la rimozione e la ricerca di oggetti, oltre a fornire utility per la visualizzazione e la serializzazione dello stato.
- `Room.java`  
    Definisce una stanza del mondo di gioco. Gestisce collegamenti con altre stanze, oggetti presenti, descrizioni, immagini e proprietà speciali (es. stanze bloccate). E' strettamente legato a `GameMap.java`.
    
---
## Package: `com.mycompany.poggioadventure.observers`
Implementa il pattern Observer per la gestione reattiva degli eventi di gioco. Ogni observer è responsabile di una specifica categoria di comandi o interazioni, permettendo di separare la logica delle azioni dal core *engine* e garantendo estendibilità e manutenibilità. Gli observer *"ascoltano"* i comandi del giocatore **(es. USE, OPEN, PUSH, ecc.)** e producono effetti contestuali, feedback, o modifiche allo stato del gioco.
> Di seguito viene riportata solo la descrizione della classe più importante.
- `GameObserver.java`  
    Interfaccia base per tutti gli observer. Definisce il contratto per l'implementazione degli *observer* di gioco tramite il metodo `update`.

---
## Package: `com.mycompany.poggioadventure.parser`
 Si occupa dell’analisi e interpretazione dei comandi testuali inseriti dal giocatore. Tutta la logica di riconoscimento dei comandi, gestione degli oggetti e parsing avanzato delle frasi è incapsulata qui.
 - `Parser.java`
	 Analizza la stringa di input dell’utente, suddividendola in comandi e oggetti. Supporta parsing avanzato: comandi concatenati ("prendi chiave e apri porta") e oggetti multipli. Restituisce una lista di `ParserOutput` che rappresentano le azioni atomiche da eseguire.
- `ParserOutput.java` 
	Incapsula il comando riconosciuto e l’oggetto target (o gli oggetti) che sia nell'inventario o nella stanza.
- `Command.java`
	Definisce un comando all'interno del gioco costituita da  un `CommandType` e un *set* di *Alias* associati a quel comando.
- `CommandType.java`
	Enum che rappresenta i possibili comandi usabili nel gioco *(es. PRENDI, USA, GUARDA, PARLA, SALVA, etc)*.

---
## Package: `com.mycompany.poggioadventure.persistence`
Gestione della persistenza locale dei salvataggi, caricamento delle risorse (file, immagini, font), gestione dei log di gioco, DTO per la classifica.
- `SaveGame.java`
    Implementa il sistema di salvataggio e caricamento dello stato di gioco tramite serializzazione Java. Si occupa del cleanup automatico dei salvataggi obsoleti e della validazione dell’integrità dei dati. Integra la sincronizzazione utente con il backend tramite chiamate REST (es. registrazione utente su server).
- `ResourceLoader.java`
    Classe funzione utility centrale per il caricamento di risorse statiche dell’applicazione
    - Carica immagini, font, file di configurazione e dati di gioco.
    - Fornisce path centralizzati per directory di log, salvataggi, immagini e download.
    - Gestisce la validazione e la presenza delle risorse richieste dal gioco.
- `LoggerInput.java`  
    Gestisce la scrittura, lettura e decifratura dei file di log delle sessioni di gioco.
    - Permette la creazione di log temporanei decrittati per l’upload sicuro verso il server.
    - Fornisce metodi per la cancellazione sicura dei log e per la gestione dei percorsi dei file.
- `RankingEntryDTO.java`  
    Data Transfer Object utilizzato per rappresentare le entry della classifica scaricate dal server.
    - Contiene i dati serializzati relativi a username, punteggio, data, ora e percorso del log.
    - Facilita la deserializzazione automatica delle risposte REST per la classifica.

--- 
## Package: `com.mycompany.poggioadventure.ui`
Questo package contiene tutte le classi dedicate alla presentazione e gestione dell’interfaccia utente del gioco, sia per la modalità grafica (GUI, tramite Swing) sia per la modalità a riga di comando (CLI). L’obiettivo è fornire un layer di presentazione, garantendo un’esperienza coerente sia su console che su interfaccia grafica.

>La maggior parte delle classi di questo package si occupa esclusivamente della visualizzazione e dell’interazione con l’utente, senza implementare logica di gioco. Molte classi sono semplici componenti grafici (finestre, pannelli, dialoghi) o gestori di input/output, e non sono fondamentali per comprendere il funzionamento core dell’applicazione. **Per questo motivo, non tutte le classi vengono documentate singolarmente.**

- `ui.cli` (*Sottopackage*)
    Contiene tutte le classi per la gestione dell’interfaccia a riga di comando:
    - Gestione input/output testuale con supporto ai colori ANSI (`CLIOutputHandler`).
    - Gestione errori (`CLIErrorHandler`).
    - Menu da CLI (`CLIMenu`).
- `ui.gui` (*Sottopackage*)
    Contiene tutte le classi per la gestione dell’interfaccia grafica:
    - Gestione pannelli, dialoghi e output grafico (`GUIOutputHandler`)
    - Gestione errori tramite dialoghi Swing (`GUIErrorHandler`)
    - Sottopackage `views` con tutte le schermate principali del gioco (partita, inventario, classifica, ecc.)
- **`UI_Abstract`**  
    Classe base astratta per tutte le finestre GUI, centralizza la configurazione e l’inizializzazione dei componenti.
- `CLIOutputHandler`/ `GUIOutputHandler`  
    Implementano l’output formattato rispettivamente per console e GUI, con gestione avanzata di colori, immagini (solo GUI), markup e layout. Usando l'interfaccia `OutputHandler.java`
- `InputHandler`  
    Definisce il contratto per la lettura dell’input utente. Metodo unico `getInput()` che restituisce la stringa inserita dall’utente. Permette di disaccoppiare la logica di acquisizione input dalla logica di gioco.
- `CLIInputHandler`
    Implementazione per la modalità console
    - Utilizza uno `Scanner` su `System.in` per leggere l’input.
    - Effettua il **trimming** automatico degli spazi bianchi.
- `GUIInputHandler`
    Implementazione per la modalità grafica:
    - Riceve l’input da un componente `JTextField` Swing.
    - Pulisce l’input rimuovendo spazi bianchi iniziali e finali.
    - Progettata per essere thread-safe e integrarsi con l’EDT di Swing.

---