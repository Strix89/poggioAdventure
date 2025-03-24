package di.uniba.map.b.adventure;

/**
 *
 * @author tomma
 */
public interface FlowOutput {
    void write(String message, ColorText color);
    void writeln(String message, ColorText color);
    void writeln();
}
