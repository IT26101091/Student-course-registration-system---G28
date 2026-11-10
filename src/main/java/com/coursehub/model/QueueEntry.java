
package com.coursehub.model;

public class QueueEntry {

    private String queueEntryId;
    private String studentId;
    private String courseCode;
    private String joinedAt;

    public QueueEntry() {
    }

    public QueueEntry(String queueEntryId,
                      String studentId,
                      String courseCode,
                      String joinedAt) {
        this.queueEntryId = queueEntryId;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.joinedAt = joinedAt;
    }

    public String getQueueEntryId() {
        return queueEntryId;
    }

    public void setQueueEntryId(String queueEntryId) {
        this.queueEntryId = queueEntryId;
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

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }
}
