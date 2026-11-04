package com.ecrans.vente.application.port.in;

import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;

import java.time.LocalDateTime;

/** Ce que le client reçoit : sa commande, le total, où payer, et jusqu'à quand. */
public record Reservation(CommandeId commande, Montant total, String urlPaiement, LocalDateTime payerAvant) {
}
