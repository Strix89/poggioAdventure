# File

Il progetto PoggioAdventure implementa file per diverse funzionalità critiche del gioco. Di seguito sono descritti i principali utilizzi dei file all'interno dell'avventura.


### 1. Sistema di Persistenza: Salvataggio e Caricamento del Gioco

Una delle caratteristiche più importanti di qualsiasi avventura testuale moderna è la capacità di salvare il progresso del giocatore, permettendo di riprendere la partita in un secondo momento. Nel nostro progetto, questa funzionalità è implementata attraverso un sistema di serializzazione classico di Java.
Il fulcro del sistema di salvataggio risiede nel metodo `saveGame()` della classe `Engine`, il quale effettua una chiamata al metodo statico `saveGame()` della classe `SaveGame` dove viene utilizzata la serializzazione di oggetti per mantenere l'intero stato del gioco:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/persistence/SaveGame.java
public static void saveGame(Engine engine, OutputHandler output) {
PoggioClientJersey gameClient = null;
try {
    String playerName = engine.getPlayerName();
    LocalDateTime now = LocalDateTime.now();

    // Cleanup salvataggi esistenti per giocatore
    try {
        List<Path> existingSaves = Files.list(SAVE_DIR)
            .filter(p -> {
                String fileName = p.getFileName().toString();
                return fileName.startsWith(playerName + "_") && fileName.endsWith(".dat");
            })
            .collect(Collectors.toList());

        for (Path saveFile : existingSaves) {
            try {
                Files.delete(saveFile);
            } catch (IOException e) {
                output.writeln("\nATTENZIONE: Errore durante la cancellazione del vecchio salvataggio "
                                + saveFile.getFileName() + ": " + e.getMessage(), ColorText.ERROR);
            }
        }
    } catch (IOException e) {
        output.writeln("\nATTENZIONE: Errore durante l'accesso ai salvataggi per la pulizia: " + e.getMessage(), ColorText.ERROR);
    }

    // Generazione nuovo file con timestamp
    String fileName = String.format("%s_%s.dat",
        playerName,
        DATE_FORMATTER.format(now)
    );
    Path savePath = SAVE_DIR.resolve(fileName);

    // Serializzazione componenti in ordine fisso
    try (ObjectOutputStream out = new ObjectOutputStream(
        Files.newOutputStream(savePath, StandardOpenOption.CREATE)
    )) {
        out.writeObject(engine.getPlayerName());
        out.writeObject(engine.getGame());
        out.writeObject(engine.getLongGameTime());
        out.writeObject(engine.getLogger().getFileName());
        
        GameStateManager gsm = engine.getGameStateManager();
        if (gsm != null) {
            out.writeObject(gsm.getCurrentLevelIndex());
            out.writeObject(gsm.getCurrentLevelElapsedTime());
            out.writeObject(gsm.getLevelMapSnapshot());
            out.writeObject(gsm.getLevelInventorySnapshot());
            out.writeObject(gsm.getLevelStartingRoomSnapshot());
        } else {
            // Fallback values per GSM null
            out.writeObject(0);
            out.writeObject(0L);
            out.writeObject(null);
            out.writeObject(null);
            out.writeObject(null);
        }
    }
    output.writeln("\nGioco salvato con successo come: " + fileName, ColorText.GREEN);

    // Sync user con backend server
        gameClient = new PoggioClientJersey();
    ApiClientResult addResult = gameClient.addUser(playerName);
    if (addResult != ApiClientResult.SUCCESS_CREATED && addResult != ApiClientResult.USER_ALREADY_EXISTS) {
            output.writeln("Attenzione: problema nella sincronizzazione utente con server (" + addResult + ")", ColorText.YELLOW);
    }
    gameClient.close();

} catch (IOException e) {
    output.writeln("\nERRORE durante il salvataggio del gioco: " + e.getMessage(), ColorText.ERROR);
} catch(Exception e) {
        output.writeln("\nERRORE IMPREVISTO durante il salvataggio: " + e.getMessage(), ColorText.ERROR);
        if (gameClient != null) gameClient.close();
}
}
```

Non si tratta solo di memorizzare la posizione del giocatore o gli oggetti nell'inventario, ma di preservare l'intero oggetto `GameDescription` che contiene tutte le stanze, gli oggetti, i personaggi non giocanti e le loro relazioni. Viene salvato anche il tempo di gioco trascorso, permettendo di ripristinare con precisione la durata della sessione di gioco, e il buffer temporaneo dei comandi per mantenere la continuità del logging.
Vengono inoltre serializzati gli attributi chiave della classe `GameStateManager`, come l'indice del livello corrente, il tempo trascorso nel livello e snapshot della mappa, dell'inventario e della stanza iniziale del livello, per garantire un ripristino fedele dello stato del livello al momento del salvataggio.
La scelta di utilizzare file con estensione `.dat` per i salvataggi garantisce compattezza e sicurezza dei dati. Il formato del nome file segue la convenzione `<nomeGiocatore><dataEOra>.dat`, permettendo di identificare univocamente ogni salvataggio.

### 2. Sistema di Logging: Tracciamento Completo delle Azioni

Parallelamente al sistema di salvataggio, il progetto implementa un sistema di logging delle azioni del giocatore che serve sia per consentire all'utente di visualizzare quali sono i comandi che in serie altri utenti hanno lanciato, sia da paramentro per la costruzione della classifica nel DB.
La classe `LoggerInput` rappresenta il nucleo di questo sistema:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/persistence/LoggerInput.java
private void createLogFile() {
if (fileName == null) return;

try {
    File logFile = new File(fileName);
    if (!logFile.createNewFile()) {
        errorHandler.handleRecoverableError("File di log già esistente: " + fileName);
    }
} catch (IOException ex) {
    errorHandler.handleFatalError("Errore durante la creazione del file di log", ex);
    Utils.exitApplication(Utils.EXIT_CODE_LOG_ERROR);
}
}
```

