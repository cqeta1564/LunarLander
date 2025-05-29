// LandingZone.java - Represents data for configuring multipliers on landscape lines
public class LandingZone {
    public int lineNum; // Index of the line in the landscape's line list
    public int multiplier;

    public LandingZone(int lineNum, int multiplier) {
        this.lineNum = lineNum;
        this.multiplier = multiplier;
    }
}