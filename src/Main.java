import javax.swing.*;

public class Main {
    /**
     * Hlavní metoda pro spuštění aplikace.
     * Vytváří instanci {@link VectorPointAppZoomUndo} v rámci Swing Event Dispatch Threadu.
     *
     * @param args Argumenty příkazové řádky (nejsou využity).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(VectorPointAppZoomUndo::new);
    }
}