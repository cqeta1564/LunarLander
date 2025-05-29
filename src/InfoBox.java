// InfoBox.java - Can be a JLabel or a custom painted component
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.SwingConstants;

// Alignment constants
enum TextAlign {
    LEFT, CENTRE, RIGHT
}

public class InfoBox extends JLabel { // Using JLabel for simplicity
    private boolean hidden;
    private int originalWidth;
    private TextAlign alignment;


    public InfoBox(String text, TextAlign align, int width) {
        super(text);
        this.originalWidth = width > 0 ? width : 200; // Default width
        this.alignment = align != null ? align : TextAlign.LEFT; // Default align

        setPreferredSize(new Dimension(this.originalWidth, 20)); // Height can be adjusted

        switch(this.alignment) {
            case LEFT:
                setHorizontalAlignment(SwingConstants.LEFT);
                break;
            case CENTRE:
                setHorizontalAlignment(SwingConstants.CENTER);
                break;
            case RIGHT:
                setHorizontalAlignment(SwingConstants.RIGHT);
                break;
        }
        this.hidden = false;
        // setForeground(Color.WHITE); // Set text color
        // setOpaque(false); // If it's on a custom background
    }

    public InfoBox(String text) {
        this(text, TextAlign.LEFT, 200);
    }


    // setText is inherited from JLabel
    public boolean updateText(String newText) { // Renamed to avoid conflict if not extending JLabel
        if (!newText.equals(getText())) {
            setText(newText);
            return true;
        }
        return false;
    }

    public void setPos(int x, int y) {
        // Adjust x based on alignment for fixed width component
        int actualX = x;
        if (alignment == TextAlign.RIGHT) {
            actualX = x - originalWidth;
        } else if (alignment == TextAlign.CENTRE) {
            actualX = x - originalWidth / 2;
        }
        setLocation(actualX, y);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.hidden = !visible;
    }

    public void showBox() { // To match JS API
        setVisible(true);
    }

    public void hideBox() { // To match JS API
        setVisible(false);
    }
}

