package com.example.model;

public class EvaluatorSummary {
    private int evaluatorId;
    private String name;
    private double avgRate;
    private int count;

    public EvaluatorSummary(int evaluatorId, String name, double avgRate, int count) {
        this.evaluatorId = evaluatorId;
        this.name = name;
        this.avgRate = avgRate;
        this.count = count;
    }

    public int getEvaluatorId() { return evaluatorId; }
    public String getName() { return name; }
    public double getAvgRate() { return avgRate; }
    public int getCount() { return count; }
}
