package model;

/** A display-ready row in the drivers' championship table.
 * It includes the driver's team name alongside their position and points.
 */
public final class DriverStanding {
    private final int position;
    private final int driverId;
    private final String driverName;
    private final String teamName;
    private final double points;

    public DriverStanding(
            int position,
            int driverId,
            String driverName,
            String teamName,
            double points) {
        this.position = position;
        this.driverId = driverId;
        this.driverName = driverName;
        this.teamName = teamName;
        this.points = points;
    }

    public int getPosition() {
        return position;
    }

    public int getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getTeamName() {
        return teamName;
    }

    public double getPoints() {
        return points;
    }
}
