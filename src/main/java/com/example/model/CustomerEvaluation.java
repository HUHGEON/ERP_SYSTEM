package com.example.model;

public class CustomerEvaluation {
    private int id;
    private int customerId;
    private String customerName;
    private String participationCategory;

    public CustomerEvaluation() {}

    public CustomerEvaluation(int id, int customerId, String customerName, String participationCategory) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.participationCategory = participationCategory;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getParticipationCategory() { return participationCategory; }
    public void setParticipationCategory(String participationCategory) { this.participationCategory = participationCategory; }
}
