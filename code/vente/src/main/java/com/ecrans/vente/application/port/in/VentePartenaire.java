package com.ecrans.vente.application.port.in;

import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.SeanceId;

import java.util.List;

/**
 * Une vente déjà payée chez une plateforme partenaire, traduite dans NOTRE langage par l'ACL de ce partenaire.
 * {@code montantPaye} est le prix payé chez le partenaire, remise comprise : il n'est pas celui de notre grille (B11).
 */
public record VentePartenaire(String partenaire, String referencePartenaire, SeanceId seance,
                              List<DemandeBillet> billets, Montant montantPaye) {
    public VentePartenaire {
        billets = List.copyOf(billets);
    }
}
