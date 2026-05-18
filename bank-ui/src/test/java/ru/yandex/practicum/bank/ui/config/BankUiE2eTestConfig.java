package ru.yandex.practicum.bank.ui.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(SecurityConfig.class)
@ComponentScan(basePackages = {
    "ru.yandex.practicum.bank.ui.api.controller",
    "ru.yandex.practicum.bank.ui.service"
})
public class BankUiE2eTestConfig { }
