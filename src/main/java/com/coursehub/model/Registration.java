
package com.coursehub.model;

public class Registration {

    public static final String ACTIVE = "ACTIVE";
    public static final String CANCELLED = "CANCELLED";

    private String registrationId;
    private String studentId;
    private String courseCode;
    private String registrationDate;
    private String status = ACTIVE;

    public Registration() {
    }

    public Registration(String registrationId,
                        String studentId,
                        String courseCode,
                        String registrationDate,
                        String status) {
        this.registrationId = registrationId;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.registrationDate = registrationDate;
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

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