Quando il giocatore completa l'avventura, il sistema automaticamente elimina sia i file di salvataggio che i log locali, come si può vedere nel metodo `handleGameCompleted()`:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/core/GameStateManager.java
private void handleGameCompleted() {
// Ferma il cronometro di gioco
gameTime.stop();
saveGame();


output.writeln("\nGIOCO TERMINATO", ColorText.EMERALD);

// Pausa di 3 secondi prima di chiudere
try {
    Thread.sleep(2000); // 3000ms = 3 secondi
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
}

// Invio dati vittoria al server
sendVictoryDataToServer();

// Pausa di 1 secondo prima delle operazioni di pulizia
try {
    Thread.sleep(2000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

output.writeln("Eliminazione salvataggi... [RED]Non ti servono[/]!!", ColorText.YELLOW);
deleteCurrentSaveEndGame();
LoggerInput.deleteLogFile(logger.getPathFile());

// Pausa di 2 secondi prima di chiudere
try {
    Thread.sleep(2000); // 2000ms = 2 secondi
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
}

output.writeln("Tempo di gioco: " + getFormattedGameTime(), ColorText.WHITE);
output.writeln("Grazie per aver giocato a poggioAdventure!", ColorText.GREEN);

// Pausa di 2 secondi prima di chiudere
try {
    Thread.sleep(2000); // 2000ms = 2 secondi
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
}

returnToAppropriateMenu();
}
```

In questo caso, i file di log vengono inviati al poggioServer, il quale lo utilzzerà come paramentro per il calcolo del rank all'interno della classifca. 
Nel caso in cui invece l'utente non completa l'avventura, i file di log verranno salvati all'interno della cartella `./rosources/logs`


### 3. Gestione delle Risorse e Configurazioni

Il sistema di gestione delle risorse è centralizzato nella classe `ResourceLoader`, che definisce una struttura di directory organizzata e fornisce metodi utility per il caricamento di file di configurazione. 
Nello specifico, il file contente le stopwords viene caricato attraverso il metodo `loadFileListInSet()`. Le stopwords rappresentano parole comuni (articoli, preposizioni, congiunzioni) che il parser del gioco deve ignorare per concentrarsi sui termini significativi dei comandi:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/persistence/ResourceLoader.java
public static Set<String> loadFileListInSet(File file) throws IOException {
if (file == null) {
    throw new IllegalArgumentException("Il file non può essere null");
}

Set<String> set = new HashSet<>();
try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    String line;
    while ((line = reader.readLine()) != null) {
        set.add(line.trim().toLowerCase());
    }
}
return set;
}
```

Questo approccio permette di configurare il comportamento del parser senza dover ricompilare il codice, offrendo flessibilità nella gestione del linguaggio naturale e permettendo potenziali localizzazioni future del gioco.


### 4. Comunicazione con Servizi Esterni

Un'altro aspetto del sistema di gestione file è l'integrazione con un server esterno attraverso la classe `PoggioClientJersey`. Questa implementazione permette di scaricare file di log dal server, consentendo ai giocatori di condividere i propri progressi e scaricare log di altri giocatori per confronti.

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/core/utils/PoggioClientJersey.java
public ApiClientResult downloadLogFile(String username) {
String localSavePath = ResourceLoader.LOGS_DW_DIRECTORY.resolve(username + "_log.txt").toString();

if (username == null || username.trim().isEmpty() || localSavePath == null || localSavePath.trim().isEmpty()) {
    return ApiClientResult.INVALID_INPUT_CLIENT;
}

WebTarget target = client.target(serverBaseUri).path("players").path(username).path("log");
Response response = null;

try {
    response = target.request(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HEADER_API_KEY, SHARED_SECRET)
                    .get();

    int statusCode = response.getStatus();

    if (statusCode == Response.Status.OK.getStatusCode()) {
        try (InputStream inputStream = response.readEntity(InputStream.class)) {
            Path targetPath = Paths.get(localSavePath);
            Path parentDir = targetPath.getParent();
            if (parentDir != null) {
                    Files.createDirectories(parentDir);
            }
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return ApiClientResult.SUCCESS_OK;
        } catch (IOException e) {
            return ApiClientResult.FILE_ERROR;
        } catch (Exception e) {
            return ApiClientResult.CONNECTION_ERROR;
        }

    } else {
        return handleResponseStatus(response, "downloadLogFile", username, 200, 404);
    }

} catch (ProcessingException | WebApplicationException e) {
    return ApiClientResult.CONNECTION_ERROR;
} catch (Exception e) {
    return ApiClientResult.UNKNOWN_ERROR;
} finally {
    if (response != null) response.close();
}
}
```

# Database (JDBC) - REST

### Persistenza e Classifica tramite JDBC

Per la gestione della classifica e la persistenza dei dati dei giocatori, il progetto utilizza un database relazionale **file-based** tramite Java Database Connectivity (JDBC), implementato interamente nel modulo **poggioServer**.  
È stato scelto il database **H2** in modalità ***file-based***: ciò significa che tutte le informazioni vengono memorizzate su disco locale, senza la necessità di un server esterno o di installazioni aggiuntive. 
La configurazione di H2 avviene tramite il file `db.properties`, dove viene specificato l'URL JDBC in modalità file-based, ad esempio:
```
db.url=jdbc:h2:./resources/poggioDatabase;AUTO_SERVER=TRUE
db.user=sa
db.password=
```
In questo modo, i dati vengono salvati in file fisici all'interno della directory del progetto, rendendo il database facilmente trasportabile e azzerando le dipendenze esterne.

#### **Lato Server: Gestione JDBC**

Nel modulo `poggioServer`, il database viene gestito tramite JDBC, con una struttura tipica che prevede:

- **Connessione al database**:  
La configurazione della connessione è centralizzata in un file di proprietà (`resources/db.properties`), che contiene i parametri come URL, username e password.  
Il caricamento avviene tramite la classe [`DatabaseManager`](poggioServer/src/main/java/com/mycompany/poggioserver/db/DatabaseManager.java), che utilizza HikariCP per il connection pooling e inizializza lo schema del database se necessario:

```java
// filepath: poggioServer/src/main/java/com/mycompany/poggioserver/db/DatabaseManager.java
Properties dbProps = loadProperties();
HikariConfig config = new HikariConfig();
config.setJdbcUrl(dbProps.getProperty("db.url"));
config.setUsername(dbProps.getProperty("db.user"));
config.setPassword(dbProps.getProperty("db.password"));
dataSource = new HikariDataSource(config);
```

> **Nota sull'uso di HikariCP**  
> All'interno della classe `DatabaseManager`, i moduli `HikariConfig` e `HikariDataSource` sono utilizzati per gestire efficientemente le connessioni al database tramite un connection pool.  
> - `HikariConfig` serve per configurare i parametri del pool (URL JDBC, utente, password, driver, dimensione del pool, timeout, ecc.).
> - `HikariDataSource` rappresenta il pool vero e proprio e fornisce connessioni già pronte, gestendo il loro riutilizzo.
> Questo approccio evita la creazione e distruzione continua di connessioni: invece di aprire e chiudere una nuova connessione ad ogni richiesta, le connessioni vengono create una sola volta e mantenute pronte nel pool. Quando serve una connessione, viene presa dal pool e poi restituita, riducendo il tempo di attesa e il carico sul database.  
> In sintesi, il pool riutilizza le stesse connessioni per molte richieste, ottimizzando l’accesso al database H2 e migliorando le prestazioni e la scalabilità del server, soprattutto in presenza di molte richieste concorrenti.


- **Operazioni CRUD e Classifica**:  
Tutte le operazioni di creazione, lettura, aggiornamento e cancellazione degli utenti e delle partite vengono eseguite tramite query SQL, gestite dalle classi, tramite l'interfaccia [`PlayerDAO`](poggioServer/src/main/java/com/mycompany/poggioserver/db/PlayerDAO.java) e la implementazione [`PlayerDAOImpl`](poggioServer/src/main/java/com/mycompany/poggioserver/db/PlayerDAOImpl.java).  
Ad esempio, per aggiungere un nuovo utente:

```java
// filepath: poggioServer/src/main/java/com/mycompany/poggioserver/db/PlayerDAOImpl.java
String sql = "INSERT INTO players (username) VALUES (?)";
try (Connection conn = DatabaseManager.getConnection();
    PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, username);
    pstmt.executeUpdate();
}
```

E per registrare una vittoria:

```java
// filepath: poggioServer/src/main/java/com/mycompany/poggioserver/db/PlayerDAOImpl.java
String sql = "UPDATE players SET data=?, ora=?, percorso_file_log=?, durata_ms=?, punteggio=? WHERE username=?";
try (Connection conn = DatabaseManager.getConnection();
    PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setDate(1, data);
    pstmt.setTime(2, ora);
    pstmt.setString(3, logFilePath);
    pstmt.setLong(4, durataMs);
    pstmt.setInt(5, punteggio);
    pstmt.setString(6, username);
    pstmt.executeUpdate();
}
```

La struttura della tabella `players` è creata automaticamente se non esiste, grazie al metodo `initializeDatabaseSchema()` di [`DatabaseManager`](poggioServer/src/main/java/com/mycompany/poggioserver/db/DatabaseManager.java):

```java
// filepath: poggioServer/src/main/java/com/mycompany/poggioserver/db/DatabaseManager.java
String createTableSQL = "CREATE TABLE IF NOT EXISTS players (" +
                        "username VARCHAR(255) PRIMARY KEY, " +
                        "data DATE NULL, " +
                        "ora TIME NULL, " +
                        "percorso_file_log VARCHAR(1024) NULL, " +
                        "durata_ms BIGINT NULL, " +
                        "punteggio INT NULL)";
```

#### **Lato Client: Interazione con il Database tramite REST**

Nel modulo `poggioAdventure` (client), non viene effettuato alcun accesso diretto al database. Tutte le operazioni avvengono tramite chiamate HTTP al server, utilizzando la classe [`PoggioClientJersey`](poggioAdventure/src/main/java/com/mycompany/poggioadventure/core/utils/PoggioClientJersey.java):

- **Registrazione utente**:  
Quando si salva una partita o si avvia una nuova sessione, il client invia una richiesta per registrare l'utente sul server:

```java
ApiClientResult addResult = gameClient.addUser(playerName);
```

- **Registrazione vittoria**:  
Al termine del gioco, il client invia i dati della vittoria (incluso il file di log) al server, che li memorizza nel database:

```java
ApiClientResult result = gameClient.recordVictoryWithLog(
    playerName, 
    currentDate, 
    currentTime, 
    gameDurationMs, 
    tempLogFile.toString()
);
```

- **Recupero classifica e log**:  
Il client può richiedere la classifica aggiornata o scaricare i log delle partite direttamente dal server, che a sua volta interroga il database per fornire i dati richiesti.

### Integrazione REST: Comunicazione Client-Server

L’interazione tra client e server per la gestione della classifica e delle vittorie avviene esclusivamente tramite chiamate REST, garantendo una netta separazione tra la logica di gioco (client) e la persistenza dei dati (server). Tutte le operazioni CRUD sugli utenti, la registrazione delle vittorie e il download dei log sono esposte tramite endpoint RESTful definiti nella classe [`PlayerResource`](poggioServer/src/main/java/com/mycompany/poggioserver/resources/PlayerResource.java) del modulo **poggioServer**.

Le principali chiamate REST implementate sono:

- **Registrazione utente (POST /players/{username})**  
  Permette di creare un nuovo utente nel database.  
  Esempio di chiamata dal client:
  ```java
  ApiClientResult addResult = gameClient.addUser(playerName);
  ```

  Lato server, la richiesta viene gestita dal metodo `addPlayer` di [`PlayerResource`](poggioServer/src/main/java/com/mycompany/poggioserver/resources/PlayerResource.java).

- **Registrazione vittoria con log (PUT /players/{username}/victory)**  
  Permette di registrare una vittoria, inviando anche il file di log della partita tramite multipart form-data.  
  Esempio di chiamata dal client:
  ```java
  ApiClientResult result = gameClient.recordVictoryWithLog(
      playerName, 
      currentDate, 
      currentTime, 
      gameDurationMs, 
      tempLogFile.toString()
  );
  ```
  Lato server, la richiesta viene gestita dal metodo `recordVictory` di [`PlayerResource`](poggioServer/src/main/java/com/mycompany/poggioserver/resources/PlayerResource.java), che si occupa di validare i parametri, salvare il file di log e aggiornare il database.

- **Recupero classifica (GET /players/ranking)**  
  Consente al client di ottenere la classifica aggiornata interrogando il server:
  ```java
  List<RankingEntryDTO> ranking = gameClient.getRanking();
  ```
  Il server restituisce una lista ordinata di giocatori con i relativi punteggi.

- **Download log partita (GET /players/{username}/log)**  
  Permette di scaricare il file di log associato a una vittoria:
  ```java
  ApiClientResult downloadResult = gameClient.downloadLogFile(username);
  ```
  Il server restituisce il file come stream binario.

>Nota:  
>La classe [`RankingEntryDTO`](poggioAdventure/src/main/java/com/mycompany/poggioadventure/persistence/RankingEntryDTO.java) rappresenta il Data Transfer Object utilizzato per trasferire in modo semplice e strutturato le informazioni di una singola voce della classifica tra server e client.  
>Questa classe contiene i campi essenziali per la visualizzazione della classifica: username del giocatore, data e ora della vittoria, e punteggio ottenuto.  
>Segue le convenzioni JavaBeans (costruttore vuoto, getter e setter pubblici) per garantire la compatibilità con i framework di serializzazione/deserializzazione JSON utilizzati nelle chiamate REST.  
>Inoltre, implementa i metodi `equals`, `hashCode` e `toString` per un corretto funzionamento nelle collezioni e per facilitare il debug.
>Per la controparte server, la stessa struttura è definita nella classe [`RankingEntryDTO`](poggioServer/src/main/java/com/mycompany/poggioserver/resources/RankingEntryDTO.java).

# Lambda Expression, Stream e Pipeline

Le **Lambda Expression** e gli **Stream** sono strumenti moderni di Java che permettono di scrivere codice più pulito ed efficiente, specialmente quando si lavora con collezioni di dati. 
Nel progetto *PoggioAdventure*, abbiamo usato queste funzionalità per rendere il codice più compatto, leggibile e facile da gestire.

### 1. Filtraggio e Manipolazione di Collezioni

Un esempio si trova nel sistema di salvataggio, dove si utilizzano **stream** e **lambda** per filtrare e raccogliere i file di salvataggio associati a un determinato giocatore:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/persistence/SaveGame.java
List<Path> existingSaves = Files.list(SAVE_DIR)
    .filter(p -> {
        String fileName = p.getFileName().toString();
        return fileName.startsWith(playerName + "_") && fileName.endsWith(".dat");
    })
    .collect(Collectors.toList());
```
In questo frammento, viene creato uno stream di file nella directory dei salvataggi, filtrato tramite una lambda che seleziona solo quelli appartenenti al giocatore corrente, e infine raccolto in una lista. Questo approccio sostituisce cicli tradizionali e rende il codice più espressivo.

### 2. Analisi delle Risposte nei Test (Stream Pipeline)

Nel sistema dei quiz/logica, gli **Stream** vengono utilizzate per confrontare in modo funzionale le risposte date dal giocatore con quelle corrette, calcolando rapidamente il numero di errori:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/core/levels/Test.java
public int countWrongAnswers(List<Integer> answers) {
    return (int) java.util.stream.IntStream.range(0, Math.min(questions.size(), answers.size()))
        .filter(i -> !questions.get(i).isCorrectAnswer(answers.get(i)))
        .count();
}
```
Qui si crea una pipeline che itera sugli indici delle domande, filtra quelli in cui la risposta è errata (tramite una lambda), e conta il totale. Questo pattern è ricorrente in tutto il progetto per operazioni di confronto e conteggio.

### 3. Mapping e Trasformazione di Oggetti

Per estrarre rapidamente gli ID degli oggetti richiesti per un test, si utilizza una **stream pipeline** con operazione di mapping:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/core/levels/Test.java
public List<Integer> getRequiredObjectIds() {
    if (requiredObjects == null) {
        return null;
    }
    return requiredObjects.stream()
        .map(AdvObject::getId)
        .collect(java.util.stream.Collectors.toList());
}
```
In questo caso, la lambda `AdvObject::getId` viene applicata a ogni elemento della lista, producendo una nuova lista di ID in modo compatto e leggibile.

### 4. Uso di Optional e Lambda per Gestione Null-Safe

Per verificare la presenza di oggetti richiesti, si sfrutta `Optional` con lambda per evitare controlli null espliciti:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/core/levels/Test.java
public boolean hasRequiredObjects() {
    return java.util.Optional.ofNullable(requiredObjects)
        .map(list -> !list.isEmpty())
        .orElse(false);
}
```
Questo pattern riduce la verbosità e rende la logica più robusta contro i `NullPointerException`.

### 5. Uso di Lambda per Eventi e Callback

Nel codice di avvio della GUI, viene utilizzata una lambda per eseguire il setup dell’interfaccia grafica in modo asincrono e thread-safe:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/PoggioAdventure.java
if(guiMode) {
    java.awt.EventQueue.invokeLater(() -> {
        UI_Init uiInit = new UI_Init();
        uiInit.setVisible(true);
    });
}
```
Questa lambda viene passata come callback da eseguire nel thread dell’Event Dispatch Thread di Swing.

# Swing

Per offrire un'esperienza utente moderna e accessibile anche a chi non ama la riga di comando, PoggioAdventure implementa una **interfaccia grafica completa** basata su **Swing**, la libreria standard di Java per la creazione di GUI. L'uso di Swing è stato fondamentale per realizzare un'interfaccia ricca, personalizzabile e facilmente estendibile, in grado di gestire output testuale formattato, immagini, input utente e dialog interattivi.

## Architettura e Pattern

L'interfaccia grafica è stata progettata seguendo il pattern **MVC** (Model-View-Controller), separando la logica di gioco dalla presentazione. Tutte le finestre Swing derivano dalla classe astratta [`UI_Abstract`](poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/UI_Abstract.java), che centralizza la configurazione comune (tema, stili, layout) e fornisce un template method per l'inizializzazione delle componenti:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/UI_Abstract.java
public abstract class UI_Abstract extends JFrame {
    public UI_Abstract() {
        initUI(); // Template method
    }

    private void initUI() {
        FlatLightLaf.setup(); // Tema moderno
        setTitle(getWindowTitle());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        setIconImage(UI_Config.getShieldImage());
        initComponents(); // Hook per le sottoclassi
        applyDialogStyles();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    protected abstract void initComponents();
    protected abstract String getWindowTitle();
}
```
Questa struttura garantisce coerenza visiva e comportamentale tra tutte le finestre dell'applicazione.

## Componenti Principali

### 1. Finestra di Gioco Principale

La finestra principale del gioco è implementata nella classe [`UI_Game`](poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/gui/views/UI_Game.java), che integra:

- **JTextPane** per l'output del gioco, con supporto a testo colorato e immagini.
- **JTextField** per l'inserimento dei comandi da parte dell'utente.
- **JButton** per inviare i comandi.
- **JScrollPane** per gestire lo scrolling automatico dell'output.
- **Dialog di conferma e salvataggio** tramite `JOptionPane`.

Esempio di creazione del pannello di output e input:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/gui/views/UI_Game.java
private JPanel createLeftPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setOpaque(false);

    gameOutputArea = new JTextPane();
    gameOutputArea.setEditable(false);
    gameOutputArea.setFont(UI_Config.getNormalFont().deriveFont(18f));
    gameOutputArea.setForeground(Color.WHITE);
    gameOutputArea.setBackground(new Color(60, 60, 60));
    
    outputScrollPane = new JScrollPane(gameOutputArea);
    outputScrollPane.setBorder(createSectionBorder("Log Gioco"));

    JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
    inputPanel.setOpaque(false);
    
    commandInput = new JTextField();
    sendButton = createButton("INVIA", UI_Config.BUTTON_BASE_COLOR, 14f);
    // ...
}
```

### 2. OutputHandler per GUI

La classe [`GUIOutputHandler`](poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/gui/GUIOutputHandler.java) si occupa di gestire l'output formattato nella JTextPane, supportando markup per colori e immagini, e garantendo la **thread safety** tramite `SwingUtilities.invokeLater`:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/gui/GUIOutputHandler.java
@Override
public void writeFormatted(String formattedMessage, ColorText baseColor) {
    SwingUtilities.invokeLater(() -> {
        try {
            StyledDocument doc = outputPane.getStyledDocument();
            // Parsing e inserimento testo colorato e immagini
            // ...
            outputPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            // Gestione errori
        }
    });
}
```
Questo permette di visualizzare in modo ricco e dinamico sia testo che immagini, migliorando l'immersione del giocatore.

### 3. Finestre Speciali e Dialoghi

Oltre alla finestra principale, sono state realizzate altre interfacce dedicate, come:

- **UI_Flipper**: una finestra custom per l'interazione con il "Flipper Zero", con input dedicato e visualizzazione di immagini ASCII.
- **UI_Inventory**: pannello per la gestione e visualizzazione dell'inventario, con descrizioni testuali e layout responsive.
- **UI_NewGame** e **UI_Init**: schermate di avvio e creazione nuova partita, con effetti hover, validazione input e feedback tramite dialoghi Swing.

Tutte queste finestre sfruttano componenti Swing come `JPanel`, `JLabel`, `JTextArea`, `JButton`, e dialoghi modali tramite `JOptionPane` per fornire feedback immediato all'utente.

### 4. Personalizzazione e Temi

Per migliorare l'aspetto grafico, è stato adottato il tema **FlatLaf**, che dona un look moderno e professionale a tutte le finestre. La personalizzazione dei colori, font e icone avviene tramite la classe [`UI_Config`](poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/gui/views/UI_Config.java), garantendo coerenza e facilità di manutenzione.

### Integrazione con la Logica di Gioco

L'integrazione tra la GUI e la logica di gioco avviene tramite pattern **Strategy**: le classi `GUIInputHandler` e `GUIOutputHandler` implementano le interfacce comuni `InputHandler` e `OutputHandler`, permettendo di intercambiare facilmente la modalità CLI e GUI senza modificare la logica sottostante.

### Esempio di Avvio GUI

L'avvio della GUI sfrutta una lambda per garantire l'esecuzione nel thread dell'**Event Dispatch Thread** di Swing:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/PoggioAdventure.java
if(guiMode) {
    java.awt.EventQueue.invokeLater(() -> {
        UI_Init uiInit = new UI_Init();
        uiInit.setVisible(true);
    });
}
```
# Thread e Programmazione Concorrente

La programmazione concorrente è stata adottata in *PoggioAdventure* per garantire un'esperienza utente fluida, reattiva e moderna sia in modalità CLI che GUI. L'utilizzo dei thread consente di gestire operazioni che richiedono attese (come timer, download di file o transizioni di stato) senza bloccare l'interfaccia utente o il flusso principale del gioco.

### 1. Gestione del Timer di Gioco

Uno degli utilizzi principali dei thread è nella gestione del **countdown** e del **cronometro di gioco**. La classe [`TimeManager`](poggioAdventure/src/main/java/com/mycompany/poggioadventure/core/utils/TimeManager.java) implementa l'interfaccia `Runnable` e viene eseguita in un thread dedicato, così da non bloccare il thread principale durante il countdown:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/core/utils/TimeManager.java
@Override
public void run() {
    while (inEsecuzione && tempoTrascorso < tempoTotale) {
        try {
            Thread.sleep(intervallo);
        } catch (InterruptedException e) {
            break; // Terminazione richiesta
        }
        tempoTrascorso += intervallo;
    }
    inEsecuzione = false;
}
```
In questo modo, il timer può essere avviato, fermato o riavviato in modo asincrono, senza interferire con l'elaborazione dei comandi o l'aggiornamento della GUI.

### 2. Aggiornamento Thread-Safe della GUI

Nell'interfaccia grafica, tutte le operazioni che modificano componenti Swing vengono eseguite nel **thread dell'Event Dispatch Thread (EDT)**, come raccomandato dalle best practice Java. Ad esempio, la classe [`GUIOutputHandler`](poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/gui/GUIOutputHandler.java) utilizza `SwingUtilities.invokeLater` per garantire che l'output venga renderizzato in modo sicuro e reattivo:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/gui/GUIOutputHandler.java
@Override
public void writeFormatted(String formattedMessage, ColorText baseColor) {
    SwingUtilities.invokeLater(() -> {
        try {
            StyledDocument doc = outputPane.getStyledDocument();
            // Parsing e inserimento testo colorato e immagini
            // ...
            outputPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            // Gestione errori
        }
    });
}
```
Questo pattern viene utilizzato in tutta la GUI per evitare condizioni di race e garantire la stabilità dell'interfaccia anche durante operazioni concorrenti.

### 3. Gestione Asincrona di Eventi e Transizioni di Stato

Alcune operazioni, come la gestione della fine del gioco o il download di file di log dal server, vengono eseguite in thread separati per non bloccare l'interazione dell'utente. Ad esempio, nella gestione della fine partita in modalità GUI, viene avviato un nuovo thread per gestire la transizione e le eventuali attese:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/core/GameStateManager.java
if (output instanceof GUIOutputHandler) {
    new Thread(() -> {
        try {
            Thread.sleep(2000);
            onGameCompleted.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }, "GameCompletionHandler").start();
} else {
    if (onGameCompleted != null) {
        onGameCompleted.run();
    }
}
```
In questo modo, la GUI rimane reattiva anche durante le pause o le operazioni di cleanup.

### 4. Download di File in Background

Per operazioni di I/O potenzialmente lente, come il download dei file di log dal server, viene utilizzato un thread dedicato che mostra una finestra di progresso e gestisce eventuali timeout senza bloccare la GUI:

```java
// filepath: poggioAdventure/src/main/java/com/mycompany/poggioadventure/ui/gui/views/UI_Rank.java
private void downloadLogInBackground(String username) {
    new Thread(() -> {
        // ... download file ...
        // Aggiornamento UI sul thread EDT
        SwingUtilities.invokeLater(() -> {
            // ... aggiorna stato UI ...
        });
    }, "LogDownloadThread").start();
}
```
Questo approccio migliora l'usabilità e previene blocchi dell'interfaccia durante operazioni di rete.
