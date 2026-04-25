package ru.yandex.practicum.bank.ui.domain;

public enum MessageType {
    SUCCESS("success"),
    ERROR("danger"),
    PENDING("secondary");

    private final String alertClassSuffix;

    MessageType(String alertClassSuffix) {
        this.alertClassSuffix = alertClassSuffix;
    }

    public String alertClassSuffix() {
        return alertClassSuffix;
    }
}
