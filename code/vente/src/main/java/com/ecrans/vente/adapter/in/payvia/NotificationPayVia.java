package com.ecrans.vente.adapter.in.payvia;

/**
 * Le JSON envoyé par PayVia sur notre webhook (annexe A du sujet), tel quel.
 * Ce modèle appartient à PayVia : il ne doit JAMAIS entrer dans le domaine.
 */
public record NotificationPayVia(String event, String paymentId, String reference, long amount, String currency,
                                 String occurredAt) {
}
