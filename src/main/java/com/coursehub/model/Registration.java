
package com.coursehub.model;

import java.time.LocalDateTime;

public class Registration {

    public static final String ACTIVE = "ACTIVE";
    public static final String CANCELLED = "CANCELLED";

    private String registrationId;
    private String studentId;
    private String courseCode;
    private LocalDateTime registrationDateTime;
    private String status = ACTIVE;

    public Registration() {
    }

    public Registration(String registrationId,
                        String studentId,
                        String courseCode,
                        LocalDateTime registrationDateTime,
                        String status) {

        this.registrationId = registrationId;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.registrationDateTime = registrationDateTime;
        this.status = status;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
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

    public LocalDateTime getRegistrationDateTime() {
        return registrationDateTime;
    }

    public void setRegistrationDateTime(LocalDateTime registrationDateTime) {
        this.registrationDateTime = registrationDateTime;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
