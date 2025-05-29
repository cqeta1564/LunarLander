// InfoDisplay.java - Manages multiple InfoBox objects
// This would typically be a JPanel in Swing that holds JLabels (InfoBoxes)
import javax.swing.JPanel;
import java.awt.LayoutManager; // Or use null layout and setBounds
import java.awt.Dimension;

public class InfoDisplay extends JPanel {
    public InfoBox scoreLabel, timeLabel, fuelLabel;
    public InfoBox scoreValue, timeValue, fuelValue;
    public InfoBox altLabel, horizSpeedLabel, vertSpeedLabel;
    public InfoBox altValue, horizSpeedValue, vertSpeedValue;
    public InfoBox messages; // For game messages

    private int screenWidth, screenHeight;

    public InfoDisplay(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        setLayout(null); // Using absolute positioning
        // setOpaque(false); // Make panel transparent if drawn over game

        // Initialize InfoBox instances
        scoreLabel = new InfoBox("Score", TextAlign.LEFT, 80);
        timeLabel = new InfoBox("Time", TextAlign.LEFT, 80);
        fuelLabel = new InfoBox("Fuel", TextAlign.LEFT, 80);
        scoreValue = new InfoBox("0000", TextAlign.LEFT, 60); // Example width
        timeValue = new InfoBox("0:00", TextAlign.LEFT, 60);
        fuelValue = new InfoBox("0000", TextAlign.LEFT, 60);

        altLabel = new InfoBox("Altitude", TextAlign.LEFT, 150);
        horizSpeedLabel = new InfoBox("Horizontal Speed", TextAlign.LEFT, 150);
        vertSpeedLabel = new InfoBox("Vertical Speed", TextAlign.LEFT, 150);
        altValue = new InfoBox("000", TextAlign.RIGHT, 50);
        horizSpeedValue = new InfoBox("000", TextAlign.RIGHT, 50);
        vertSpeedValue = new InfoBox("000", TextAlign.RIGHT, 50);

        messages = new InfoBox("START MESSAGE", TextAlign.CENTRE, 300);
        // messages.setFont(messages.getFont().deriveFont(16f)); // Larger font for messages

        // Add to panel
        add(scoreLabel); add(timeLabel); add(fuelLabel);
        add(scoreValue); add(timeValue); add(fuelValue);
        add(altLabel); add(horizSpeedLabel); add(vertSpeedLabel);
        add(altValue); add(horizSpeedValue); add(vertSpeedValue);
        add(messages);

        arrangeBoxes(width, height);
        setPreferredSize(new Dimension(width, height)); // So panel takes up space if needed
    }

    public void arrangeBoxes(int w, int h) {
        this.screenWidth = w;
        this.screenHeight = h;

        int leftMargin = Math.min(50, w / 12);
        int topMargin = Math.min(50, h / 12);
        int rightMargin = w - Math.min(50, w / 10);
        int vSpace = 25; // Vertical spacing between items
        int column2Left = leftMargin + ((w < 650) ? 70 : 100); // Adjusted for typical label widths
        int column3Left = rightMargin - ((w < 650) ? 200 : 320); // Adjusted

        int yPos = topMargin;

        scoreLabel.setPos(leftMargin, yPos);
        scoreValue.setPos(column2Left, yPos);
        altLabel.setPos(column3Left, yPos);
        altValue.setPos(rightMargin, yPos); // Align right means x is the right edge

        yPos += vSpace;
        timeLabel.setPos(leftMargin, yPos);
        timeValue.setPos(column2Left, yPos);
        horizSpeedLabel.setPos(column3Left, yPos);
        horizSpeedValue.setPos(rightMargin, yPos);

        yPos += vSpace;
        fuelLabel.setPos(leftMargin, yPos);
        fuelValue.setPos(column2Left, yPos);
        vertSpeedLabel.setPos(column3Left, yPos);
        vertSpeedValue.setPos(rightMargin, yPos);

        messages.setPos(w / 2, ((h - topMargin) / 4) + topMargin);
        // messages.setSize(300, 50); // Example size for centered message box

        // Font size adjustment would be more complex, potentially iterating components
        // or using a base font size scaled by screen dimensions.
        // For simplicity, font sizes can be set individually or through a theme.
    }

    public void showGameInfo(String msg) {
        messages.updateText(msg);
        messages.showBox();
    }

    public void hideGameInfo() {
        messages.updateText("");
        messages.hideBox();
    }

    public void updateBoxInt(InfoBox box, int value, int padding) {
        String textValue = String.valueOf(value);
        if (padding > 0) {
            textValue = String.format("%0" + padding + "d", value);
        }
        box.updateText(textValue);
    }

    public void updateBoxIntByName(String boxName, int value, int padding) {
        InfoBox box = null;
        switch(boxName) {
            case "score": box = scoreValue; break;
            case "fuel": box = fuelValue; break;
            case "alt": box = altValue; break;
            case "horizSpeed": box = horizSpeedValue; break;
            case "vertSpeed": box = vertSpeedValue; break;
        }
        if (box != null) {
            updateBoxInt(box, value, padding);
        }
    }


    public void updateBoxTime(InfoBox box, long totalMillis) {
        long secs = totalMillis / 1000;
        long mins = secs / 60;
        secs %= 60;
        String timeStr = String.format("%d:%02d", mins, secs);
        box.updateText(timeStr);
    }
    public void updateBoxTimeByName(String boxName, long totalMillis) {
        if ("time".equals(boxName) && timeValue != null) {
            updateBoxTime(timeValue, totalMillis);
        }
    }
}