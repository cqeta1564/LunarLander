package graphics;

import model.Lander;
import model.Terrain;
import sound.SoundManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import javax.swing.*;

//Vykresluje herní objekty
public class Renderer extends JPanel {
    private final Lander lander;
    private final Terrain terrain;
    private final SoundManager soundManager; // Přidáme referenci

    public Renderer(Lander lander, Terrain terrain, SoundManager soundManager) {
        this.lander = lander;
        this.terrain = terrain;
        this.soundManager = soundManager; // Inicializace
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Antialiasing pro hladké čáry
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawTerrain(g2d);
        drawLander(g2d);
        drawHUD(g2d);
        drawGameStatus(g2d);
    }

    private void drawTerrain(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        List<Point> groundPoints = terrain.getGroundPoints();
        for (int i = 0; i < groundPoints.size() - 1; i++) {
            Point p1 = groundPoints.get(i);
            Point p2 = groundPoints.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Přistávací plocha (zelený čtverec)
        g2d.setColor(Color.GREEN);
        g2d.fillRect(600, 500, 100, 5);
    }

    private void drawExplosion(Graphics2D g2d) {
        g2d.setColor(Color.ORANGE);
        int radius = 30;
        int x = (int) lander.getX() - radius / 2;
        int y = (int) lander.getY() - radius / 2;
        g2d.fillOval(x, y, radius, radius);
    }

    private void drawLander(Graphics2D g2d) {
        int x = (int) lander.getX();
        int y = (int) lander.getY();
        double angle = lander.getAngle();

        // Pokud došlo k nárazu, zobrazíme explozi
        if (lander.isCrashed()) {
            drawExplosion(g2d);
            return;
        }

        // Tvar landeru (trojúhelník)
        Polygon landerShape = new Polygon();
        landerShape.addPoint(x, y - 10);
        landerShape.addPoint(x - 8, y + 10);
        landerShape.addPoint(x + 8, y + 10);

        // Rotace landeru
        AffineTransform oldTransform = g2d.getTransform();
        g2d.rotate(Math.toRadians(angle), x, y);
        g2d.setColor(Color.CYAN);
        g2d.fill(landerShape);

        // Výfuk (pokud je motor zapnutý)
        if (lander.isThrusting()) {
            g2d.setColor(Color.ORANGE);
            g2d.fillRect(x - 3, y + 10, 6, 15);
        }
        g2d.setTransform(oldTransform); // Obnovení původní transformace
    }

    private void drawHUD(Graphics2D g2d) {
        // Přidáme indikátor zvuku
        if (soundManager != null && soundManager.isEnginePlaying()) {
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(10, 100, 10, 10);
        }
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.drawString("Fuel: " + (int) lander.getFuel(), 20, 20);
        g2d.drawString("VX: " + (int) lander.getVelocityX(), 20, 40);
        g2d.drawString("VY: " + (int) lander.getVelocityY(), 20, 60);
    }

    private void drawGameStatus(Graphics2D g2d) {
        if (lander.isLanded()) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("PŘISTÁNO!", 350, 300);
            g2d.drawString("Skóre: " + lander.getScore(), 350, 330);
        } else if (lander.isCrashed()) {
            g2d.setColor(Color.RED);
            g2d.drawString("EXPLOZE!", 350, 300);
        }
    }
}
