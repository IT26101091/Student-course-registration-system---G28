
package com.coursehub.model;

public abstract class Person {

    private String name;
    private String email;

    // No-argument constructor for Jackson
    public Person() {
    }

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Subclasses must implement this method
    public abstract String getRole();
}
