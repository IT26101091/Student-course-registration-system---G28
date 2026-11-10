
package com.coursehub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Student extends Person {

    private String studentId;
    private String degreeProgram;

    public Student() {
        super();
    }

    public Student(String studentId, String name,
                   String email, String degreeProgram) {
        super(name, email);
        this.studentId = studentId;
        this.degreeProgram = degreeProgram;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDegreeProgram() {
        return degreeProgram;
    }

    public void setDegreeProgram(String degreeProgram) {
        this.degreeProgram = degreeProgram;
    }

    @Override
    @JsonIgnore
    public String getRole() {
        return "STUDENT";
    }
}
