public class Driver {
    private int driverId;
    private int teamId;
    private String name;
    private int carNumber;
    private String portraitPath;

    private int experience;
    private int racecraft;
    private int pace;
    private int awareness;

    public Driver(int driverId, int teamId, String name, int carNumber, String portraitPath,
                  int experience, int racecraft, int pace, int awareness) {
                this.driverId = driverId;
                this.teamId = teamId;
                this.name = name;
                this.carNumber = carNumber;
                this.portraitPath = portraitPath;
                this.experience = experience;
                this.racecraft = racecraft;
                this.pace = pace;
                this.awareness = awareness;
            }

    public int getDriverId() {
        return driverId;
    }

    public int getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public int getCarNumber() {
        return carNumber;
    }

    public String getPortraitPath() {
        return portraitPath;
    }

    public int getExperience() {
        return experience;
    }

    public int getRacecraft() {
        return racecraft;
    }

    public int getPace() {
        return pace;
    }

    public int getAwareness() {
        return awareness;
    }
}
