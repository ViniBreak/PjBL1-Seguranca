package com.pucpr;
import com.pucpr.handlers.AuthHandler;
import com.pucpr.repository.UsuarioRepository;
import com.pucpr.service.JwtService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static  void main(String[] args) throws IOException {

        // Estamos instanciando as classes manualmente para que vocês percebam como
        // o AuthHandler depende do Repository e do Service para funcionar.
        // Em frameworks como Spring, isso é feito automaticamente via @Autowired.

        // Configuração de CORS (Opcional para o aluno implementar):
        // Dica: Se o frontend estiver em outra porta, vocês precisarão adicionar
        // headers de "Access-Control-Allow-Origin" em todas as respostas.

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        UsuarioRepository repository = new UsuarioRepository();
        JwtService jwtService = new JwtService();
        AuthHandler authHandler = new AuthHandler(repository, jwtService);

// Rota Register com CORS
        server.createContext("/auth/register", exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            authHandler.handleRegister(exchange);
        });

// Rota Login com CORS
        server.createContext("/auth/login", exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            authHandler.handleLogin(exchange);
        });

        server.setExecutor(null);
        System.out.println("Servidor iniciado na porta 8080...");
        server.start();
    }
}