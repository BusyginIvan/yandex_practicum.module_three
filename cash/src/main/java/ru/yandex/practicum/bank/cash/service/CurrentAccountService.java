package ru.yandex.practicum.bank.cash.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentAccountService {
    private static final String LOGIN_CLAIM = "preferred_username";

    public String getCurrentLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BadCredentialsException("Authentication is missing");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new BadCredentialsException("JWT principal is missing");
        }

        String login = jwt.getClaimAsString(LOGIN_CLAIM);
        if (login == null || login.isBlank()) {
            throw new BadCredentialsException("JWT claim '%s' is missing".formatted(LOGIN_CLAIM));
        }

        return login;
    }
}
