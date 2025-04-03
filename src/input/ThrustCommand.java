package input;

import model.Lander;

public class ThrustCommand implements Command {
    private Lander lander;

    public ThrustCommand(Lander lander) {
        this.lander = lander;
    }

    @Override
    public void execute() {
        lander.setThrusting(true);
    }
}
