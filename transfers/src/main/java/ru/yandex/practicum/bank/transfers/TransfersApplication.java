package ru.yandex.practicum.bank.transfers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TransfersApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransfersApplication.class, args);
    }
}
