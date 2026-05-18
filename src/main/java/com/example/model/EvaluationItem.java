package com.example.model;

public class EvaluationItem {
    private int id;
    private int evaluationId;
    private double rate;
    private String content;

    public EvaluationItem() {}

    public EvaluationItem(int id, int evaluationId, double rate, String content) {
        this.id = id;
        this.evaluationId = evaluationId;
        this.rate = rate;
        this.content = content;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEvaluationId() { return evaluationId; }
    public void setEvaluationId(int evaluationId) { this.evaluationId = evaluationId; }
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
