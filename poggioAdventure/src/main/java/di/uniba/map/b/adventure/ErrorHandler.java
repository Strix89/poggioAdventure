package di.uniba.map.b.adventure;

/**
 *
 * @author tomma
 */
public interface ErrorHandler {
    void handleFatalError(String message, Throwable ex);
    void handleRecoverableError(String message);
}
