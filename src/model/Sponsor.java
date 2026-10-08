package model;

public class Sponsor {
    private int sponsorId;
    private int teamId;
    private String name;
    private String logoPath;
    private float fundingAmount;

    public Sponsor(int sponsorId, int teamId, String name, String logoPath, float fundingAmount) {
        this.sponsorId = sponsorId;
        this.teamId = teamId;
        this.name = name;
        this.logoPath = logoPath;
        this.fundingAmount = fundingAmount;
    }

    public int getSponsorId() {
        return sponsorId;
    }

    public int getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public float getFundingAmount() {
        return fundingAmount;
    }
}
