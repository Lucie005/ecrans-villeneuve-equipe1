package com.ecrans.vente.domaine.commande;

public enum StatutCommande {
    EN_ATTENTE_PAIEMENT,
    PAYEE,
    EXPIREE,
    /** Bonus B13 : payée trop tard, places reprises par quelqu'un d'autre : il faut rembourser. */
    A_REMBOURSER
}
