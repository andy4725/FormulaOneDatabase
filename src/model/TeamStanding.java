package model;

public final class TeamStanding {
    private final int position;
    private final int teamId;
    private final String teamName;
    private final String logoPath;
    private final double points;

    public TeamStanding(
            int position,
            int teamId,
            String teamName,
            String logoPath,
            double points) {
        this.position = position;
        this.teamId = teamId;
        this.teamName = teamName;
        this.logoPath = logoPath;
        this.points = points;
    }

    public int getPosition() {
        return position;
    }

    public int getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public double getPoints() {
        return points;
    }
}