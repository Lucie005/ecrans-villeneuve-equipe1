package com.ecrans.vente.domaine.evenement;

import com.ecrans.vente.domaine.commun.CommandeId;

import java.time.LocalDateTime;

/** Ce qui s'est passé dans la Vente, au passé. Publié APRÈS l'enregistrement de l'agrégat. */
public sealed interface EvenementVente permits CommandePayee, CommandeExpiree, PaiementARembourser {
    CommandeId commande();
    LocalDateTime survenuLe();
}
