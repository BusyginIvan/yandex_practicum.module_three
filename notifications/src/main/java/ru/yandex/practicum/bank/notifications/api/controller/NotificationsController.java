package ru.yandex.practicum.bank.notifications.api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.notifications.api.model.CashNotificationRequest;
import ru.yandex.practicum.bank.notifications.api.model.ProfileNotificationRequest;
import ru.yandex.practicum.bank.notifications.api.model.TransferNotificationRequest;
import ru.yandex.practicum.bank.notifications.service.NotificationsService;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {
    private final NotificationsService notificationsService;

    public NotificationsController(NotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }

    @PostMapping("/cash")
    @PreAuthorize("hasAuthority('notifications:cash')")
    public void sendCashOperationNotification(
        @RequestHeader("Operation-Id") String operationId,
        @RequestBody CashNotificationRequest request
    ) {
        notificationsService.sendCashOperationNotification(
            operationId,
            request.login(),
            request.type(),
            request.amount()
        );
    }

    @PostMapping("/profile")
    @PreAuthorize("hasAuthority('notifications:profile')")
    public void sendProfileUpdateNotification(
        @RequestHeader("Operation-Id") String operationId,
        @RequestBody ProfileNotificationRequest request
    ) {
        notificationsService.sendProfileUpdateNotification(operationId, request.login());
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('notifications:transfer')")
    public void sendTransferNotification(
        @RequestHeader("Operation-Id") String operationId,
        @RequestBody TransferNotificationRequest request
    ) {
        notificationsService.sendTransferNotification(
            operationId,
            request.senderLogin(),
            request.recipientLogin(),
            request.amount()
        );
    }
}
