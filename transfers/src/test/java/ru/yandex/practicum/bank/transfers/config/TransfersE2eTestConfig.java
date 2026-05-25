package ru.yandex.practicum.bank.transfers.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "ru.yandex.practicum.bank.transfers.api",
    "ru.yandex.practicum.bank.transfers.service"
})
@EntityScan(basePackages = "ru.yandex.practicum.bank.transfers.persistence.entity")
@EnableJpaRepositories(basePackages = "ru.yandex.practicum.bank.transfers.persistence.repository")
@Import({
    PostgresTestConfig.class,
    SecurityConfig.class,
    SecurityTestConfig.class
})
public class TransfersE2eTestConfig { }
