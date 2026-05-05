package ru.yandex.practicum.bank.ui.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CurrentAccountService {
    private static final String LOGIN_ATTRIBUTE = "preferred_username";

    public String getCurrentLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BadCredentialsException("Authentication is missing");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OidcUser oidcUser)) {
            throw new BadCredentialsException("OIDC principal is missing");
        }

        String login = oidcUser.getAttribute(LOGIN_ATTRIBUTE);
        if (login == null || login.isBlank()) {
            throw new BadCredentialsException("OIDC attribute '%s' is missing".formatted(LOGIN_ATTRIBUTE));
        }

        return login;
    }
}
