package ru.yandex.practicum.bank.notifications.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.bank.notifications.persistence.repository.ProcessedNotificationRepository;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = {
    "ru.yandex.practicum.bank.notifications.service",
    "ru.yandex.practicum.bank.notifications.cron"
})
public class BusinessLayerTestConfig {

    @Bean
    ProcessedNotificationRepository processedNotificationRepository() {
        return mock(ProcessedNotificationRepository.class);
    }
}
