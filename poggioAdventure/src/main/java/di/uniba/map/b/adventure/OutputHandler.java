package di.uniba.map.b.adventure;

/**
 *
 * @author tomma
 */
public interface OutputHandler {
    void write(String message, ColorText color);
    void writeln(String message, ColorText color);
    void writeln();
    void clear();
}
