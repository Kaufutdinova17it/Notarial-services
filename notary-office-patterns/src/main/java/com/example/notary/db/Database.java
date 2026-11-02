package com.example.notary.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ПОРОЖДАЮЩИЙ ПАТТЕРН — ОДИНОЧКА (Singleton).
 *
 * Единственная точка доступа к базе данных H2. Все репозитории
 * получают подключение через Database.getInstance(), поэтому
 * в приложении существует ровно одно подключение и одна схема БД.
 */
public final class Database {

    private static final String URL = "jdbc:h2:./data/notarydb";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static volatile Database instance;

    private final Connection connection;

    private Database() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            initSchema();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка подключения к базе данных", e);
        }
    }

    /** Потокобезопасная ленивая инициализация (double-checked locking). */
    public static Database getInstance() {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    instance = new Database();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    /** Создание схемы БД по модели данных из архитектурного плана. */
    private void initSchema() throws SQLException {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS clients ("
                + "id UUID PRIMARY KEY, full_name VARCHAR(255) NOT NULL, "
                + "passport_data VARCHAR(255) NOT NULL, phone VARCHAR(32), "
                + "email VARCHAR(255) NOT NULL UNIQUE, auth_data VARCHAR(255))",
            "CREATE TABLE IF NOT EXISTS services ("
                + "id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL, "
                + "description CLOB, price DECIMAL(15,2) NOT NULL, "
                + "pricing_type VARCHAR(32) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS applications ("
                + "id UUID PRIMARY KEY, client_id UUID NOT NULL, service_id UUID NOT NULL, "
                + "status VARCHAR(32) NOT NULL, note VARCHAR(1000), created_at TIMESTAMP NOT NULL)",
            "CREATE TABLE IF NOT EXISTS documents ("
                + "id UUID PRIMARY KEY, application_id UUID NOT NULL, "
                + "file_url VARCHAR(500) NOT NULL, doc_type VARCHAR(32) NOT NULL, "
                + "signed BOOLEAN NOT NULL DEFAULT FALSE)",
            "CREATE TABLE IF NOT EXISTS payments ("
                + "id UUID PRIMARY KEY, application_id UUID NOT NULL, "
                + "amount DECIMAL(15,2) NOT NULL, status VARCHAR(32) NOT NULL, "
                + "created_at TIMESTAMP NOT NULL)",
            "CREATE TABLE IF NOT EXISTS notifications ("
                + "id UUID PRIMARY KEY, client_id UUID NOT NULL, application_id UUID NOT NULL, "
                + "notification_type VARCHAR(64) NOT NULL, message CLOB, "
                + "send_status VARCHAR(32) NOT NULL, created_at TIMESTAMP NOT NULL)",
            "CREATE TABLE IF NOT EXISTS audit_log ("
                + "id UUID PRIMARY KEY, actor VARCHAR(255), action VARCHAR(255) NOT NULL, "
                + "details CLOB, created_at TIMESTAMP NOT NULL)"
        };
        try (Statement st = connection.createStatement()) {
            for (String sql : ddl) {
                st.execute(sql);
            }
        }
    }
}
