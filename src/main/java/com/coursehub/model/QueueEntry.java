
package com.coursehub.model;

import java.time.LocalDateTime;

public class QueueEntry {

    public static final String WAITING = "WAITING";
    public static final String PROMOTED = "PROMOTED";
    public static final String CANCELLED = "CANCELLED";

    private String queueId;
    private String studentId;
    private String courseCode;
    private LocalDateTime queueDateTime;
    private int position;
    private String status;

    public QueueEntry() {
    }

    public QueueEntry(String queueId, String studentId,
                      String courseCode, LocalDateTime queueDateTime,
                      int position, String status) {
        this.queueId = queueId;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.queueDateTime = queueDateTime;
        this.position = position;
        this.status = status;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public LocalDateTime getQueueDateTime() {
        return queueDateTime;
    }

    public void setQueueDateTime(LocalDateTime queueDateTime) {
        this.queueDateTime = queueDateTime;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
