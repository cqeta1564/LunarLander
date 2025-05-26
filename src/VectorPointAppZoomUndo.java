import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hlavní třída aplikace {@code VectorPointAppZoomUndo}, která umožňuje uživateli načíst rastrový obrázek,
 * klikáním na něj přidávat vektorové body, přibližovat a oddalovat zobrazení obrázku,
 * odvolat poslední přidaný bod a exportovat souřadnice všech bodů do textového souboru.
 * Aplikace využívá Java Swing pro grafické uživatelské rozhraní.
 *
 * @author Tvoje Jméno (nebo AI Asistent)
 * @version 1.1
 */
public class VectorPointAppZoomUndo extends JFrame {

    /**
     * Panel zobrazující obrázek a vektorové body.
     */
    private ImagePanel imagePanel;
    /**
     * Aktuálně načtený rastrový obrázek.
     */
    private BufferedImage loadedImage;
    /**
     * Seznam uchovávající souřadnice vektorových bodů přidaných uživatelem. Souřadnice jsou relativní k originální velikosti obrázku.
     */
    private List<Point> vectorPoints;

    /**
     * Aktuální faktor přiblížení obrázku. 1.0 odpovídá 100% velikosti.
     */
    private double zoomFactor = 1.0;
    /**
     * Krok, o který se mění {@link #zoomFactor} při přiblížení/oddálení.
     */
    private final double ZOOM_INCREMENT = 0.1;
    /**
     * Maximální povolený faktor přiblížení.
     */
    private final double MAX_ZOOM = 5.0;
    /**
     * Minimální povolený faktor oddálení.
     */
    private final double MIN_ZOOM = 0.1;

    /**
     * Tlačítko pro odvolání posledního přidaného bodu.
     */
    private JButton undoButton;
    /**
     * Tlačítko pro přiblížení obrázku.
     */
    private JButton zoomInButton;
    /**
     * Tlačítko pro oddálení obrázku.
     */
    private JButton zoomOutButton;

