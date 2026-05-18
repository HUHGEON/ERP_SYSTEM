package com.example.model;

public class StudyActivityHistory {
    private int id;
    private int studyId;
    private String studyName;
    private String activityDate;
    private String content;

    public StudyActivityHistory() {}

    public StudyActivityHistory(int id, int studyId, String studyName, String activityDate, String content) {
        this.id = id;
        this.studyId = studyId;
        this.studyName = studyName;
        this.activityDate = activityDate;
        this.content = content;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int studyId) { this.studyId = studyId; }
    public String getStudyName() { return studyName; }
    public void setStudyName(String studyName) { this.studyName = studyName; }
    public String getActivityDate() { return activityDate; }
    public void setActivityDate(String activityDate) { this.activityDate = activityDate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
