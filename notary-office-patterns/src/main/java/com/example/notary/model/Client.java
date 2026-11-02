package com.example.notary.model;

import java.util.UUID;

/** Клиент нотариальной конторы. */
public class Client {
    private UUID id = UUID.randomUUID();
    private String fullName;
    private String passportData;
    private String phone;
    private String email;
    private String authData;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPassportData() { return passportData; }
    public void setPassportData(String passportData) { this.passportData = passportData; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAuthData() { return authData; }
    public void setAuthData(String authData) { this.authData = authData; }

    @Override
    public String toString() {
        return fullName + " (" + email + ")";
    }
}