    /**
     * Konstruktor třídy {@code VectorPointAppZoomUndo}.
     * Inicializuje hlavní okno aplikace, jeho komponenty (menu, tlačítka, panel pro obrázek)
     * a nastavuje listenery událostí.
     */
    public VectorPointAppZoomUndo() {
        setTitle("Nástroj pro vektorové body s Zoomem a Undo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1000, 750);
        setLocationRelativeTo(null); // Centrovat okno

        vectorPoints = new ArrayList<>();

        imagePanel = new ImagePanel();

        imagePanel.addMouseListener(new MouseAdapter() {
            /**
             * Voláno při kliknutí myší na panel s obrázkem.
             * Pokud je načten obrázek, přepočítá souřadnice kliku na souřadnice
             * originálního obrázku (s ohledem na aktuální zoom) a přidá nový bod.
             * @param e Událost myši obsahující informace o kliknutí.
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                if (loadedImage != null) {
                    int actualX = (int) (e.getX() / zoomFactor);
                    int actualY = (int) (e.getY() / zoomFactor);

                    actualX = Math.max(0, Math.min(actualX, loadedImage.getWidth() - 1));
                    actualY = Math.max(0, Math.min(actualY, loadedImage.getHeight() - 1));

                    vectorPoints.add(new Point(actualX, actualY));
                    System.out.println("Přidán bod: X=" + actualX + ", Y=" + actualY + " (na originále)");
                    updateUndoButtonState();
                    imagePanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(VectorPointAppZoomUndo.this,
                            "Nejprve načtěte obrázek.",
                            "Chyba",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        add(new JScrollPane(imagePanel), BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Soubor");

        JMenuItem loadMenuItem = new JMenuItem("Načíst obrázek...");
        loadMenuItem.addActionListener(e -> loadImage());
        fileMenu.add(loadMenuItem);

        JMenuItem exportMenuItem = new JMenuItem("Exportovat body...");
        exportMenuItem.addActionListener(e -> exportPoints());
        fileMenu.add(exportMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitMenuItem = new JMenuItem("Konec");
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel controlPanel = new JPanel(new FlowLayout());

        JButton loadButton = new JButton("Načíst obrázek");
        loadButton.addActionListener(e -> loadImage());
        controlPanel.add(loadButton);

        JButton exportButton = new JButton("Exportovat body");
        exportButton.addActionListener(e -> exportPoints());
        controlPanel.add(exportButton);

        zoomInButton = new JButton("Přiblížit (+)");
        zoomInButton.addActionListener(e -> zoomIn());
        zoomInButton.setEnabled(false);
        controlPanel.add(zoomInButton);

        zoomOutButton = new JButton("Oddálit (-)");
        zoomOutButton.addActionListener(e -> zoomOut());
        zoomOutButton.setEnabled(false);
        controlPanel.add(zoomOutButton);

        undoButton = new JButton("Odvolat poslední bod");
        undoButton.addActionListener(e -> undoLastPoint());
        undoButton.setEnabled(false);
        controlPanel.add(undoButton);

        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Vnitřní třída {@code ImagePanel} reprezentující panel, na kterém se vykresluje
     * načtený obrázek a přidané vektorové body. Podporuje dynamickou změnu velikosti
     * na základě faktoru přiblížení.
     */
    class ImagePanel extends JPanel {
        /**
         * Přepsaná metoda pro vykreslení komponenty.
         * Vykreslí načtený obrázek škálovaný podle aktuálního {@link VectorPointAppZoomUndo#zoomFactor}
         * a následně na něj vykreslí všechny vektorové body.
         *
         * @param g Grafický kontext použitý pro kreslení.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (loadedImage != null) {
                Graphics2D g2d = (Graphics2D) g.create();

                int zoomedWidth = (int) (loadedImage.getWidth() * zoomFactor);
                int zoomedHeight = (int) (loadedImage.getHeight() * zoomFactor);

                g2d.drawImage(loadedImage, 0, 0, zoomedWidth, zoomedHeight, this);

                g2d.setColor(Color.RED);
                for (Point p : vectorPoints) {
                    int displayX = (int) (p.x * zoomFactor);
                    int displayY = (int) (p.y * zoomFactor);
                    g2d.fillOval(displayX - 5, displayY - 5, 10, 10); // Vykreslí kroužek o průměru 10px
                }
                g2d.dispose();
            }
        }

        /**
         * Vrací preferovanou velikost tohoto panelu.
         * Velikost je vypočtena na základě rozměrů načteného obrázku a aktuálního {@link VectorPointAppZoomUndo#zoomFactor}.
         * Tato metoda je klíčová pro správnou funkci {@link JScrollPane}.
         *
         * @return Objekt {@link Dimension} reprezentující preferovanou velikost panelu.
         */
        @Override
        public Dimension getPreferredSize() {
            if (loadedImage != null) {
                return new Dimension(
                        (int) (loadedImage.getWidth() * zoomFactor),
                        (int) (loadedImage.getHeight() * zoomFactor)
                );
            }
            return super.getPreferredSize(); // Výchozí velikost, pokud není obrázek
        }
    }

    /**
     * Aktualizuje zobrazení obrázkového panelu.
     * Nastaví preferovanou velikost panelu, vyvolá jeho přeuspořádání a překreslení.
     * Také aktualizuje stav (aktivní/neaktivní) tlačítek pro zoom.
     * Volá se po změně zoomu nebo načtení nového obrázku.
     */
    private void updateImageDisplay() {
        if (loadedImage != null) {
            // getPreferredSize() v ImagePanel vypočítá správnou velikost na základě zoomFactor
            imagePanel.setPreferredSize(imagePanel.getPreferredSize());
            imagePanel.revalidate(); // Informuje JScrollPane o možné změně velikosti
            imagePanel.repaint();
            zoomInButton.setEnabled(zoomFactor < MAX_ZOOM);
            zoomOutButton.setEnabled(zoomFactor > MIN_ZOOM);
        } else {
            imagePanel.setPreferredSize(new Dimension(200, 200)); // Nějaká výchozí velikost, když není obrázek
            imagePanel.revalidate();
            imagePanel.repaint();
            zoomInButton.setEnabled(false);
            zoomOutButton.setEnabled(false);
        }
    }


    /**
     * Zobrazí dialog pro výběr souboru a načte vybraný rastrový obrázek.
     * Podporované formáty jsou JPG, PNG, GIF.
     * Po úspěšném načtení obrázku se resetuje zoom, vymažou se existující body
     * a aktualizuje se zobrazení.
     */
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Vyberte rastrový obrázek");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Obrázky (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage tempImage = ImageIO.read(selectedFile);
                if (tempImage == null) {
                    JOptionPane.showMessageDialog(this,
                            "Vybraný soubor není platný obrázek nebo jej nelze načíst.",
                            "Chyba načítání obrázku",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                loadedImage = tempImage;
                vectorPoints.clear();
                zoomFactor = 1.0; // Reset zoomu
                updateImageDisplay();
                updateUndoButtonState();
                System.out.println("Obrázek načten: " + selectedFile.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Došlo k chybě při načítání obrázku: " + ex.getMessage(),
                        "Chyba",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                loadedImage = null; // V případě chyby odstraňte reference na obrázek
                updateImageDisplay(); // Aktualizujte GUI (např. deaktivujte tlačítka zoomu)
            }
        }
    }

    /**
     * Exportuje souřadnice všech přidaných vektorových bodů do textového souboru.
     * Každý bod je na samostatném řádku ve formátu "X,Y".
     * Zobrazí dialog pro výběr umístění a názvu souboru.
     */
    private void exportPoints() {
        if (vectorPoints.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nejsou žádné body k exportu.",
                    "Informace",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Uložit vektorové body");
        fileChooser.setSelectedFile(new File("vektorove_body.txt")); // Návrh názvu
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Textové soubory (*.txt)", "txt");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Zajistí, že soubor bude mít příponu .txt
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }

            try (FileWriter writer = new FileWriter(fileToSave)) {
                for (Point p : vectorPoints) {
                    writer.write(p.x + "," + p.y + System.lineSeparator());
                }
                JOptionPane.showMessageDialog(this,
                        "Body byly úspěšně exportovány do: " + fileToSave.getAbsolutePath(),
                        "Export úspěšný",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Došlo k chybě při exportu bodů: " + ex.getMessage(),
                        "Chyba exportu",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Zvýší faktor přiblížení (zoom in) obrázku, pokud není dosaženo maxima.
     * Následně aktualizuje zobrazení.
     */
    private void zoomIn() {
        if (loadedImage != null && zoomFactor < MAX_ZOOM) {
            zoomFactor += ZOOM_INCREMENT;
            zoomFactor = Math.min(zoomFactor, MAX_ZOOM); // Ošetření proti překročení
            updateImageDisplay();
        }
    }

    /**
     * Sníží faktor přiblížení (zoom out) obrázku, pokud není dosaženo minima.
     * Následně aktualizuje zobrazení.
     */
    private void zoomOut() {
        if (loadedImage != null && zoomFactor > MIN_ZOOM) {
            zoomFactor -= ZOOM_INCREMENT;
            zoomFactor = Math.max(zoomFactor, MIN_ZOOM); // Ošetření proti přílišnému oddálení
            updateImageDisplay();
        }
    }

    /**
     * Odvolá (odstraní) poslední přidaný vektorový bod ze seznamu.
     * Aktualizuje stav tlačítka "Undo" a překreslí panel s obrázkem.
     */
    private void undoLastPoint() {
        if (!vectorPoints.isEmpty()) {
            vectorPoints.remove(vectorPoints.size() - 1);
            imagePanel.repaint(); // Jen překreslit, velikost se nemění
            updateUndoButtonState();
            System.out.println("Poslední bod odvolán.");
        }
    }

    /**
     * Aktualizuje stav (aktivní/neaktivní) tlačítka "Undo" na základě toho,
     * zda jsou v seznamu {@link #vectorPoints} nějaké body.
     */
    private void updateUndoButtonState() {
        undoButton.setEnabled(!vectorPoints.isEmpty());
    }
}