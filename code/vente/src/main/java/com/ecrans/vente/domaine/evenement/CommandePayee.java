package com.ecrans.vente.domaine.evenement;

import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;

import java.time.LocalDateTime;
import java.util.Set;

/** La Notification envoie les billets ; le Contrôle d'accès les ajoute à sa liste. */
public record CommandePayee(CommandeId commande, SeanceId seance, Set<PlaceId> places, Montant total,
                            LocalDateTime survenuLe) implements EvenementVente {
}
