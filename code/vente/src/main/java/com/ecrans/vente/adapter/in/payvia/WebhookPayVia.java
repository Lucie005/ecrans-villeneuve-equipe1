package com.ecrans.vente.adapter.in.payvia;

import com.ecrans.vente.application.port.in.ConfirmerPaiement;
import com.ecrans.vente.domaine.commun.CommandeId;

/**
 * Adapter PRIMAIRE et anti-corruption layer : il traduit le langage de PayVia
 * (event, reference, amount en centimes…) en un appel du cas d'usage, dans notre langage.
 * En production, ce serait un contrôleur REST ; la traduction resterait la même.
 */
public class WebhookPayVia {

    private final ConfirmerPaiement confirmerPaiement;

    public WebhookPayVia(ConfirmerPaiement confirmerPaiement) {
        this.confirmerPaiement = confirmerPaiement;
    }

    public void recevoir(NotificationPayVia notification) {
        if (!"payment.succeeded".equals(notification.event())) {
            return; // paiement refusé : rien à faire, le blocage expirera tout seul (B2)
        }
        confirmerPaiement.executer(new CommandeId(notification.reference()));
    }
}
