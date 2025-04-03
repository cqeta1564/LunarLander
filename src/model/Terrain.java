package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Generuje náhodný terén
public class Terrain {
    private List<Point> groundPoints;
    private List<Rectangle> landingZones; // Přistávací plochy

    public Terrain() {
        groundPoints = new ArrayList<>();
        landingZones = new ArrayList<>();
    }

    public static Terrain generateRandom(int width, int height) {
        Terrain terrain = new Terrain();
        Random random = new Random();

        // Generování terénu
        for (int x = 0; x <= width; x += 50) {
            int y = height - 50 - random.nextInt(100);
            terrain.groundPoints.add(new Point(x, y));
        }

        // Přidání přistávací plochy (vždy na fixní pozici pro jednoduchost)
        Rectangle landingZone = new Rectangle(width - 200, height - 50, 100, 5);
        terrain.landingZones.add(landingZone);

        return terrain;
    }

    public List<Point> getGroundPoints() {
        return groundPoints;
    }

    public List<Rectangle> getLandingZones() {
        return landingZones;
    }
}
