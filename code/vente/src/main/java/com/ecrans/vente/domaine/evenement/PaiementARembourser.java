package com.ecrans.vente.domaine.evenement;

import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;

import java.time.LocalDateTime;

/** Bonus B13 : le paiement est arrivé trop tard, les places ont été revendues. Une policy rembourse. */
public record PaiementARembourser(CommandeId commande, Montant montant, LocalDateTime survenuLe) implements EvenementVente {
}
