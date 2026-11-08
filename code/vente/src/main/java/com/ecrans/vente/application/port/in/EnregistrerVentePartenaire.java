package com.ecrans.vente.application.port.in;

import com.ecrans.vente.domaine.commun.CommandeId;

/** Port primaire : enregistrer une vente déjà payée chez un partenaire, en un seul appel (B11). */
public interface EnregistrerVentePartenaire {

    /** @return l'identifiant de la commande créée chez nous */
    CommandeId executer(VentePartenaire vente);
}
