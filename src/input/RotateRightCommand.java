package input;

import model.Lander;

public class RotateRightCommand implements Command {
    private Lander lander;

    public RotateRightCommand(Lander lander) {
        this.lander = lander;
    }

    @Override
    public void execute() {
        lander.setAngle(lander.getAngle() + 5); // Otočení doprava
    }
}
