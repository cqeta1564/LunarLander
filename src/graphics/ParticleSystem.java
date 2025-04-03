package graphics;

import core.GameEvent;
import core.GameEventListener;

//(Observer) – Efekty výfuku, exploze
public class ParticleSystem implements GameEventListener {
    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case LANDER_CRASHED:
                System.out.println("Výbuch! Částice exploze...");
                break;
            case LANDER_LANDED:
                System.out.println("Kouř z prachu...");
                break;
        }
    }
}
