package ru.yandex.practicum.bank.accounts.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "ru.yandex.practicum.bank.accounts.api",
    "ru.yandex.practicum.bank.accounts.service"
})
@EntityScan(basePackages = "ru.yandex.practicum.bank.accounts.persistence.entity")
@EnableJpaRepositories(basePackages = "ru.yandex.practicum.bank.accounts.persistence.repository")
@Import({
    PostgresTestConfig.class,
    SecurityConfig.class,
    SecurityTestConfig.class
})
public class AccountsE2eTestConfig { }
