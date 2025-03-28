package di.uniba.map.b.adventure;

/**
 * Interfaccia per la gestione della navigazione tra i menu del gioco.
 * 
 * <p>Definisce il contratto per la gestione delle principali schermate di menu:
 * <ul>
 *   <li>Menu principale</li>
 *   <li>Nuova partita</li>
 *   <li>Caricamento partita</li>
 *   <li>Classifica</li>
 *   <li>Uscita dal gioco</li>
 * </ul>
 * 
 * <p>Implementazioni tipiche:
 * <ul>
 *   <li>{@code CLIMenu} - gestione menu da terminale</li>
 *   <li>{@code UI_Init} - gestione menu grafici</li>
 * </ul>
 * 
 * @author Strix89
 */
public interface MenuManager {

    /**
     * Mostra il menu principale del gioco.
     * 
     * <p>Contiene tipicamente le opzioni:
     * <ul>
     *   <li>Nuova partita</li>
     *   <li>Carica partita</li>
     *   <li>Classifica</li>
     *   <li>Esci</li>
     * </ul>
     */
    void showMainMenu();

    /**
     * Mostra la schermata per iniziare una nuova partita.
     * 
     * <p>Dovrebbe gestire:
     * <ul>
     *   <li>Inserimento nome giocatore</li>
     *   <li>Selezione difficoltà (se applicabile)</li>
     *   <li>Avvio del gioco</li>
     * </ul>
     */
    void showNewGame();

    /**
     * Mostra la schermata di caricamento partita.
     * 
     * <p>Dovrebbe:
     * <ul>
     *   <li>Elencare i salvataggi disponibili</li>
     *   <li>Permettere la selezione</li>
     *   <li>Gestire il caricamento</li>
     * </ul>
     */
    void showLoadGame();

    /**
     * Mostra la classifica dei punteggi.
     * 
     * <p>Dovrebbe visualizzare:
     * <ul>
     *   <li>La top 10 dei punteggi</li>
     *   <li>Eventuali statistiche aggiuntive</li>
     *   <li>Un'opzione per tornare al menu</li>
     * </ul>
     */
    void showRanking();

    /**
     * Gestisce l'uscita dal gioco.
     * 
     * <p>Dovrebbe:
     * <ul>
     *   <li>Mostrare un messaggio di conferma</li>
     *   <li>Salvare eventuali impostazioni</li>
     *   <li>Chiudere l'applicazione</li>
     * </ul>
     */
    void exit();
}
