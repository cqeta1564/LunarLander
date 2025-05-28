package entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Terrain {
    private static final float X_AXIS_SHRINK_FACTOR = 1f;
    private static final float Y_AXIS_SHRINK_FACTOR = 2.0f;
    private static final float TERRAIN_VIEWPORT_TOP_Y_RATIO = 0.40f;
    private static final float TERRAIN_VIEWPORT_BOTTOM_Y_RATIO = 0.95f;
    private static final String RAW_TERRAIN_DATA = "0,1429 45,1429 60,1367 60,1340 76,1340 83,1315 98,1299 105,1287 108,1276 118,1261 134,1261 148,1204 163,1198 178,1211 207,1211 222,1198 228,1169 237,1140 253,1134 254,1122 266,1108 280,1096 298,1096 301,1108 316,1109 318,1122 327,1122 330,1160 341,1197 342,1213 348,1236 372,1249 379,1279 385,1289 399,1300 416,1300 429,1289 437,1277 444,1266 455,1254 465,1244 475,1236 481,1224 496,1210 501,1198 517,1192 532,1174 548,1169 560,1159 577,1159 592,1163 606,1173 609,1184 620,1198 625,1211 635,1224 637,1236 649,1250 666,1249 676,1225 686,1218 698,1224 709,1236 716,1262 737,1275 742,1316 752,1351 752,1366 766,1377 796,1378 812,1366 826,1353 833,1339 848,1327 854,1315 869,1308 884,1251 899,1244 909,1234 917,1225 924,1216 930,1207 936,1196 943,1185 951,1159 965,1147 972,1134 976,1123 986,1109 1003,1108 1017,1096 1025,1083 1031,1073 1045,1058 1060,997 1061,962 1064,941 1073,916 1074,910 1089,903 1095,889 1103,877 1119,877 1122,890 1138,890 1142,904 1149,904 1159,909 1165,916 1170,922 1178,947 1201,962 1221,1017 1237,1022 1252,1032 1254,1044 1266,1058 1269,1069 1279,1082 1296,1083 1298,1123 1309,1158 1310,1173 1324,1173 1328,1186 1339,1196 1342,1209 1344,1216 1346,1223 1349,1228 1353,1235 1369,1249 1372,1289 1382,1325 1382,1340 1386,1351 1397,1365 1413,1365 1419,1390 1425,1399 1456,1428 1573,1428 1582,1390 1596,1366 1603,1339 1611,1339 1622,1314 1634,1304 1647,1288 1663,1283 1675,1274 1691,1274 1714,1333 1716,1345 1734,1362 1738,1364 1748,1376 1756,1403 1778,1417 1840,1419 1867,1428 1879,1429";
    private final List<Point> basePatternScreenPoints;
    private final List<LandingPad.PatternPadDefinition> basePatternPadDefinitions;
    private final List<Point> terrainSurfacePoints;
    private final List<LandingPad> landingPads;
    private final Polygon terrainPolygon;
    private final int screenWidth;
    private final int screenHeight;
    private float finalPatternCycleWidth;
    private float finalPatternMinScreenY_DEBUG;
    private float finalPatternMaxScreenY_DEBUG;

    public Terrain(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.basePatternScreenPoints = new ArrayList<>();
        this.basePatternPadDefinitions = new ArrayList<>();
        this.terrainSurfacePoints = new ArrayList<>();
        this.landingPads = new ArrayList<>();
        this.terrainPolygon = new Polygon();

        System.out.println("--- Terrain Constructor (Relief-Inverting Y, X-Shrink: " + X_AXIS_SHRINK_FACTOR + ", Y-Shrink: " + Y_AXIS_SHRINK_FACTOR + ") ---");
        System.out.println("ScreenW: " + screenWidth + ", ScreenH: " + screenHeight);

        parseAndTransformBasePattern();
        populateVisibleTerrain(0, this.screenWidth);

        System.out.println("Final pattern cycle width on screen: " + String.format("%.2f", finalPatternCycleWidth));
        System.out.println("Final pattern Y range on screen (DEBUG): " + String.format("%.2f", finalPatternMinScreenY_DEBUG) + " to " + String.format("%.2f", finalPatternMaxScreenY_DEBUG));
        System.out.println("--- Terrain Constructor END ---");
    }

    private void parseAndTransformBasePattern() {
        System.out.println("--- parseAndTransformBasePattern START (Relief-Inverting Y) ---");
        List<PointData> tempParsedData = new ArrayList<>();
        String[] pairs = RAW_TERRAIN_DATA.split(" ");
        float minRawX = Float.MAX_VALUE;
        float maxRawX = Float.MIN_VALUE;
        float minRawY = Float.MAX_VALUE;
        float maxRawY = Float.MIN_VALUE;

        for (String pair : pairs) {
            String[] coords = pair.split(",");
            if (coords.length == 2) {
                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    tempParsedData.add(new PointData(x, y));
                    if (x < minRawX) minRawX = x;
                    if (x > maxRawX) maxRawX = x;
                    if (y < minRawY) minRawY = y;
                    if (y > maxRawY) maxRawY = y;
                } catch (NumberFormatException e) {
                    System.err.println("Chyba při parsování dat terénu: " + pair);
                }
            }
        }

        if (tempParsedData.isEmpty()) {
            System.err.println("Chyba: Nebyla načtena žádná data pro terén!");
            this.finalPatternCycleWidth = 0;
            return;
        }
        for (PointData pd : tempParsedData) {
            pd.x -= (int) minRawX;
        }
        maxRawX -= minRawX;
        minRawX = 0;

        float originalPatternWidth = maxRawX;
        this.finalPatternCycleWidth = originalPatternWidth / X_AXIS_SHRINK_FACTOR;
        System.out.println("Původní šířka vzoru (normalizovaná): " + originalPatternWidth + ", Škálovaná šířka (na obrazovce): " + this.finalPatternCycleWidth);
        System.out.println("Původní Y rozsah dat (minRawY až maxRawY): " + minRawY + " až " + maxRawY);

        float targetScreenTopY = screenHeight * TERRAIN_VIEWPORT_TOP_Y_RATIO;
        float targetScreenBottomY = screenHeight * TERRAIN_VIEWPORT_BOTTOM_Y_RATIO;
        float targetScreenDisplayHeight = targetScreenBottomY - targetScreenTopY;
        if (targetScreenDisplayHeight <= 0) targetScreenDisplayHeight = 1;
        float rawYRange = maxRawY - minRawY;
        if (rawYRange <= 0) rawYRange = 1;

        System.out.println("Cílové Y pásmo na obrazovce: " + targetScreenTopY + " až " + targetScreenBottomY + " (výška: " + targetScreenDisplayHeight + ")");

        basePatternScreenPoints.clear();
        this.finalPatternMinScreenY_DEBUG = Float.MAX_VALUE;
        this.finalPatternMaxScreenY_DEBUG = Float.MIN_VALUE;

        for (PointData rawPD : tempParsedData) {
            int screenX = (int) (rawPD.x / X_AXIS_SHRINK_FACTOR);

            float normalizedRawY = (rawPD.y - minRawY) / rawYRange;
            int screenY = (int) (targetScreenTopY + (normalizedRawY * targetScreenDisplayHeight));

            basePatternScreenPoints.add(new Point(screenX, screenY));

            if (screenY < this.finalPatternMinScreenY_DEBUG) this.finalPatternMinScreenY_DEBUG = screenY;
            if (screenY > this.finalPatternMaxScreenY_DEBUG) this.finalPatternMaxScreenY_DEBUG = screenY;
        }

        basePatternPadDefinitions.clear();
        float pA_normY = (1096f - minRawY) / rawYRange;
        float pA_screenY = targetScreenTopY + (pA_normY * targetScreenDisplayHeight);
        basePatternPadDefinitions.add(new LandingPad.PatternPadDefinition((280 / X_AXIS_SHRINK_FACTOR), (298 / X_AXIS_SHRINK_FACTOR), 1096, 10, pA_screenY));

        float pB_normY = (877f - minRawY) / rawYRange;
        float pB_screenY = targetScreenTopY + (pB_normY * targetScreenDisplayHeight);
        basePatternPadDefinitions.add(new LandingPad.PatternPadDefinition((1103 / X_AXIS_SHRINK_FACTOR), (1119 / X_AXIS_SHRINK_FACTOR), 877, 10, pB_screenY));

        float pC_normY = (1428f - minRawY) / rawYRange;
        float pC_screenY = targetScreenTopY + (pC_normY * targetScreenDisplayHeight);
        basePatternPadDefinitions.add(new LandingPad.PatternPadDefinition((1456 / X_AXIS_SHRINK_FACTOR), (1573 / X_AXIS_SHRINK_FACTOR), 1428, 2, pC_screenY));

        System.out.println("Ploška A (screen Y - invertovaná): " + pA_screenY);
        System.out.println("Ploška B (screen Y - invertovaná, měla by být nahoře v pásmu): " + pB_screenY);
        System.out.println("Ploška C (screen Y - invertovaná, měla by být dole v pásmu): " + pC_screenY);
        System.out.println("--- parseAndTransformBasePattern END ---");
    }

    public void populateVisibleTerrain(float worldXOffset, int viewWidth) {
        terrainSurfacePoints.clear();
        landingPads.clear();
        terrainPolygon.reset();

        if (basePatternScreenPoints.isEmpty() || finalPatternCycleWidth <= 0) {
            System.err.println("populateVisibleTerrain: Základní vzor prázdný nebo má nulovou šířku.");
            return;
        }

        int startRepetitionIndex = (int) Math.floor(worldXOffset / finalPatternCycleWidth);
        int endRepetitionIndex = (int) Math.ceil((worldXOffset + viewWidth) / finalPatternCycleWidth);
        startRepetitionIndex--;
        endRepetitionIndex++;

        Point lastOverallScreenPoint = null;

        for (int i = startRepetitionIndex; i <= endRepetitionIndex; i++) {
            float currentPatternCycleWorldXStart = i * finalPatternCycleWidth;
            for (Point patternPoint : basePatternScreenPoints) {
                int pointWorldX = (int) (currentPatternCycleWorldXStart + patternPoint.x);
                int pointScreenX = pointWorldX - (int) worldXOffset;
                Point currentScreenPoint = new Point(pointScreenX, patternPoint.y);
                if (pointScreenX >= -finalPatternCycleWidth && pointScreenX <= viewWidth + finalPatternCycleWidth) {
                    if (!currentScreenPoint.equals(lastOverallScreenPoint) || (terrainSurfacePoints.isEmpty() || !currentScreenPoint.equals(terrainSurfacePoints.get(terrainSurfacePoints.size() - 1)))) {
                        terrainSurfacePoints.add(currentScreenPoint);
                        lastOverallScreenPoint = currentScreenPoint;
                    }
                }
            }
            for (LandingPad.PatternPadDefinition def : basePatternPadDefinitions) {
                float padWorldStartX = currentPatternCycleWorldXStart + def.patternStartX;
                float padWorldEndX = currentPatternCycleWorldXStart + def.patternEndX;
                float padScreenStartX = padWorldStartX - worldXOffset;
                float padScreenEndX = padWorldEndX - worldXOffset;
                if (padScreenEndX >= 0 && padScreenStartX <= viewWidth) {
                    landingPads.add(new LandingPad(padScreenStartX, padScreenEndX, def.transformedScreenY, def.multiplier));
                }
            }
        }
        removeConsecutiveDuplicatePoints();
        buildTerrainPolygon();
    }

    private void removeConsecutiveDuplicatePoints() {
        if (terrainSurfacePoints.size() < 2) return;
        List<Point> uniquePoints = new ArrayList<>();
        uniquePoints.add(terrainSurfacePoints.get(0));
        for (int i = 1; i < terrainSurfacePoints.size(); i++) {
            if (!terrainSurfacePoints.get(i).equals(terrainSurfacePoints.get(i - 1))) {
                uniquePoints.add(terrainSurfacePoints.get(i));
            }
        }
        terrainSurfacePoints.clear();
        terrainSurfacePoints.addAll(uniquePoints);
    }

    private void buildTerrainPolygon() {
        terrainPolygon.reset();
        if (terrainSurfacePoints.isEmpty()) {
            terrainPolygon.addPoint(0, screenHeight);
            terrainPolygon.addPoint(screenWidth, screenHeight);
            terrainPolygon.addPoint(screenWidth, screenHeight - 1);
            terrainPolygon.addPoint(0, screenHeight - 1);
            return;
        }
        terrainPolygon.addPoint(terrainSurfacePoints.get(0).x, screenHeight);
        for (Point p : terrainSurfacePoints) {
            terrainPolygon.addPoint(p.x, p.y);
        }
        terrainPolygon.addPoint(terrainSurfacePoints.get(terrainSurfacePoints.size() - 1).x, screenHeight);
    }

    public void render(Graphics2D g) {
        g.setColor(new Color(40, 40, 50));
        g.fillPolygon(terrainPolygon);

        g.setColor(new Color(170, 170, 180));
        g.setStroke(new BasicStroke(2f));
        if (terrainSurfacePoints.size() > 1) {
            Point previousPoint = terrainSurfacePoints.get(0);
            for (int i = 1; i < terrainSurfacePoints.size(); i++) {
                Point currentPoint = terrainSurfacePoints.get(i);
                g.drawLine(previousPoint.x, previousPoint.y, currentPoint.x, currentPoint.y);
                previousPoint = currentPoint;
            }
        }

        for (LandingPad pad : landingPads) {
            g.setColor(pad.getColor());
            g.setStroke(new BasicStroke(3f));
            g.drawLine((int) pad.getStartX(), (int) pad.getY(), (int) pad.getEndX(), (int) pad.getY());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 13));
            String multiplierText = pad.getMultiplier() + "x";
            int textWidth = g.getFontMetrics().stringWidth(multiplierText);
            g.drawString(multiplierText, (int) (pad.getStartX() + (pad.getLength() - textWidth) / 2), (int) pad.getY() - 8);
        }
        g.setStroke(new BasicStroke(1f));

    }

    public List<Point> getTerrainSurfacePoints() {
        return Collections.unmodifiableList(terrainSurfacePoints);
    }

    public List<LandingPad> getLandingPads() {
        return Collections.unmodifiableList(landingPads);
    }

    public Polygon getTerrainPolygon() {
        return terrainPolygon;
    }

    public int getScreenWidth() {
        return this.screenWidth;
    }

    public int getScreenHeight() {
        return this.screenHeight;
    }

    private static class PointData {
        public final int y;
        public int x;

        public PointData(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}