package com.example.notary.service;

import com.example.notary.model.Client;
import com.example.notary.repository.ClientRepository;

import java.util.List;

/**
 * Сервис регистрации и поиска клиентов конторы.
 */
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client registerClient(String fullName, String passportData, String phone, String email) {
        return registerClient(fullName, passportData, phone, email, passportData);
    }

    public Client registerClient(String fullName, String passportData, String phone, String email,
                                 String password) {
        validate(fullName, passportData, email);
        String normalizedEmail = email.trim().toLowerCase();
        if (clientRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Клиент с таким e-mail уже зарегистрирован");
        }
        Client client = new Client(null, fullName.trim(), passportData.trim(),
                phone == null ? "" : phone.trim(), normalizedEmail, Passwords.hash(password));
        return clientRepository.save(client);
    }

    public Client login(String email, String password) {
        Client client = clientRepository.findByEmail(email == null ? "" : email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Неверный e-mail или пароль"));
        if (!Passwords.matches(password, client.getAuthData())) {
            throw new IllegalArgumentException("Неверный e-mail или пароль");
        }
        return client;
    }

    public Client updateProfile(long id, String fullName, String passportData, String phone,
                                String email, String newPassword) {
        validate(fullName, passportData, email);
        Client client = findById(id);
        String normalizedEmail = email.trim().toLowerCase();
        clientRepository.findByEmail(normalizedEmail)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Этот e-mail уже используется");
                });
        client.setFullName(fullName.trim());
        client.setPassportData(passportData.trim());
        client.setPhone(phone == null ? "" : phone.trim());
        client.setEmail(normalizedEmail);
        if (newPassword != null && !newPassword.isBlank()) {
            client.setAuthData(Passwords.hash(newPassword));
        }
        return clientRepository.update(client);
    }

    private void validate(String fullName, String passportData, String email) {
        if (fullName == null || fullName.isBlank()
                || passportData == null || passportData.isBlank()
                || email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Заполните ФИО, паспортные данные и корректный e-mail");
        }
    }

    public Client findById(long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Клиент #" + id + " не найден"));
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }
}
