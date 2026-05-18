package com.example.model;

public class PmEvaluation {
    private int id;
    private int pmId;
    private String pmName;
    private String participationCategory;
    private double avgRate;

    public PmEvaluation() {}

    public PmEvaluation(int id, int pmId, String pmName, String participationCategory) {
        this.id = id;
        this.pmId = pmId;
        this.pmName = pmName;
        this.participationCategory = participationCategory;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPmId() { return pmId; }
    public void setPmId(int pmId) { this.pmId = pmId; }
    public String getPmName() { return pmName; }
    public void setPmName(String pmName) { this.pmName = pmName; }
    public String getParticipationCategory() { return participationCategory; }
    public void setParticipationCategory(String participationCategory) { this.participationCategory = participationCategory; }
    public double getAvgRate() { return avgRate; }
    public void setAvgRate(double avgRate) { this.avgRate = avgRate; }
}
