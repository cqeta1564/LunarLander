package states;

import core.Game; // Pro přístup k DEFAULT_WIDTH/HEIGHT a dalším konstantám
import entities.Terrain;
import input.GameAction;
import input.InputHandler;
import input.KeyBindings; // Pro debug zobrazení aktuálních kláves

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
// import entities.Lander; // Až budete mít třídu Lander

public class PlayingState implements GameState {

    private final StateManager stateManager;
    private final InputHandler inputHandler;
    private Terrain terrain;
    // private Lander lander; // Instance lodi hráče

    // Pozice kamery nebo levého okraje viditelné části světa
    // Prozatím statická, ale pro "nekonečný" terén by se měnila
    private float cameraX = 0;

    public PlayingState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
    }

    @Override
    public void init(StateManager manager) {
        // Tato metoda se volá jednou při vytváření stavu StateManagerem.
        // Můžeme zde inicializovat zdroje, které se nemění při každém vstupu do stavu.
        // Terén se nyní vytváří v onEnter pro případ, že by se měl generovat vždy nový.
        // Pokud je terén statický (jako naše pevná mapa), mohl by se vytvářet i zde.
        // lander = new Lander(Game.DEFAULT_WIDTH / 2.0f, Game.DEFAULT_HEIGHT / 4.0f); // Příklad pozice landeru
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do PlayingState.");
        // Vytvoření (nebo znovuvytvoření/načtení) terénu
        if (this.terrain == null) { // Vytvoříme terén jen pokud ještě neexistuje
            this.terrain = new Terrain(Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);
        }
        // Aktualizujeme viditelnou část terénu na základě pozice kamery
        // Pro statickou kameru na začátku světa:
        this.cameraX = 0; // Nebo pozice landeru, pokud se kamera centruje na něj
        this.terrain.populateVisibleTerrain(cameraX, Game.DEFAULT_WIDTH);

        // Reset landeru (pozice, palivo, atd.)
        // if (lander != null) {
        //     lander.reset(Game.DEFAULT_WIDTH / 2.0f, Game.DEFAULT_HEIGHT / 4.0f);
        // }
    }

    @Override
    public void update(double deltaTime) {
        handleInput();

        // Aktualizace logiky lodi
        // if (lander != null) {
        //     lander.update(deltaTime, terrain); // Lander může potřebovat info o terénu pro kolize
        // }

        // Pokud by se kamera pohybovala s landerem:
        // if (lander != null) {
        //     cameraX = lander.getX() - Game.DEFAULT_WIDTH / 2.0f; // Příklad jednoduchého sledování
        //     // Omezit cameraX, aby se nezobrazovalo "mimo svět" pokud není nekonečný
        //     terrain.populateVisibleTerrain(cameraX, Game.DEFAULT_WIDTH);
        // }

        // Zde další herní logika:
        // - Kontrola přistání na plošce
        // - Kontrola havárie
        // - Správa paliva
        // - Výpočet skóre
        // - Přechod do stavu GAME_OVER nebo další úrovně
    }

    @Override
    public void handleInput() {
        // Návrat do menu
        if (inputHandler.isEscJustPressed()) {
            stateManager.setState(StateManager.StateType.MENU);
            return; // Ukončíme handleInput, pokud přecházíme do jiného stavu
        }

        // Ovládání lodi pomocí herních akcí
        // if (lander != null) {
        //     lander.setThrusterActive(inputHandler.isActionActive(GameAction.THRUST));
        //
        //     if (inputHandler.isActionActive(GameAction.ROTATE_LEFT)) {
        //         lander.rotateLeft(Lander.ROTATION_ANGLE_PER_SECOND); // Předpokládá konstantu v Lander
        //     } else if (inputHandler.isActionActive(GameAction.ROTATE_RIGHT)) {
        //         lander.rotateRight(Lander.ROTATION_ANGLE_PER_SECOND);
        //     } else {
        //         lander.stopRotation();
        //     }
        // }

        // Ladící výpisy pro aktivní akce (lze odstranit v produkční verzi)
        if (inputHandler.isActionActive(GameAction.THRUST)) {
            // System.out.println("HRA: Akce TAH MOTORU");
        }
        if (inputHandler.isActionActive(GameAction.ROTATE_LEFT)) {
            // System.out.println("HRA: Akce ROTACE VLEVO");
        }
        if (inputHandler.isActionActive(GameAction.ROTATE_RIGHT)) {
            // System.out.println("HRA: Akce ROTACE VPRAVO");
        }
    }

    @Override
    public void render(Graphics2D g) {
        // Vykreslení pozadí (např. hvězdy, pokud terén nekryje celou výšku)
        g.setColor(new Color(10, 10, 20)); // Velmi tmavě modrá pro vesmír
        g.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        // Vykreslení terénu
        // Metoda terrain.render() by měla kreslit relativně k aktuálnímu pohledu (cameraX)
        // nebo Graphics2D context by mohl být transformován (g.translate(-cameraX, 0))
        // Prozatím náš Terrain.render() kreslí body tak, jak jsou v terrainSurfacePoints,
        // a populateVisibleTerrain() se stará o to, aby tyto body byly pro aktuální pohled.
        if (terrain != null) {
            terrain.render(g);
        }

        // Vykreslení lodi
        // if (lander != null) {
        //      // Podobně jako u terénu, lander by se kreslil s ohledem na cameraX
        //      // Graphics2D gLander = (Graphics2D) g.create();
        //      // gLander.translate(-cameraX, 0);
        //      // lander.render(gLander);
        //      // gLander.dispose();
        //      // Nebo lander.render(g, cameraX);
        //      lander.render(g); // Pokud lander počítá se svou pozicí na obrazovce
        // }


        // Zobrazení herních informací (HUD) - skóre, palivo, rychlost atd.
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString("PALIVO: XXX", 10, 20);
        g.drawString("VÝŠKA: YYY", 10, 40);
        g.drawString("RYCHLOST X: Vx", 10, 60);
        g.drawString("RYCHLOST Y: Vy", 10, 80);
        g.drawString("SKÓRE: ZZZ", Game.DEFAULT_WIDTH - 150, 20);


        // Debug info - aktuální klávesy pro akce (můžete ponechat nebo odstranit)
        KeyBindings kb = inputHandler.getKeyBindings();
        if (kb != null) {
            g.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g.setColor(new Color(200, 200, 200, 180)); // Poloprůhledná světle šedá
            int yPos = Game.DEFAULT_HEIGHT - 45;
            g.drawString("Ovládání:", 10, yPos); yPos += 12;
            g.drawString(" Tah: " + kb.getKeyTextForAction(GameAction.THRUST), 10, yPos); yPos += 12;
            g.drawString(" Vlevo: " + kb.getKeyTextForAction(GameAction.ROTATE_LEFT), 10, yPos); yPos += 12;
            g.drawString(" Vpravo: " + kb.getKeyTextForAction(GameAction.ROTATE_RIGHT), 10, yPos);
        }
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění PlayingState.");
        // Případné uvolnění zdrojů specifických pro tento stav,
        // nebo zastavení zvuků motoru lodi atd.
        // if (lander != null) {
        //    lander.setThrusterActive(false);
        // }
    }
}