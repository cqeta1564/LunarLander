package input;

import model.Lander;

public class RotateLeftCommand implements Command {
    private Lander lander;

    public RotateLeftCommand(Lander lander) {
        this.lander = lander;
    }

    @Override
    public void execute() {
        lander.setAngle(lander.getAngle() - 5); // Otočení doleva
    }
}
