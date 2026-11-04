package com.ecrans.vente.application.port.out;

import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;

/** Port secondaire vers le prestataire de paiement. Aucun mot de PayVia ici : c'est le rôle de l'ACL. */
public interface Paiements {

    /** @return l'adresse où le client doit payer */
    String demanderPaiement(CommandeId commande, Montant montant);
}
