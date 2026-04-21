package com.pucpr.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucpr.Exceptions.BusinessException;
import com.pucpr.model.Usuario;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {
    private final String FILE_PATH = "usuarios.json";
    private final ObjectMapper mapper = new ObjectMapper();
    
    public Optional<Usuario> findByEmail(String email) {
        // Carregar a lista completa de usuários usando o método findAll().
        // Utilizar Java Streams para encontrar o primeiro usuário que possua o e-mail informado.
        boolean existe = findAll().stream()
        // Importante: A comparação de e-mail deve ser 'case-insensitive' (ignorar maiúsculas/minúsculas).
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        if (existe) {
            return findAll().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst();


        }
        // Retornar um Optional.of(usuario) se encontrar, ou Optional.empty() se não existir.
        return Optional.empty();
    }

    public List<Usuario> findAll() {
        File file = new File(FILE_PATH);
        // Verificar se o arquivo definido em 'FILE_PATH' existe no sistema.
        if (!file.exists()) return new ArrayList<>(); // Se o arquivo NÃO existir, deve retornar uma lista vazia (new ArrayList<>()) para evitar erros.
        try {
            // Retorna todos os usuários cadastrados no arquivo JSON.
            System.out.println(new File(FILE_PATH).getAbsolutePath());

            // Se existir, usar o 'mapper.readValue' do Jackson para converter o conteúdo do arquiv
            // em uma List<Usuario>. Dica: Use 'new TypeReference<List<Usuario>>(){}'.

            return mapper.readValue(file, new TypeReference<List<Usuario>>(){});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    /** FEITO
     * Salva um novo usuário no arquivo JSON.
     * * TODO: O ALUNO DEVE IMPLEMENTAR:
     * 2. Verificar se o e-mail do novo usuário já está cadastrado (Regra de Negócio).
     * 3. Adicionar o novo objeto à lista.
     * 4. Utilizar 'mapper.writerWithDefaultPrettyPrinter().writeValue' para gravar a lista
     * atualizada no arquivo, garantindo que o JSON fique legível (formatado).
     */
    public void save(Usuario usuario) throws IOException {
        List<Usuario> lista = findAll();

        lista.add(usuario);
        System.out.println("Senha recebida: " + usuario.getSenha());
        // Salva um novo usuário no arquivo JSON.
        // Utilizar 'mapper.writerWithDefaultPrettyPrinter().writeValue' para gravar a lista
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(FILE_PATH), lista);
    }
}