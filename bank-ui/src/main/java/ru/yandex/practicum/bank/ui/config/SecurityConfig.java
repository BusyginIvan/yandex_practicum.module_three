package ru.yandex.practicum.bank.ui.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity security,
        LogoutSuccessHandler logoutSuccessHandler
    ) throws Exception {
        security
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/oauth2/**", "/error", "/actuator/health", "/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/account", true)
            )
            .logout(logout -> logout
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessHandler(logoutSuccessHandler)
            );

        return security.build();
    }

    @Bean
    LogoutSuccessHandler logoutSuccessHandler(
        @Value("${keycloak.logout-uri}") String logoutUri,
        @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId
    ) {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            if (authentication == null) {
                throw new IllegalStateException("Expected authentication during logout, but it was null");
            }

            if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
                throw new IllegalStateException(
                    "Expected OidcUser principal during logout, but got " +
                    authentication.getPrincipal().getClass().getName()
                );
            }

            String redirectUri = UriComponentsBuilder
                .fromUriString(logoutUri)
                .queryParam("client_id", clientId)
                .queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue())
                .build(true)
                .toUriString();

            response.sendRedirect(redirectUri);
        };
    }
}
