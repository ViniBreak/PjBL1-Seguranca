package com.pucpr.service;
import com.pucpr.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtService {
    // FEITO
    // TODO: O ALUNO DEVE BUSCAR DE UMA VARIÁVEL DE AMBIENTE (System.getenv)
    // A chave deve ter pelo menos 256 bits (32 caracteres) para o algoritmo HS256.
    private SecretKey getSigningKey() {
        String secret = System.getenv("JWT_SECRET");

        if (secret == null || secret.length() < 32) {
            throw new RuntimeException("JWT_SECRET não configurado ou muito curto.");
        }

        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** FEITO
     * Gera o token assinado.
     * 1. Define o 'subject' (e-mail do usuário).
     * 2. Adiciona Claims customizadas (como o 'role').
     * 3. Define a data de emissão e expiração (ex: 15 min).
     * 4. Assina com a chave e o algoritmo HS256.
     */
    public String generateToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("role", usuario.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1800000)) // 30 min
                .signWith(getSigningKey())
                .compact();
    }

    /** FEITO
     * Extrai o e-mail (subject) do token.
     * TODO: O ALUNO DEVE IMPLEMENTAR:
     * 1. Usar Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).
     * 2. Retornar o Subject do Payload.
     */
    public String extractEmail(String token) {
        token = token.replace("Bearer ", "");
        Jws<Claims> parser = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);

        return parser.getPayload().getSubject();
    }

    /** FEITO
     * Valida se o token é autêntico e não expirou.
     * TODO: O ALUNO DEVE IMPLEMENTAR:
     * 1. Tentar fazer o parse do token.
     * 2. Se o parse falhar (assinatura errada ou expirado), a biblioteca joga uma Exception.
     * 3. Retornar true se o token for válido e false caso capture uma exceção.
     */
    public boolean validateToken(String token) {
        token = token.replace("Bearer ", "");
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("Token expirado");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            System.out.println("Assinatura inválida");
        } catch (Exception e) {
            System.out.println("Token inválido");
        }

        return false;
    }

}
