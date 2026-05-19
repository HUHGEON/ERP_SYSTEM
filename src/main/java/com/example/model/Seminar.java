package com.example.model;

public class Seminar {
    private int id;
    private String seminarName;
    private String topic;
    private String dateTime;

    public Seminar() {}

    public Seminar(int id, String seminarName, String topic, String dateTime) {
        this.id = id;
        this.seminarName = seminarName;
        this.topic = topic;
        this.dateTime = dateTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSeminarName() { return seminarName; }
    public void setSeminarName(String seminarName) { this.seminarName = seminarName; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    @Override
    public String toString() { return seminarName; }
}
