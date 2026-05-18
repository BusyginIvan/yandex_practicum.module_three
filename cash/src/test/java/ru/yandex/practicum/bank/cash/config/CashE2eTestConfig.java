package ru.yandex.practicum.bank.cash.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "ru.yandex.practicum.bank.cash.api",
    "ru.yandex.practicum.bank.cash.service"
})
@EntityScan(basePackages = "ru.yandex.practicum.bank.cash.persistence.entity")
@EnableJpaRepositories(basePackages = "ru.yandex.practicum.bank.cash.persistence.repository")
@Import({
    PostgresTestConfig.class,
    SecurityConfig.class,
    SecurityTestConfig.class
})
public class CashE2eTestConfig { }
