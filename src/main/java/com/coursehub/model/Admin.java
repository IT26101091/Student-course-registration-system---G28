
package com.coursehub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Admin extends Person {

    private String adminId;
    private String role = "ADMIN";

    public Admin() {
        super();
    }

    public Admin(String adminId, String name,
                 String email) {
        super(name, email);
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    @Override
    @JsonIgnore
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
