package com.example.notary;

import com.example.notary.web.NotaryWebServer;

public class WebApplication {

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        NotaryWebServer server = new NotaryWebServer(port);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("Нотариальная контора запущена: http://localhost:" + port);
        System.out.println("Кабинет нотариуса: notary / notary123");
        Thread.currentThread().join();
    }
}
