package com.pucpr.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucpr.model.Usuario;
import com.pucpr.repository.UsuarioRepository;
import com.pucpr.service.JwtService;
import com.sun.net.httpserver.HttpExchange;
import net.bytebuddy.jar.asm.TypeReference;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class AuthHandler {
    private final UsuarioRepository repository;
    private final JwtService jwtService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthHandler(UsuarioRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    /**
     * Gerencia o processo de Login.
     * Objetivo: Validar credenciais e emitir um passaporte (JWT).
     */
    public void handleLogin(HttpExchange exchange) throws IOException {
        // DICA DIDÁTICA: Em APIs REST, o Login sempre deve ser POST para
        // garantir que a senha viaje no corpo (body) e não na URL.
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        // FEITO
        // TODO: O ALUNO DEVE IMPLEMENTAR OS SEGUINTES PASSOS:
        // 1. EXTRAÇÃO: Use exchange.getRequestBody() para ler os bytes do JSON enviado.
        // 2. CONVERSÃO: Transforme esse JSON em um objeto (ex: LoginRequest) usando Jackson.

        InputStream reqBody = exchange.getRequestBody();
        String bodyStr = new String(reqBody.readAllBytes());

        LoginRequest loginReq = mapper.readValue(bodyStr, LoginRequest.class);

        //FEITO
        // 3. BUSCA E SEGURANÇA:
        //    a) Busque o usuário no 'repository' pelo e-mail fornecido.
        //    b) Se existir, use BCrypt.checkpw(senhaInformada, senhaDoArquivo) para validar.
        // 4. REGRA DE OURO DA SEGURANÇA:
        //    - NUNCA use .equals() ou == para comparar senhas. O BCrypt é a sugestão.
        //    - Em caso de falha, retorne uma mensagem GENÉRICA (ex: "E-mail ou senha inválidos").
        //      Revelar qual dos dois está errado ajuda atacantes em técnicas de enumeração.
        // 5. RESPOSTA:
        //    - Se as credenciais estiverem OK: Gere o Token via jwtService e retorne 200 OK.
        //    - Se falhar: Retorne 401 Unauthorized com o JSON de erro.
        Optional<Usuario> usuarioOpt = repository.findByEmail(loginReq.getEmail());

        if (usuarioOpt.isEmpty()) {
            String erro = "{\"message\": \"E-mail ou senha inválidos\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, erro.getBytes().length);
            exchange.getResponseBody().write(erro.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        String storedHash = usuarioOpt.get().getSenhaHash() != null
                ? usuarioOpt.get().getSenhaHash()
                : usuarioOpt.get().getSenha();

        if (storedHash == null || !BCrypt.checkpw(loginReq.getPassword(), storedHash)) {
            String erro = "{\"erro\": \"E-mail ou senha inválidos\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, erro.getBytes().length);
            exchange.getResponseBody().write(erro.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        Usuario usuario = usuarioOpt.get();
        System.out.println("Body recebido: " + bodyStr);
        System.out.println("Nome: " + usuario.getNome());
        System.out.println("Email: " + usuario.getEmail());
        System.out.println("Senha: " + usuario.getSenhaHash());
        String token = jwtService.generateToken(usuario);
        String resposta = "{\"token\": \"" + token + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, resposta.getBytes().length);
        exchange.getResponseBody().write(resposta.getBytes());
        exchange.getResponseBody().close();



    }

    /**
     * Gerencia o processo de Cadastro (Registro).
     * Objetivo: Criar um novo usuário de forma segura.
     */
    public void handleRegister(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // TODO: O ALUNO DEVE IMPLEMENTAR OS SEGUINTES PASSOS:

        // 1. VALIDAÇÃO DE EXISTÊNCIA:
        //    Antes de cadastrar, verifique se o e-mail já está em uso no 'repository'.
        //    Se já existir, interrompa e retorne 400 Bad Request.
        InputStream reqBody = exchange.getRequestBody();
        String bodyStr = new String(reqBody.readAllBytes());
        System.out.println("BODY: " + bodyStr);


        // Verifica se o e-mail já existe
        RegisterRequest req = mapper.readValue(bodyStr, RegisterRequest.class);

        Optional<Usuario> usuarioExiste = repository.findByEmail(req.getEmail());

        if (usuarioExiste.isPresent()) {
            String erro = "{\"erro\": \"E-mail já cadastrado\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, erro.getBytes().length);
            exchange.getResponseBody().write(erro.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        // ✅ HASH DA SENHA PURA (não de null)
        String senhaHash = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt(12));
        Usuario usuarioHash = new Usuario(req.getName(), req.getEmail(), senhaHash, null);
        //FEITO
        // 2. CRIPTOGRAFIA (Hashing):
        //    A senha recebida NUNCA deve chegar ao arquivo em texto claro.
        //    Gere o hash: BCrypt.hashpw(senhaPura, BCrypt.gensalt(12)).
        //    O "salt" (fator 12) protege contra ataques de Rainbow Tables.

        //FEITO
        // 3. PERSISTÊNCIA:
        //    Crie uma nova instância de Usuario (model) com a senha já HASHEADA.
        //    Use o repository.save(novoUsuario) para gravar no arquivo JSON.

        // 4. RESPOSTA: Se tudo der certo, retorne 201 Created.
        try {
            repository.save(usuarioHash);
            String resposta = "{\"message\": \"Usuário criado com sucesso\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, resposta.getBytes().length);
            exchange.getResponseBody().write(resposta.getBytes());
            exchange.getResponseBody().close();
        } catch (Exception e) {
            e.printStackTrace();
            String erro = "{\"erro\": \"Erro interno\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, erro.getBytes().length);
            exchange.getResponseBody().write(erro.getBytes());
            exchange.getResponseBody().close();
        }
    }
}