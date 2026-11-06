package com.ecrans.vente.adapter.in.payvia;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
public class WebhookPayViaController {

    private final WebhookPayVia webhookPayVia;

    public WebhookPayViaController(WebhookPayVia webhookPayVia) {
        this.webhookPayVia = webhookPayVia;
    }

    @PostMapping("/webhooks/payvia")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recevoirNotification(@RequestBody NotificationPayVia notification) {
        webhookPayVia.recevoir(notification);
    }
}