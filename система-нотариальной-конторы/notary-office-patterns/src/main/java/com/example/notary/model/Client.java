package com.example.notary.model;

/**
 * Сущность «Клиент» нотариальной конторы.
 */
public class Client {

    private Long id;
    private String fullName;
    private String passportData;
    private String phone;
    private String email;
    private String authData;

    public Client() {
    }

    public Client(Long id, String fullName, String passportData, String phone, String email, String authData) {
        this.id = id;
        this.fullName = fullName;
        this.passportData = passportData;
        this.phone = phone;
        this.email = email;
        this.authData = authData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassportData() {
        return passportData;
    }

    public void setPassportData(String passportData) {
        this.passportData = passportData;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAuthData() {
        return authData;
    }

    public void setAuthData(String authData) {
        this.authData = authData;
    }

    @Override
    public String toString() {
        return "Клиент #%d: %s (паспорт %s, %s, %s)".formatted(id, fullName, passportData, phone, email);
    }
}
