package com.ecrans.vente.application.port.in;

import com.ecrans.vente.domaine.commun.CommandeId;

/** Port primaire : le PSP a confirmé le paiement d'une commande. */
public interface ConfirmerPaiement {
    void executer(CommandeId commande);
}
