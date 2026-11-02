package com.example.notary.service;

import com.example.notary.model.Client;
import com.example.notary.repository.ClientRepository;

import java.util.UUID;

/** Сервис клиентов: регистрация, авторизация, профиль. */
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client register(String fullName, String passportData, String phone,
                           String email, String password) {
        if (clientRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Клиент с email " + email + " уже зарегистрирован");
        }
        Client client = new Client();
        client.setFullName(fullName);
        client.setPassportData(passportData);
        client.setPhone(phone);
        client.setEmail(email);
        client.setAuthData(hashPassword(password));
        return clientRepository.save(client);
    }

    public Client login(String email, String password) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден: " + email));
        if (!client.getAuthData().equals(hashPassword(password))) {
            throw new IllegalArgumentException("Неверный пароль");
        }
        return client;
    }

    public Client getProfile(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден: " + id));
    }

    public Client updateProfile(UUID id, String fullName, String phone) {
        Client client = getProfile(id);
        client.setFullName(fullName);
        client.setPhone(phone);
        return clientRepository.save(client);
    }

    /** Учебная имитация хеширования пароля (не для продакшена). */
    private String hashPassword(String password) {
        return "hash:" + Integer.toHexString(password.hashCode());
    }
}
