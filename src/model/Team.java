package model;
/** A team profile with its presentation data and simulation ratings.
 * Pace includes the sponsor modifier; reliability is returned unchanged.
 */
public class Team {
    private int teamId;
    private String name;
    private String logoPath;

    private int pace;
    private int reliability;
    private float sponsorModifier;

    public Team(int teamId, String name, String logoPath, int pace, int reliability, float sponsorModifier) {
        this.teamId = teamId;
        this.name = name;
        this.logoPath = logoPath;
        this.pace = pace;
        this.reliability = reliability;
        this.sponsorModifier = sponsorModifier;
    }

    public int getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return name;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public int getPace() {
        // Apply sponsor funding's modifier here so callers always receive adjusted pace.
        return Math.round(pace * (1 + sponsorModifier));
    }

    public int getReliability() {
        return reliability;
    }

    public float getSponsorModifier() {
        return sponsorModifier;
    }
}
