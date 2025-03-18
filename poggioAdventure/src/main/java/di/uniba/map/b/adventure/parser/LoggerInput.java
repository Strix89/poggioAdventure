package di.uniba.map.b.adventure.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe che si occupa di registrare tutti gli input validi dell'utente
 * in un file di log denominato <nomeGiocatore>_Input.txt.
 */
public class LoggerInput {

    private final String fileName;

    /**
     * Costruttore della classe LoggerInput.
     *
     * @param playerName Nome del giocatore.
     */
    public LoggerInput(String playerName) {
        this.fileName = generateUniqueFileName(playerName);
        createLogFile();
    }

    /**
     * Metodo per generare un nome di file univoco.
     * Se il file <playerName>_Input.txt esiste, crea <playerName>2_Input.txt, <playerName>3_Input.txt, ecc.
    */
    private String generateUniqueFileName(String playerName) {
        File directory = new File("userInput");
        if(!directory.exists()){
            directory.mkdirs();
        }

        String baseName = "userInput/" + playerName + "_1" + "_Input.txt";
        File file = new File(baseName);

        int count = 2;
        while(file.exists()){
            baseName = "userInput/" + playerName + "_" + count + "_Input.txt";
            file = new File(baseName);
            count++;
        }

        return baseName;
    }




    /**
     * Metodo che crea il file di log se non esiste.
     */
    private void createLogFile(){
        File file = new File(fileName);
        if(!file.exists()){
            try{
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
               System.err.println("Errore durante la creazione del file di log." + e.getMessage());
            }
        }
    }

    /**
     * Metodo che registra l'input dell'utente se corretto nel file di log.
     *
     * @param parserOutput il risultato del parsing del comando inserito dall'utente.
     */
    public void logInput(String input){
        if (input != null && !input.trim().isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                writer.write(input+ "\n");
            } catch (IOException e) {
                System.err.println("Errore durante la scrittura sul file di log." + e.getMessage());
            }
        }
    }
    
}
