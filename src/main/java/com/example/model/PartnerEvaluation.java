package com.example.model;

public class PartnerEvaluation {
    private int id;
    private int partnerId;
    private String partnerName;
    private String participationCategory;
    private double avgRate;

    public PartnerEvaluation() {}

    public PartnerEvaluation(int id, int partnerId, String partnerName, String participationCategory) {
        this.id = id;
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.participationCategory = participationCategory;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPartnerId() { return partnerId; }
    public void setPartnerId(int partnerId) { this.partnerId = partnerId; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public String getParticipationCategory() { return participationCategory; }
    public void setParticipationCategory(String participationCategory) { this.participationCategory = participationCategory; }
    public double getAvgRate() { return avgRate; }
    public void setAvgRate(double avgRate) { this.avgRate = avgRate; }
}
