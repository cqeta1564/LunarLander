package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {

    private final boolean[] keys = new boolean[256]; // Toto zůstává privátní

    // Veřejné příznaky pro herní akce (pohyb)
    public boolean up, down, left, right; // Pro šipky (pohyb lodi)
    public boolean w, a, s, d;           // Pro WSAD (pohyb lodi)

    // Veřejné příznaky pro UI a obecné akce
    public boolean escapePressed;
    public boolean enterPressed;
    public boolean sPressed; // Pro klávesu 'S'
    public boolean leftArrowPressed; // Specificky pro šipku vlevo (může se lišit od 'left' pro pohyb)
    public boolean rightArrowPressed; // Specificky pro šipku vpravo

    // Pro myš
    public int mouseX, mouseY;
    public boolean mouseLeftPressed, mouseRightPressed;

    // Příznaky pro "právě stisknuto" - aby se akce neopakovala každé kolo, pokud je klávesa držena
    // Toto je pokročilejší a prozatím můžeme řešit pomocí "lastUpdate" flagů ve stavech,
    // ale pro budoucí vylepšení je to dobré zvážit.
    // public boolean enterJustPressed;
    // public boolean escapeJustPressed;


    public InputHandler() {
        // Inicializace, pokud je potřeba
    }

    public void update() {
        // Aktualizace pohybových kláves
        up = keys[KeyEvent.VK_UP];
        down = keys[KeyEvent.VK_DOWN];
        left = keys[KeyEvent.VK_LEFT];
        right = keys[KeyEvent.VK_RIGHT];

        w = keys[KeyEvent.VK_W];
        // 'a' a 's' jsou už názvy proměnných, takže nemůžu mít 'a = keys[KeyEvent.VK_A];'
        // přejmenujeme proměnné pro WSAD, aby nedocházelo ke kolizi s názvy tříd/proměnných
        // Například: keyW, keyA, keyS, keyD
        // Pro jednoduchost teď použiji stávající 'a', 's', 'd', ale 'a' a 's' jsou problematické
        // v kontextu `public boolean sPressed;`
        // Opravíme to:
        // w = keys[KeyEvent.VK_W]; // Zůstává
        // a = keys[KeyEvent.VK_A]; // Zůstává (pokud 'a' není použito jinde jako specifický příznak)
        // s = keys[KeyEvent.VK_S]; // Toto koliduje s 'sPressed' pro klávesu S.
        // d = keys[KeyEvent.VK_D]; // Zůstává

        // Lepší pojmenování pro WSAD pro pohyb:
        // this.keyW = keys[KeyEvent.VK_W];
        // this.keyA = keys[KeyEvent.VK_A];
        // this.keyS = keys[KeyEvent.VK_S]; // Pokud 's' není public boolean sPressed
        // this.keyD = keys[KeyEvent.VK_D];
        // Prozatím ponechám w,a,s,d s vědomím, že 's' pro pohyb a 'sPressed' pro UI 'S' mohou být matoucí.
        // Ideálně by 's' pro pohyb bylo např. 'sKeyForMovement'.

        // Aktualizace UI kláves
        escapePressed = keys[KeyEvent.VK_ESCAPE];
        enterPressed = keys[KeyEvent.VK_ENTER];
        sPressed = keys[KeyEvent.VK_S]; // Pro klávesu 'S'
        leftArrowPressed = keys[KeyEvent.VK_LEFT]; // Pro UI navigaci v nastavení
        rightArrowPressed = keys[KeyEvent.VK_RIGHT]; // Pro UI navigaci v nastavení
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Většinou nepotřebujeme pro hry
    }

    // Metody MouseListener a MouseMotionListener zůstávají stejné ...
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) { mouseLeftPressed = true; }
        else if (e.getButton() == MouseEvent.BUTTON3) { mouseRightPressed = true; }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) { mouseLeftPressed = false; }
        else if (e.getButton() == MouseEvent.BUTTON3) { mouseRightPressed = false; }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
    @Override
    public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }

    /**
     * Pomocná metoda pro "spotřebování" stisku klávesy.
     * Nastaví příznak dané klávesy na false, aby nebyla detekována jako stisknutá
     * v následujícím cyklu, dokud není fyzicky znovu stisknuta.
     * POZOR: Toto by mělo být voláno opatrně, typicky hned po zpracování akce.
     * @param keyCode Kód klávesy z KeyEvent.VK_XXX
     */
    public void consumeKey(int keyCode) {
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = false;
            // Případně aktualizovat i odvozené public booleany, pokud je to nutné ihned
            // Např. if (keyCode == KeyEvent.VK_ENTER) enterPressed = false;
            // Ale update() metoda by to měla srovnat v dalším cyklu.
            // Pro jednoduchost a konzistenci je lepší, aby stavy měnila pouze update() metoda
            // na základě `keys` pole. Takže `consumeKey` přímo mění `keys` pole.
        }
    }
}