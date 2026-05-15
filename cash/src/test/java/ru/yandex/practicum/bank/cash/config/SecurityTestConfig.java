package ru.yandex.practicum.bank.cash.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;

@TestConfiguration
public class SecurityTestConfig {
    @Bean
    JwtDecoder jwtDecoder() {
        return token -> {
            String authority = switch (token) {
                case "cash-write-token" -> "cash:write";
                case "wrong-token" -> "cash:unknown";
                default -> null;
            };

            Jwt.Builder jwtBuilder = Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "test-user")
                .claim("preferred_username", "alice");

            if (authority != null) {
                jwtBuilder.claim("authorities", List.of(authority));
            }

            return jwtBuilder.build();
        };
    }
}
