package org.icbtcn.dropoutprediction.model;

import java.util.HashSet;
import java.util.Set;

public class StudentFeatures {
    private String enrollmentId;
    private String username;
    private String courseId;
    private int totalEvents;
    private Set<String> uniqueEventTypes;
    private int label; // 0 or 1

    public StudentFeatures(String enrollmentId) {
        this.enrollmentId = enrollmentId;
        this.uniqueEventTypes = new HashSet<>();
        this.totalEvents = 0;
        this.label = -1; // default unset
    }

    public void addEvent(String event) {
        this.totalEvents++;
        this.uniqueEventTypes.add(event);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    // Getters
    public String getEnrollmentId() { return enrollmentId; }
    public String getUsername() { return username; }
    public String getCourseId() { return courseId; }
    public int getTotalEvents() { return totalEvents; }
    public Set<String> getUniqueEventTypes() { return uniqueEventTypes; }
    public int getLabel() { return label; }

    @Override
    public String toString() {
        return "StudentFeatures{" +
                "enrollmentId='" + enrollmentId + '\'' +
                ", username='" + username + '\'' +
                ", courseId='" + courseId + '\'' +
                ", totalEvents=" + totalEvents +
                ", uniqueEventTypes=" + uniqueEventTypes +
                ", label=" + label +
                '}';
    }
}

