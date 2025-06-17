# File

Il progetto PoggioAdventure implementa file per diverse funzionalità critiche del gioco. Di seguito sono descritti i principali utilizzi dei file all'interno dell'avventura.


### 1. Sistema di Persistenza: Salvataggio e Caricamento del Gioco

Una delle caratteristiche più importanti di qualsiasi avventura testuale moderna è la capacità di salvare il progresso del giocatore, permettendo di riprendere la partita in un secondo momento. Nel nostro progetto, questa funzionalità è implementata attraverso un sistema di serializzazione classico di Java.
Il fulcro del sistema di salvataggio risiede nel metodo `saveGame()` della classe `Engine`, il quale effettua una chiamata al metodo statico `saveGame()` della classe `SaveGame` dove viene utilizzata la serializzazione di oggetti per mantenere l'intero stato del gioco:

```java
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
